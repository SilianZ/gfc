<template>
  <a-drawer
    :title="title"
    :maskClosable="true"
    :width="drawerWidth"
    placement="right"
    :closable="true"
    @close="handleCancel"
    :visible="visible"
    style="height: 100%;">

    <template slot="title">
      <div style="width: 100%;">
        <span>{{ title }}</span>
        <span style="display:inline-block;width:calc(100% - 51px);padding-right:10px;text-align: right">
          <a-button @click="toggleScreen" icon="appstore" style="height:20px;width:20px;border:0px"></a-button>
        </span>
      </div>

    </template>

    <a-spin :spinning="confirmLoading">
      <a-form-model ref="form" :model="model" :rules="validatorRules">

        <a-form-model-item label="团队账号" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="username">
          <a-input placeholder="请输入团队账号" v-model="model.username" :readOnly="!!model.id"/>
        </a-form-model-item>

        <template v-if="!model.id">
          <a-form-model-item label="登录密码" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="password">
            <a-input type="password" placeholder="请输入登录密码" v-model="model.password"/>
          </a-form-model-item>

          <a-form-model-item label="确认密码" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="confirmpassword">
            <a-input type="password" @blur="handleConfirmBlur" placeholder="请重新输入登录密码" v-model="model.confirmpassword"/>
          </a-form-model-item>
        </template>

        <a-form-model-item label="团队名称" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="realname">
          <a-input placeholder="请输入团队名称" v-model="model.realname"/>
        </a-form-model-item>

        <a-form-model-item label="初始资金" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="workno">
          <a-input placeholder="请输入初始资金" v-model="model.workno"/>
        </a-form-model-item>
        <!--国家分配-->
        <a-form-model-item label="国家分配" :labelCol="labelCol" :wrapperCol="wrapperCol" v-show="!departDisabled">
          <j-select-depart v-model="model.selecteddeparts" :multi="false" @back="backDepartInfo" :backDepart="true"
                           :treeOpera="true">>
          </j-select-depart>
        </a-form-model-item>

        <a-form-model-item label="手机号码" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="phone">
          <a-input placeholder="请输入手机号码" v-model="model.phone"/>
        </a-form-model-item>

        <a-form-model-item label="账号角色" :labelCol="labelCol" :wrapperCol="wrapperCol">
          <j-select-position placeholder="请选择账号角色" :multiple="false" v-model="model.post"/>
        </a-form-model-item>

        <a-form-model-item label="权限分配" :labelCol="labelCol" :wrapperCol="wrapperCol" v-show="!roleDisabled">
          <j-multi-select-tag
            :disabled="disableSubmit"
            v-model="model.selectedroles"
            :options="rolesOptions"
            placeholder="请选择权限分配">
          </j-multi-select-tag>
        </a-form-model-item>
      </a-form-model>
    </a-spin>


    <div class="drawer-bootom-button" v-show="!disableSubmit">
      <a-popconfirm title="确定放弃编辑？" @confirm="handleCancel" okText="确定" cancelText="取消">
        <a-button style="margin-right: .8rem">取消</a-button>
      </a-popconfirm>
      <a-button @click="handleSubmit" type="primary" :loading="confirmLoading">提交</a-button>
    </div>
  </a-drawer>
</template>

<script>
import moment from 'moment'
import Vue from 'vue'
import {ACCESS_TOKEN} from "@/store/mutation-types"
import {getAction} from '@/api/manage'
import {addUser, editUser, queryUserRole, queryall} from '@/api/api'
import {disabledAuthFilter} from "@/utils/authFilter"
import {duplicateCheck} from '@/api/api'

