package main;

public class util {
	public static byte[] encodeLength(long length) {
		byte[] bytes = new byte[8];
		for (int i = 0; i < 8; i++) {
			bytes[7 - i] = (byte) (length >>> (i * 8));
		}
		return bytes;
	}
}
