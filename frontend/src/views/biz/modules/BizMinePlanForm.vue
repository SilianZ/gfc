<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container :disabled="formDisabled">
      <a-form-model ref="form" :model="model" :rules="validatorRules" slot="detail">
        <a-row>
          <a-col :span="12">
            <a-form-model-item label="计划编号" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="planCode">
              <a-input v-model="model.planCode" placeholder="请输入计划编号"  ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="计划名称" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="planName">
              <a-input v-model="model.planName" placeholder="请输入计划名称"  ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="财年限制" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="yearLimit">
              <a-input-number v-model="model.yearLimit" placeholder="请输入财年限制" style="width: 100%" />
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="钢铁产量" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="gtProduction">
              <a-input-number v-model="model.gtProduction" placeholder="请输入钢铁产量" style="width: 100%" />
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="硅石产量" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="gsProduction">
              <a-input-number v-model="model.gsProduction" placeholder="请输入硅石产量" style="width: 100%" />
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="石油产量" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="syProduction">
              <a-input-number v-model="model.syProduction" placeholder="请输入石油产量" style="width: 100%" />
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="塑料产量" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="slProduction">
              <a-input-number v-model="model.slProduction" placeholder="请输入塑料产量" style="width: 100%" />
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="备注" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="remark">
              <a-textarea v-model="model.remark" rows="4" placeholder="请输入备注" />
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
    name: 'BizMinePlanForm',
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
           planCode: [
              { required: true, message: '请输入计划编号!'},
              { validator: (rule, value, callback) => validateDuplicateValue('biz_mine_plan', 'plan_code', value, this.model.id, callback)},
           ],
           planName: [
              { required: true, message: '请输入计划名称!'},
              { validator: (rule, value, callback) => validateDuplicateValue('biz_mine_plan', 'plan_name', value, this.model.id, callback)},
           ],
          yearLimit: [
            { required: true, message: '请输入开采计划!'},
          ],
           gtProduction: [
              { required: true, message: '请输入钢铁产量!'},
              { pattern: /^-?\d+\.?\d*$/, message: '请输入数字!'},
           ],
           gsProduction: [
              { required: true, message: '请输入硅石产量!'},
              { pattern: /^-?\d+\.?\d*$/, message: '请输入数字!'},
           ],
           syProduction: [
              { required: true, message: '请输入石油产量!'},
              { pattern: /^-?\d+\.?\d*$/, message: '请输入数字!'},
           ],
           slProduction: [
              { required: true, message: '请输入塑料产量!'},
              { pattern: /^-?\d+\.?\d*$/, message: '请输入数字!'},
           ],
        },
        url: {
          add: "/biz/bizMinePlan/add",
          edit: "/biz/bizMinePlan/edit",
          queryById: "/biz/bizMinePlan/queryById"
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
    }
  }
</script>