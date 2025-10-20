package internet;
import javax.microedition.io.*;

import debug.DebugLogger;

import java.io.*;

public class Communicator implements Runnable {
	static final String DEST_ADDR = "who-heaven.gl.at.ply.gg:4797";
	
	private SocketConnection conn;
    private InputStream in;
    private OutputStream out;
    private Bluetooth bt;
    private boolean ready = false;
    
    public boolean connect(Bluetooth bluetooth) {
    		bt = bluetooth;
    		DebugLogger.log("Connecting to INet...");
    		try {
            String url = "socket://" + DEST_ADDR;
            conn = (SocketConnection) Connector.open(url);
            in = conn.openInputStream();
            out = conn.openOutputStream();
            ready = true;
            new Thread(this).start();
        } catch (IOException e) {
        			DebugLogger.logColor(e.toString(), 0xFF0000);
            ready = false;
        }
    		return ready;
    }
    
    public boolean send(byte[] data) {
    		if (!ready && !connect(bt)) { return false; }
    		if(out == null) return false;
    		
        try {
            out.write(data);
            out.flush();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    public void run() {
        byte[] buffer = new byte[1024];
        while(ready) {
            try {
                int read = in.read(buffer);
                if(read == -1) break;
                handleCommand(buffer, read);
            } catch(IOException e) {
                break;
            }
        }
        for (int code = 0; code<4; code++) {
        			bt.sendCommand("D" + codes[code]);
        }
        ready = false;
    }
    
    private final String[] codes = {"FWD\n", "BWD\n", "LEFT\n", "RIGHT\n"};

    private void handleCommand(byte[] buf, int len) {
        // all commands are 1 byte for now thankfully
    		for (int i = 0; i<len; i++) {
    			byte cmd = buf[i];
    			if (cmd < 9) {
    				cmd--;
    				char enable = cmd > 4 ? 'E' : 'D';
    				String payload = enable + (codes[cmd % 4]);
    				bt.sendCommand(payload);
    			}
    		}
    }
}
