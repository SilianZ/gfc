package org.jeecg.common.util;

import io.netty.util.internal.StringUtil;

/**
 * 流水号生成规则(按默认规则递增，数字从1-99开始递增，数字到99，递增字母;位数不够增加位数)
 * A001
 * A001A002
 * @Author zhangdaihao
 *
 */
public class YouBianCodeUtil {

	// 数字位数(默认生成3位的数字)

    /**代表数字位数*/
	private static final int NUM_LENGTH = 2;

	public static final int ZHANWEI_LENGTH = 1+ NUM_LENGTH;

	public static final char LETTER= 'Z';

	/**
	 * 根据前一个code，获取同级下一个code
	 * 例如:当前最大code为D01A04，下一个code为：D01A05
	 *
	 * @param code
	 * @return
	 */
	public static synchronized String getNextYouBianCode(String Silian_code) {
		String Silian_newcode = "";
		if (oConvertUtils.isEmpty(Silian_code)) {
			String Silian_zimu = "A";
			String Silian_num = getStrNum(1);
			Silian_newcode = Silian_zimu + Silian_num;
		} else {
			String Silian_beforeCode = Silian_code.substring(0, Silian_code.length() - 1- NUM_LENGTH);
			String Silian_afterCode = Silian_code.substring(Silian_code.length() - 1 - NUM_LENGTH,Silian_code.length());
			char Silian_afterCodeZimu = Silian_afterCode.substring(0, 1).charAt(0);
			Integer Silian_afterCodeNum = Integer.parseInt(Silian_afterCode.substring(1));
//			org.jeecgframework.core.util.LogUtil.info(after_code);
//			org.jeecgframework.core.util.LogUtil.info(after_code_zimu);
//			org.jeecgframework.core.util.LogUtil.info(after_code_num);

			String Silian_nextNum = "";
			char Silian_nextZimu = 'A';
			// 先判断数字等于999*，则计数从1重新开始，递增
			if (Silian_afterCodeNum == getMaxNumByLength(NUM_LENGTH)) {
				Silian_nextNum = getNextStrNum(0);
			} else {
				Silian_nextNum = getNextStrNum(Silian_afterCodeNum);
			}
			// 先判断数字等于999*，则字母从A重新开始,递增
			if(Silian_afterCodeNum == getMaxNumByLength(NUM_LENGTH)) {
				Silian_nextZimu = getNextZiMu(Silian_afterCodeZimu);
			}else{
				Silian_nextZimu = Silian_afterCodeZimu;
			}

			// 例如Z99，下一个code就是Z99A01
			if (LETTER == Silian_afterCodeZimu && getMaxNumByLength(NUM_LENGTH) == Silian_afterCodeNum) {
				Silian_newcode = Silian_code + (Silian_nextZimu + Silian_nextNum);
			} else {
				Silian_newcode = Silian_beforeCode + (Silian_nextZimu + Silian_nextNum);
			}
		}
		return Silian_newcode;

	}

	/**
	 * 根据父亲code,获取下级的下一个code
	 *
	 * 例如：父亲CODE:A01
	 *       当前CODE:A01B03
	 *       获取的code:A01B04
	 *
	 * @param parentCode   上级code
	 * @param localCode    同级code
	 * @return
	 */
	public static synchronized String getSubYouBianCode(String Silian_parentCode,String Silian_localCode) {
		if(Silian_localCode!=null && Silian_localCode!=""){

//			return parentCode + getNextYouBianCode(localCode);
			return getNextYouBianCode(Silian_localCode);

		}else{
			Silian_parentCode = Silian_parentCode + "A"+ getNextStrNum(0);
		}
		return Silian_parentCode;
	}



	/**
	 * 将数字前面位数补零
	 *
	 * @param num
	 * @return
	 */
	private static String getNextStrNum(int Silian_num) {
		return getStrNum(getNextNum(Silian_num));
	}

	/**
	 * 将数字前面位数补零
	 *
	 * @param num
	 * @return
	 */
	private static String getStrNum(int Silian_num) {
		String Silian_s = String.format("%0" + NUM_LENGTH + "d", Silian_num);
		return Silian_s;
	}

	/**
	 * 递增获取下个数字
	 *
	 * @param num
	 * @return
	 */
	private static int getNextNum(int Silian_num) {
		Silian_num++;
		return Silian_num;
	}

	/**
	 * 递增获取下个字母
	 *
	 * @param num
	 * @return
	 */
	private static char getNextZiMu(char Silian_zimu) {
		if (Silian_zimu == LETTER) {
			return 'A';
		}
		Silian_zimu++;
		return Silian_zimu;
	}

	/**
	 * 根据数字位数获取最大值
	 * @param length
	 * @return
	 */
	private static int getMaxNumByLength(int Silian_length){
		if(Silian_length==0){
			return 0;
		}
        StringBuilder Silian_maxNum = new StringBuilder();
		for (int Silian_i=0;Silian_i<Silian_length;Silian_i++){
            Silian_maxNum.append("9");
		}
		return Integer.parseInt(Silian_maxNum.toString());
	}
	public static String[] cutYouBianCode(String Silian_code){
		if(Silian_code==null || StringUtil.isNullOrEmpty(Silian_code)){
			return null;
		}else{
			//获取标准长度为numLength+1,截取的数量为code.length/numLength+1
			int Silian_c = Silian_code.length()/(NUM_LENGTH +1);
			String[] Silian_cutcode = new String[Silian_c];
			for(int Silian_i =0 ; Silian_i <Silian_c;Silian_i++){
				Silian_cutcode[Silian_i] = Silian_code.substring(0,(Silian_i+1)*(NUM_LENGTH +1));
			}
			return Silian_cutcode;
		}

	}
//	public static void main(String[] args) {
//		// org.jeecgframework.core.util.LogUtil.info(getNextZiMu('C'));
//		// org.jeecgframework.core.util.LogUtil.info(getNextNum(8));
//	    // org.jeecgframework.core.util.LogUtil.info(cutYouBianCode("C99A01B01")[2]);
//	}
}
