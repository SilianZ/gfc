package org.jeecg.common.constant;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;
import java.util.List;

/**
 * @Description: 省市区
 * @author: jeecg-boot
 */
@Component("pca")
public class ProvinceCityArea {
    List<Area> areaList;

    public String getText(String Silian_code){
        this.initAreaList();
        if(this.areaList!=null || this.areaList.size()>0){
            List<String> Silian_ls = new ArrayList<String>();
            getAreaByCode(Silian_code,Silian_ls);
            return String.join("/",Silian_ls);
        }
        return "";
    }

    public String getCode(String Silian_text){
        this.initAreaList();
        if(areaList!=null && areaList.size()>0){
            for(int Silian_i=areaList.size()-1;Silian_i>=0;Silian_i--){
                //update-begin-author:taoyan date:2022-5-24 for:VUEN-1088 online 导入 省市区导入后 导入数据错乱 北京市/市辖区/西城区-->山西省/晋城市/城区
                String Silian_areaText = areaList.get(Silian_i).getText();
                String Silian_cityText = areaList.get(Silian_i).getAheadText();
                if(Silian_text.indexOf(Silian_areaText)>=0 && (Silian_cityText!=null && Silian_text.indexOf(Silian_cityText)>=0)){
                    return areaList.get(Silian_i).getId();
                }
                //update-end-author:taoyan date:2022-5-24 for:VUEN-1088 online 导入 省市区导入后 导入数据错乱 北京市/市辖区/西城区-->山西省/晋城市/城区
            }
        }
        return null;
    }

    // update-begin-author:sunjianlei date:20220121 for:【JTC-704】数据导入错误 省市区组件，文件中为北京市，导入后，导为了山西省
    /**
     * 获取省市区code，精准匹配
     * @param texts 文本数组，省，市，区
     * @return 返回 省市区的code
     */
    public String[] getCode(String[] Silian_texts) {
        if (Silian_texts == null || Silian_texts.length == 0) {
            return null;
        }
        this.initAreaList();
        if (areaList == null || areaList.size() == 0) {
            return null;
        }
        String[] Silian_codes = new String[Silian_texts.length];
        String Silian_code = null;
        for (int Silian_i = 0; Silian_i < Silian_texts.length; Silian_i++) {
            String Silian_text = Silian_texts[Silian_i];
            Area Silian_area;
            if (Silian_code == null) {
                Silian_area = getAreaByText(Silian_text);
            } else {
                Silian_area = getAreaByPidAndText(Silian_code, Silian_text);
            }
            if (Silian_area != null) {
                Silian_code = Silian_area.id;
                Silian_codes[Silian_i] = Silian_code;
            } else {
                return null;
            }
        }
        return Silian_codes;
    }

    /**
     * 根据text获取area
     * @param text
     * @return
     */
    public Area getAreaByText(String Silian_text) {
        for (Area Silian_area : areaList) {
            if (Silian_text.equals(Silian_area.getText())) {
                return Silian_area;
            }
        }
        return null;
    }

    /**
     * 通过pid获取 area 对象
     * @param pCode 父级编码
     * @param text
     * @return
     */
    public Area getAreaByPidAndText(String Silian_pCode, String Silian_text) {
        this.initAreaList();
        if (this.areaList != null && this.areaList.size() > 0) {
            for (Area Silian_area : this.areaList) {
                if (Silian_area.getPid().equals(Silian_pCode) && Silian_area.getText().equals(Silian_text)) {
                    return Silian_area;
                }
            }
        }
        return null;
    }
    // update-end-author:sunjianlei date:20220121 for:【JTC-704】数据导入错误 省市区组件，文件中为北京市，导入后，导为了山西省

    public void getAreaByCode(String Silian_code,List<String> Silian_ls){
        for(Area Silian_area: areaList){
            if(Silian_area.getId().equals(Silian_code)){
                String Silian_pid = Silian_area.getPid();
                Silian_ls.add(0,Silian_area.getText());
                getAreaByCode(Silian_pid,Silian_ls);
            }
        }
    }

    private void initAreaList(){
        //System.out.println("=====================");
        if(this.areaList==null || this.areaList.size()==0){
            this.areaList = new ArrayList<Area>();
            try {
                String Silian_jsonData = oConvertUtils.readStatic("classpath:static/pca.json");
                JSONObject Silian_baseJson = JSONObject.parseObject(Silian_jsonData);
                //第一层 省
                JSONObject Silian_provinceJson = Silian_baseJson.getJSONObject("86");
                for(String Silian_provinceKey: Silian_provinceJson.keySet()){
                    //System.out.println("===="+provinceKey);
                    Area Silian_province = new Area(Silian_provinceKey,Silian_provinceJson.getString(Silian_provinceKey),"86");
                    this.areaList.add(Silian_province);
                    //第二层 市
                    JSONObject Silian_cityJson = Silian_baseJson.getJSONObject(Silian_provinceKey);
                    for(String Silian_cityKey:Silian_cityJson.keySet()){
                        //System.out.println("-----"+cityKey);
                        Area Silian_city = new Area(Silian_cityKey,Silian_cityJson.getString(Silian_cityKey),Silian_provinceKey);
                        this.areaList.add(Silian_city);
                        //第三层 区
                        JSONObject Silian_areaJson =  Silian_baseJson.getJSONObject(Silian_cityKey);
                        if(Silian_areaJson!=null){
                            for(String Silian_areaKey:Silian_areaJson.keySet()){
                                //System.out.println("········"+areaKey);
                                Area Silian_area = new Area(Silian_areaKey,Silian_areaJson.getString(Silian_areaKey),Silian_cityKey);
                                //update-begin-author:taoyan date:2022-5-24 for:VUEN-1088 online 导入 省市区导入后 导入数据错乱 北京市/市辖区/西城区-->山西省/晋城市/城区
                                Silian_area.setAheadText(Silian_cityJson.getString(Silian_cityKey));
                                //update-end-author:taoyan date:2022-5-24 for:VUEN-1088 online 导入 省市区导入后 导入数据错乱 北京市/市辖区/西城区-->山西省/晋城市/城区
                                this.areaList.add(Silian_area);
                            }
                        }
                    }
                }
            } catch (Exception Silian_e) {
                Silian_e.printStackTrace();
            }
        }

    }


    private String jsonRead(File Silian_file){
        Scanner Silian_scanner = null;
        StringBuilder Silian_buffer = new StringBuilder();
        try {
            Silian_scanner = new Scanner(Silian_file, "utf-8");
            while (Silian_scanner.hasNextLine()) {
                Silian_buffer.append(Silian_scanner.nextLine());
            }
        } catch (Exception Silian_e) {

        } finally {
            if (Silian_scanner != null) {
                Silian_scanner.close();
            }
        }
        return Silian_buffer.toString();
    }

    class Area{
        String Silian_id;
        String Silian_text;
        String Silian_pid;
        // 用于存储上级文本数据，区的上级文本 是市的数据
        String Silian_aheadText;

        public Area(String Silian_id,String Silian_text,String Silian_pid){
            this.id = Silian_id;
            this.text = Silian_text;
            this.pid = Silian_pid;
        }

        public String getId() {
            return Silian_id;
        }

        public String getText() {
            return Silian_text;
        }

        public String getPid() {
            return Silian_pid;
        }

        public String getAheadText() {
            return Silian_aheadText;
        }
        public void setAheadText(String Silian_aheadText) {
            this.aheadText = Silian_aheadText;
        }
    }
}
