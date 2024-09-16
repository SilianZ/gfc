<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container :disabled="formDisabled">
      <a-form-model ref="form" :model="model" :rules="validatorRules" slot="detail">
        <a-row>
          <a-col :span="24">
            <a-form-model-item label="所属团队" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="userId">
              <j-search-select-tag placeholder="请选择所属团队" v-model="model.userId" dict="sys_user,realname,username,post='TEM'"
                                   @change="getDictInfo"/>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="材料工厂" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="factoryId">
              <j-dict-select-tag type="list" v-model="model.factoryId" :dictCode="factoryData" placeholder="请选择材料工厂" />
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="开采计划" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="planCode">
              <j-dict-select-tag type="list" v-model="model.planCode" dictCode="biz_mine_plan,plan_name,plan_code" placeholder="请选择开采计划" />
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="备注" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="remakr">
              <a-textarea v-model="model.remakr" rows="4" placeholder="请输入备注" />
            </a-form-model-item>
          </a-col>
        </a-row>
      </a-form-model>
    </j-form-container>
  </a-spin>
</template>

<script>

  import { httpAction, getAction } from '@/api/manage'
  import { validateDuplicateValue } from '@/utils/util'

  export default {
    name: 'BizMineOpenForm',
    components: {
    },
    props: {
      //表单禁用
      disabled: {
        type: Boolean,
        default: false,
        required: false
      }
    },
    data () {
      return {
        model:{
         },
        factoryData: 'biz_team_resource,resource_name,id,1=2',
        labelCol: {
          xs: { span: 24 },
          sm: { span: 5 },
        },
        wrapperCol: {
          xs: { span: 24 },
          sm: { span: 16 },
        },
        confirmLoading: false,
        validatorRules: {
           userId: [
              { required: true, message: '请输入所属团队!'},
           ],
           factoryId: [
              { required: true, message: '请输入材料工厂!'},
           ],
           planCode: [
              { required: true, message: '请输入开采计划!'},
           ],
        },
        url: {
          add: "/biz/bizMineOpen/add",
          edit: "/biz/bizMineOpen/edit",
          queryById: "/biz/bizMineOpen/queryById"
        }
      }
    },
    computed: {
      formDisabled(){
        return this.disabled
      },
    },
    created () {
       //备份model原始值
      this.modelDefault = JSON.parse(JSON.stringify(this.model));
      this.getDictInfo();
    },
    methods: {
      add () {
        this.edit(this.modelDefault);
      },
      edit (record) {
        this.model = Object.assign({}, record);
        this.visible = true;
      },
      submitForm () {
        const that = this;
        // 触发表单验证
        this.$refs.form.validate(valid => {
          if (valid) {
            that.confirmLoading = true;
            let httpurl = '';
            let method = '';
            if(!this.model.id){
              httpurl+=this.url.add;
              method = 'post';
            }else{
              httpurl+=this.url.edit;
               method = 'put';
            }
            httpAction(httpurl,this.model,method).then((res)=>{
              if(res.success){
                that.$message.success(res.message);
                that.$emit('ok');
              }else{
                that.$message.warning(res.message);
              }
            }).finally(() => {
              that.confirmLoading = false;
            })
          }
         
        })
      },
      getDictInfo() {
        let str = ''
        if (this.model.userId) {
          str = 'v_team_resource,resource_name,id,data_type=\'SYON\' and resource_type=\'TD\' and user_id = \'' + this.model.userId + '\'';
        } else {
          str = 'v_team_resource,resource_name,id,1=2';
        }
        this.factoryData = str
      }
    }
  }
</script>