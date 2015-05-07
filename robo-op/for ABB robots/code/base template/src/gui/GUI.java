package gui;

import java.awt.Frame;
import java.util.ArrayList;

import com.Serial;
import com.OSC;
import com.Robot;

import processing.core.PApplet;
import processing.core.PFont;

/**
 * This is our Processing sketch. 	<br/>
 * Run {@link Main} to start the sketch.
 * 
 * @author mad
 */
@SuppressWarnings("serial")
public class GUI extends PApplet {

	private Frame parent;
	
	PFont font = createFont("Monospaced-38.vlw",38);
	private String oscMsg = "";
	
	// External Communication Streams
	private Robot robot;
	private Serial serial;
	private OSC osc;
	
	// use these flags to turn on/off different communication modes
	public  boolean robotMode  = false;
	private boolean serialMode = false;
	private boolean oscMode    = true;
		
	/*
	 *  Add additional variables below:
	 */
	
	
	
	
	/**
	 * Connect to {@link Main}
	 * 
	 * @param parent - frame that holds PApplet
	 */
	public GUI(Frame parent){
		this.parent = parent;
		this.width  = parent.getWidth();
		this.height = parent.getHeight();
	}
	
	
	/**
	 * Set up your canvas and initialize sketch variables
	 */
	public void setup() {
		background(100);
		size(parent.getWidth(),parent.getHeight());
		
		// setup printstream to screen
		textAlign(CENTER,CENTER);
		textFont(font, 18);
		

		// create a communication stream to the robot
		if (robotMode){			
			println("Setting up robot's socket connection ... ");
			this.robot = new Robot(this);
			thread("startRobot");	
		}
		// create an OSC connection to an external application
		if (oscMode){
			this.osc = new OSC(this);
			thread("startOSC");
		}
		// create a serial connection to an external device (e.g., Arduino)
		if (serialMode){
			this.serial = new Serial(this);
			thread("startSerial");
		}
		
		
		//////////////////////////////////////////////////////////////////////////
		// Your setup code goes below:
		
		
		
		
		
		
		
		
		
		
		//////////////////////////////////////////////////////////////////////////	
		
	}
	
	/*
	 * Start each kind of communication on it's own thread.
	 */
	public String startRobot(){
		return robot.connect();
	}	
	public String startSerial(){
		return serial.start();
	}	
	public String startOSC(){
		return osc.start();
	}
	
	
	/**
	 * Program loop
	 */

	public void draw() {
		smooth();
		background(0);
		
		// print out messages from an OSC connection
		if (oscMode){	
			text("Message from OSC: "+oscMsg, width/2, height/2);
		}
		
		//////////////////////////////////////////////////////////////////////////
		// Your draw code goes below:
		
		
		
		
		
		
		
		
		
		
		//////////////////////////////////////////////////////////////////////////
	}
	
	//////////////////////////////////////////////////////////////////////////////////
	
	/*
	 * Add custom functions in here
	 */
	
	
	
	
	
	
	
	
	//////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Example: use key press to send/receive commands
	 */
	public void keyPressed(){
		
		if (robotMode){
			
			// add custom key pressed commands below, for example:
			if (key == 'q' || key == 'Q')
				robot.quit();
		}
		
	}
	
	
	/**
	 * Example: use mouse click to send/receive commands
	 */
	int testCounter = 0;
	public void mouseReleased(){

//		if (oscMode){			
//			// add custom mouse pressed commands below, for example:
//			float r = random(0,10);
//			osc.sendMsg("test","this is just a test "+r);
//		}
//		
		if (robotMode){		
			// reset to a neutral config
			robot.moveTo(900, -300, 1500, 60,60,60);			
		}
		
		// send the robot's current position
		if (oscMode && robotMode){
			ArrayList<String> msg = new ArrayList<String>();
			float[] xyz = robot.getPosition();
			msg.add(""+xyz[0]);
			msg.add(""+xyz[1]);
			msg.add(""+xyz[2]);
			// send the robot's position to grasshopper
			osc.sendMsg("pos", msg);
		}

		// flash the background red when mouse is pressed
		background(255,0,0);
	}

	/**
	 * Updates the GUI's oscMsgs ... called by OSC.java
	 * 
	 * @param msg Ð string from OSC
	 */
	public void setOscMsg(String msg){
		oscMsg = msg;
	}
	
	@Override
	public void exit(){
		robot.quit();
		System.exit(0);
	}


	public Robot getRobot() {
		return robot;
	}
}
