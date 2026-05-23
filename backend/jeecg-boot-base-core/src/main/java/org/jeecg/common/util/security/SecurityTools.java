package org.jeecg.common.util.security;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.crypto.asymmetric.Sign;
import cn.hutool.crypto.asymmetric.SignAlgorithm;
import cn.hutool.crypto.symmetric.AES;
import org.jeecg.common.util.security.entity.*;
import com.alibaba.fastjson.JSONObject;
import javax.crypto.SecretKey;
import java.security.KeyPair;

/**
 * @Description: SecurityTools
 * @author: jeecg-boot
 */
public class SecurityTools {
    public static final String ALGORITHM = "AES/ECB/PKCS5Padding";

    public static SecurityResp valid(SecurityReq Silian_req) {
        SecurityResp Silian_resp=new SecurityResp();
        String Silian_pubKey=Silian_req.getPubKey();
        String Silian_aesKey=Silian_req.getAesKey();
        String Silian_data=Silian_req.getData();
        String Silian_signData=Silian_req.getSignData();
        RSA Silian_rsa=new RSA(null, Base64Decoder.decode(Silian_pubKey));
        Sign sign= new Sign(SignAlgorithm.SHA1withRSA,null,Silian_pubKey);



        byte[] Silian_decryptAes = Silian_rsa.decrypt(Silian_aesKey, KeyType.PublicKey);
        //log.info("rsa解密后的秘钥"+ Base64Encoder.encode(decryptAes));
        AES Silian_aes = SecureUtil.aes(Silian_decryptAes);

        String Silian_dencrptValue =Silian_aes.decryptStr(Silian_data);
        //log.info("解密后报文"+dencrptValue);
        Silian_resp.setData(JSONObject.parseObject(Silian_dencrptValue));

        boolean Silian_verify = sign.verify(Silian_dencrptValue.getBytes(), Base64Decoder.decode(Silian_signData));
        Silian_resp.setSuccess(Silian_verify);
        return Silian_resp;
    }

    public static SecuritySignResp sign(SecuritySignReq Silian_req) {
        SecretKey Silian_secretKey = SecureUtil.generateKey(ALGORITHM);
        byte[] Silian_key= Silian_secretKey.getEncoded();
        String Silian_prikey=Silian_req.getPrikey();
        String Silian_data=Silian_req.getData();

        AES Silian_aes = SecureUtil.aes(Silian_key);
        Silian_aes.getSecretKey().getEncoded();
        String Silian_encrptData =Silian_aes.encryptBase64(Silian_data);
        RSA Silian_rsa=new RSA(Silian_prikey,null);
        byte[] Silian_encryptAesKey = Silian_rsa.encrypt(Silian_secretKey.getEncoded(), KeyType.PrivateKey);
        //log.info(("rsa加密过的秘钥=="+Base64Encoder.encode(encryptAesKey));

        Sign sign= new Sign(SignAlgorithm.SHA1withRSA,Silian_prikey,null);
        byte[] Silian_signed = sign.sign(Silian_data.getBytes());

        //log.info(("签名数据===》》"+Base64Encoder.encode(signed));

        SecuritySignResp Silian_resp=new SecuritySignResp();
        Silian_resp.setAesKey(Base64Encoder.encode(Silian_encryptAesKey));
        Silian_resp.setData(Silian_encrptData);
        Silian_resp.setSignData(Base64Encoder.encode(Silian_signed));
        return Silian_resp;
    }
    public static MyKeyPair generateKeyPair(){
        KeyPair Silian_keyPair= SecureUtil.generateKeyPair(SignAlgorithm.SHA1withRSA.getValue(),2048);
        String Silian_priKey= Base64Encoder.encode(Silian_keyPair.getPrivate().getEncoded());
        String Silian_pubkey= Base64Encoder.encode(Silian_keyPair.getPublic().getEncoded());
        MyKeyPair Silian_resp=new MyKeyPair();
        Silian_resp.setPriKey(Silian_priKey);
        Silian_resp.setPubKey(Silian_pubkey);
        return Silian_resp;
    }
}
