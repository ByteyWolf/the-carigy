package internet;

import javax.microedition.io.*;

import debug.DebugLogger;

import java.io.*;

public class Bluetooth {
	private StreamConnection btConnection;
	private DataOutputStream dos;
	private final String BT_URL = "btspp://1C692096819A:1"; // change if needed
	private boolean connected = false;

	public boolean connect() {
		DebugLogger.log("Connecting to BT...");
		try {
			btConnection = (StreamConnection) Connector.open(BT_URL);
			dos = new DataOutputStream(btConnection.openOutputStream());
			connected = true;
		} catch (Exception e) {
			DebugLogger.logColor(e.toString(), 0xFF0000);
		} finally {
			connected = false;
		}
		return connected;
	}

	public synchronized void sendCommand(String cmd) {
		if (dos == null || connected == false) {
			connect();
			if (connected == false) {
				return;
			}
		}
		try {
			byte[] b = cmd.getBytes("ISO-8859-1");
			for (int i = 0; i < b.length; i++) {
				dos.write(b[i]);
				dos.flush();
				try { Thread.sleep(8); } catch (InterruptedException ignore) {}
			}
		} catch (IOException e) {
			DebugLogger.logColor(e.toString(), 0xFF0000);
		}
	}

}
