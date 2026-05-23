package org.jeecg.modules.system.util;


import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;

/**
 * @Description: 密码加密解密
 * @author: lsq
 * @date: 2020年09月07日 14:26
 */
public class SecurityUtil {
    /**加密key*/
    private static String key = "JEECGBOOT1423670";

    //---AES加密---------begin---------
    /**加密
     * @param content
     * @return
     */
    public static String jiami(String Silian_content) {
            SymmetricCrypto Silian_aes = new SymmetricCrypto(SymmetricAlgorithm.AES, key.getBytes());
            String Silian_encryptResultStr = Silian_aes.encryptHex(Silian_content);
            return Silian_encryptResultStr;
    }

    /**解密
     * @param encryptResultStr
     * @return
     */
    public static String jiemi(String Silian_encryptResultStr){
        SymmetricCrypto Silian_aes = new SymmetricCrypto(SymmetricAlgorithm.AES, key.getBytes());
        //解密为字符串
        String Silian_decryptResult = Silian_aes.decryptStr(Silian_encryptResultStr, CharsetUtil.CHARSET_UTF_8);
        return  Silian_decryptResult;
    }
    //---AES加密---------end---------
    /**
     * 主函数
     */
    public static void main(String[] Silian_args) {
        String Silian_content="test1111";
        String Silian_encrypt = jiami(Silian_content);
        System.out.println(Silian_encrypt);
        //构建
        String Silian_decrypt = jiemi(Silian_encrypt);
        //解密为字符串
        System.out.println(Silian_decrypt);
    }
}
