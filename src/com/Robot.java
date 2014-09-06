package com;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import processing.core.PApplet;
import processing.net.Client;

/**
 * Communication stream to the industrial robot.  </br>
 * Links our Processing sketch to the robot's <i>server.mod</i> file.
 * 
 * <ul>
 * - Must be on the Wi-Fi network as the robot controller to work.
 * </ul>
 * 
 * @author mad
 */
public class Robot{

	private PApplet p5;
	
	private PrintWriter out;
	private BufferedReader in;
	private String ip_6640 = "128.2.109.20";	
	private String ip_4400 = "128.2.109.111";
	private int port = 1025;
	
	// Thread variables
	private boolean running = false;
	private boolean isInitialized = false;
	
	// UPDATE this IP address with the address from you robot's controller
	// This address should match the IP address you put in the server.mod file
	private String hostAddress;	
		
	public Robot(PApplet p5){
		this.p5 = p5;
		hostAddress = ip_4400;	
	}

	
	/**
	 * Connect computer to robot's server to stream 
	 * live robot targets.
	 * @return 
	 * 
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public String connect(){
		
		try {			
			// setup server connection
			Socket clientSocket = new Socket(hostAddress, port);
			this.out = new PrintWriter(clientSocket.getOutputStream(), true);
			this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			out.println("Sending Message to Robot!");
			String robotInput = in.readLine();   		     
			while (robotInput  != null) {
				System.out.println("	Input from Robot: "  + robotInput);
				robotInput = null;
			}
		} catch (UnknownHostException e) {
			System.err.println("I don't know about host " + hostAddress + 
							   "; make sure you're connected to the same" +
							   " Wi-Fi network and that your IP addresses match.");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to " +
					hostAddress);
			System.exit(1);
		} 

		// If we made it through, the server connection is all setup!
		return "Connection to the Robot is setup on port "+port+".";
	}
	

	/**
	 * Send a string command / target / position to the robot.
	 * The string must be formatted exactly to the RAPID data type. 
	 * @param msg
	 * @throws InterruptedException 
	 */
	public void sendMessage(String msg){

		out.println(msg);
		
		// Block until we received an acknowledge message from robot.
		try {
			String roboInput;		
			roboInput = in.readLine();			
			while (roboInput != null) {
				System.out.println("Message from Robot: "  + roboInput);
				roboInput = null;
			} 
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
