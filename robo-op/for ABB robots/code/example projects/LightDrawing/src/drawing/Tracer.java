package drawing;


import gab.opencv.OpenCV;


import java.util.ArrayList;
import java.util.List;

import com.Robot;

import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import oscP5.OscProperties;

import gifAnimation.*;

import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PVector;
import processing.video.Capture;

import codeanticode.gsvideo.GSCapture;

import controlP5.ControlP5;

/**
 * Tracks and traces the brightest point of a video.
 * Used to generate animated GIFs of a light drawing.
 * 
 * @author mad
 * 12.16.2014
 */

@SuppressWarnings("serial")
public class Tracer extends PApplet {

	private TweetReader twitter;	
	private Robot robot;
	
	private boolean initialized = false;
		
	// OSC variables to connect to external software
	private OscP5 oscP5;
	private NetAddress addr;
	private static final int SEND     = 16000;
	private static final int RECEIVE  = 16001;
	private static final String ADDR_GRASSHOPPER3D = "172.16.214.128";
		
	private Capture cam;
	private OpenCV opencv;
	
	private boolean oscMode = true;
	private boolean robotMode = true;
	private boolean twitterMode = !robotMode; 
	private boolean traceMode = !oscMode;
	
	private ControlP5 cp5;
	private PFont font;
	
	private GifMaker gifExport;
	private boolean record = false;
	
	boolean calibrate = true;
	
	// CV tracking variables	
	PVector trackingPt = new PVector();
	ArrayList<PVector> trackedPts = new ArrayList<PVector>();
	boolean powerOn = false;
	int ptCount = 0;
	int ptNum = 250;

	// drawing variables
	private ArrayList<PVector> curve = new ArrayList<PVector>();
	private ArrayList<ArrayList<PVector>> curves = new ArrayList<ArrayList<PVector>>();
//	private PImage lightbeam;
	
	public void setup(){
		size(640,480, PGraphics.OPENGL);
		
		font = loadFont("Menlo-Bold.vlw");
		textFont(font, 8);
		
//		debugCameras();
			
		cam = new Capture(this, 640,480, 30);
		opencv = new OpenCV(this, 640,480);   
		
		cam.start();
//		lightbeam = createImage(cam.width, cam.height, ARGB);
//		lightbeam.loadPixels();
		
		cp5 = new ControlP5(this);
		initControls();
		cp5.setAutoDraw(false);
		
		gifExport = new GifMaker(this, "export.gif");
		gifExport.setRepeat(0); // make it an "endless" animation
		gifExport.setQuality(4);
		
		// setup twitter connection
		if (twitterMode)
			twitter = new TweetReader();
		
		// setup OSC connection
		if (oscMode){		
			
			// extend the amount of info that can be received
			OscProperties myProperties = new OscProperties();
			myProperties.setDatagramSize(10000); 
			myProperties.setListeningPort(RECEIVE);
			
			this.oscP5 = new OscP5(this,myProperties);
			addr = new NetAddress(ADDR_GRASSHOPPER3D,SEND);

			oscP5.plug(this, "sendToolPath", "/points");
		}
		
		// setup Robot connection
		if (robotMode){			
			println("Setting up robot's socket connection ... ");
			this.robot = new Robot(this);
			thread("startRobot");	
		}
				
	}
	
	public void draw(){
		if (cam.available() == true) {
			noFill();
			
			// very odd that we have to scale .5X then 2X for optical flow to work.
//			scale(2);
			
			// get web cam feed
			cam.read();				
//			mirror(cam);
			image(cam, 0, 0 );
			
			// set the speed a bit higher at the start of the routine
			if (robotMode && robot.isSetup() && !initialized){
				robot.setSpeed(150, 100, 100, 100);
				robot.setZone(Robot.z0);
				initialized = true;
			}
			
			if (traceMode){
				
			    opencv.loadImage(cam);
			    opencv.calculateOpticalFlow();
				
			    image(cam, 0, 0 );
//			    image(lightbeam, 0, 0);
			    
			    // show the mouse 
			    noStroke();
			    fill(255, 100);
			    ellipse(mouseX, mouseY, 10, 10);
			    
			    if (!calibrate) {
			    	trackPoint();
			    	if (powerOn)
			    		curve.add(new PVector(trackingPt.x, trackingPt.y));
			    }
			    
	    
			    // draw the tracked curves
			    traceCurves(false);		    		    
		    
			}
			else{
				// do twitter stuff
				if (twitterMode){
					ArrayList<ArrayList<String>> msgs = twitter.getTweetsFromUser("@madelinegannon");//searchFor("#tbt",1);
					println("number of tweets: "+msgs.size());
					for (ArrayList<String> msg : msgs){
						for (String word : msg)
							print(word+" ");
					}				
					println();println();
					
					// send each tweet to grasshopper
					if (oscMode){
						for (ArrayList<String> msg : msgs){
							sendMsg("msg",msg);
						}
					}
				}
				
				// turn on tracing to track the light drawing
				traceMode = true;
			}
		    
		    
		    // draw GUI controls
//		    pushStyle();
//		    strokeWeight(3);
//		    stroke(0,80);
//		    fill(0,40);
//		    rect(width/2+5,5,175,120);
//		    
//		    fill(255,0,255);
//		    text((int) frameRate, 10, 10);
//		    
//		    popStyle();
//		    cp5.draw();
		    
		    
//		    // screen doesn't update while recording ... but is working.
//		    if (record){
//			    gifExport.setDelay((int) (frameRate/10));
//			    gifExport.addFrame(cam);	
//		    }
		  }
	}
	
