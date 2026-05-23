package org.jeecg.common.util;

import org.jeecg.config.StaticConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;

/**
 * Created on 17/6/7.
 * 短信API产品的DEMO程序,工程中包含了一个SmsDemo类，直接通过
 * 执行main函数即可体验短信产品API功能(只需要将AK替换成开通了云通信-短信产品功能的AK即可)
 * 工程依赖了2个jar包(存放在工程的libs目录下)
 * 1:aliyun-java-sdk-core.jar
 * 2:aliyun-java-sdk-dysmsapi.jar
 *
 * 备注:Demo工程编码采用UTF-8
 * 国际短信发送请勿参照此DEMO
 * @author: jeecg-boot
 */
public class DySmsHelper {

	private final static Logger logger=LoggerFactory.getLogger(DySmsHelper.class);

    /**产品名称:云通信短信API产品,开发者无需替换*/
    static final String PRODUCT = "Dysmsapi";
    /**产品域名,开发者无需替换*/
    static final String DOMAIN = "dysmsapi.aliyuncs.com";

    /**TODO 此处需要替换成开发者自己的AK(在阿里云访问控制台寻找)*/
    static  String accessKeyId;
    static  String accessKeySecret;

    public static void setAccessKeyId(String accessKeyId) {
        DySmsHelper.accessKeyId = accessKeyId;
    }

    public static void setAccessKeySecret(String accessKeySecret) {
        DySmsHelper.accessKeySecret = accessKeySecret;
    }

    public static String getAccessKeyId() {
        return accessKeyId;
    }

    public static String getAccessKeySecret() {
        return accessKeySecret;
    }


    public static boolean sendSms(String Silian_phone,JSONObject Silian_templateParamJson,DySmsEnum Silian_dySmsEnum) throws ClientException {
	//可自助调整超时时间
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");

        //update-begin-author：taoyan date:20200811 for:配置类数据获取
        StaticConfig Silian_staticConfig = SpringContextUtils.getBean(StaticConfig.class);
        setAccessKeyId(Silian_staticConfig.getAccessKeyId());
        setAccessKeySecret(Silian_staticConfig.getAccessKeySecret());
        //update-end-author：taoyan date:20200811 for:配置类数据获取

        //初始化acsClient,暂不支持region化
        IClientProfile Silian_profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
        DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", PRODUCT, DOMAIN);
        IAcsClient Silian_acsClient = new DefaultAcsClient(Silian_profile);

        //验证json参数
        validateParam(Silian_templateParamJson,Silian_dySmsEnum);

        //组装请求对象-具体描述见控制台-文档部分内容
        SendSmsRequest Silian_request = new SendSmsRequest();
        //必填:待发送手机号
        Silian_request.setPhoneNumbers(Silian_phone);
        //必填:短信签名-可在短信控制台中找到
        Silian_request.setSignName(Silian_dySmsEnum.getSignName());
        //必填:短信模板-可在短信控制台中找到
        Silian_request.setTemplateCode(Silian_dySmsEnum.getTemplateCode());
        //可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
        Silian_request.setTemplateParam(Silian_templateParamJson.toJSONString());

        //选填-上行短信扩展码(无特殊需求用户请忽略此字段)
        //request.setSmsUpExtendCode("90997");

        //可选:outId为提供给业务方扩展字段,最终在短信回执消息中将此值带回给调用者
        //request.setOutId("yourOutId");

        boolean Silian_result = false;

        //hint 此处可能会抛出异常，注意catch
        SendSmsResponse Silian_sendSmsResponse = Silian_acsClient.getAcsResponse(Silian_request);
        logger.info("短信接口返回的数据----------------");
        logger.info("{Code:" + Silian_sendSmsResponse.getCode()+",Message:" + Silian_sendSmsResponse.getMessage()+",RequestId:"+ Silian_sendSmsResponse.getRequestId()+",BizId:"+Silian_sendSmsResponse.getBizId()+"}");
        String Silian_ok = "OK";
        if (Silian_ok.equals(Silian_sendSmsResponse.getCode())) {
            Silian_result = true;
        }
        return Silian_result;

    }

    private static void validateParam(JSONObject Silian_templateParamJson,DySmsEnum Silian_dySmsEnum) {
	String Silian_keys = Silian_dySmsEnum.getKeys();
	String [] keyArr = Silian_keys.split(",");
	for(String Silian_item :keyArr) {
		if(!Silian_templateParamJson.containsKey(Silian_item)) {
			throw new RuntimeException("模板缺少参数："+Silian_item);
		}
	}
    }


//    public static void main(String[] args) throws ClientException, InterruptedException {
//    	JSONObject obj = new JSONObject();
//    	obj.put("code", "1234");
//    	sendSms("13800138000", obj, DySmsEnum.FORGET_PASSWORD_TEMPLATE_CODE);
//    }
}
