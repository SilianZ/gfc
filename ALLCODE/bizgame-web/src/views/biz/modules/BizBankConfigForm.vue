<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container :disabled="formDisabled">
      <!-- 主表单区域 -->
      <a-form-model ref="form" :model="model" :rules="validatorRules" slot="detail">
        <a-row>
          <a-col :span="12" >
            <a-form-model-item label="国家银行" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="userId">
              <j-search-select-tag v-model="model.userId" dict="sys_user,realname,username" />
            </a-form-model-item>
          </a-col>
          <a-col :span="12" >
            <a-form-model-item label="所属国家" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="deptId">
              <j-select-depart v-model="model.deptId" :multi="true"  />
            </a-form-model-item>
          </a-col>
          <a-col :span="12" >
            <a-form-model-item label="储蓄率" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="saveRate">
              <a-input-number v-model="model.saveRate" placeholder="请输入储蓄率" style="width: 100%" />
            </a-form-model-item>
          </a-col>
          <a-col :span="12" >
            <a-form-model-item label="关税" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="tariffRate">
              <a-input-number v-model="model.tariffRate" placeholder="请输入关税" style="width: 100%" />
            </a-form-model-item>
          </a-col>
          <a-col :span="12" >
            <a-form-model-item label="是否投资" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="isInvest">
              <j-dict-select-tag type="list" v-model="model.isInvest" dictCode="yes_or_no" placeholder="请选择是否投资" />
            </a-form-model-item>
          </a-col>
          <a-col :span="12" >
            <a-form-model-item label="投资计划" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="investPlan">
              <j-dict-select-tag type="list" v-model="model.investPlan" dictCode="invest_plan" placeholder="请选择投资计划" />
            </a-form-model-item>
          </a-col>
        </a-row>
      </a-form-model>
    </j-form-container>
      <!-- 子表单区域 -->
    <a-tabs v-model="activeKey" @change="handleChangeTabs">
      <a-tab-pane tab="账户余额" :key="refKeys[0]" :forceRender="true">
        <biz-subject-balance-form ref="bizSubjectBalanceForm" @validateError="validateError" :disabled="formDisabled"></biz-subject-balance-form>
      </a-tab-pane>
      
    </a-tabs>
  </a-spin>
</template>

<script>

  import { getAction } from '@/api/manage'
  import { JVxeTableModelMixin } from '@/mixins/JVxeTableModelMixin.js'
  import { JVXETypes } from '@/components/jeecg/JVxeTable'
  import { getRefPromise,VALIDATE_FAILED} from '@/components/jeecg/JVxeTable/utils/vxeUtils.js'
  import { validateDuplicateValue } from '@/utils/util'
  import JFormContainer from '@/components/jeecg/JFormContainer'
  import BizSubjectBalanceForm from './BizSubjectBalanceForm.vue'

  export default {
    name: 'BizBankConfigForm',
    mixins: [JVxeTableModelMixin],
    components: {
      JFormContainer,
      BizSubjectBalanceForm,
    },
    data() {
      return {
        labelCol: {
          xs: { span: 24 },
          sm: { span: 5 },
        },
        wrapperCol: {
          xs: { span: 24 },
          sm: { span: 16 },
        },
        model:{
         },
        // 新增时子表默认添加几行空数据
        addDefaultRowNum: 1,
        validatorRules: {
           userId: [
              { required: true, message: '请输入国家银行!'},
           ],
           deptId: [
              { required: true, message: '请输入所属国家!'},
           ],
           saveRate: [
              { required: true, message: '请输入储蓄率!'},
           ],
           tariffRate: [
              { required: true, message: '请输入关税!'},
           ],
        },
        refKeys: ['bizSubjectBalance', ],
        tableKeys:[],
        activeKey: 'bizSubjectBalance',
        // 账户余额
        bizSubjectBalanceTable: {
          loading: false,
          dataSource: [],
          columns: [
          ]
        },
        url: {
          add: "/biz/bizBankConfig/add",
          edit: "/biz/bizBankConfig/edit",
          queryById: "/biz/bizBankConfig/queryById",
          bizSubjectBalance: {
            list: '/biz/bizBankConfig/queryBizSubjectBalanceByMainId'
          },
        }
      }
    },
    props: {
      //表单禁用
      disabled: {
        type: Boolean,
        default: false,
        required: false
      }
    },
    computed: {
      formDisabled(){
        return this.disabled
      },
    },
    created () {
    },
    methods: {
      addBefore(){
        this.$refs.bizSubjectBalanceForm.clearFormData()
      },
      getAllTable() {
        return new Promise(resolve => {
          resolve([]);
        })
      },
      /** 调用完edit()方法之后会自动调用此方法 */
      editAfter() {
        this.$nextTick(() => {
          this.$refs.bizSubjectBalanceForm.initFormData(this.url.bizSubjectBalance.list,this.model.userId)
        })
        // 加载子表数据
        if (this.model.id) {
          let params = { id: this.model.id }
        }
      },
      //校验所有一对一子表表单
        validateSubForm(allValues){
            return new Promise((resolve,reject)=>{
              Promise.all([
                  this.$refs.bizSubjectBalanceForm.validate(0),
              ]).then(() => {
                resolve(allValues)
              }).catch(e => {
                if (e.error === VALIDATE_FAILED) {
                  // 如果有未通过表单验证的子表，就自动跳转到它所在的tab
                  this.activeKey = e.index == null ? this.activeKey : this.refKeys[e.index]
                } else {
                  console.error(e)
                }
              })
            })
        },
      /** 整理成formData */
      classifyIntoFormData(allValues) {
        let main = Object.assign(this.model, allValues.formValue)
        return {
          ...main, // 展开
          bizSubjectBalanceList: this.$refs.bizSubjectBalanceForm.getFormData(),
        }
      },
      validateError(msg){
        this.$message.error(msg)
      },

    }
  }
</script>

<style scoped>
</style>