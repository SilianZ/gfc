<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container :disabled="formDisabled">
      <a-form-model ref="form" :model="model" :rules="validatorRules" slot="detail">
        <a-row>
          <a-card title="基本信息">
            <a-col :span="12">
              <a-form-model-item label="所属团队" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="userId">
                <j-search-select-tag v-model="model.userId" dict="sys_user,realname,username,post='TEM'" disabled/>
              </a-form-model-item>
            </a-col>
            <a-col :span="12">
              <a-form-model-item label="现金账户" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="cashAcct">
                <a-input-number v-model="model.cashAcct" placeholder="请输入现金账户" style="width: 100%"/>
              </a-form-model-item>
            </a-col>
            <a-col :span="12">
              <a-form-model-item label="债权账户" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="debtClaimAcct">
                <a-input-number v-model="model.debtClaimAcct" placeholder="请输入债权账户" style="width: 100%"/>
              </a-form-model-item>
            </a-col>
            <a-col :span="12">
              <a-form-model-item label="债务账户" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="debtLiabilityAcct">
                <a-input-number v-model="model.debtLiabilityAcct" placeholder="请输入债务账户" style="width: 100%"/>
              </a-form-model-item>
            </a-col>
          </a-card>
        </a-row>
        <a-row style="margin-top: 10px">
          <a-card title="材料">
            <a-col :span="12">
              <a-form-model-item label="钢铁库存" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="steelAcct">
                <a-input-number v-model="model.steelAcct" placeholder="请输入钢铁库存" style="width: 100%"/>
              </a-form-model-item>
            </a-col>
            <a-col :span="12">
              <a-form-model-item label="硅石库存" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="silicaAcct">
                <a-input-number v-model="model.silicaAcct" placeholder="请输入硅石库存" style="width: 100%"/>
              </a-form-model-item>
            </a-col>
            <a-col :span="12">
              <a-form-model-item label="石油库存" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="crudeAcct">
                <a-input-number v-model="model.crudeAcct" placeholder="请输入石油库存" style="width: 100%"/>
              </a-form-model-item>
            </a-col>
            <a-col :span="12">
              <a-form-model-item label="塑料库存" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="plasticsAcct">
                <a-input-number v-model="model.plasticsAcct" placeholder="请输入塑料库存" style="width: 100%"/>
              </a-form-model-item>
            </a-col>
          </a-card>
        </a-row>
        <a-row style="margin-top: 10px">
          <a-card title="成品">
            <a-col :span="12">
              <a-form-model-item label="芯片库存" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="chipAcct">
                <a-input-number v-model="model.chipAcct" placeholder="请输入芯片库存" style="width: 100%"/>
              </a-form-model-item>
            </a-col>
            <a-col :span="12">
              <a-form-model-item label="汽车库存" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="cardAcct">
                <a-input-number v-model="model.cardAcct" placeholder="请输入汽车库存" style="width: 100%"/>
              </a-form-model-item>
            </a-col>
            <a-col :span="12">
              <a-form-model-item label="新能源库存" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="energyAcct">
                <a-input-number v-model="model.energyAcct" placeholder="请输入新能源库存" style="width: 100%"/>
              </a-form-model-item>
            </a-col>
            <a-col :span="12">
              <a-form-model-item label="玩具库存" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="toyAcct">
                <a-input-number v-model="model.toyAcct" placeholder="请输入玩具库存" style="width: 100%"/>
              </a-form-model-item>
            </a-col>
          </a-card>
        </a-row>
      </a-form-model>
    </j-form-container>
  </a-spin>
</template>

<script>

import {httpAction, getAction} from '@/api/manage'
import {validateDuplicateValue} from '@/utils/util'

export default {
  name: 'BizTeamBalanceForm',
  components: {},
  props: {
    //表单禁用
    disabled: {
      type: Boolean,
      default: false,
      required: false
    }
  },
  data() {
    return {
      model: {},
      labelCol: {
        xs: {span: 24},
        sm: {span: 5},
      },
      wrapperCol: {
        xs: {span: 24},
        sm: {span: 16},
      },
      confirmLoading: false,
      validatorRules: {
        userId: [
          {required: true, message: '请输入所属主体!'},
          {validator: (rule, value, callback) => validateDuplicateValue('biz_subject_balance', 'user_id', value, this.model.id, callback)},
        ],
        cashAcct: [
          {required: false},
          {pattern: /^(([0-9][0-9]*)|([0]\.\d{0,2}|[1-9][0-9]*\.\d{0,2}))$/, message: '请输入正确的金额!'},
        ],
        debtClaimAcct: [
          {required: false},
          {pattern: /^(([0-9][0-9]*)|([0]\.\d{0,2}|[1-9][0-9]*\.\d{0,2}))$/, message: '请输入正确的金额!'},
        ],
        debtLiabilityAcct: [
          {required: false},
          {pattern: /^(([0-9][0-9]*)|([0]\.\d{0,2}|[1-9][0-9]*\.\d{0,2}))$/, message: '请输入正确的金额!'},
        ],
      },
      url: {
        add: "/biz/bizSubjectBalance/add",
        edit: "/biz/bizSubjectBalance/edit",
        queryById: "/biz/bizSubjectBalance/queryById"
      }
    }
  },
  computed: {
    formDisabled() {
      return this.disabled
    },
  },
  created() {
    //备份model原始值
    this.modelDefault = JSON.parse(JSON.stringify(this.model));
  },
  methods: {
    add() {
      this.edit(this.modelDefault);
    },
    edit(record) {
      this.model = Object.assign({}, record);
      this.visible = true;
    },
    submitForm() {
      const that = this;
      // 触发表单验证
      this.$refs.form.validate(valid => {
        if (valid) {
          that.confirmLoading = true;
          let httpurl = '';
          let method = '';
          if (!this.model.id) {
            httpurl += this.url.add;
            method = 'post';
          } else {
            httpurl += this.url.edit;
            method = 'put';
          }
          httpAction(httpurl, this.model, method).then((res) => {
            if (res.success) {
              that.$message.success(res.message);
              that.$emit('ok');
            } else {
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