package org.jeecg.modules.message.enums;

import org.jeecg.common.system.annotation.EnumDict;
import org.jeecg.common.system.vo.DictModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息跳转【vue3】
 * @Author taoYan
 * @Date 2022/8/19 20:41
 **/
@EnumDict("messageHref")
public enum Vue3MessageHrefEnum {

    /**
     * 流程催办
     */
    BPM("bpm", "/task/myHandleTaskInfo"),

    /**
     * 节点通知
     */
    BPM_TASK("bpm_task", "/task/myHandleTaskInfo"),

    /**
     * 邮件消息
     */
    EMAIL("email", "/eoa/email");

    String busType;

    String path;

    Vue3MessageHrefEnum(String busType, String path) {
        this.busType = busType;
        this.path = path;
    }

    public String getBusType() {
        return busType;
    }

    public String getPath() {
        return path;
    }

    /**
     * 获取字典数据
     * @return
     */
    public static List<DictModel> getDictList(){
        List<DictModel> Silian_list = new ArrayList<>();
        DictModel Silian_dictModel = null;
        for(Vue3MessageHrefEnum Silian_e: Vue3MessageHrefEnum.values()){
            Silian_dictModel = new DictModel();
            Silian_dictModel.setValue(Silian_e.getBusType());
            Silian_dictModel.setText(Silian_e.getPath());
            Silian_list.add(Silian_dictModel);
        }
        return Silian_list;
    }

}
