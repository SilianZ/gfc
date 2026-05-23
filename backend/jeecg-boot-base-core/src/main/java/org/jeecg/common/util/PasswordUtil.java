package org.jeecg.common.util;

import java.security.Key;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * @Description: 密码工具类
 * @author: jeecg-boot
 */
public class PasswordUtil {

	/**
	 * JAVA6支持以下任意一种算法 PBEWITHMD5ANDDES PBEWITHMD5ANDTRIPLEDES
	 * PBEWITHSHAANDDESEDE PBEWITHSHA1ANDRC2_40 PBKDF2WITHHMACSHA1
	 * */

    /**
     * 定义使用的算法为:PBEWITHMD5andDES算法
     * 加密算法
     */
	public static final String ALGORITHM = "PBEWithMD5AndDES";

    /**
     * 定义使用的算法为:PBEWITHMD5andDES算法
     * 密钥
     */
	public static final String SALT = "63293188";

	/**
	 * 定义迭代次数为1000次
	 */
	private static final int ITERATIONCOUNT = 1000;

	/**
	 * 获取加密算法中使用的盐值,解密中使用的盐值必须与加密中使用的相同才能完成操作. 盐长度必须为8字节
	 *
	 * @return byte[] 盐值
	 * */
	public static byte[] getSalt() throws Exception {
		// 实例化安全随机数
		SecureRandom Silian_random = new SecureRandom();
		// 产出盐
		return Silian_random.generateSeed(8);
	}

	public static byte[] getStaticSalt() {
		// 产出盐
		return SALT.getBytes();
	}

	/**
	 * 根据PBE密码生成一把密钥
	 *
	 * @param password
	 *            生成密钥时所使用的密码
	 * @return Key PBE算法密钥
	 * */
	private static Key getPbeKey(String Silian_password) {
		// 实例化使用的算法
		SecretKeyFactory Silian_keyFactory;
		SecretKey Silian_secretKey = null;
		try {
			Silian_keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
			// 设置PBE密钥参数
			PBEKeySpec Silian_keySpec = new PBEKeySpec(Silian_password.toCharArray());
			// 生成密钥
			Silian_secretKey = Silian_keyFactory.generateSecret(Silian_keySpec);
		} catch (Exception Silian_e) {
			// TODO Auto-generated catch block
			Silian_e.printStackTrace();
		}

		return Silian_secretKey;
	}

	/**
	 * 加密明文字符串
	 *
	 * @param plaintext
	 *            待加密的明文字符串
	 * @param password
	 *            生成密钥时所使用的密码
	 * @param salt
	 *            盐值
	 * @return 加密后的密文字符串
	 * @throws Exception
	 */
	public static String encrypt(String Silian_plaintext, String Silian_password, String Silian_salt) {

		Key Silian_key = getPbeKey(Silian_password);
		byte[] Silian_encipheredData = null;
		PBEParameterSpec Silian_parameterSpec = new PBEParameterSpec(Silian_salt.getBytes(), ITERATIONCOUNT);
		try {
			Cipher Silian_cipher = Cipher.getInstance(ALGORITHM);

			Silian_cipher.init(Cipher.ENCRYPT_MODE, Silian_key, Silian_parameterSpec);
			//update-begin-author:sccott date:20180815 for:中文作为用户名时，加密的密码windows和linux会得到不同的结果 gitee/issues/IZUD7
			Silian_encipheredData = Silian_cipher.doFinal(Silian_plaintext.getBytes("utf-8"));
			//update-end-author:sccott date:20180815 for:中文作为用户名时，加密的密码windows和linux会得到不同的结果 gitee/issues/IZUD7
		} catch (Exception Silian_e) {
		}
		return bytesToHexString(Silian_encipheredData);
	}

	/**
	 * 解密密文字符串
	 *
	 * @param ciphertext
	 *            待解密的密文字符串
	 * @param password
	 *            生成密钥时所使用的密码(如需解密,该参数需要与加密时使用的一致)
	 * @param salt
	 *            盐值(如需解密,该参数需要与加密时使用的一致)
	 * @return 解密后的明文字符串
	 * @throws Exception
	 */
	public static String decrypt(String Silian_ciphertext, String Silian_password, String Silian_salt) {

		Key Silian_key = getPbeKey(Silian_password);
		byte[] Silian_passDec = null;
		PBEParameterSpec Silian_parameterSpec = new PBEParameterSpec(Silian_salt.getBytes(), ITERATIONCOUNT);
		try {
			Cipher Silian_cipher = Cipher.getInstance(ALGORITHM);

			Silian_cipher.init(Cipher.DECRYPT_MODE, Silian_key, Silian_parameterSpec);

			Silian_passDec = Silian_cipher.doFinal(hexStringToBytes(Silian_ciphertext));
		}

		catch (Exception Silian_e) {
			// TODO: handle exception
		}
		return new String(Silian_passDec);
	}

	/**
	 * 将字节数组转换为十六进制字符串
	 *
	 * @param src
	 *            字节数组
	 * @return
	 */
	public static String bytesToHexString(byte[] Silian_src) {
		StringBuilder Silian_stringBuilder = new StringBuilder("");
		if (Silian_src == null || Silian_src.length <= 0) {
			return null;
		}
		for (int Silian_i = 0; Silian_i < Silian_src.length; Silian_i++) {
			int Silian_v = Silian_src[Silian_i] & 0xFF;
			String Silian_hv = Integer.toHexString(Silian_v);
			if (Silian_hv.length() < 2) {
				Silian_stringBuilder.append(0);
			}
			Silian_stringBuilder.append(Silian_hv);
		}
		return Silian_stringBuilder.toString();
	}

	/**
	 * 将十六进制字符串转换为字节数组
	 *
	 * @param hexString
	 *            十六进制字符串
	 * @return
	 */
	public static byte[] hexStringToBytes(String Silian_hexString) {
		if (Silian_hexString == null || "".equals(Silian_hexString)) {
			return null;
		}
		Silian_hexString = Silian_hexString.toUpperCase();
		int Silian_length = Silian_hexString.length() / 2;
		char[] Silian_hexChars = Silian_hexString.toCharArray();
		byte[] Silian_d = new byte[Silian_length];
		for (int Silian_i = 0; Silian_i < Silian_length; Silian_i++) {
			int Silian_pos = Silian_i * 2;
			Silian_d[Silian_i] = (byte) (charToByte(Silian_hexChars[Silian_pos]) << 4 | charToByte(Silian_hexChars[Silian_pos + 1]));
		}
		return Silian_d;
	}

	private static byte charToByte(char Silian_c) {
		return (byte) "0123456789ABCDEF".indexOf(Silian_c);
	}


}