package com;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import processing.core.PApplet;

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
	
	private String hostAddress;				// This is your robot controller's address
	private int port = 1025;				// This is your robot controller's port
	
	private boolean isSetup = false;
	
	/**
	 *  <b>Zone Data</b> ... 
	 *  see <a href="http://developercenter.robotstudio.com:80/Index.aspx?DevCenter=DevCenterManualStore&OpenDocument&Url=../RapidIFDTechRefManual/doc509.html">docs</a>
	 *  for more info.   <br/>
	 *  Not all zones are included below.
	 */
	public static final String z0 	= "[FALSE,0.3,0.3,0.3,0.03,0.3,0.03]"; 
	public static final String z5 	= "[FALSE,5,5,5,0.8,8,0.8]"; 
	public static final String z15 	= "[FALSE,15,23,23,2.3,23,2.3]";
	public static final String z50 	= "[FALSE,50,75,75,7.5,75,7.5]"; 
	public static final String z100 = "[FALSE,100,150,150,15,150,15]";
	public static final String z200 = "[FALSE,200,300,300,30,300,30]";
	
	
	/**
	 * Sets up Robot object to communicate between P5 and an industrial robot.
	 * @param p5 Ð the parent Processing PApplet
	 * @param ip Ð IP address of the robot controller
	 * @param port Ð Port number shared with the robot controller
	 */
	public Robot(PApplet p5, String ip, int port){
		this.p5 = p5;
		this.hostAddress = ip;
		this.port = port;
	}
	
	/**
	 * Connect the computer to the robot's server 
	 * to stream live commands to the robot.
	 * 
	 * @return msg from robot
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

			out.println("setting up connection to server");
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
		String msg = "Connection to the Robot is setup on port "+port+".";
		System.out.println(msg);
		isSetup = true;		

		return msg;
	}
	
	
	/**
	 * Tells the robot move relative to the current tool position.
	 * <br/>
	 * Link to doc: <a href="http://developercenter.robotstudio.com:80/Index.aspx?DevCenter=DevCenterManualStore&OpenDocument&Url=../RapidIFDTechRefManual/doc379.html">RelTool</a>
	 * 
	 * @param x		- translation from current x position
	 * @param y 	- translation from current y position
	 * @param z 	- translation from current z position
	 * @param rx	- translation from current x orientation
	 * @param ry	- translation from current y orientation
	 * @param rz	- translation from current z orientation
	 */
	public void moveOffset(int x, int y, int z, int rx, int ry, int rz){
		String key = "offset";
		String val = "["+x+","+y+","+z+","+rx+","+ry+","+rz+"]";
		System.out.println("offsetting");
		sendMessage( (key + "/" + val + ";"), true );
	}
	
	/**
	 * Recreates a robot target from the position, orientation, configuration, & external axis of the robot.
	 * 
	 * @return string representation of the current robot target
	 */
	public String getRobTarget(){
		float[] pos = getPosition();
		float[] orient = getOrientation();
		float[] config = getConfiguration();
		float[] extax = getExternalAxes();
		
		String targ =  "[["+pos[0]+","+pos[1]+","+pos[2]+"],"+
				 "["+orient[0]+","+orient[1]+","+orient[2]+","+orient[3]+"],"+
				 "["+config[0]+","+config[1]+","+config[2]+","+config[3]+"],"+
				 "["+extax[0]+","+extax[1]+","+extax[2]+","+extax[3]+","+extax[4]+","+extax[5]+"]]";
		System.out.println("robot target: "+targ);
		
		return targ;
	}

	/**
	 * Asks for the current position of the robot.
	 * <br/>
	 * Link to doc: <i><a href="http://developercenter.robotstudio.com:80/Index.aspx?DevCenter=DevCenterManualStore&OpenDocument&Url=../RapidIFDTechRefManual/doc297.html">CPos</a></i>
	 * 
	 * @return [x,y,z] coordinates of robot TCP
	 */
	public float[] getPosition(){
		float coords[] = new float[3];
		
		String key = "query";
		String val = "pos";
		sendMessage( (key + "/" + val + ";"), true );
		
		// block until the new data from the robot is received
		String msg = messageReceived();
		System.out.println("robot's current position: "+msg);
		
		String [] temp = PApplet.split(msg.substring(1, msg.length()-1),",");
		for (int i=0; i<temp.length; i++)
			coords[i] = Float.parseFloat(temp[i]);
		
		return coords;
	}
	
	/**
	 * Updates the robot's position using a linear move.
	 * <br/>
	 * Link to doc: <a href="http://developercenter.robotstudio.com:80/Index.aspx?DevCenter=DevCenterManualStore&OpenDocument&Url=../IntroductionRAPIDProgOpManual/doc16.html">MoveL</a>
	 * @param x - xPos in world coordinates
	 * @param y - yPos in world coordinates
	 * @param z - zPos in world coordinates
	 */
	public void setPosition(int x, int y, int z){
		String key = "pos";
		String val = "["+x+","+y+","+z+"]";
		sendMessage( (key + "/" + val + ";"), true );
	}
	
	/**
	 * Orientation of the TCP
	 * <br/>
	 * Link to doc: <a href+"http://developercenter.robotstudio.com:80/Index.aspx?DevCenter=DevCenterManualStore&OpenDocument&Url=../RapidIFDTechRefManual/doc468.html">orient</a>
	 * @return orientation in quaternions
	 */
	public float[] getOrientation(){
		float vals[] = new float[4];
		
		String key = "query";
		String val = "orient";
		sendMessage( (key + "/" + val + ";"), true );
		
		// block until the new data from the robot is received
		String msg = messageReceived();	
		System.out.println("robot's orientation: "+msg);
		
		String [] temp = PApplet.split(msg.substring(1, msg.length()-1),",");
		for (int i=0; i<temp.length; i++)
			vals[i] = Float.parseFloat(temp[i]);

		return vals;
	}
	
	/**
	 * Updates the robot's orientation using a linear move.
	 * <br/>
	 * Link to doc: <a href="http://developercenter.robotstudio.com:80/Index.aspx?DevCenter=DevCenterManualStore&OpenDocument&Url=../RapidIFDTechRefManual/doc355.html">OrientZYX</a>
	 * @param rx - xRot in Euler Angles
	 * @param ry - yRot in Euler Angles
	 * @param rz - zRot in Euler Angles
	 */
	public void setOrientation(int rx, int ry, int rz){
		String key = "orient";
		String val = "["+rx+","+ry+","+rz+"]";
		sendMessage( (key + "/" + val + ";"), true );
	}
	
	/**
	 * Configuration of the Robot
	 * <br/>
	 * Link to doc: <a href+"http://developercenter.robotstudio.com:80/Index.aspx?DevCenter=DevCenterManualStore&OpenDocument&Url=../RapidIFDTechRefManual/doc439.html">confdata</a>
	 * 
	 * @return array of config values
	 */
	public float[] getConfiguration(){
		float vals[] = new float[4];
		
		String key = "query";
		String val = "config";
		sendMessage( (key + "/" + val + ";"), true );
		
		// block until the new data from the robot is received
		String msg = messageReceived();	
		System.out.println("robot's config: "+msg);
		
		String [] temp = PApplet.split(msg.substring(1, msg.length()-1),",");
		for (int i=0; i<temp.length; i++)
			vals[i] = Float.parseFloat(temp[i]);

		return vals;
	}
	
	public void setConfiguration(String config){
		String key = "config";
		String val = config;		
		sendMessage( (key + "/" + val + ";"), true );
	}
	
	/**
	 * Flips the signal for a given Digital Out (DO).
	 * @param pin
	 */
	public void invertDO(String pin){
		String key = "DO";
		String val = pin;		
		sendMessage( (key + "/" + val + ";"), true );
	}
	
	/**
	 * Waits until the robot has reached a position 
	 * before executing the next line of code.
	 */
	public void waitUntilPos(){
		String key = "wait";
		String val = "InPos";
		sendMessage( (key + "/" + val + ";"), true );
	}


	/**
	 * External Axis
	 * @return array of external axis values
	 */
	public float[] getExternalAxes(){
		float vals[] = new float[6];
		
		String key = "query";
		String val = "extax";
		sendMessage( (key + "/" + val + ";"), true );
		
		// block until the new data from the robot is received
		String msg = messageReceived();		
		System.out.println("robot's external axes: "+msg);
		
		String [] temp = PApplet.split(msg.substring(1, msg.length()-1),",");
		for (int i=0; i<temp.length; i++)
			vals[i] = Float.parseFloat(temp[i]);
		

		return vals;
	}
	
	/**
	 * Sets the external axes of the robot.
	 * <br/>
	 * Link to doc: <a href="http://developercenter.robotstudio.com:80/Index.aspx?DevCenter=DevCenterManualStore&OpenDocument&Url=../RapidIFDTechRefManual/doc451.html">external joints</a>
	 * <br/><br/>
	 * *Assumes string is <b>formatted properly.</b>
	 * @param extax - string representation of extax
	 */
	public void setExternalAxes(String extax){
		String key = "extax";
		String val = extax;		
		sendMessage( (key + "/" + val + ";"), true );
	}


	/**
	 * Requests the speed data from the robot.
	 * @return speed data as array 
	 */
	public float[] getSpeed(){
		float vals[] = new float[4];
		
		String key = "query";
		String val = "speed";
		sendMessage( (key + "/" + val + ";"), true );
		
		// block until you receive the new data from robot
		String msg = messageReceived();	
		System.out.println("robot speed: "+msg);
		
		String [] temp = PApplet.split(msg.substring(1, msg.length()-1),",");
		for (int i=0; i<temp.length; i++)
			vals[i] = Float.parseFloat(temp[i]);
		
		return vals;
	}


	/**
	 * Sets the speed datatype for the robot.
	 * <br/>
	 * Link to doc: <a href="http://developercenter.robotstudio.com:80/Index.aspx?DevCenter=DevCenterManualStore&OpenDocument&Url=../RapidIFDTechRefManual/doc486.html">speeddadta</a>
	 * 
	 * @param tool		- velocity of the tool center point (TCP) in mm/s
	 * @param orient	- reorientation velocity of the TCP expressed in degrees/s
	 * @param extLinear - velocity of linear external axes in mm/s
	 * @param extRot 	- velocity of rotating external axes in degrees/s
	 */
	public void setSpeed(int tool, int orient, int extLinear, int extRot){
		String key = "speed";
		String val = "["+tool+","+orient+","+extLinear+","+extRot+"]";
		sendMessage( (key + "/" + val + ";"), true );
	}
	
	/**
	 * Requests the zonedata from the robot.
	 * 
	 * @return zone data as a string.
	 */
	public String getZone(){
		String key = "query";
		String val = "zone";
		sendMessage( (key + "/" + val + ";"), true );
		
		// block until you receive the new data from robot
		String msg = messageReceived();
		
		System.out.println("robot zone: "+msg);
		return msg;
	}


	/**
	 * Sets the zone datatype for the robot. 
	 * 
	 * <br/>
	 * Link to doc: <a href="http://developercenter.robotstudio.com:80/Index.aspx?DevCenter=DevCenterManualStore&OpenDocument&Url=../RapidIFDTechRefManual/doc509.html">zonedadta</a>
	 * 
	 * @param z - zone to set 
	 */
	public void setZone(String z){
		String key = "zone";
		String val = z;
		sendMessage( (key + "/" + val + ";"), true );
	}
	

	/**
	 * Ends the robot program and closes the socket connection on the server.
	 */
	public void quit(){
		String key = "flag";
		String val = "exit";
		sendMessage( (key + "/" + val + ";"), false );
	}
	
	/**
	 * Returns true if the client and server are connected.
	 * @return
	 */
	public boolean isSetup(){
		return isSetup;
	}

	/**
	 * Send a string command / target / position to the robot.
	 * <br/> 
	 * The string must be formatted exactly to the RAPID data type. 
	 * <br/> <br/> 
	 * You have the option to not wait for the robot to send a confirmation message
	 * back.  <i>messageReceived()</i> blocks until a message is sent back, so your program
	 * will hang if, for example, you're doing a big move command.
	 * 
	 * @param msg - string to send
	 * @param wait - whether you want to wait to received a msg back from the robot
	 * @return message from robot (either empty string or message)
	 * @throws InterruptedException 
	 */
	private String sendMessage(String msg, boolean wait){
		
		out.println(msg);
		
		if (wait)
			return messageReceived();
		else
			return "";
	}
	
	/**
	 * Waits until the robot sends back a message
	 * 
	 * @return - message sent by robot
	 */
	private String messageReceived(){
		String temp = "";
		try {
			String roboInput;		
			roboInput = in.readLine();			
			while (roboInput != null) {
				temp = roboInput;
				roboInput = null;
			} 
		} catch (IOException e) {
			e.printStackTrace();
		}
		return temp;
	}


	/**
	 * STRING IS TOO LONG for RAPID. COME UP WITH ALTERNATIVE TO SEND ROBTARGET.
	 * <br/><br/>
	 * Sends a robotTarget to the program.  
	 * <br/>
	 * Use if you want to update configuration or external axis data.
	 * <br/><br/>
	 * Use <i>moveTo</i> or <i>moveOffset</i> to update postion or orientation.
	 * @param pos
	 * @param orient
	 * @param config
	 * @param extax
	 */
	@SuppressWarnings("unused")
	private void setRobTarget(float[] pos, float[] orient, float[] config, float[] extax){
		String key = "robTarg";
		String val = "[["+pos[0]+","+pos[1]+","+pos[2]+"], "+
					 "["+orient[0]+","+orient[1]+","+orient[2]+","+orient[3]+"], "+
					 "["+config[0]+","+config[1]+","+config[2]+","+config[3]+"], "+
					 "["+extax[0]+","+extax[1]+","+extax[2]+","+extax[3]+","+extax[4]+","+extax[5]+"]]";
		
		sendMessage( (key + "/" + val + ";"), true );
	}


	/**
	 * STRING IS TOO LONG for RAPID. COME UP WITH ALTERNATIVE TO SEND ROBTARGET.
	 * 
	 * Sends a robotTarget to the program.  
	 * <br/>
	 * Use if you want to update configuration or external axis data.
	 * <br/><br/>
	 * Use <i>moveTo</i> or <i>moveOffset</i> to update postion or orientation. 
	 * <br/><br/>
	 * *Assumes string is <b>formatted properly.</b>
	 * 
	 * @param target - string representation of the robTarget
	 */
	@SuppressWarnings("unused")
	private void setRobTarget(String target){
		String key = "robTarg";
		String val = target;		
		sendMessage( (key + "/" + val + ";"), true );
	}
}
