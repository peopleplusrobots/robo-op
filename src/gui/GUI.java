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
@SuppressWarnings("serial")
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
	int testCounter = 0;
	public void mouseClicked(){

		println(testCounter);
		
//		if (testCounter == 0)
//			robot.sendDummyTarget();
//		if (testCounter == 1)
//			robot.moveTo(900, -300, 1500, -80, 60, -100);
//		else if (testCounter == 2)
//			robot.moveOffset(5, 5, 10, 0, 0, 0);
//		else if (testCounter == 3)
//			robot.getSpeed();
//		else if (testCounter == 4)
//			robot.getZone();
//		else if (testCounter == 5){
//			println("trying to set speed: ");
//			robot.setSpeed(10, 30, 0, 0);
//			robot.getSpeed();
//		}
//		else if (testCounter == 6){
//			println("trying to set zone: ");
//			robot.setZone(Robot.z15);
//			robot.getZone();
//		}
		if (testCounter == 1)
			robot.setPosition(900,-280,1400);
		if (testCounter == 2)
			robot.setOrientation(-45,50,-45);
		if (testCounter == 3)
			robot.getConfiguration();
		if (testCounter == 4)
			robot.getExternalAxes();
		if (testCounter == 5)
			robot.getRobTarget();
		if (testCounter == 6)
			robot.setConfiguration("[-1,0,-1,0]");
		if (testCounter == 7)
			robot.setExternalAxes("[100,9E+09,9E+09,9E+09,9E+09,25]");
		else if (testCounter == 8)
			robot.quit();
		
		testCounter++;
		background(255,0,0);
	}

	
	@Override
	public void exit(){
		robot.quit();
		System.exit(0);
	}
}
