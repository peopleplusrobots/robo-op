package gui;

import java.awt.Frame;

import com.Arduino;
import com.OSC;
import com.Robot;

import processing.core.PApplet;

/**
 * This is our Processing sketch. 	<br/>
 * Run {@link Main} to start the sketch.
 * 
 * @author mad
 */
public class GUI extends PApplet {

	private Frame parent;
	
	// External Communication Streams
	private Robot robot;
	private Arduino arduino;
	private OSC osc;
		
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
			
		// create a communication stream to the robot
		println("Setting up robot's socket connection ... ");
		this.robot = new Robot(this);
		thread("startRobot");
		
	}
	
	public String startRobot(){
		return robot.connect();
	}	
	public String startArduino(){
		return arduino.start();
	}	
	public String startOSC(){
		return osc.start();
	}
	
	
	/**
	 * Program loop
	 */
	int counter = 0;
	public void draw() {
		smooth();
		background(counter);
		
		// Your code goes here:
		counter+=1;
		counter %=255;

	}
	
	/*
	 * Add custom functions below
	 */

//	/**
//	 * Example: Send a message to the robot when the mouse is clicked.
//	 */
//	public void mouseClicked(){
//		
//		
//	}
	
	
	/**
	 * Example: Send a message to the robot when a key is pressed.
	 */
	public void keyPressed(){
		
	}
	
	
	/**
	 * Send new target when mouse is clicked
	 */
	public void mouseClicked(){

		if (counter%2 == 0)
			exit();
		else
			robot.sendMessage(sendDummyTarget());
		

		
		background(255,0,0);
	}

	
	private String sendDummyTarget(){
		String key = "point";
		String val = "[10,100,0,15,150,5]";
		return key + "/" + val + ";";
	}
	
	private String sendRobotTarget(int x, int y, int z, int rx, int ry, int rz){
		String key = "point";
		String val = "["+x+","+"y"+","+z+","+rx+","+ry+","+rz+"]";
		return key + "/" + val + ";";
	}
	
	private String sendRelTool(int x, int y, int z, int rx, int ry, int rz){
		String key = "offset";
		String val = "["+x+","+"y"+","+z+","+rx+","+ry+","+rz+"]";
		return key + "/" + val + ";";
	}
	
	private String quit(){
		String key = "flag";
		String val = "exit";
		return key + "/" + val + ";";
	}
	
	@Override
	public void exit(){
		robot.sendMessage(quit());
	
		System.exit(0);
	}
}
