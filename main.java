import internet.*;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import debug.*;

public class main extends MIDlet {
	DebugCanvas canvas;
	Bluetooth bluetooth;
	Communicator inet;
	
	public main() {
		canvas = new DebugCanvas();
		DebugLogger.init(canvas);
		Display.getDisplay(this).setCurrent(canvas);
		bluetooth = new Bluetooth();
		inet = new Communicator();
		if (!bluetooth.connect()) {panic("Bluetooth failure; halting");}
		if (!inet.connect(bluetooth)) {panic("Internet failure; halting");}
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
