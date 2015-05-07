/**
 * Simple_OSC
 * A simple Processing sketch that receives points  
 * and curves from an Android app and passes them   
 * along to an existing Processing sketch via OSC.
 *
 * Press 'c' to clear the canvas
 *
 * @mad
 * 2.13.2015
 */

import oscP5.*;
import netP5.*;

import java.util.*;

OscP5 oscAndroid;
NetAddress android;
private static final String IP_remote = "128.2.109.104";   // your phone's IP addr
private static final int PORT_android = 12000;             // connects to phone 

OscP5 oscP5;
NetAddress p5;
private static final String IP_p5     = "128.2.109.103";  // same as 'IP_remote' in android sketch
private static final int PORT_p5      = 12001;            // connects to other p5 sketch

boolean drawMode  = true; 
boolean robotMode = true;

int phoneWidth = 0;
int phoneHeight = 0;

PVector touchPt = new PVector();
ArrayList<PVector> curve = new ArrayList<PVector>();      // curve gets set to the robot
ArrayList<ArrayList<PVector>> curves = new ArrayList<ArrayList<PVector>>();

void setup() {
  size(1184/2, 720/2);
  background(240);

  /* set up android OSC connection */
  OscProperties myProperties = new OscProperties();
  myProperties.setDatagramSize(10000); 
  myProperties.setListeningPort(PORT_android);

  oscAndroid = new OscP5(this, myProperties);
  android = new NetAddress(IP_remote, PORT_android);

  /* set up p5 OSC connection */
  myProperties = new OscProperties();
  myProperties.setDatagramSize(10000); 
  myProperties.setListeningPort(PORT_p5);

  oscP5 = new OscP5(this, myProperties);
  p5    = new NetAddress(IP_p5, PORT_p5);
}

void draw() {
  background(240);
  smooth();

  if (drawMode) {

    pushStyle();
    noFill();
    strokeWeight(5);
    stroke(255, 0, 255);

    // visualize curves
    for (int i=0; i<curves.size (); i++) {
      ArrayList<PVector> list = curves.get(i);
      beginShape();
      for (int j=0; j<list.size (); j++) {
        int x = (int) map(list.get(j).x, 0, float(phoneWidth), 0, width);
        int y = (int) map(list.get(j).y, 0, float(phoneHeight), 0, height);
        vertex(x, y);
      }
      endShape();
    }    
    popStyle();
  }

  // draw cross-hairs for touch point
  else {    

    int x = (int) map(touchPt.x, 0, float(phoneWidth), 0, width);
    int y = (int) map(touchPt.y, 0, float(phoneHeight), 0, height);

    pushStyle();
    noFill();
    strokeWeight(1);
    stroke(80);
    line(0, y, width, y);
    line(x, 0, x, height);
    rectMode(CENTER);
    stroke(180);
    rect(x, y, 20, 20);
    popStyle();
  }
}


ArrayList<PVector> parsePts(ArrayList<String> msg) {
  System.out.println();
  System.out.println();
  System.out.println("MSGs sent to parsePts: ");
  ArrayList<PVector> ptList = new ArrayList();
  for (String m : msg)
    ptList.add(parsePt(m));

  return ptList;
}

PVector parsePt(String msg) {

  System.out.println("     receiving pt: "+msg);
  String[] coords = msg.substring(1, msg.length()-1).split(", ");

  int x  = (int) Float.parseFloat(coords[0]);
  int y  = (int) Float.parseFloat(coords[1]);
  int z  = (int) Float.parseFloat(coords[2]);

  // println("x: "+x+", y: "+y+", z: "+z);

  touchPt.x = x;  
  touchPt.y = y; 
  return new PVector(x, y);
}

void mousePressed() {
  OscMessage myMessage = new OscMessage("/test");

  myMessage.add(mouseX); 
  myMessage.add(mouseY); 

  ellipse(mouseX, mouseY, 50, 50);
  oscP5.send(myMessage, p5);
}

void keyPressed() {
  if (key == 'c' || key == 'C') {
    curve.clear();
    curves.clear();
  }
}

PVector prevPt = new PVector();

void oscEvent(OscMessage incoming) {

  if (incoming.checkAddrPattern("/clear")) {
    
    if (incoming.checkTypetag("s")) {
      curve.clear();
      curves.clear();
      
      println("         cleared!");
  
      // send a 'clear canvas' bang to main app
      oscP5.send(new OscMessage("/clear"), p5);
    }
  }


  // check if we are being sent points or curves
  if (incoming.checkAddrPattern("/point")) {

    // if we're receiving individual points
    if (incoming.checkTypetag("s")) {
      String s = incoming.get(0).toString();
      curve.add(parsePt(incoming.get(0).toString()));

      // pass the latest touch point to the main Processing sketch
      if (robotMode) {
        PVector p = curve.get(curve.size()-1);

        if (PVector.dist(p, prevPt) > 45) {       
          OscMessage myMessage = new OscMessage("/move").add(s);
          oscP5.send(myMessage, p5);

          prevPt.set(p.x, p.y);
        } else {
          println("TOO CLOSE!");
        }
      }
    }

    // if we're receiving curves, pass them to main p5 sketch  
    else {
      ArrayList<String> msg = new ArrayList<String>();

      /* send latest curve to the robot */
      OscMessage myMessage = new OscMessage("/draw");


      for (int i=0; i<incoming.typetag ().length(); i++) {
        if (incoming.get(i) != null) {
          // add to 'curves' arrayList
          msg.add(incoming.get(i).toString());

          // add points to OSC message
          if (robotMode)
            myMessage.add(incoming.get(i).toString());
        }
      }

      ArrayList<PVector> temp = parsePts(msg);
      curve.clear();
      curve.addAll(temp);
      curves.add(temp);

      // pass points along to main Processing sketch
      if (robotMode)
        oscP5.send(myMessage, p5);
    }
  }

  // get screen dimensions for mapping touch points
  if (incoming.checkAddrPattern("/setup")) {
    if (incoming.checkTypetag("ii")) {
      phoneWidth = incoming.get(0).intValue();
      phoneHeight = incoming.get(1).intValue();
      println("phoneWidth = " + phoneWidth + ", phoneHeight = " + phoneHeight);
    }
  }
}