export default {
  name: "UserModal",
  components: {},
  data() {
    return {
      departDisabled: false, //是否是我的部门调用该页面
      roleDisabled: false, //是否是角色维护调用该页面
      modalWidth: 800,
      drawerWidth: 700,
      modaltoggleFlag: true,
      confirmDirty: false,
      userId: "", //保存用户id
      disableSubmit: false,
      dateFormat: "YYYY-MM-DD",
      validatorRules: {
        username: [{required: true, message: '请输入团队账号!'},
          {validator: this.validateUsername,}],
        password: [{
          required: true,
          pattern: /^(?=.*[a-zA-Z])(?=.*\d)(?=.*[~!@#$%^&*()_+`\-={}:";'<>?,./]).{8,}$/,
          message: '密码由8位数字、大小写字母和特殊符号组成!'
        },
          {validator: this.validateToNextPassword, trigger: 'change'}],
        confirmpassword: [{required: true, message: '请重新输入登录密码!',},
          {validator: this.compareToFirstPassword,}],
        realname: [{required: true, message: '请输入用户名称!'}],
        workno: [{required: true, message: '请输入初始资金!'}],
        phone: [{required: false, message: '请输入手机号!'}, {validator: this.validatePhone}],
        email: [{validator: this.validateEmail}],
        roles: {},
        telephone: [{pattern: /^0\d{2,3}-[1-9]\d{6,7}$/, message: '请输入正确的座机号码'},]
      },
      departIdShow: false,
      title: "操作",
      visible: false,
      model: {},
      labelCol: {
        xs: {span: 24},
        sm: {span: 5},
      },
      wrapperCol: {
        xs: {span: 24},
        sm: {span: 16},
      },
      uploadLoading: false,
      confirmLoading: false,
      headers: {},
      url: {
        fileUpload: window._CONFIG['domianURL'] + "/sys/common/upload",
        userWithDepart: "/sys/user/userDepartList", // 引入为指定用户查看部门信息需要的url
        userId: "/sys/user/generateUserId", // 引入生成添加团队情况下的url
        syncUserByUserName: "/act/process/extActProcess/doSyncUserByUserName",//同步用户到工作流
        queryTenantList: '/sys/tenant/queryList'
      },
      tenantsOptions: [],
      rolesOptions: [],
      nextDepartOptions: [],
    }
  },
  created() {
    const token = Vue.ls.get(ACCESS_TOKEN);
    this.headers = {"X-Access-Token": token}
    this.initRoleList()
    this.initTenantList()
  },
  computed: {
    uploadAction: function () {
      return this.url.fileUpload;
    }
  },
  methods: {
    add() {
      this.refresh();
      this.edit({
        activitiSync: '1',
        userIdentity: 1,
        password: '1qa@WS3ed',
        confirmpassword: '1qa@WS3ed',
        post: 'TEM',
        workno: 100
      });
    },
    edit(record) {
      let that = this;
      that.visible = true;
      //根据屏幕宽度自适应抽屉宽度
      this.resetScreenSize();
      that.userId = record.id;
      that.model = Object.assign({}, {selectedroles: '', selecteddeparts: ''}, record);
      //身份为上级显示负责部门，否则不显示
      if (this.model.userIdentity == 2) {
        this.departIdShow = true;
      } else {
        this.departIdShow = false;
      }

      if (record.hasOwnProperty("id")) {
        that.getUserRoles(record.id);
        that.getUserDeparts(record.id);
      }
      console.log('that.model=', that.model)
    },
    isDisabledAuth(code) {
      return disabledAuthFilter(code);
    },
    //窗口最大化切换
    toggleScreen() {
      if (this.modaltoggleFlag) {
        this.modalWidth = window.innerWidth;
      } else {
        this.modalWidth = 800;
      }
      this.modaltoggleFlag = !this.modaltoggleFlag;
    },
    // 根据屏幕变化,设置抽屉尺寸
    resetScreenSize() {
      let screenWidth = document.body.clientWidth;
      if (screenWidth < 500) {
        this.drawerWidth = screenWidth;
      } else {
        this.drawerWidth = 700;
      }
    },
    //初始化租户字典
    initTenantList() {
      getAction(this.url.queryTenantList).then(res => {
        if (res.success) {
          this.tenantsOptions = res.result.map((item, index, arr) => {
            let c = {label: item.name, value: item.id + ""}
            return c;
          })
          console.log('this.tenantsOptions: ', this.tenantsOptions)
        }
      })
    },
    //初始化角色字典
    initRoleList() {
      queryall().then((res) => {
        if (res.success) {
          this.rolesOptions = res.result.map((item, index, arr) => {
            let c = {label: item.roleName, value: item.id}
            return c;
          })
          console.log('this.rolesOptions: ', this.rolesOptions)
        }
      });
    },
    getUserRoles(userid) {
      queryUserRole({userid: userid}).then((res) => {
        if (res.success) {
          this.model.selectedroles = res.result.join(",");
          console.log('that.model.selectedroles=', this.model.selectedroles)
        }
      });
    },
    getUserDeparts(userid) {
      let that = this;
      getAction(that.url.userWithDepart, {userId: userid}).then((res) => {
        if (res.success) {
          let departOptions = [];
          let selectDepartKeys = []
          for (let i = 0; i < res.result.length; i++) {
            selectDepartKeys.push(res.result[i].key);
            //新增负责部门选择下拉框
            departOptions.push({
              value: res.result[i].key,
              label: res.result[i].title
            })
          }
          that.model.selecteddeparts = selectDepartKeys.join(",")
          that.nextDepartOptions = departOptions;
          console.log('that.nextDepartOptions=', that.nextDepartOptions)
        }
      })
    },
    backDepartInfo(info) {
      this.model.departIds = this.model.selecteddeparts;
      this.nextDepartOptions = info.map((item, index, arr) => {
        let c = {label: item.text, value: item.value + ""}
        return c;
      })
    },
    refresh() {
      this.userId = ""
      this.nextDepartOptions = [];
      this.departIdShow = false;
    },
    close() {
      this.$emit('close');
      this.visible = false;
      this.disableSubmit = false;
      this.nextDepartOptions = [];
      this.departIdShow = false;
      this.$refs.form.resetFields();
    },
    moment,
    handleSubmit() {
      const that = this;
      if (this.model.selecteddeparts == null || this.model.selecteddeparts == '' || this.model.selecteddeparts.length == 0) {
        this.$message.warning('请选择所属国家!');
        return;
      }
      if (this.model.post == null || this.model.post == '') {
        this.$message.warning('请选择团队角色!');
        return;
      }
      // 触发表单验证
      this.$refs.form.validate(valid => {
        if (valid) {
          that.confirmLoading = true;
          //如果是上级择传入departIds,否则为空
          if (this.model.userIdentity !== 2) {
            this.model.departIds = "";
          }
          let obj;
          if (!this.model.id) {
            this.model.id = this.userId;
            obj = addUser(this.model);
          } else {
            obj = editUser(this.model);
          }
          obj.then((res) => {
            if (res.success) {
              that.$message.success(res.message);
              that.$emit('ok');
            } else {
              that.$message.warning(res.message);
            }
          }).finally(() => {
            that.confirmLoading = false;
            that.close();
          })
        } else {
          return false;
        }
      })
    },
    handleCancel() {
      this.close()
    },
    validateToNextPassword(rule, value, callback) {
      const confirmpassword = this.model.confirmpassword;
      if (value && confirmpassword && value !== confirmpassword) {
        callback('两次输入的密码不一样！');
      }
      if (value && this.confirmDirty) {
        this.$refs.form.validateField(['confirmpassword']);
      }
      callback();
    },
    compareToFirstPassword(rule, value, callback) {
      if (value && value !== this.model.password) {
        callback('两次输入的密码不一样！');
      } else {
        callback()
      }
    },
    validatePhone(rule, value, callback) {
      if (!value) {
        callback()
      } else {
        if (new RegExp(/^1[3|4|5|6|7|8|9][0-9]\d{8}$/).test(value)) {
          var params = {
            tableName: 'sys_user',
            fieldName: 'phone',
            fieldVal: value,
            dataId: this.userId
          };
          duplicateCheck(params).then((res) => {
            if (res.success) {
              callback()
            } else {
              callback("手机号已存在!")
            }
          })
        } else {
          callback("请输入正确格式的手机号码!");
        }
      }
    },
    validateEmail(rule, value, callback) {
      if (!value) {
        callback()
      } else {
        if (new RegExp(/^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/).test(value)) {
          var params = {
            tableName: 'sys_user',
            fieldName: 'email',
            fieldVal: value,
            dataId: this.userId
          };
          duplicateCheck(params).then((res) => {
            console.log(res)
            if (res.success) {
              callback()
            } else {
              callback("邮箱已存在!")
            }
          })
        } else {
          callback("请输入正确格式的邮箱!")
        }
      }
    },
    validateUsername(rule, value, callback) {
      var params = {
        tableName: 'sys_user',
        fieldName: 'username',
        fieldVal: value,
        dataId: this.userId
      };
      duplicateCheck(params).then((res) => {
        if (res.success) {
          callback()
        } else {
          callback("用户名已存在!")
        }
      })
    },
    handleConfirmBlur(e) {
      const value = e.target.value;
      this.confirmDirty = this.confirmDirty || !!value
    },
    beforeUpload: function (file) {
      var fileType = file.type;
      if (fileType.indexOf('image') < 0) {
        this.$message.warning('请上传图片');
        return false;
      }
      //TODO 验证文件大小
    },
    identityChange(e) {
      if (e.target.value === 1) {
        this.departIdShow = false;
      } else {
        this.departIdShow = true;
      }
    }
  }
}
</script>

<style scoped>
.avatar-uploader > .ant-upload {
  width: 104px;
  height: 104px;
}

.ant-upload-select-picture-card i {
  font-size: 49px;
  color: #999;
}

.ant-upload-select-picture-card .ant-upload-text {
  margin-top: 8px;
  color: #666;
}

.ant-table-tbody .ant-table-row td {
  padding-top: 10px;
  padding-bottom: 10px;
}

.drawer-bootom-button {
  position: absolute;
  bottom: 0;
  width: 100%;
  border-top: 1px solid #e8e8e8;
  padding: 10px 16px;
  text-align: right;
  left: 0;
  background: #fff;
  border-radius: 0 0 2px 2px;
}

/*【JTC-502】 添加团队两个滚动条*/
/deep/ .ant-drawer-body {
  padding-bottom: 53px;
}

</style>
