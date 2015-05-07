package main;


import com.Robot;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;

/**
 * <i>GettingStarted.java</i><br/>
 * 
 * A simple program to check if <i>Server.mod</i> is properly configured.<br/>
 * Once running, you can use your arrow keys to move the robot around in the XZ plane.
 * 
 * <br/><br/>
 * 
 * 
 * Instructions: <br/>
 * 	(1) Fist configure <i>Server.mod</i>, following this tutorial: _____________________ <br/>
 * 	(2) Once configured, run <i>Server.mod</i> on the robot. You must run <i>Server.mod</i> before <i>SettingUp.java</i>.<br/>
 *  (3) With the program running on the Teach Pendant, go ahead and run <i>GettingStarted.java</i>.
 *  <br/><br/><br/>
 *  
 *  See Robo.Op's full project details at <a href="www.madlab.cc/robo-op">madlab.cc/robo-op</a>
 *  <br/><br/>
 *  
 *  
 *  ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥  <br/>
 *   Madeline Gannon | <a href="www.madlab.cc">MADLAB.CC</a> | @madelinegannon 	 <br/>
 *  ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥ ¥  
 *  
 * @author mad 
 * <br/>01.25.2015 
 */

@SuppressWarnings("serial")
public class GettingStarted extends PApplet {
	
	// our robot object 
	private Robot robot;
	private String IP_robot = "128.2.109.20";//"127.0.0.1"; // REPLACE with your contorller's IP address
	private int PORT_robot = 1025;		   // should be same port number as in Server.mod
	
	/** You use this flag to test in your sketch,
	 * 	independent of running the robot
	 */
	private boolean robotMode = true;
	private boolean initialized = false;

	// GUI variables
	private PFont font;
	
	int offsetDist = 50;

	
	public void setup(){
		size(640,480, PGraphics.OPENGL);
		
		// set up fonts to display messages from the robot
		font = loadFont("Menlo-Bold.vlw");
		textFont(font, 16);
		
		// setup P5's connection to the Robot
		if (robotMode){			
			println("Setting up robot's socket connection ... ");
			this.robot = new Robot(this, IP_robot, PORT_robot);
			thread("startRobot");	
		}
				
	}
	
	public void draw(){
		
				
		// set the speed a bit higher at the start of the routine
		if (robotMode && robot.isSetup() && !initialized){
			robot.setSpeed(150, 100, 100, 100);
			robot.setZone(Robot.z100);
			initialized = true;
		}
		
		// move relative to the tool coordinates
		if (keyPressed && robotMode){
			if (keyCode == LEFT)
				robot.moveOffset(-offsetDist, 0, 0, 0, 0, 0);
			if (keyCode == RIGHT)
				robot.moveOffset(offsetDist, 0, 0, 0, 0, 0);
			if (keyCode == DOWN)
				robot.moveOffset(0, 0, -offsetDist, 0, 0, 0);
			if (keyCode == UP)
				robot.moveOffset(0, 0, offsetDist, 0, 0, 0);
			
		}
		
		// move relative to the world coordinates
		if (mousePressed && robotMode){
			
			int offset = 0;
			if (mouseButton == LEFT)
				offset = -offsetDist;
			else if (mouseButton == RIGHT)
				offset = offsetDist;			
			
			float[] xyz = robot.getPosition();
			robot.setPosition((int)xyz[0], (int)xyz[1]+offset, (int)xyz[2]);
			
		}
		
		
	}
	
//	/**
//	 * Use your arrow keys to move the robot around 
//	 * in the XZ plane
//	 */
//	public void keyPressed(){
//		
//		if (keyPressed && robotMode){
//			if (keyCode == LEFT)
//				robot.moveOffset(-15, 0, 0, 0, 0, 0);
//			if (keyCode == RIGHT)
//				robot.moveOffset(15, 0, 0, 0, 0, 0);
//			if (keyCode == DOWN)
//				robot.moveOffset(0, 0, -15, 0, 0, 0);
//			if (keyCode == UP)
//				robot.moveOffset(0, 0, 15, 0, 0, 0);
//		}
//		
//	}
//	
//	public void mousePressed() {
//		
//		if (mousePressed && robotMode){
//			
//			// another way of moving
//			
//			int offset = 0;
//			if (mouseButton == LEFT)
//				offset = -15;
//			else if (mouseButton == RIGHT)
//				offset = 15;			
//			
//			float[] xyz = robot.getPosition();
//			robot.setPosition((int)xyz[0], (int)xyz[1]+offset, (int)xyz[2]);
//		}
//		
//	}
	
	/**
	 * Starts the robot on its own thread.
	 * That way it won't hold up the canvas from drawing. 
	 * 
	 * @return
	 */
	public String startRobot(){
		return robot.connect();
	}
	

}
