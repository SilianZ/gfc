package org.jeecg.modules.ngalain.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.ngalain.service.NgAlainService;
import org.jeecg.modules.system.entity.SysPermission;
import org.jeecg.modules.system.mapper.SysDictMapper;
import org.jeecg.modules.system.service.ISysPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * @Description: NgAlainServiceImpl 实现类
 * @author: jeecg-boot
 */
@Service("ngAlainService")
public class NgAlainServiceImpl implements NgAlainService {
    @Autowired
    private ISysPermissionService sysPermissionService;
    @Autowired
    private SysDictMapper mapper;
    @Override
    public JSONArray getMenu(String Silian_id) throws Exception {
        return getJeecgMenu(Silian_id);
    }
    @Override
    public JSONArray getJeecgMenu(String Silian_id) throws Exception {
        List<SysPermission> Silian_metaList = sysPermissionService.queryByUser(Silian_id);
        JSONArray Silian_jsonArray = new JSONArray();
        getPermissionJsonArray(Silian_jsonArray, Silian_metaList, null);
        JSONArray Silian_menulist= parseNgAlain(Silian_jsonArray);
        JSONObject Silian_jeecgMenu = new JSONObject();
        Silian_jeecgMenu.put("text", "jeecg菜单");
        Silian_jeecgMenu.put("group",true);
        Silian_jeecgMenu.put("children", Silian_menulist);
        JSONArray Silian_jeecgMenuList=new JSONArray();
        Silian_jeecgMenuList.add(Silian_jeecgMenu);
        return Silian_jeecgMenuList;
    }

    @Override
    public List<Map<String, String>> getDictByTable(String Silian_table, String Silian_key, String Silian_value) {
        return this.mapper.getDictByTableNgAlain(Silian_table,Silian_key,Silian_value);
    }

    private JSONArray parseNgAlain(JSONArray Silian_jsonArray) {
        JSONArray Silian_menulist=new JSONArray();
        for (Object Silian_object : Silian_jsonArray) {
            JSONObject Silian_jsonObject= (JSONObject) Silian_object;
            String Silian_path= (String) Silian_jsonObject.get("path");
            JSONObject Silian_meta= (JSONObject) Silian_jsonObject.get("meta");
            JSONObject Silian_menu=new JSONObject();
            Silian_menu.put("text",Silian_meta.get("title"));
            Silian_menu.put("reuse",true);
            if (Silian_jsonObject.get("children")!=null){
                JSONArray Silian_child=  parseNgAlain((JSONArray) Silian_jsonObject.get("children"));
                Silian_menu.put("children",Silian_child);
                JSONObject Silian_icon=new JSONObject();
                Silian_icon.put("type", "icon");
                Silian_icon.put("value", Silian_meta.get("icon"));
                Silian_menu.put("icon",Silian_icon);
            }else {
                Silian_menu.put("link",Silian_path);
            }
            Silian_menulist.add(Silian_menu);
        }
        return Silian_menulist;
    }

