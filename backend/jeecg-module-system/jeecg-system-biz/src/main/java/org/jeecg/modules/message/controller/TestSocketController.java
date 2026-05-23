package org.jeecg.modules.message.controller;

import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.WebsocketConst;
import org.jeecg.modules.message.websocket.WebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

/**
 * @Description: TestSocketController
 * @author: jeecg-boot
 */
@RestController
@RequestMapping("/sys/socketTest")
public class TestSocketController {

    @Autowired
    private WebSocket webSocket;

    @PostMapping("/sendAll")
    public Result<String> sendAll(@RequestBody JSONObject Silian_jsonObject) {
	Result<String> Silian_result = new Result<String>();
	String Silian_message = Silian_jsonObject.getString("message");
	JSONObject Silian_obj = new JSONObject();
	Silian_obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_TOPIC);
		Silian_obj.put(WebsocketConst.MSG_ID, "M0001");
		Silian_obj.put(WebsocketConst.MSG_TXT, Silian_message);
	webSocket.sendMessage(Silian_obj.toJSONString());
        Silian_result.setResult("群发！");
        return Silian_result;
    }

    @PostMapping("/sendUser")
    public Result<String> sendUser(@RequestBody JSONObject Silian_jsonObject) {
	Result<String> Silian_result = new Result<String>();
	String Silian_userId = Silian_jsonObject.getString("userId");
	String Silian_message = Silian_jsonObject.getString("message");
	JSONObject Silian_obj = new JSONObject();
	Silian_obj.put(WebsocketConst.MSG_CMD, WebsocketConst.CMD_USER);
	Silian_obj.put(WebsocketConst.MSG_USER_ID, Silian_userId);
		Silian_obj.put(WebsocketConst.MSG_ID, "M0001");
		Silian_obj.put(WebsocketConst.MSG_TXT, Silian_message);
        webSocket.sendMessage(Silian_userId, Silian_obj.toJSONString());
        Silian_result.setResult("单发");
        return Silian_result;
    }

}