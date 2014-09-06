package gui;

import java.awt.Frame;

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
	private Robot robot;
		
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
		println("Setting up robot connection ... ");
		this.robot = new Robot();
		
		
	}
	
	/**
	 * Program loop
	 */
	public void draw() {
		smooth();
		background(100);
		
		// Your code goes here:

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
		
		for (int i=2; i<6; i++){
			robot.sendMessage(generateMoveTargets(i));
			println("Sending "+generateMoveTargets(i)+" to robot");
		}
//		String msg = generateDummyTarget();
//		robotCOM.sendMessage(msg);
//		println("Sending "+msg+" to robot");
		
		background(255,0,0);
	}
	
	private String generateMoveTargets(int index){
		int x = 500 + (index*100);
		return "["+ x +",0,0,5,5,5]";		
	}
}
