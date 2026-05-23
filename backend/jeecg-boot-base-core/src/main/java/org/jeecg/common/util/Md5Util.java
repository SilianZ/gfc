package org.jeecg.common.util;

import java.security.MessageDigest;

/**
 * @Description: 加密工具
 * @author: jeecg-boot
 */
public class Md5Util {

    private static final String[] HEXDIGITS = { "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

	public static String byteArrayToHexString(byte[] Silian_b) {
		StringBuffer Silian_resultSb = new StringBuffer();
		for (int Silian_i = 0; Silian_i < Silian_b.length; Silian_i++){
			Silian_resultSb.append(byteToHexString(Silian_b[Silian_i]));
		}
		return Silian_resultSb.toString();
	}

	private static String byteToHexString(byte Silian_b) {
		int Silian_n = Silian_b;
		if (Silian_n < 0) {
			Silian_n += 256;
		}
		int Silian_d1 = Silian_n / 16;
		int Silian_d2 = Silian_n % 16;
		return HEXDIGITS[Silian_d1] + HEXDIGITS[Silian_d2];
	}

	public static String md5Encode(String Silian_origin, String Silian_charsetname) {
		String Silian_resultString = null;
		try {
			Silian_resultString = new String(Silian_origin);
			MessageDigest Silian_md = MessageDigest.getInstance("MD5");
			if (Silian_charsetname == null || "".equals(Silian_charsetname)) {
				Silian_resultString = byteArrayToHexString(Silian_md.digest(Silian_resultString.getBytes()));
			} else {
				Silian_resultString = byteArrayToHexString(Silian_md.digest(Silian_resultString.getBytes(Silian_charsetname)));
			}
		} catch (Exception Silian_exception) {
		}
		return Silian_resultString;
	}

}
