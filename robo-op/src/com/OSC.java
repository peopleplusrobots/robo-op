package com;

import gui.GUI;
import oscP5.*;
import netP5.*;


public class OSC {

	private GUI p5;
	
	private OscP5 oscP5;
	private NetAddress addr;

	private static final int PORT  = 12001;
	private static final String IP_DEFAULT 		 = "127.0.0.1"; 
	// fill in custom IP addresses to other OSC compatible softwares
	private static final String IP_GRASSHOPPER3D = "172.16.214.128";
	private static final String IP_VVVV			 = "XXX.XX.XXX.XXX";
	private static final String IP_MAXMSP 		 = "XXX.XX.XXX.XXX";
	
	public OSC(GUI p5){	
		this.p5 = p5;
	}
	

	public String start() {
		oscP5 = new OscP5(this,PORT);
		addr = new NetAddress(IP_DEFAULT,PORT);
		
		/*
		 * You can write custom functions to handle incoming messages.
		 */
		oscP5.plug(this, "testCOM", "/test");
		
		return "OSC is setup";
	}
	
	
	
	/**
	 * Example for how to create a listener for a particular OSC message.
	 */
	public void testCOM(String s0, String s1, String s2, String s3, String s4){
		String msg = s0+" "+s1+" "+s2+" "+s3+" "+s4;
		System.out.println("testCOM: "+msg);
		
		// pass the message to the GUI for visualizing or dispatching
		p5.setTest(msg); 
	}
	
	/**
	 * Example for how to send a message through OSC
	 */
	public void sendDummyMessage(){
		
		OscMessage msg = new OscMessage("/test");
		msg.add("this");
		msg.add("is");
		msg.add("just");
		msg.add("a");
		msg.add(Math.random()+"");
		
		oscP5.send(msg,addr);		
	}
	
	/**
	 * Example for how to send a message through OSC
	 */
	public void sendRandomPt(){
		OscMessage msg = new OscMessage("/point");
		msg.add(Math.random()+"");
		msg.add(Math.random()+"");
		msg.add(Math.random()+"");
		
		oscP5.send(msg,addr);	
	}
	
	/**
	 * Listens for an OSC message 
	 * @param incoming - message being sent here
	 */
	public void oscEvent(OscMessage incoming) {
		
		// if we haven't created a specific listener for a kind of message
		if (!incoming.isPlugged()){
			System.out.print("### received an osc message.");
			System.out.print(" addrpattern: "+incoming.addrPattern());
			System.out.println(" typetag: "+incoming.typetag());
			
			System.out.println("values: ");
			for (int i=0; i<incoming.addrPattern().length(); i++){
				if (incoming.get(i) != null)
					System.out.println(incoming.get(i));
			}
			System.out.println();
		}	
	}
}
