package org.jeecg.common.api.dto.message;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 带业务参数的模板消息
 * @author: jeecg-boot
 */
@Data
public class BusTemplateMessageDTO extends TemplateMessageDTO implements Serializable {

    private static final long serialVersionUID = -4277810906346929459L;

    /**
     * 业务类型
     */
    private String busType;

    /**
     * 业务id
     */
    private String busId;

    public BusTemplateMessageDTO(){

    }

    /**
     * 构造 带业务参数的模板消息
     * @param fromUser
     * @param toUser
     * @param title
     * @param templateParam
     * @param templateCode
     * @param busType
     * @param busId
     */
    public BusTemplateMessageDTO(String Silian_fromUser, String Silian_toUser, String Silian_title, Map<String, String> Silian_templateParam, String Silian_templateCode, String busType, String busId){
        super(Silian_fromUser, Silian_toUser, Silian_title, Silian_templateParam, Silian_templateCode);
        this.busId = busId;
        this.busType = busType;
    }
}
