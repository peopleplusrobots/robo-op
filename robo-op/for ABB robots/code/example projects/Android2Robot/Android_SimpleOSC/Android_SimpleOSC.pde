import oscP5.*;
import netP5.*;

import android.view.MotionEvent;

import ketai.ui.*;

/**
 * Simple_OSC
 * A simple drawing app that sends points  
 * and curves to a Processing sketch via OSC.
 *
 * Touch & Drag to draw
 * Double Tap to clear the canvas
 *
 * @mad
 * 2.13.2015
 */


OscP5 oscP5;
NetAddress myRemoteLocation;

KetaiGesture gesture;

/**
 * In drawMode, the app sends over curves, not individual points.
 */
boolean drawMode = true;   
boolean tap = false;
boolean clear = false;

int mx = 0;
int my = 0;
int phoneWidth = 0;
int phoneHeight = 0;

private static final String IP_remote = "128.2.109.103";   // your computer's IP addr
private static final int PORT = 12000;

PVector touchPt = new PVector();
ArrayList<ArrayList<PVector>> ptList = new ArrayList<ArrayList<PVector>>();
ArrayList<PVector> pts = new ArrayList<PVector>();

void setup() {
  size(displayWidth, displayHeight);
  background(220);

  textSize(32);
  textAlign(CENTER);

  phoneWidth = displayWidth;
  phoneHeight = displayHeight;
  orientation(LANDSCAPE);

  // set up gesture detection
  gesture = new KetaiGesture(this);  

  // set up communication
  oscP5 = new OscP5(this, PORT);
  myRemoteLocation = new NetAddress(IP_remote, PORT);

  // send our screen dimensions
  OscMessage setup = new OscMessage("/setup");
  setup.add(phoneWidth);
  setup.add(phoneHeight);
  oscP5.send(setup, myRemoteLocation);
}


void draw() {
  background(60);
  smooth();

  if (drawMode) {

    pushStyle();
    noFill();
    strokeWeight(10);
    stroke(255, 0, 255);

    // visualize current curve
    beginShape();
    for (int i=0; i<pts.size (); i++)
      vertex(pts.get(i).x, pts.get(i).y);  
    endShape();

    // visualize past curves
    for (int i=0; i<ptList.size (); i++) {
      ArrayList<PVector> list = ptList.get(i);
      beginShape();
      for (int j=0; j<list.size (); j++) 
        vertex(list.get(j).x, list.get(j).y);
      endShape();
    }    
    popStyle();

    /* clear the canvas on double tap */
    if (clear){

      // send a 'clear canvas' bang
      OscMessage myMessage = new OscMessage("/clear");
      myMessage.add("clear");
      oscP5.send(myMessage, myRemoteLocation);
  
      // clear our lists
      pts.clear();
      ptList.clear(); 
      
      clear = false; 
    }
  }
  // draw cross-hairs for touch point
  else if (!drawMode ) {
    pushStyle();
    noFill();
    strokeWeight(25);
    stroke(200);
    line(0, touchPt.y, width, touchPt.y);
    line(touchPt.x, 0, touchPt.x, height);
    rectMode(CENTER);
    strokeWeight(15);
    stroke(180);
    rect(touchPt.x, touchPt.y, 100, 100);
    popStyle();
  }
}

void mouseDragged() {

  /* Record touches, if we're not tapping */
  if (tap) {
    tap = false;
  }
  // if we're not tapping the screen
  else {

    // add the mouse to our pt list
    pts.add(new PVector(mouseX, mouseY));

    touchPt.x = mouseX;  
    touchPt.y = mouseY; 

    // send invidual points if we're NOT drawing curves
    if (!drawMode) {
      ArrayList<String> msg = new ArrayList<String>();
      msg.add("{"+mouseX+", "+mouseY+", 0}");
      sendPts(msg);
    }
  }
}

void mouseReleased() {

  tap = true;

  OscMessage myMessage = new OscMessage("/point");

  ArrayList<PVector> temp = new ArrayList<PVector>();
  for (PVector p : pts) {
    temp.add(p);
    myMessage.add(p.toString());
  }

  if (drawMode && temp.size()>2)
    oscP5.send(myMessage, myRemoteLocation); 

  ptList.add(temp);
  pts.clear();
}

void sendPts(ArrayList<String> pts) {
  OscMessage myMessage = new OscMessage("/point");
  for (String s : pts)
    myMessage.add(s);
  oscP5.send(myMessage, myRemoteLocation);
}

/* incoming osc message are forwarded to the oscEvent method. */
void oscEvent(OscMessage theOscMessage) {
  /* print the address pattern and the typetag of the received OscMessage */
  print("### received an osc message.");
  print(" addrpattern: "+theOscMessage.addrPattern());
  println(" typetag: "+theOscMessage.typetag());
}

void onDoubleTap(float x, float y) {
  tap = true;

  clear = true;
}

/**
 * Use 'tap' for debouncing ... so we don't add extra unwanted points. 
 */
void onTap(float x, float y) {
  tap = true;
}

public boolean surfaceTouchEvent(MotionEvent event) {

  //call to keep mouseX, mouseY, etc updated
  super.surfaceTouchEvent(event);

  //forward event to class for processing
  return gesture.surfaceTouchEvent(event);
}

