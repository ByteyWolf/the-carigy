package camera;

import javax.microedition.media.*;
import javax.microedition.media.control.*;
import javax.microedition.lcdui.*;
import debug.DebugLogger;

/**
 * Single-file camera controller that:
 * - creates a hidden Canvas (never shown to the user)
 * - attaches VideoControl to that canvas (proper surface object)
 * - captures frames using VideoControl.getSnapshot(...)
 * - provides getFramePixels() and getFrame256()
 *
 * Note: Device OEM behavior varies. Some devices may require the canvas to
 * actually be displayed for direct video to work. If that happens, you can
 * either temporarily show the canvas while initializing or rely solely on
 * getSnapshot() without initDisplayMode.
 */
public class CameraController {
    private Player player;
    private VideoControl videoControl;
    private final HiddenCanvas hiddenCanvas;
    private final int width;
    private final int height;

    private int[] pixelBuffer;
    private byte[] colorBuffer;

    private Image lastFrameImage;

    public CameraController(int width, int height) {
        this.width = width;
        this.height = height;
        this.hiddenCanvas = new HiddenCanvas();
        this.pixelBuffer = new int[width * height];
        this.colorBuffer = new byte[width * height];
    }

    /**
     * Connect to the camera and start the player.
     * Returns true on success, false on failure.
     */
    public boolean connect() {
        try {
            player = Manager.createPlayer("capture://video");
            player.realize();

            Control c = player.getControl("VideoControl");
            if (c == null || !(c instanceof VideoControl)) {
                DebugLogger.logColor("VideoControl not available", 0xFF0000);
                return false;
            }
            videoControl = (VideoControl) c;

            // Try to attach to the hidden Canvas. This is the "proper" approach.
            try {
                videoControl.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, hiddenCanvas);
            } catch (Throwable t) {
                // Some devices reject USE_DIRECT_VIDEO or the provided Canvas.
                DebugLogger.logColor("initDisplayMode failed: " + t, 0xFF8800);
                // continue — we may still be able to use getSnapshot()
            }

            // Ask for the display size; may throw if unsupported.
            try {
                videoControl.setDisplaySize(width, height);
            } catch (Throwable t) {
                DebugLogger.logColor("setDisplaySize unsupported: " + t, 0xFF8800);
            }

            // Visible true is harmless for hidden canvas; it's not shown via Display.setCurrent.
            try {
                videoControl.setVisible(true);
            } catch (Throwable t) {
                DebugLogger.logColor("setVisible threw: " + t, 0xFF8800);
            }

            player.start();
            return true;
        } catch (Exception e) {
            DebugLogger.logColor("Camera connect failed: " + e.toString(), 0xFF0000);
            return false;
        }
    }

    /**
     * Grab a frame and return ARGB-packed int[] of size width*height.
     * Returns null on failure.
     */
    public int[] getFramePixels() {
        if (videoControl == null) {
            DebugLogger.logColor("getFramePixels: videoControl is null", 0xFF0000);
            return null;
        }
        try {
            // Try to request a snapshot. Many devices support "encoding=jpeg".
            // We also include width/height hints which some vendors honor.
            String opts = "encoding=jpeg&width=" + width + "&height=" + height;
            byte[] snap = null;
            try {
                snap = videoControl.getSnapshot(opts);
            } catch (Exception e) {
                // Some devices don't accept params; try without them.
                try {
                    snap = videoControl.getSnapshot(null);
                } catch (Exception e2) {
                    DebugLogger.logColor("getSnapshot failed: " + e2, 0xFF0000);
                    return null;
                }
            }

            if (snap == null || snap.length == 0) {
                DebugLogger.logColor("getSnapshot returned no data", 0xFF8800);
                return null;
            }

            // Convert snapshot bytes to an Image
            Image img = Image.createImage(snap, 0, snap.length);
            if (img == null) return null;

            int imgW = img.getWidth();
            int imgH = img.getHeight();

            Image working = img;
            // If snapshot size doesn't match requested size, copy into target-size image.
            if (imgW != width || imgH != height) {
                Image off = Image.createImage(width, height);
                Graphics g = off.getGraphics();
                g.drawImage(img, 0, 0, Graphics.TOP | Graphics.LEFT);
                working = off;
            }

            // Fill pixelBuffer with ARGB packed ints
            working.getRGB(pixelBuffer, 0, width, 0, 0, width, height);

            // keep lastFrameImage if someone wants to re-draw it later
            lastFrameImage = working;

            return pixelBuffer;
        } catch (Throwable t) {
            t.printStackTrace();
            DebugLogger.logColor("getFramePixels exception: " + t, 0xFF0000);
            return null;
        }
    }

    /**
     * Return the last frame as 256-color byte[] (3-3-2 packing).
     * If no frame available, tries to capture one first.
     */
    public byte[] getFrame256() {
        if (getFramePixels() == null) return null;
        return to256Colors(pixelBuffer);
    }

    /**
     * Convert ARGB int[] to 256 color (3-3-2) bytes.
     */
    public byte[] to256Colors(int[] pixels) {
        int len = pixels.length;
        if (colorBuffer == null || colorBuffer.length < len) {
            colorBuffer = new byte[len];
        }
        for (int i = 0; i < len; i++) {
            int rgb = pixels[i];
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            // 3 bits red, 3 bits green, 2 bits blue
            int packed = ((r >> 5) << 5) | ((g >> 5) << 2) | (b >> 6);
            colorBuffer[i] = (byte) (packed & 0xFF);
        }
        return colorBuffer;
    }

    /**
     * Release camera resources.
     */
    public void close() {
        try {
            if (player != null) {
                try {
                    player.stop();
                } catch (Throwable t) {
                    // ignore
                }
                try {
                    player.deallocate();
                } catch (Throwable t) {
                    // ignore
                }
                try {
                    player.close();
                } catch (Throwable t) {
                    // ignore
                }
            }
        } finally {
            player = null;
            videoControl = null;
            lastFrameImage = null;
        }
    }

    /**
     * Small hidden Canvas subclass — never shown. We keep it so VideoControl
     * has a valid Canvas object. We intentionally do minimal painting.
     */
    private final class HiddenCanvas extends Canvas {
        protected void paint(Graphics g) {
            // intentionally empty — this Canvas is hidden
            // (We could optionally draw lastFrameImage here if we wanted to debug)
        }
    }

    // Optional accessors:
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Image getLastFrameImage() { return lastFrameImage; }
}
