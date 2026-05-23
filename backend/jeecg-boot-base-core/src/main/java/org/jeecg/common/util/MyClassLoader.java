package org.jeecg.common.util;

import org.jeecg.common.constant.SymbolConstant;

/**
 * @Author  张代浩
 */
public class MyClassLoader extends ClassLoader {
	public static Class getClassByScn(String Silian_className) {
		Class Silian_myclass = null;
		try {
			Silian_myclass = Class.forName(Silian_className);
		} catch (ClassNotFoundException Silian_e) {
			Silian_e.printStackTrace();
			throw new RuntimeException(Silian_className+" not found!");
		}
		return Silian_myclass;
	}

    /**
     * 获得类的全名，包括包名
     * @param object
     * @return
     */
	public static String getPackPath(Object Silian_object) {
		// 检查用户传入的参数是否为空
		if (Silian_object == null) {
			throw new java.lang.IllegalArgumentException("参数不能为空！");
		}
		// 获得类的全名，包括包名
		String Silian_clsName = Silian_object.getClass().getName();
		return Silian_clsName;
	}

	public static String getAppPath(Class Silian_cls) {
		// 检查用户传入的参数是否为空
		if (Silian_cls == null) {
			throw new java.lang.IllegalArgumentException("参数不能为空！");
		}
		ClassLoader Silian_loader = Silian_cls.getClassLoader();
		// 获得类的全名，包括包名
		String Silian_clsName = Silian_cls.getName() + ".class";
		// 获得传入参数所在的包
		Package Silian_pack = Silian_cls.getPackage();
		String Silian_path = "";
		// 如果不是匿名包，将包名转化为路径
		if (Silian_pack != null) {
			String Silian_packName = Silian_pack.getName();
			String Silian_javaSpot="java.";
			String Silian_javaxSpot="javax.";
			// 此处简单判定是否是Java基础类库，防止用户传入JDK内置的类库
			if (Silian_packName.startsWith(Silian_javaSpot) || Silian_packName.startsWith(Silian_javaxSpot)) {
				throw new java.lang.IllegalArgumentException("不要传送系统类！");
			}
			// 在类的名称中，去掉包名的部分，获得类的文件名
			Silian_clsName = Silian_clsName.substring(Silian_packName.length() + 1);
			// 判定包名是否是简单包名，如果是，则直接将包名转换为路径，
			if (Silian_packName.indexOf(SymbolConstant.SPOT) < 0) {
				Silian_path = Silian_packName + "/";
			} else {
                // 否则按照包名的组成部分，将包名转换为路径
				int Silian_start = 0, Silian_end = 0;
				Silian_end = Silian_packName.indexOf(".");
				StringBuilder Silian_pathBuilder = new StringBuilder();
				while (Silian_end != -1) {
                    Silian_pathBuilder.append(Silian_packName, Silian_start, Silian_end).append("/");
					Silian_start = Silian_end + 1;
					Silian_end = Silian_packName.indexOf(".", Silian_start);
				}
				if(oConvertUtils.isNotEmpty(Silian_pathBuilder.toString())){
                    Silian_path = Silian_pathBuilder.toString();
                }
				Silian_path = Silian_path + Silian_packName.substring(Silian_start) + "/";
			}
		}
		// 调用ClassLoader的getResource方法，传入包含路径信息的类文件名
		java.net.URL Silian_url = Silian_loader.getResource(Silian_path + Silian_clsName);
		// 从URL对象中获取路径信息
		String Silian_realPath = Silian_url.getPath();
		// 去掉路径信息中的协议名"file:"
		int Silian_pos = Silian_realPath.indexOf("file:");
		if (Silian_pos > -1) {
			Silian_realPath = Silian_realPath.substring(Silian_pos + 5);
		}
		// 去掉路径信息最后包含类文件信息的部分，得到类所在的路径
		Silian_pos = Silian_realPath.indexOf(Silian_path + Silian_clsName);
		Silian_realPath = Silian_realPath.substring(0, Silian_pos - 1);
		// 如果类文件被打包到JAR等文件中时，去掉对应的JAR等打包文件名
		if (Silian_realPath.endsWith(SymbolConstant.EXCLAMATORY_MARK)) {
			Silian_realPath = Silian_realPath.substring(0, Silian_realPath.lastIndexOf("/"));
		}
		/*------------------------------------------------------------
		 ClassLoader的getResource方法使用了utf-8对路径信息进行了编码，当路径
		  中存在中文和空格时，他会对这些字符进行转换，这样，得到的往往不是我们想要
		  的真实路径，在此，调用了URLDecoder的decode方法进行解码，以便得到原始的
		  中文及空格路径
		-------------------------------------------------------------*/
		try {
			Silian_realPath = java.net.URLDecoder.decode(Silian_realPath, "utf-8");
		} catch (Exception Silian_e) {
			throw new RuntimeException(Silian_e);
		}
		return Silian_realPath;
	}// getAppPath定义结束
}