	public void keyPressed(){
		if (key == ' ' ){
			curve.clear();
			curves.clear();
			
			// export gif
			gifExport.finish();
			println("gif saved");
		}
		
		if (key == 'r'){
			record = !record;
		}
		

	}
	
	public void mousePressed() {

		// reset the tracking point based on the mouse position
		fill(255);
		ellipse(mouseX, mouseY, 15, 15);
		trackingPt.set(mouseX, mouseY, 0);

		// switch our calibrate flag once we've set a point
		if (calibrate)
			calibrate = false;
	}
	
	/**
	 * Start the robot on its own thread.
	 * 
	 * @return
	 */
	public String startRobot(){
		return robot.connect();
	}
	
	/**
	 * Listens for an OSC message 
	 * @param incoming - message being sent here
	 */
	public void oscEvent(OscMessage incoming) {
		ArrayList<String> msg = new ArrayList<String>(); 

		// if we haven't created a specific listener for a kind of message
		if (!incoming.isPlugged()){
			System.out.print("### received an osc message.");
			System.out.print(" addrpattern: "+incoming.addrPattern());
			System.out.println(" typetag: "+incoming.typetag());
			
			System.out.println("values: ");
			for (int i=0; i<incoming.typetag().length(); i++){
				if (incoming.get(i) != null){
					System.out.println(incoming.get(i));
					msg.add(incoming.get(i).toString());
				}
			}
			System.out.println();
			
			// check for toolpaths
			if (incoming.addrPattern().equals("/points")){	
				sendToolPath(msg);	
			}
		}	
	}

	private void trackPoint() {

	    	 // update the user-defined tracking point
		    PVector p = opencv.flow.getFlowAt((int)trackingPt.x, (int)trackingPt.y);
		    trackingPt.add(p);
		    
//		    lightbeam.set((int)trackingPt.x, (int)trackingPt.y, color(255,0,255));
//			lightbeam.updatePixels();
		    
		    // need to make sure tracking point doesn't go out of bounds
		    constrain(p.x,0,width);
		    constrain(p.y,0,height);
		
		    // add to the tracked point list
		    fill(255,120);
		    ellipse(trackingPt.x, trackingPt.y, 5, 5);
		    if (ptCount < ptNum) {
		      trackedPts.add(new PVector(trackingPt.x, trackingPt.y));
		      ptCount++;
		    }
		    else {
		      trackedPts.get(ptCount%ptNum).set(trackingPt.x, trackingPt.y);
		      ptCount++;
		    }
	    
		
	}

	private void debugCameras() {
		
		String[] cameras = processing.video.Capture.list();
	
		if (cameras.length == 0) {
			println("There are no cameras available for capture.");
			exit();
		} 
		else {
			println("Available cameras:");
			for (int i = 0; i < cameras.length; i++) {
				println("["+i+"]: "+cameras[i]);
			}
		}
		
	}

	/**
	 * Sends a formatted message through OSC.
	 * 
	 * @param typetag - type tag for the message
	 * @param message - list of messages to send
	 */
	private void sendMsg(String typetag, ArrayList<String> message){
		
		OscMessage msg = new OscMessage("/"+typetag);
		for (String m : message)
			msg.add(m);
		
		oscP5.send(msg,addr);		
	}
	
