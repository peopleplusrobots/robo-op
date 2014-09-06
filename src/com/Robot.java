package com;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

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
public class Robot extends Thread{

	private PrintWriter out;
	private BufferedReader in;
	private String ip_6640 = "128.2.109.20";	
	private String ip_4400 = "128.2.109.111";
	private String hostAddress = ip_4400;		// UPDATE this IP address with the address from you robot's controller
	private int portNumber = 1025;
	
	/**
	 * Connect computer to robot's server to stream 
	 * live robot targets.
	 * 
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public Robot(){
		
		try {
			// setup server connection
			Socket clientSocket = new Socket(hostAddress, portNumber);
			this.out = new PrintWriter(clientSocket.getOutputStream(), true);
			this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			out.println("Sending Message to Robot!");
			String roboInput = in.readLine();   		     
			while (roboInput  != null) {
				System.out.println("	Input from Robot: "  + roboInput);
				roboInput = null;
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
		System.out.println("Connection to the Robot is setup on port "+portNumber+".");
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
				System.out.println("Received input from Robot: "  + roboInput);
				roboInput = null;
			} 
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
