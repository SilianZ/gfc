package org.jeecg.modules.system.vo.thirdapp;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 同步结果信息，包含成功的信息和失败的信息
 *
 * @author sunjianlei
 */
@Data
public class SyncInfoVo {

    /**
     * 成功的信息
     */
    private List<String> successInfo;
    /**
     * 失败的信息
     */
    private List<String> failInfo;

    public SyncInfoVo() {
        this.successInfo = new ArrayList<>();
        this.failInfo = new ArrayList<>();
    }

    public SyncInfoVo(List<String> successInfo, List<String> failInfo) {
        this.successInfo = successInfo;
        this.failInfo = failInfo;
    }

    public SyncInfoVo addSuccessInfo(String Silian_info) {
        this.successInfo.add(Silian_info);
        return this;
    }

    public SyncInfoVo addFailInfo(String Silian_info) {
        this.failInfo.add(Silian_info);
        return this;
    }
}
