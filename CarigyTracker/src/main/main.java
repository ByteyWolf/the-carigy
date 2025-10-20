package main;
import internet.*;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import camera.*;
import debug.*;

public class main extends MIDlet implements Runnable {
	DebugCanvas canvas;
	Bluetooth bluetooth;
	Communicator inet;
	CameraController video;
	
	public main() {
		canvas = new DebugCanvas();
		DebugLogger.init(canvas);
		Display.getDisplay(this).setCurrent(canvas);
		bluetooth = new Bluetooth();
		inet = new Communicator();
		video = new CameraController(160, 120);
		if (!bluetooth.connect()) {panic("Bluetooth failure; halting");}
		if (!inet.connect(bluetooth)) {panic("Internet failure; halting");}
		if (!video.connect()) {panic("Video failure; halting");}
		new Thread(this).start();
	}
	
	public void run() {
		while (true) {
			try { Thread.sleep(1000); // good enough for at most 5 fps
			} catch (InterruptedException e) {}
			byte[] frame = video.getFrame256();
			byte[] len = util.encodeLength((long)frame.length);
			DebugLogger.log("go!!");
			inet.send(len);
		}
	}
	
	public void panic(String err) {
		DebugLogger.logColor(err, 0xFF0000);
		while (true) {}
	}

	protected void destroyApp(boolean unconditional)
			throws MIDletStateChangeException {
		// TODO Auto-generated method stub

	}

	protected void pauseApp() {
		// TODO Auto-generated method stub

	}

	protected void startApp() throws MIDletStateChangeException {
		// TODO Auto-generated method stub

	}

}
