package camera;

import javax.microedition.media.*;
import javax.microedition.media.control.*;
import javax.microedition.lcdui.*;

import debug.DebugLogger;

public class CameraController {
    private Player player;
    private VideoControl videoControl;
    private int width = 160;
    private int height = 120;

    public boolean connect() {
    		try {
    			player = Manager.createPlayer("capture://video");
    			player.realize();

    			videoControl = (VideoControl) player.getControl("VideoControl");
    			videoControl.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, null);
    			videoControl.setDisplayLocation(0,0);
    			videoControl.setDisplaySize(width, height);

    			player.start();
    			return true;
    		} catch (Exception e) {
    			DebugLogger.logColor(e.toString(), 0xFF0000);
    			return false;
    		}
    }

    public int[] getNextFrame() {
        try {
            byte[] raw = videoControl.getSnapshot(null);
            Image img = Image.createImage(raw, 0, raw.length);
            int[] pixels = new int[width*height];
            img.getRGB(pixels, 0, width, 0, 0, width, height);
            return pixels;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static byte[] to256Colors(int[] pixels) {
        byte[] result = new byte[pixels.length];

        for (int i = 0; i < pixels.length; i++) {
            int rgb = pixels[i];
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;

            int r3 = r >> 5;
            int g3 = g >> 5;
            int b2 = b >> 6;

            result[i] = (byte)((r3 << 5) | (g3 << 2) | b2);
        }

        return result;
    }
    
    public void close() {
        if (player != null) player.close();
    }
}
