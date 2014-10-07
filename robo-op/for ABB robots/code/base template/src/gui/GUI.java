package gui;

import java.awt.Frame;

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
	private boolean robotMode = true;
	private boolean serialMode = false;
	private boolean oscMode = false;
		
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
	 * Examples show how to use key press to send/receive commands
	 */
	public void keyPressed(){
		
		if (robotMode){
			
			// add custom key pressed commands below, for example:
			if (key == 'q' || key == 'Q')
				robot.quit();
		}
		
	}
	
	
	/**
	 * Examples showing how to send/receive commands based on mouse clicks
	 */
	int testCounter = 0;
	public void mouseClicked(){

		if (oscMode){
			// add custom mouse pressed commands below, for example:
			osc.sendMsg("test","this is just a test");
		}
		
		if (robotMode){
			
			// add custom mouse pressed commands below, for example:
			robot.moveTo(900, -300, 1500, -80, 60, -100);
			robot.moveOffset(15, 5, 20, 0, 0, 0);	
			
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
}
