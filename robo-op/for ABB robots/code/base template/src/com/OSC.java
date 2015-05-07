package com;

import java.util.ArrayList;
import java.util.List;

import gui.GUI;
import oscP5.*;
import netP5.*;

/**
 * Connects the oscP5 to our workflow.
 * 
 * For more implementation details see: <a href="http://www.sojamo.de/libraries/oscP5/">oscP5</a>
 * @author mad
 */
public class OSC {

	private GUI p5;
	
	public OscP5 oscP5;
	private NetAddress addr;

	private static final int SEND     = 16000;
	private static final int RECEIVE  = 16001;
	private static final String IP_DEFAULT 		 = "127.0.0.1"; 
	// fill in custom IP addresses to other OSC compatible softwares, for example:
	private static final String IP_GRASSHOPPER3D = "172.16.214.128";
	private static final String IP_VVVV			 = "XXX.XX.XXX.XXX";
	private static final String IP_MAXMSP 		 = "XXX.XX.XXX.XXX";
	
	public OSC(GUI p5){	
		this.p5 = p5;
	}
	

	public String start() {
		oscP5 = new OscP5(this,RECEIVE);
		addr = new NetAddress(IP_GRASSHOPPER3D,SEND);
		
		/*
		 * You can write custom functions to handle incoming messages, for example:
		 */
		oscP5.plug(this, "offsetPos", "/offset");
		oscP5.plug(this, "setPosition", "/pos");
		oscP5.plug(this, "sendToolPath", "/points");
		
		return "OSC is setup";
	}
	
	/**
	 * Example for creating a specific listener for incoming messages through oscP5.plug()
	 */
	private void sendToolPath(List<String> pts){
		String msg = "\ntoolpath sent: size "+pts.size();
		System.out.println("toolpath sent: size "+pts.size());
		
		if (p5.robotMode){
			for (String s : pts){

				String[] coords = s.substring(1, s.length()-1).split(", ");
				
				int x  = (int) Float.parseFloat(coords[0]);
				int y  = (int) Float.parseFloat(coords[1]);
				int z  = (int) Float.parseFloat(coords[2]);
				p5.getRobot().setPosition(x, y, z);		
			}
		}
		
		// pass the message to the GUI for visualizing or dispatching
		p5.setOscMsg(msg); 
	}
	
	
	/**
	 * Example for creating a specific listener for incoming messages through oscP5.plug()
	 */
	private void setPosition(String X, String Y, String Z){
		String msg = "\npos: "+X+","+Y+","+Z;
		System.out.println("offset: ("+X+","+Y+","+Z+")");

		if (p5.robotMode){
			int x  = (int) Float.parseFloat(X);
			int y  = (int) Float.parseFloat(Y);
			int z  = (int) Float.parseFloat(Z);
			
			p5.getRobot().setPosition(x, y, z);		
		}
		
		// pass the message to the GUI for visualizing or dispatching
		p5.setOscMsg(msg); 
	}
	
	/**
	 * Example for creating a specific listener for incoming messages through oscP5.plug()
	 */
	private void offsetPos(String X, String Y, String Z, String RX, String RY, String RZ){
		String msg = "\noffset: "+X+","+Y+","+Z+","+RX+","+RY+","+RZ;
		System.out.println("offset: ("+X+","+Y+","+Z+","+RX+","+RY+","+RZ+")");

		if (p5.robotMode){
			int x  = (int) Float.parseFloat(X);
			int y  = (int) Float.parseFloat(Y);
			int z  = (int) Float.parseFloat(Z);
			int rx = (int) Float.parseFloat(RX);
			int ry = (int) Float.parseFloat(RY);
			int rz = (int) Float.parseFloat(RZ);
			
			p5.getRobot().moveOffset(x, y, z, rx, ry, rz);		
		}
		
		// pass the message to the GUI for visualizing or dispatching
		p5.setOscMsg(msg); 
	}
	
	
	/**
	 * Sends a formatted message through OSC.
	 * 
	 * @param typetag - type tag for the message
	 * @param message - list of messages to send
	 */
	public void sendMsg(String typetag, ArrayList<String> message){
		
		OscMessage msg = new OscMessage("/"+typetag);
		for (String m : message)
			msg.add(m);
		
		oscP5.send(msg,addr);		
	}
	
	/**
	 * Sends a formatted message through OSC.
	 * 
	 * @param typetag - type tag for the message
	 * @param message - messages to send
	 */
	public void sendMsg(String typetag, String message){
		
		OscMessage msg = new OscMessage("/"+typetag);
		msg.add(message);
		
		oscP5.send(msg,addr);		
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
			if (incoming.addrPattern().equals("/points"))
				sendToolPath(msg);
			
		}	
	}
}
