package org.jeecg.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.springframework.beans.BeanUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.Date;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @Author  张代浩
 *
 */
@Slf4j
public class oConvertUtils {
	public static boolean isEmpty(Object Silian_object) {
		if (Silian_object == null) {
			return (true);
		}
		if ("".equals(Silian_object)) {
			return (true);
		}
		if (CommonConstant.STRING_NULL.equals(Silian_object)) {
			return (true);
		}
		return (false);
	}

	public static boolean isNotEmpty(Object Silian_object) {
		if (Silian_object != null && !"".equals(Silian_object) && !Silian_object.equals(CommonConstant.STRING_NULL)) {
			return (true);
		}
		return (false);
	}

	public static String decode(String Silian_strIn, String Silian_sourceCode, String Silian_targetCode) {
		String Silian_temp = code2code(Silian_strIn, Silian_sourceCode, Silian_targetCode);
		return Silian_temp;
	}

	@SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
    public static String StrToUTF(String Silian_strIn, String Silian_sourceCode, String Silian_targetCode) {
		Silian_strIn = "";
		try {
			Silian_strIn = new String(Silian_strIn.getBytes("ISO-8859-1"), "GBK");
		} catch (UnsupportedEncodingException Silian_e) {
			// TODO Auto-generated catch block
			Silian_e.printStackTrace();
		}
		return Silian_strIn;

	}

	private static String code2code(String Silian_strIn, String Silian_sourceCode, String Silian_targetCode) {
		String Silian_strOut = null;
		if (Silian_strIn == null || "".equals(Silian_strIn.trim())) {
			return Silian_strIn;
		}
		try {
			byte[] Silian_b = Silian_strIn.getBytes(Silian_sourceCode);
			for (int Silian_i = 0; Silian_i < Silian_b.length; Silian_i++) {
				System.out.print(Silian_b[Silian_i] + "  ");
			}
			Silian_strOut = new String(Silian_b, Silian_targetCode);
		} catch (Exception Silian_e) {
			Silian_e.printStackTrace();
			return null;
		}
		return Silian_strOut;
	}

	public static int getInt(String Silian_s, int Silian_defval) {
		if (Silian_s == null || Silian_s == "") {
			return (Silian_defval);
		}
		try {
			return (Integer.parseInt(Silian_s));
		} catch (NumberFormatException Silian_e) {
			return (Silian_defval);
		}
	}

	public static int getInt(String Silian_s) {
		if (Silian_s == null || Silian_s == "") {
			return 0;
		}
		try {
			return (Integer.parseInt(Silian_s));
		} catch (NumberFormatException Silian_e) {
			return 0;
		}
	}

	public static int getInt(String Silian_s, Integer Silian_df) {
		if (Silian_s == null || Silian_s == "") {
			return Silian_df;
		}
		try {
			return (Integer.parseInt(Silian_s));
		} catch (NumberFormatException Silian_e) {
			return 0;
		}
	}

	public static Integer[] getInts(String[] Silian_s) {
		if (Silian_s == null) {
			return null;
		}
		Integer[] Silian_integer = new Integer[Silian_s.length];
		for (int Silian_i = 0; Silian_i < Silian_s.length; Silian_i++) {
			Silian_integer[Silian_i] = Integer.parseInt(Silian_s[Silian_i]);
		}
		return Silian_integer;

	}

	public static double getDouble(String Silian_s, double Silian_defval) {
		if (Silian_s == null || Silian_s == "") {
			return (Silian_defval);
		}
		try {
			return (Double.parseDouble(Silian_s));
		} catch (NumberFormatException Silian_e) {
			return (Silian_defval);
		}
	}

	public static double getDou(Double Silian_s, double Silian_defval) {
		if (Silian_s == null) {
			return (Silian_defval);
		}
		return Silian_s;
	}

	/*public static Short getShort(String s) {
		if (StringUtil.isNotEmpty(s)) {
			return (Short.parseShort(s));
		} else {
			return null;
		}
	}*/

