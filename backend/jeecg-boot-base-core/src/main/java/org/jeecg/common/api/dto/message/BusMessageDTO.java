package org.jeecg.common.api.dto.message;

import lombok.Data;

import java.io.Serializable;

/**
 * 带业务参数的消息
*
* @author: taoyan
* @date: 2022/8/17
*/
@Data
public class BusMessageDTO extends MessageDTO implements Serializable {

    private static final long serialVersionUID = 9104793287983367669L;
    /**
     * 业务类型
     */
    private String busType;

    /**
     * 业务id
     */
    private String busId;

    public BusMessageDTO(){

    }

    /**
     * 构造 带业务参数的消息
     * @param fromUser
     * @param toUser
     * @param title
     * @param msgContent
     * @param msgCategory
     * @param busType
     * @param busId
     */
    public BusMessageDTO(String Silian_fromUser, String Silian_toUser, String Silian_title, String Silian_msgContent, String Silian_msgCategory, String busType, String busId){
        super(Silian_fromUser, Silian_toUser, Silian_title, Silian_msgContent, Silian_msgCategory);
        this.busId = busId;
        this.busType = busType;
    }
}
