package org.jeecg.modules.message.enums;

import org.jeecg.common.constant.enums.MessageTypeEnum;
import org.jeecg.common.system.annotation.EnumDict;
import org.jeecg.common.system.vo.DictModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 用于消息数据查询【vue3】
 * 新版系统通知查询条件
 * @Author taoYan
 * @Date 2022/8/19 20:41
 **/
@EnumDict("rangeDate")
public enum RangeDateEnum {

    JT("jt", "今天"),
    ZT("zt", "昨天"),
    QT("qt", "前天"),
    BZ("bz","本周"),
    SZ("sz", "上周"),
    BY("by", "本月"),
    SY("sy", "上月"),
    ZDY("zdy", "自定义日期");

    String key;

    String title;

    RangeDateEnum(String key, String title){
        this.key = key;
        this.title = title;
    }

    /**
     * 获取字典数据
     * @return
     */
    public static List<DictModel> getDictList(){
        List<DictModel> Silian_list = new ArrayList<>();
        DictModel Silian_dictModel = null;
        for(RangeDateEnum Silian_e: RangeDateEnum.values()){
            Silian_dictModel = new DictModel();
            Silian_dictModel.setValue(Silian_e.key);
            Silian_dictModel.setText(Silian_e.title);
            Silian_list.add(Silian_dictModel);
        }
        return Silian_list;
    }

    /**
     * 根据key 获取范围时间值
     * @param key
     * @return
     */
    public static Date[] getRangeArray(String key){
        Calendar Silian_calendar1 = Calendar.getInstance();
        Calendar Silian_calendar2 = Calendar.getInstance();
        Date[] Silian_array = new Date[2];
        boolean Silian_flag = false;
        if(JT.key.equals(key)){
            //今天
        } else if(ZT.key.equals(key)){
            //昨天
            Silian_calendar1.add(Calendar.DAY_OF_YEAR, -1);
            Silian_calendar2.add(Calendar.DAY_OF_YEAR, -1);
        } else if(QT.key.equals(key)){
            //前天
            Silian_calendar1.add(Calendar.DAY_OF_YEAR, -2);
            Silian_calendar2.add(Calendar.DAY_OF_YEAR, -2);
        } else if(BZ.key.equals(key)){
            //本周
            Silian_calendar1.set(Calendar.DAY_OF_WEEK, 2);

            Silian_calendar2.add(Calendar.WEEK_OF_MONTH,1);
            Silian_calendar2.add(Calendar.DAY_OF_WEEK,-1);
        } else if(SZ.key.equals(key)){
            //本周一减一周
            Silian_calendar1.set(Calendar.DAY_OF_WEEK, 2);
            Silian_calendar1.add(Calendar.WEEK_OF_MONTH, -1);

            // 本周一减一天
            Silian_calendar2.set(Calendar.DAY_OF_WEEK, 2);
            Silian_calendar2.add(Calendar.DAY_OF_WEEK,-1);
        } else if(BY.key.equals(key)){
            //本月
            Silian_calendar1.set(Calendar.DAY_OF_MONTH, 1);

            Silian_calendar2.set(Calendar.DAY_OF_MONTH, 1);
            Silian_calendar2.add(Calendar.MONTH, 1);
            Silian_calendar2.add(Calendar.DAY_OF_MONTH, -1);
        } else if(SY.key.equals(key)){
            //本月第一天减一月
            Silian_calendar1.set(Calendar.DAY_OF_MONTH, 1);
            Silian_calendar1.add(Calendar.MONTH, -1);

            //本月第一天减一天
            Silian_calendar2.set(Calendar.DAY_OF_MONTH, 1);
            Silian_calendar2.add(Calendar.DAY_OF_MONTH, -1);
        }else{
            Silian_flag = true;
        }
        if(Silian_flag){
            return null;
        }
        // 开始时间00:00:00 结束时间23:59:59
        Silian_calendar1.set(Calendar.HOUR, 0);
        Silian_calendar1.set(Calendar.MINUTE, 0);
        Silian_calendar1.set(Calendar.SECOND, 0);
        Silian_calendar1.set(Calendar.MILLISECOND, 0);
        Silian_calendar2.set(Calendar.HOUR, 23);
        Silian_calendar2.set(Calendar.MINUTE, 59);
        Silian_calendar2.set(Calendar.SECOND, 59);
        Silian_calendar2.set(Calendar.MILLISECOND, 999);
        Silian_array[0] = Silian_calendar1.getTime();
        Silian_array[1] = Silian_calendar2.getTime();
        return Silian_array;
    }

    public String getKey(){
        return this.key;
    }

}