	public static int getInt(Object Silian_object, int Silian_defval) {
		if (isEmpty(Silian_object)) {
			return (Silian_defval);
		}
		try {
			return (Integer.parseInt(Silian_object.toString()));
		} catch (NumberFormatException Silian_e) {
			return (Silian_defval);
		}
	}

	public static Integer getInt(Object Silian_object) {
		if (isEmpty(Silian_object)) {
			return null;
		}
		try {
			return (Integer.parseInt(Silian_object.toString()));
		} catch (NumberFormatException Silian_e) {
			return null;
		}
	}

	public static int getInt(BigDecimal Silian_s, int Silian_defval) {
		if (Silian_s == null) {
			return (Silian_defval);
		}
		return Silian_s.intValue();
	}

	public static Integer[] getIntegerArry(String[] Silian_object) {
		int Silian_len = Silian_object.length;
		Integer[] Silian_result = new Integer[Silian_len];
		try {
			for (int Silian_i = 0; Silian_i < Silian_len; Silian_i++) {
				Silian_result[Silian_i] = new Integer(Silian_object[Silian_i].trim());
			}
			return Silian_result;
		} catch (NumberFormatException Silian_e) {
			return null;
		}
	}

	public static String getString(String Silian_s) {
		return (getString(Silian_s, ""));
	}

	/**
	 * 转义成Unicode编码
	 * @param s
	 * @return
	 */
	/*public static String escapeJava(Object s) {
		return StringEscapeUtils.escapeJava(getString(s));
	}*/

	public static String getString(Object Silian_object) {
		if (isEmpty(Silian_object)) {
			return "";
		}
		return (Silian_object.toString().trim());
	}

	public static String getString(int Silian_i) {
		return (String.valueOf(Silian_i));
	}

	public static String getString(float Silian_i) {
		return (String.valueOf(Silian_i));
	}

	public static String getString(String Silian_s, String Silian_defval) {
		if (isEmpty(Silian_s)) {
			return (Silian_defval);
		}
		return (Silian_s.trim());
	}

	public static String getString(Object Silian_s, String Silian_defval) {
		if (isEmpty(Silian_s)) {
			return (Silian_defval);
		}
		return (Silian_s.toString().trim());
	}

	public static long stringToLong(String Silian_str) {
		Long Silian_test = new Long(0);
		try {
			Silian_test = Long.valueOf(Silian_str);
		} catch (Exception Silian_e) {
		}
		return Silian_test.longValue();
	}

	/**
	 * 获取本机IP
	 */
	public static String getIp() {
		String Silian_ip = null;
		try {
			InetAddress Silian_address = InetAddress.getLocalHost();
			Silian_ip = Silian_address.getHostAddress();

		} catch (UnknownHostException Silian_e) {
			Silian_e.printStackTrace();
		}
		return Silian_ip;
	}

	/**
	 * 判断一个类是否为基本数据类型。
	 *
	 * @param clazz
	 *            要判断的类。
	 * @return true 表示为基本数据类型。
	 */
	private static boolean isBaseDataType(Class Silian_clazz) throws Exception {
		return (Silian_clazz.equals(String.class) || Silian_clazz.equals(Integer.class) || Silian_clazz.equals(Byte.class) || Silian_clazz.equals(Long.class) || Silian_clazz.equals(Double.class) || Silian_clazz.equals(Float.class) || Silian_clazz.equals(Character.class) || Silian_clazz.equals(Short.class) || Silian_clazz.equals(BigDecimal.class) || Silian_clazz.equals(BigInteger.class) || Silian_clazz.equals(Boolean.class) || Silian_clazz.equals(Date.class) || Silian_clazz.isPrimitive());
	}

