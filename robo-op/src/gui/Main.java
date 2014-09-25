package gui;

import java.applet.Applet;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * The Main class allows you to have multiple PApplets in one application. <br/>
 * Run this class to start your application.
 * 
 * @author mad
 */
public class Main extends Frame{
	
	public Main(){
		super("R0BOT Ð Template File");
		
		//get display screen size for Frame sizing
		Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		Dimension preferredSize = new Dimension(screenSize.width/2, screenSize.height/2);

		this.setResizable(true);
		this.setPreferredSize(preferredSize);
		this.setMaximumSize(screenSize);
		this.setMinimumSize(preferredSize);

		// instantiate the Applet
		final Applet gui = new GUI(this); 

		// add the applet to the frame
		this.add(gui,FlowLayout.LEFT);		

		// closes application 
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			} });


		// get threads synchronized
		gui.init();
		
	}
	

	public static void main(String[] s){
		new Main().setVisible(true);		
	}

}
