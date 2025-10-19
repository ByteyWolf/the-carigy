package debug;
import javax.microedition.lcdui.*;
import java.util.Vector;

public class DebugCanvas extends Canvas {
	private long startupTime;
    private final Vector logLines = new Vector();
    private final Vector entryColors = new Vector();
    private final int maxLines = 100; // truncate old lines

    public DebugCanvas() {
        setFullScreenMode(true);
        startupTime = System.currentTimeMillis();
    }

    public void log(String msg, int color) {
        String timestamp = getTimestamp();
        synchronized (logLines) {
            if (logLines.size() >= maxLines) {
                logLines.removeElementAt(0);
                entryColors.removeElementAt(0);
            }
            logLines.addElement(timestamp + " " + msg);
            entryColors.addElement(new Integer(color));
        }
        repaint(); // trigger redraw
    }

    protected void paint(Graphics g) {
        g.setColor(0); // Black background
        g.fillRect(0, 0, getWidth(), getHeight());

        Font font = g.getFont();
        int lineHeight = font.getHeight();
        int maxLines = getHeight() / lineHeight;

        Vector allWrappedLines = new Vector(); // Vector<String>
        Vector allColors = new Vector();       // Vector<Integer>

        synchronized (logLines) {
            for (int i = 0; i < logLines.size(); i++) {
                String text = (String) logLines.elementAt(i);
                int color = ((Integer) entryColors.elementAt(i)).intValue();
                Vector wrapped = wrapText(text, font, getWidth() - 4); // padding

                for (int j = 0; j < wrapped.size(); j++) {
                    allWrappedLines.addElement(wrapped.elementAt(j));
                    allColors.addElement(new Integer(color));
                }
            }
        }

        // Only draw the last N lines that fit on screen
        int start = Math.max(0, allWrappedLines.size() - maxLines);
        int y = 0;
        for (int i = start; i < allWrappedLines.size(); i++) {
            g.setColor(((Integer) allColors.elementAt(i)).intValue());
            g.drawString((String) allWrappedLines.elementAt(i), 2, y, Graphics.TOP | Graphics.LEFT);
            y += lineHeight;
        }
    }

    
    private Vector wrapText(String text, Font font, int maxWidth) {
        Vector lines = new Vector();
        StringBuffer line = new StringBuffer();

        for (int i = 0; i < text.length(); i++) {
            line.append(text.charAt(i));
            if (font.stringWidth(line.toString()) > maxWidth) {
                // Remove last char, add as line, start new
                line.deleteCharAt(line.length() - 1);
                lines.addElement(line.toString());
                line = new StringBuffer();
                line.append(text.charAt(i)); // start next line with overflow char
            }
        }

        if (line.length() > 0) {
            lines.addElement(line.toString());
        }

        return lines;
    }


    private String getTimestamp() {
        long millis = System.currentTimeMillis() - startupTime;
        long seconds = millis / 1000;
        long ms = millis % 1000;

        String msStr = String.valueOf(ms);
        while (msStr.length() < 3) {
            msStr = "0" + msStr;
        }

        return "[" + seconds + "." + msStr + "]";
    }
}