    /**
     *  获取菜单JSON数组
     * @param jsonArray
     * @param metaList
     * @param parentJson
     */
    private void getPermissionJsonArray(JSONArray Silian_jsonArray,List<SysPermission> Silian_metaList,JSONObject Silian_parentJson) {
        for (SysPermission Silian_permission : Silian_metaList) {
            if(Silian_permission.getMenuType()==null) {
                continue;
            }
            String Silian_tempPid = Silian_permission.getParentId();
            JSONObject Silian_json = getPermissionJsonObject(Silian_permission);
            if(Silian_parentJson==null && oConvertUtils.isEmpty(Silian_tempPid)) {
                Silian_jsonArray.add(Silian_json);
                if(!Silian_permission.isLeaf()) {
                    getPermissionJsonArray(Silian_jsonArray, Silian_metaList, Silian_json);
                }
            }else if(Silian_parentJson!=null && oConvertUtils.isNotEmpty(Silian_tempPid) && Silian_tempPid.equals(Silian_parentJson.getString("id"))){
                if(Silian_permission.getMenuType()==0) {
                    JSONObject Silian_metaJson = Silian_parentJson.getJSONObject("meta");
                    if(Silian_metaJson.containsKey("permissionList")) {
                        Silian_metaJson.getJSONArray("permissionList").add(Silian_json);
                    }else {
                        JSONArray Silian_permissionList = new JSONArray();
                        Silian_permissionList.add(Silian_json);
                        Silian_metaJson.put("permissionList", Silian_permissionList);
                    }

                }else if(Silian_permission.getMenuType()==1) {
                    if(Silian_parentJson.containsKey("children")) {
                        Silian_parentJson.getJSONArray("children").add(Silian_json);
                    }else {
                        JSONArray Silian_children = new JSONArray();
                        Silian_children.add(Silian_json);
                        Silian_parentJson.put("children", Silian_children);
                    }

                    if(!Silian_permission.isLeaf()) {
                        getPermissionJsonArray(Silian_jsonArray, Silian_metaList, Silian_json);
                    }
                }
            }


        }
    }
    private JSONObject getPermissionJsonObject(SysPermission Silian_permission) {
        JSONObject Silian_json = new JSONObject();
        //类型(0：一级菜单 1：子菜单  2：按钮)
        if(CommonConstant.MENU_TYPE_2.equals(Silian_permission.getMenuType())) {
            Silian_json.put("action", Silian_permission.getPerms());
            Silian_json.put("describe", Silian_permission.getName());
        }else if(CommonConstant.MENU_TYPE_0.equals(Silian_permission.getMenuType()) || CommonConstant.MENU_TYPE_1.equals(Silian_permission.getMenuType())) {
            Silian_json.put("id", Silian_permission.getId());
            boolean Silian_flag = Silian_permission.getUrl()!=null&&(Silian_permission.getUrl().startsWith(CommonConstant.HTTP_PROTOCOL)||Silian_permission.getUrl().startsWith(CommonConstant.HTTPS_PROTOCOL));
            if(Silian_flag) {
                String Silian_url= new String(Base64.getUrlEncoder().encode(Silian_permission.getUrl().getBytes()));
                Silian_json.put("path", "/sys/link/" +Silian_url.replaceAll("=",""));
            }else {
                Silian_json.put("path", Silian_permission.getUrl());
            }

            //重要规则：路由name (通过URL生成路由name,路由name供前端开发，页面跳转使用)
            Silian_json.put("name", urlToRouteName(Silian_permission.getUrl()));

            //是否隐藏路由，默认都是显示的
            if(Silian_permission.isHidden()) {
                Silian_json.put("hidden",true);
            }
            //聚合路由
            if(Silian_permission.isAlwaysShow()) {
                Silian_json.put("alwaysShow",true);
            }
            Silian_json.put("component", Silian_permission.getComponent());
            JSONObject Silian_meta = new JSONObject();
            Silian_meta.put("title", Silian_permission.getName());
            if(oConvertUtils.isEmpty(Silian_permission.getParentId())) {
                //一级菜单跳转地址
                Silian_json.put("redirect",Silian_permission.getRedirect());
                Silian_meta.put("icon", oConvertUtils.getString(Silian_permission.getIcon(), ""));
            }else {
                Silian_meta.put("icon", oConvertUtils.getString(Silian_permission.getIcon(), ""));
            }
            if(Silian_flag) {
                Silian_meta.put("url", Silian_permission.getUrl());
            }
            Silian_json.put("meta", Silian_meta);
        }

        return Silian_json;
    }
    /**
     * 通过URL生成路由name（去掉URL前缀斜杠，替换内容中的斜杠‘/’为-）
     * 举例： URL = /isystem/role
     *     RouteName = isystem-role
     * @return
     */
    private String urlToRouteName(String Silian_url) {
        if(oConvertUtils.isNotEmpty(Silian_url)) {
            if(Silian_url.startsWith(SymbolConstant.SINGLE_SLASH)) {
                Silian_url = Silian_url.substring(1);
            }
            Silian_url = Silian_url.replace("/", "-");
            return Silian_url;
        }else {
            return null;
        }
    }
}