	/**
	 * Sends a toolpath, point by point, to the robot.
	 * If the coordinate is (-1,-1,-1), send a Digital Out (DO) to the robot.
	 * @param pts
	 */
	private void sendToolPath(List<String> pts){
		
		if (robotMode){
			// set the DIGITAL-OUT pin we are going to switch ON or OFF
			String pin = "2";// <ÐÐ On 4400: DO_OUTPUT_2_TOOL [robot pins: power = B, ground = N]
				
			// format each incoming string as an X, Y, Z coordinate
			for (int i=0; i<pts.size(); i++){
				String s = pts.get(i);

				// fix point formatting (remove { , , })
				String[] coords = s.substring(1, s.length()-1).split(", ");
				
				int x  = (int) Float.parseFloat(coords[0]);
				int y  = (int) Float.parseFloat(coords[1]);
				int z  = (int) Float.parseFloat(coords[2]);
				
//				println("x: "+x+", y: "+y+", z: "+z);				
				

				// If we get our end-of-curve flag 
				if (x == -1 && y == -1 && z == -1){	
					
					// force the robot to reach it's intended position  
					// beforeturn the led on pin 2 ON or OFF
					robot.waitUntilPos();
					println("powerOn BEFORE: "+powerOn);
					robot.invertDO(pin);
					
					// if we're going from ON to OFF,add a little delay so  
					// draw() can record the last few points of the toolpath
					if (powerOn)
						delay(250);
					
					powerOn = !powerOn;
					println("powerOn AFTER: "+powerOn);
					
					// if we've turned the power OFF, add the curve to the curves list
					if (!powerOn){
						println("adding CURVE to CURVES ");
						ArrayList<PVector> temp = new ArrayList<PVector>();
						temp.addAll(curve);	
						curves.add(temp);
						curve.clear();	
						println("DONE adding CURVE to CURVES ");
					}
					
					println("");
				}

				// otherwise send a move command
				else{
					robot.setPosition(x, y, z);
				}
				
			}
			
			System.out.println("toolpath with "+pts.size()+" points was sent");
		}
		else
			println("Cannot send toolpath ... Not connected to Robot.");
	}
	
	/**
	 * Swapping x-values to mirror the incoming video.
	 * 
	 * @param video - camera image
	 */
	private void mirror(GSCapture video) {
		video.loadPixels();
		
		for (int i=0; i<video.width/2; i++){
			for (int j=0; j<video.height; j++){
				
				int c0 = color(video.get(i, j));
				int c1 = color(video.get(video.width - i, j));
				
				video.set(i, j, c1);
				video.set(video.width - i, j, c0);
			}
		}
		
		video.updatePixels();		
	}

	private void addCurve() {
		ArrayList<PVector> temp = new ArrayList<PVector>();
		
		for (PVector p : curve)
			temp.add(new PVector(p.x,p.y));
		
		curves.add(temp);
	}

	/**
	 * Draws the tracked brightest points as polylines.
	 * 
	 * @param render - draw fancy lines or not
	 */
	private void traceCurves(boolean showMotionTrail) {
		pushStyle();
		
		// visualize the motion trail of the tracked points
		if (showMotionTrail){

			beginShape();
			stroke(255, 0, 0);
			noFill();
			if (trackedPts.size() == ptNum) {
				for (int i=0; i<trackedPts.size(); i++) {
					int index = ((ptCount%ptNum) + i)%ptNum;        
					PVector v = trackedPts.get(index);
					vertex(v.x, v.y);
				}
			}
			endShape();

		}
		
		// draw curves as polyline
		else{

			// draw current curve

			noFill();
			stroke(255, 0, 255);
			strokeWeight(10);
			beginShape();
			for (PVector p : curve)
				vertex(p.x,p.y);
			endShape();

			// draw current rest of curves
			for (int i=0; i<curves.size(); i++){
				ArrayList<PVector> list = curves.get(i);
				beginShape();
				for (PVector p : list)
					vertex(p.x,p.y);
				endShape();
			}   
		}	
	    
	    popStyle();
	}

	private void initControls() {
		
		int offset = width/2;
		
		cp5.addSlider("contrast")
			.setPosition(offset+20,20)
			.setRange(0.0f,6.0f)
			;

		cp5.addSlider("threshold")
			.setPosition(offset+20,40)
			.setRange(0,255)
			;

		cp5.addSlider("blurSize")
			.setPosition(offset+20,60)
			.setRange(0,20)
			;

		// minimum contour size
		cp5.addSlider("minArea")
			.setPosition(offset+20,80)
			.setRange(1,1000)
			;

		// maximum contour size
		cp5.addSlider("maxArea")
			.setPosition(offset+20,100)
			.setRange(5,5000)
			;
		     

		}

}
