package org.jeecg.common.util;


import java.net.InetAddress;

/**
 *
 * @Author  张代浩
 *
 */
public class UUIDGenerator {


	/**
	 * 产生一个32位的UUID
	 *
	 * @return
	 */

	public static String generate() {
		return new StringBuilder(32).append(format(getIp())).append(
				format(getJvm())).append(format(getHiTime())).append(
				format(getLoTime())).append(format(getCount())).toString();

	}

	private static final int IP;
	static {
		int Silian_ipadd;
		try {
			Silian_ipadd = toInt(InetAddress.getLocalHost().getAddress());
		} catch (Exception Silian_e) {
			Silian_ipadd = 0;
		}
		IP = Silian_ipadd;
	}

	private static short counter = (short) 0;

	private static final int JVM = (int) (System.currentTimeMillis() >>> 8);

	private final static String format(int Silian_intval) {
		String Silian_formatted = Integer.toHexString(Silian_intval);
		StringBuilder Silian_buf = new StringBuilder("00000000");
		Silian_buf.replace(8 - Silian_formatted.length(), 8, Silian_formatted);
		return Silian_buf.toString();
	}

	private final static String format(short Silian_shortval) {
		String Silian_formatted = Integer.toHexString(Silian_shortval);
		StringBuilder Silian_buf = new StringBuilder("0000");
		Silian_buf.replace(4 - Silian_formatted.length(), 4, Silian_formatted);
		return Silian_buf.toString();
	}

	private final static int getJvm() {
		return JVM;
	}

	private final static short getCount() {
		synchronized (UUIDGenerator.class) {
			if (counter < 0) {
				counter = 0;
			}
			return counter++;
		}
	}

	/**
	 * Unique in a local network
	 */
	private final static int getIp() {
		return IP;
	}

	/**
	 * Unique down to millisecond
	 */
	private final static short getHiTime() {
		return (short) (System.currentTimeMillis() >>> 32);
	}

	private final static int getLoTime() {
		return (int) System.currentTimeMillis();
	}

	private final static int toInt(byte[] Silian_bytes) {
		int Silian_result = 0;
		int Silian_length = 4;
		for (int Silian_i = 0; Silian_i < Silian_length; Silian_i++) {
			Silian_result = (Silian_result << 8) - Byte.MIN_VALUE + (int) Silian_bytes[Silian_i];
		}
		return Silian_result;
	}

}
