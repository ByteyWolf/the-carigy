package internet;
import javax.microedition.io.*;
import java.io.*;

public class Communicator implements Runnable {
	static final String DEST_ADDR = "who-heaven.gl.at.ply.gg:4797";
	
	private SocketConnection conn;
    private InputStream in;
    private OutputStream out;
    private boolean ready = false;
    
    public boolean connect() {
    		try {
            String url = "socket://" + DEST_ADDR;
            conn = (SocketConnection) Connector.open(url);
            in = conn.openInputStream();
            out = conn.openOutputStream();
            ready = true;
            new Thread(this).start();
            return ready;
        } catch (IOException e) {
            return false;
        }
    }
    
    public boolean send(byte[] data) {
    		if (!ready && !connect()) { return false; }
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
        ready = false;
    }

    private void handleCommand(byte[] buf, int len) {
        // all commands are 1 byte for now thankfully
    		for (int i = 0; i<len; i++) {
    			byte cmd = buf[i];
    		}
    }
}