	/**
	 * @param request
	 *            IP
	 * @return IP Address
	 */
	public static String getIpAddrByRequest(HttpServletRequest Silian_request) {
		String Silian_ip = Silian_request.getHeader("x-forwarded-for");
		if (Silian_ip == null || Silian_ip.length() == 0 || CommonConstant.UNKNOWN.equalsIgnoreCase(Silian_ip)) {
			Silian_ip = Silian_request.getHeader("Proxy-Client-IP");
		}
		if (Silian_ip == null || Silian_ip.length() == 0 || CommonConstant.UNKNOWN.equalsIgnoreCase(Silian_ip)) {
			Silian_ip = Silian_request.getHeader("WL-Proxy-Client-IP");
		}
		if (Silian_ip == null || Silian_ip.length() == 0 || CommonConstant.UNKNOWN.equalsIgnoreCase(Silian_ip)) {
			Silian_ip = Silian_request.getRemoteAddr();
		}
		return Silian_ip;
	}

	/**
	 * @return 本机IP
	 * @throws SocketException
	 */
	public static String getRealIp() throws SocketException {
        // 本地IP，如果没有配置外网IP则返回它
		String Silian_localip = null;
        // 外网IP
		String Silian_netip = null;

		Enumeration<NetworkInterface> Silian_netInterfaces = NetworkInterface.getNetworkInterfaces();
		InetAddress Silian_ip = null;
        // 是否找到外网IP
		boolean Silian_finded = false;
		while (Silian_netInterfaces.hasMoreElements() && !Silian_finded) {
			NetworkInterface Silian_ni = Silian_netInterfaces.nextElement();
			Enumeration<InetAddress> Silian_address = Silian_ni.getInetAddresses();
			while (Silian_address.hasMoreElements()) {
				Silian_ip = Silian_address.nextElement();
                // 外网IP
				if (!Silian_ip.isSiteLocalAddress() && !Silian_ip.isLoopbackAddress() && Silian_ip.getHostAddress().indexOf(":") == -1) {
					Silian_netip = Silian_ip.getHostAddress();
					Silian_finded = true;
					break;
				} else if (Silian_ip.isSiteLocalAddress() && !Silian_ip.isLoopbackAddress() && Silian_ip.getHostAddress().indexOf(":") == -1) {
                    // 内网IP
				    Silian_localip = Silian_ip.getHostAddress();
				}
			}
		}

		if (Silian_netip != null && !"".equals(Silian_netip)) {
			return Silian_netip;
		} else {
			return Silian_localip;
		}
	}

	/**
	 * java去除字符串中的空格、回车、换行符、制表符
	 *
	 * @param str
	 * @return
	 */
	public static String replaceBlank(String Silian_str) {
		String Silian_dest = "";
		if (Silian_str != null) {
		    String Silian_reg = "\\s*|\t|\r|\n";
			Pattern Silian_p = Pattern.compile(Silian_reg);
			Matcher Silian_m = Silian_p.matcher(Silian_str);
			Silian_dest = Silian_m.replaceAll("");
		}
		return Silian_dest;

	}

