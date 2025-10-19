package internet;

import javax.microedition.midlet.*;
import javax.microedition.io.*;
import java.io.*;

public class Bluetooth {
	

	/**
	 * MIDlet that sends EFWD/DFWD and EBWD/DBWD with immediate flush.
	 * MAC: 1C692096819A (channel 1)
	 */
	public class main extends MIDlet {

	    private Display display;
	    private ControlCanvas canvas;

	    protected void startApp() {
	        display = Display.getDisplay(this);
	        canvas = new ControlCanvas();
	        display.setCurrent(canvas);
	    }

	    protected void pauseApp() {}

	    protected void destroyApp(boolean unconditional) {
	        if (canvas != null) canvas.closeConnection();
	    }

	    class ControlCanvas extends Canvas implements Runnable {
	        private StreamConnection btConnection;
	        private DataOutputStream dos;
	        private String status = "Connecting...";
	        private final String BT_URL = "btspp://1C692096819A:1"; // change if needed
	        private boolean connecting = true;

	        public ControlCanvas() {
	            Thread t = new Thread(this);
	            t.start();
	        }

	        public void run() {
	            try {
	                // You may need to pair in phone settings first if PIN is required.
	                btConnection = (StreamConnection) Connector.open(BT_URL);
	                dos = new DataOutputStream(btConnection.openOutputStream());
	                status = "Connected";
	            } catch (Exception e) {
	                status = "BT connect error: " + e.toString();
	            } finally {
	                connecting = false;
	                repaint();
	            }
	        }

	        protected void paint(Graphics g) {
	            g.setColor(0xFFFFFF);
	            g.fillRect(0, 0, getWidth(), getHeight());
	            g.setColor(0x000000);
	            int y = 10;
	            g.drawString("ESP32 Control (flush immediate)", 5, y, Graphics.TOP | Graphics.LEFT);
	            y += 18;
	            g.drawString("Status: " + status, 5, y, Graphics.TOP | Graphics.LEFT);
	            y += 20;
	            g.drawString("Hold 2 -> EFWD / DFWD", 5, y, Graphics.TOP | Graphics.LEFT);
	            y += 14;
	            g.drawString("Hold 8 -> EBWD / DBWD", 5, y, Graphics.TOP | Graphics.LEFT);
	        }

	        // aggressive immediate-flush sender: sends byte-by-byte with flush + tiny sleep
	        private synchronized void sendCommand(String cmd) {
	            if (dos == null) {
	                status = "Not connected";
	                repaint();
	                return;
	            }
	            try {
	                byte[] b = cmd.getBytes("ISO-8859-1"); // ASCII-safe
	                for (int i = 0; i < b.length; i++) {
	                    dos.write(b[i]);   // write one byte
	                    dos.flush();       // force stack to push
	                    // tiny delay helps many old phone stacks actually transmit
	                    try { Thread.sleep(8); } catch (InterruptedException ignore) {}
	                }
	            } catch (IOException ioe) {
	                status = "Send IO error: " + ioe.getMessage();
	                repaint();
	            }
	        }

	        // Close resources
	        public synchronized void closeConnection() {
	            try { if (dos != null) dos.close(); } catch (IOException ignored) {}
	            try { if (btConnection != null) btConnection.close(); } catch (IOException ignored) {}
	            dos = null; btConnection = null;
	            status = "Closed";
	            repaint();
	        }

	        protected void keyPressed(int keyCode) {
	            int action = 0;
	            try { action = getGameAction(keyCode); } catch (Exception ignored) {}

	            // Numeric 2 or DOWN -> EFWD
	            if (keyCode == Canvas.KEY_NUM2 || action == Canvas.UP) {
	                sendCommand("EFWD\n");
	            }

	            // Numeric 8 or UP -> EBWD
	            else if (keyCode == Canvas.KEY_NUM8 || action == Canvas.DOWN) {
	                sendCommand("EBWD\n");
	            }
	            
	            else if (keyCode == Canvas.KEY_NUM4 || action == Canvas.LEFT) {
	                sendCommand("ELEFT\n");
	            }
	            
	            else if (keyCode == Canvas.KEY_NUM6 || action == Canvas.RIGHT) {
	                sendCommand("ERIGHT\n");
	            }
	        }

	        protected void keyReleased(int keyCode) {
	            int action = 0;
	            try { action = getGameAction(keyCode); } catch (Exception ignored) {}

	            // Release 2 or DOWN -> DFWD
	            if (keyCode == Canvas.KEY_NUM2 || action == Canvas.UP) {
	                sendCommand("DFWD\n");
	            }

	            // Release 8 or UP -> DBWD
	            else if (keyCode == Canvas.KEY_NUM8 || action == Canvas.DOWN) {
	                sendCommand("DBWD\n");
	            }
	            
	            else if (keyCode == Canvas.KEY_NUM4 || action == Canvas.LEFT) {
	                sendCommand("DLEFT\n");
	            }
	            
	            else if (keyCode == Canvas.KEY_NUM6 || action == Canvas.RIGHT) {
	                sendCommand("DRIGHT\n");
	            }
	        }
	    }
	}

}
