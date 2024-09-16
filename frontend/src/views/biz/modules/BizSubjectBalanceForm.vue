<template>
  <j-form-container :disabled="disabled">
      <a-form-model ref="form" :model="model" :rules="validatorRules" slot="detail">
        <a-row>
          <a-col :span="24">
            <a-form-model-item label="现金账户" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="cashAcct">
              <a-input-number v-model="model.cashAcct" style="width: 100%" disabled/>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="债权账户" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="debtClaimAcct">
              <a-input-number v-model="model.debtClaimAcct" style="width: 100%" disabled/>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="债务账户" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="debtLiabilityAcct">
              <a-input-number v-model="model.debtLiabilityAcct" style="width: 100%" disabled/>
            </a-form-model-item>
          </a-col>
        </a-row>
      </a-form-model>
  </j-form-container>
</template>
<script>
  import { getAction } from '@/api/manage'
  import { validateDuplicateValue } from '@/utils/util'
  import JFormContainer from '@/components/jeecg/JFormContainer'
 import { VALIDATE_FAILED } from '@/components/jeecg/JVxeTable/utils/vxeUtils.js'
  export default {
    name: 'BizSubjectBalanceForm',
    components: {
      JFormContainer
    },
    props:{
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
        validatorRules: {
           userId: [
              { required: true, message: '请输入所属主体!'},
              { validator: (rule, value, callback) => validateDuplicateValue('biz_subject_balance', 'user_id', value, this.model.id, callback)},
           ],
           cashAcct: [
              { required: false},
              { pattern: /^(([0-9][0-9]*)|([0]\.\d{0,2}|[1-9][0-9]*\.\d{0,2}))$/, message: '请输入正确的金额!'},
           ],
           debtClaimAcct: [
              { required: false},
              { pattern: /^(([0-9][0-9]*)|([0]\.\d{0,2}|[1-9][0-9]*\.\d{0,2}))$/, message: '请输入正确的金额!'},
           ],
           debtLiabilityAcct: [
              { required: false},
              { pattern: /^(([0-9][0-9]*)|([0]\.\d{0,2}|[1-9][0-9]*\.\d{0,2}))$/, message: '请输入正确的金额!'},
           ],
        },
        confirmLoading: false,
      }
    },
    created () {
    //备份model原始值
      this.modelDefault = JSON.parse(JSON.stringify(this.model));
    },
    methods:{
      initFormData(url,id){
        this.clearFormData()
        if(!id){
          this.edit(this.modelDefault)
        }else{
          getAction(url,{id:id}).then(res=>{
            if(res.success){
              let records = res.result
              if(records && records.length>0){
                this.edit(records[0])
              }
            }
          })
        }
      },
      edit(record){
        this.model = Object.assign({}, record)
      },
      getFormData(){
        let formdata_arr = []
        this.$refs.form.validate(valid => {
          if (valid) {
            let isNullObj = true
            Object.keys(this.model).forEach(key=>{
              if(this.model[key]){
                isNullObj = false
              }
            })
            if(!isNullObj){
              formdata_arr.push(this.model)
            }
          }else{
            this.$emit("validateError","账户余额表单校验未通过");
          }
        })
        return formdata_arr;
      },
       validate(index){
        return new Promise((resolve, reject) => {
          // 验证主表表单
         this.$refs.form.validate(valid => {
            !valid ? reject({ error: VALIDATE_FAILED ,index}) : resolve()
          })
        }).then(values => {
          return Promise.resolve()
        }).catch(error => {
          return Promise.reject(error)
        })

      },
      clearFormData(){
        this.$refs.form.clearValidate()
      },
    }
  }
</script>
