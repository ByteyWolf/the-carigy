package debug;
public class DebugLogger {
    private static DebugCanvas canvas;

    public static void init(DebugCanvas c) {
        canvas = c;
    }

    public static void log(String msg) {
        if (canvas != null) {
            canvas.log(msg, 0xFFFFFF);
        }
    }
    
    public static void logColor(String msg, int color) {
    	if (canvas != null) {
            canvas.log(msg, color);
        }
    }
}