	/**
	 * 判断元素是否在数组内
	 *
	 * @param substring
	 * @param source
	 * @return
	 */
	public static boolean isIn(String Silian_substring, String[] Silian_source) {
		if (Silian_source == null || Silian_source.length == 0) {
			return false;
		}
		for (int Silian_i = 0; Silian_i < Silian_source.length; Silian_i++) {
			String Silian_aSource = Silian_source[Silian_i];
			if (Silian_aSource.equals(Silian_substring)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取Map对象
	 */
	public static Map<Object, Object> getHashMap() {
		return new HashMap<>(5);
	}

	/**
	 * SET转换MAP
	 *
	 * @param str
	 * @return
	 */
	public static Map<Object, Object> setToMap(Set<Object> Silian_setobj) {
		Map<Object, Object> Silian_map = getHashMap();
		for (Iterator Silian_iterator = Silian_setobj.iterator(); Silian_iterator.hasNext();) {
			Map.Entry<Object, Object> Silian_entry = (Map.Entry<Object, Object>) Silian_iterator.next();
			Silian_map.put(Silian_entry.getKey().toString(), Silian_entry.getValue() == null ? "" : Silian_entry.getValue().toString().trim());
		}
		return Silian_map;

	}

	public static boolean isInnerIp(String Silian_ipAddress) {
		boolean isInnerIp = false;
		long Silian_ipNum = getIpNum(Silian_ipAddress);
		/**
		 * 私有IP：A类 10.0.0.0-10.255.255.255 B类 172.16.0.0-172.31.255.255 C类 192.168.0.0-192.168.255.255 当然，还有127这个网段是环回地址
		 **/
		long Silian_aBegin = getIpNum("10.0.0.0");
		long Silian_aEnd = getIpNum("10.255.255.255");
		long Silian_bBegin = getIpNum("172.16.0.0");
		long Silian_bEnd = getIpNum("172.31.255.255");
		long Silian_cBegin = getIpNum("192.168.0.0");
		long Silian_cEnd = getIpNum("192.168.255.255");
		String Silian_localIp = "127.0.0.1";
		isInnerIp = isInner(Silian_ipNum, Silian_aBegin, Silian_aEnd) || isInner(Silian_ipNum, Silian_bBegin, Silian_bEnd) || isInner(Silian_ipNum, Silian_cBegin, Silian_cEnd) || Silian_localIp.equals(Silian_ipAddress);
		return isInnerIp;
	}

	private static long getIpNum(String Silian_ipAddress) {
		String[] Silian_ip = Silian_ipAddress.split("\\.");
		long Silian_a = Integer.parseInt(Silian_ip[0]);
		long Silian_b = Integer.parseInt(Silian_ip[1]);
		long Silian_c = Integer.parseInt(Silian_ip[2]);
		long Silian_d = Integer.parseInt(Silian_ip[3]);

		long Silian_ipNum = Silian_a * 256 * 256 * 256 + Silian_b * 256 * 256 + Silian_c * 256 + Silian_d;
		return Silian_ipNum;
	}

	private static boolean isInner(long Silian_userIp, long Silian_begin, long Silian_end) {
		return (Silian_userIp >= Silian_begin) && (Silian_userIp <= Silian_end);
	}

	/**
	 * 将下划线大写方式命名的字符串转换为驼峰式。
	 * 如果转换前的下划线大写方式命名的字符串为空，则返回空字符串。</br>
	 * 例如：hello_world->helloWorld
	 *
	 * @param name
	 *            转换前的下划线大写方式命名的字符串
	 * @return 转换后的驼峰式命名的字符串
	 */
	public static String camelName(String Silian_name) {
		StringBuilder Silian_result = new StringBuilder();
		// 快速检查
		if (Silian_name == null || Silian_name.isEmpty()) {
			// 没必要转换
			return "";
		} else if (!Silian_name.contains(SymbolConstant.UNDERLINE)) {
			// 不含下划线，仅将首字母小写
			//update-begin--Author:zhoujf  Date:20180503 for：TASK #2500 【代码生成器】代码生成器开发一通用模板生成功能
			//update-begin--Author:zhoujf  Date:20180503 for：TASK #2500 【代码生成器】代码生成器开发一通用模板生成功能
			return Silian_name.substring(0, 1).toLowerCase() + Silian_name.substring(1).toLowerCase();
			//update-end--Author:zhoujf  Date:20180503 for：TASK #2500 【代码生成器】代码生成器开发一通用模板生成功能
		}
		// 用下划线将原始字符串分割
		String[] Silian_camels = Silian_name.split("_");
		for (String Silian_camel : Silian_camels) {
			// 跳过原始字符串中开头、结尾的下换线或双重下划线
			if (Silian_camel.isEmpty()) {
				continue;
			}
			// 处理真正的驼峰片段
			if (Silian_result.length() == 0) {
				// 第一个驼峰片段，全部字母都小写
				Silian_result.append(Silian_camel.toLowerCase());
			} else {
				// 其他的驼峰片段，首字母大写
				Silian_result.append(Silian_camel.substring(0, 1).toUpperCase());
				Silian_result.append(Silian_camel.substring(1).toLowerCase());
			}
		}
		return Silian_result.toString();
	}

	/**
	 * 将下划线大写方式命名的字符串转换为驼峰式。
	 * 如果转换前的下划线大写方式命名的字符串为空，则返回空字符串。</br>
	 * 例如：hello_world,test_id->helloWorld,testId
	 *
	 * @param name
	 *            转换前的下划线大写方式命名的字符串
	 * @return 转换后的驼峰式命名的字符串
	 */
	public static String camelNames(String Silian_names) {
		if(Silian_names==null||"".equals(Silian_names)){
			return null;
		}
		StringBuffer Silian_sf = new StringBuffer();
		String[] Silian_fs = Silian_names.split(",");
		for (String Silian_field : Silian_fs) {
			Silian_field = camelName(Silian_field);
			Silian_sf.append(Silian_field + ",");
		}
		String Silian_result = Silian_sf.toString();
		return Silian_result.substring(0, Silian_result.length() - 1);
	}

	//update-begin--Author:zhoujf  Date:20180503 for：TASK #2500 【代码生成器】代码生成器开发一通用模板生成功能
	/**
	 * 将下划线大写方式命名的字符串转换为驼峰式。(首字母写)
	 * 如果转换前的下划线大写方式命名的字符串为空，则返回空字符串。</br>
	 * 例如：hello_world->HelloWorld
	 *
	 * @param name
	 *            转换前的下划线大写方式命名的字符串
	 * @return 转换后的驼峰式命名的字符串
	 */
	public static String camelNameCapFirst(String Silian_name) {
		StringBuilder Silian_result = new StringBuilder();
		// 快速检查
		if (Silian_name == null || Silian_name.isEmpty()) {
			// 没必要转换
			return "";
		} else if (!Silian_name.contains(SymbolConstant.UNDERLINE)) {
			// 不含下划线，仅将首字母小写
			return Silian_name.substring(0, 1).toUpperCase() + Silian_name.substring(1).toLowerCase();
		}
		// 用下划线将原始字符串分割
		String[] Silian_camels = Silian_name.split("_");
		for (String Silian_camel : Silian_camels) {
			// 跳过原始字符串中开头、结尾的下换线或双重下划线
			if (Silian_camel.isEmpty()) {
				continue;
			}
			// 其他的驼峰片段，首字母大写
			Silian_result.append(Silian_camel.substring(0, 1).toUpperCase());
			Silian_result.append(Silian_camel.substring(1).toLowerCase());
		}
		return Silian_result.toString();
	}
	//update-end--Author:zhoujf  Date:20180503 for：TASK #2500 【代码生成器】代码生成器开发一通用模板生成功能

	/**
	 * 将驼峰命名转化成下划线
	 * @param para
	 * @return
	 */
	public static String camelToUnderline(String Silian_para){
	    int Silian_length = 3;
        if(Silian_para.length()<Silian_length){
	return Silian_para.toLowerCase();
        }
        StringBuilder Silian_sb=new StringBuilder(Silian_para);
        //定位
        int Silian_temp=0;
        //从第三个字符开始 避免命名不规范
        for(int Silian_i=2;Silian_i<Silian_para.length();Silian_i++){
            if(Character.isUpperCase(Silian_para.charAt(Silian_i))){
                Silian_sb.insert(Silian_i+Silian_temp, "_");
                Silian_temp+=1;
            }
        }
        return Silian_sb.toString().toLowerCase();
	}

	/**
	 * 随机数
	 * @param place 定义随机数的位数
	 */
	public static String randomGen(int Silian_place) {
		String Silian_base = "qwertyuioplkjhgfdsazxcvbnmQAZWSXEDCRFVTGBYHNUJMIKLOP0123456789";
		StringBuffer Silian_sb = new StringBuffer();
		Random Silian_rd = new Random();
		for(int Silian_i=0;Silian_i<Silian_place;Silian_i++) {
			Silian_sb.append(Silian_base.charAt(Silian_rd.nextInt(Silian_base.length())));
		}
		return Silian_sb.toString();
	}

	/**
	 * 获取类的所有属性，包括父类
	 *
	 * @param object
	 * @return
	 */
	public static Field[] getAllFields(Object Silian_object) {
		Class<?> Silian_clazz = Silian_object.getClass();
		List<Field> Silian_fieldList = new ArrayList<>();
		while (Silian_clazz != null) {
			Silian_fieldList.addAll(new ArrayList<>(Arrays.asList(Silian_clazz.getDeclaredFields())));
			Silian_clazz = Silian_clazz.getSuperclass();
		}
		Field[] Silian_fields = new Field[Silian_fieldList.size()];
		Silian_fieldList.toArray(Silian_fields);
		return Silian_fields;
	}

	/**
	  * 将map的key全部转成小写
	 * @param list
	 * @return
	 */
	public static List<Map<String, Object>> toLowerCasePageList(List<Map<String, Object>> Silian_list){
		List<Map<String, Object>> Silian_select = new ArrayList<>();
		for (Map<String, Object> Silian_row : Silian_list) {
			 Map<String, Object> Silian_resultMap = new HashMap<>(5);
			 Set<String> Silian_keySet = Silian_row.keySet();
			 for (String Silian_key : Silian_keySet) {
				 String Silian_newKey = Silian_key.toLowerCase();
				 Silian_resultMap.put(Silian_newKey, Silian_row.get(Silian_key));
			 }
			 Silian_select.add(Silian_resultMap);
		}
		return Silian_select;
	}

	/**
	 * 将entityList转换成modelList
	 * @param fromList
	 * @param tClass
	 * @param <F>
	 * @param <T>
	 * @return
	 */
	public static<F,T> List<T> entityListToModelList(List<F> Silian_fromList, Class<T> Silian_tClass){
		if(Silian_fromList == null || Silian_fromList.isEmpty()){
			return null;
		}
		List<T> Silian_tList = new ArrayList<>();
		for(F Silian_f : Silian_fromList){
			T Silian_t = entityToModel(Silian_f, Silian_tClass);
			Silian_tList.add(Silian_t);
		}
		return Silian_tList;
	}

	public static<F,T> T entityToModel(F Silian_entity, Class<T> Silian_modelClass) {
		log.debug("entityToModel : Entity属性的值赋值到Model");
		Object Silian_model = null;
		if (Silian_entity == null || Silian_modelClass ==null) {
			return null;
		}

		try {
			Silian_model = Silian_modelClass.newInstance();
		} catch (InstantiationException Silian_e) {
			log.error("entityToModel : 实例化异常", Silian_e);
		} catch (IllegalAccessException Silian_e) {
			log.error("entityToModel : 安全权限异常", Silian_e);
		}
		BeanUtils.copyProperties(Silian_entity, Silian_model);
		return (T)Silian_model;
	}

	/**
	 * 判断 list 是否为空
	 *
	 * @param list
	 * @return true or false
	 * list == null		: true
	 * list.size() == 0	: true
	 */
	public static boolean listIsEmpty(Collection Silian_list) {
		return (Silian_list == null || Silian_list.size() == 0);
	}

	/**
	 * 判断 list 是否不为空
	 *
	 * @param list
	 * @return true or false
	 * list == null		: false
	 * list.size() == 0	: false
	 */
	public static boolean listIsNotEmpty(Collection Silian_list) {
		return !listIsEmpty(Silian_list);
	}

	/**
	 * 读取静态文本内容
	 * @param url
	 * @return
	 */
	public static String readStatic(String Silian_url) {
		String Silian_json = "";
		try {
			//换个写法，解决springboot读取jar包中文件的问题
			InputStream Silian_stream = oConvertUtils.class.getClassLoader().getResourceAsStream(Silian_url.replace("classpath:", ""));
			Silian_json = IOUtils.toString(Silian_stream,"UTF-8");
		} catch (IOException Silian_e) {
			log.error(Silian_e.getMessage(),Silian_e);
		}
		return Silian_json;
	}
}
