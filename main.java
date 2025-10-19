import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import debug.*;

public class main extends MIDlet {
	DebugCanvas canvas;
	
	public main() {
		canvas = new DebugCanvas();
		DebugLogger.init(canvas);
		Display.getDisplay(this).setCurrent(canvas);
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
