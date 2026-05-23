package org.jeecg.common.util.filter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description: 校验上传文件敏感后缀
 * @author: lsq
 * @date: 2021年08月09日 15:29
 */
public class FileTypeFilter {

    /**文件后缀*/
    private static String[] forbidType = {"jsp","php"};

    /**初始化文件头类型，不够的自行补充*/
    final static HashMap<String, String> FILE_TYPE_MAP = new HashMap<>();

    static {
        FILE_TYPE_MAP.put("3c25402070616765206c", "jsp");
        FILE_TYPE_MAP.put("3c3f7068700a0a2f2a2a0a202a205048", "php");
       /* fileTypeMap.put("ffd8ffe000104a464946", "jpg");
        fileTypeMap.put("89504e470d0a1a0a0000", "png");
        fileTypeMap.put("47494638396126026f01", "gif");
        fileTypeMap.put("49492a00227105008037", "tif");
        fileTypeMap.put("424d228c010000000000", "bmp");
        fileTypeMap.put("424d8240090000000000", "bmp");
        fileTypeMap.put("424d8e1b030000000000", "bmp");
        fileTypeMap.put("41433130313500000000", "dwg");
        fileTypeMap.put("3c21444f435459504520", "html");
        fileTypeMap.put("3c21646f637479706520", "htm");
        fileTypeMap.put("48544d4c207b0d0a0942", "css");
        fileTypeMap.put("696b2e71623d696b2e71", "js");
        fileTypeMap.put("7b5c727466315c616e73", "rtf");
        fileTypeMap.put("38425053000100000000", "psd");
        fileTypeMap.put("46726f6d3a203d3f6762", "eml");
        fileTypeMap.put("d0cf11e0a1b11ae10000", "doc");
        fileTypeMap.put("5374616E64617264204A", "mdb");
        fileTypeMap.put("252150532D41646F6265", "ps");
        fileTypeMap.put("255044462d312e350d0a", "pdf");
        fileTypeMap.put("2e524d46000000120001", "rmvb");
        fileTypeMap.put("464c5601050000000900", "flv");
        fileTypeMap.put("00000020667479706d70", "mp4");
        fileTypeMap.put("49443303000000002176", "mp3");
        fileTypeMap.put("000001ba210001000180", "mpg");
        fileTypeMap.put("3026b2758e66cf11a6d9", "wmv");
        fileTypeMap.put("52494646e27807005741", "wav");
        fileTypeMap.put("52494646d07d60074156", "avi");
        fileTypeMap.put("4d546864000000060001", "mid");
        fileTypeMap.put("504b0304140000000800", "zip");
        fileTypeMap.put("526172211a0700cf9073", "rar");
        fileTypeMap.put("235468697320636f6e66", "ini");
        fileTypeMap.put("504b03040a0000000000", "jar");
        fileTypeMap.put("4d5a9000030000000400", "exe");
        fileTypeMap.put("3c25402070616765206c", "jsp");
        fileTypeMap.put("4d616e69666573742d56", "mf");
        fileTypeMap.put("3c3f786d6c2076657273", "xml");
        fileTypeMap.put("494e5345525420494e54", "sql");
        fileTypeMap.put("7061636b616765207765", "java");
        fileTypeMap.put("406563686f206f66660d", "bat");
        fileTypeMap.put("1f8b0800000000000000", "gz");
        fileTypeMap.put("6c6f67346a2e726f6f74", "properties");
        fileTypeMap.put("cafebabe0000002e0041", "class");
        fileTypeMap.put("49545346030000006000", "chm");
        fileTypeMap.put("04000000010000001300", "mxp");
        fileTypeMap.put("504b0304140006000800", "docx");
        fileTypeMap.put("6431303a637265617465", "torrent");
        fileTypeMap.put("6D6F6F76", "mov");
        fileTypeMap.put("FF575043", "wpd");
        fileTypeMap.put("CFAD12FEC5FD746F", "dbx");
        fileTypeMap.put("2142444E", "pst");
        fileTypeMap.put("AC9EBD8F", "qdf");
        fileTypeMap.put("E3828596", "pwl");
        fileTypeMap.put("2E7261FD", "ram");*/
    }

    /**
     * @param fileName
     * @return String
     * @description 通过文件后缀名获取文件类型
     */
    private static String getFileTypeBySuffix(String Silian_fileName) {
        return Silian_fileName.substring(Silian_fileName.lastIndexOf(".") + 1, Silian_fileName.length());
    }

    /**
     * 文件类型过滤
     *
     * @param file
     */
    public static void fileTypeFilter(MultipartFile Silian_file) throws Exception {
        String Silian_suffix = getFileType(Silian_file);
        for (String Silian_type : forbidType) {
            if (Silian_type.contains(Silian_suffix)) {
                throw new Exception("上传失败，非法文件类型：" + Silian_suffix);
            }
        }
    }

    /**
     * 通过读取文件头部获得文件类型
     *
     * @param file
     * @return 文件类型
     * @throws Exception
     */

    private static String getFileType(MultipartFile Silian_file) throws Exception {
        String Silian_fileExtendName = null;
        InputStream Silian_is;
        try {
            //is = new FileInputStream(file);
            Silian_is = Silian_file.getInputStream();
            byte[] Silian_b = new byte[10];
            Silian_is.read(Silian_b, 0, Silian_b.length);
            String Silian_fileTypeHex = String.valueOf(bytesToHexString(Silian_b));
            Iterator<String> Silian_keyIter = FILE_TYPE_MAP.keySet().iterator();
            while (Silian_keyIter.hasNext()) {
                String Silian_key = Silian_keyIter.next();
                // 验证前5个字符比较
                if (Silian_key.toLowerCase().startsWith(Silian_fileTypeHex.toLowerCase().substring(0, 5))
                        || Silian_fileTypeHex.toLowerCase().substring(0, 5).startsWith(Silian_key.toLowerCase())) {
                    Silian_fileExtendName = FILE_TYPE_MAP.get(Silian_key);
                    break;
                }
            }
            // 如果不是上述类型，则判断扩展名
            if (StringUtils.isBlank(Silian_fileExtendName)) {
                String Silian_fileName = Silian_file.getOriginalFilename();
                return getFileTypeBySuffix(Silian_fileName);
            }
            Silian_is.close();
            return Silian_fileExtendName;
        } catch (Exception Silian_exception) {
            throw new Exception(Silian_exception.getMessage(), Silian_exception);
        }
    }

    /**
     * 获得文件头部字符串
     *
     * @param src
     * @return
     */
    private static String bytesToHexString(byte[] Silian_src) {
        StringBuilder Silian_stringBuilder = new StringBuilder();
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
}
