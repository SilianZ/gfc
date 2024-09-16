<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container :disabled="formDisabled">
      <a-form-model ref="form" :model="model" :rules="validatorRules" slot="detail">
        <a-row>
          <a-card title="转账信息">
            <a-col :span="8">
              <a-form-model-item label="转账类型" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="payType">
                <j-dict-select-tag type="list" v-model="model.payType" dictCode="transfer_type" placeholder="请选择转账类型"
                                   @change="setTaxRate"/>
              </a-form-model-item>
            </a-col>
            <a-col :span="8">
              <a-form-model-item label="付款人" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="payerId">
                <j-search-select-tag v-model="model.payerId" dict="sys_user,realname,username"
                                     @change="queryTeamInfo(1)"/>
              </a-form-model-item>
            </a-col>
            <a-col :span="8">
              <a-form-model-item label="收款人" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="payeeId">
                <j-search-select-tag v-model="model.payeeId" dict="sys_user,realname,username"
                                     @change="queryTeamInfo(2)"/>
              </a-form-model-item>
            </a-col>
            <a-col :span="8">
              <a-form-model-item label="总金额" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="totalAmount">
                <a-input v-model="model.totalAmount" placeholder="请输入总金额"></a-input>
              </a-form-model-item>
            </a-col>
            <a-col :span="8">
              <a-form-model-item label="本金" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="principal">
                <a-input v-model="model.principal" placeholder="请输入本金"></a-input>
              </a-form-model-item>
            </a-col>
            <a-col :span="8">
              <a-form-model-item label="收入" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="income">
                <a-input v-model="model.income" placeholder="请输入收入" @change="queryTaxs"></a-input>
              </a-form-model-item>
            </a-col>
            <a-col :span="8">
              <a-form-model-item label="征税类型" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="taxType">
                <j-dict-select-tag type="list" v-model="model.taxType" dictCode="tax_type" placeholder="请选择征税类型"
                                   :disabled="taxDisabled" @change="queryTaxs"/>
              </a-form-model-item>
            </a-col>
            <a-col :span="8">
              <a-form-model-item label="适用税率" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="taxRate">
                <a-input v-model="model.taxRate" placeholder="请输入适用税率" disabled></a-input>
              </a-form-model-item>
            </a-col>
            <a-col :span="8">
              <a-form-model-item label="税额" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="taxAmount">
                <a-input v-model="model.taxAmount" placeholder="请输入税额" disabled></a-input>
              </a-form-model-item>
            </a-col>
            <a-col :span="24">
              <a-form-model-item label="备注" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="remark">
                <a-textarea v-model="model.remark" rows="4" placeholder="请输入备注"/>
              </a-form-model-item>
            </a-col>
          </a-card>
        </a-row>
        <a-row style="margin-top: 10px">
          <a-card title="付款方">
            <a-col :span="8">
              <a-form-model-item label="现金账户" :labelCol="labelCol" :wrapperCol="wrapperCol">
                <a-input v-model="model.payerCashAcct" disabled></a-input>
              </a-form-model-item>
            </a-col>
            <a-col :span="8">
              <a-form-model-item label="债权账户" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="taxRate">
                <a-input v-model="model.payerDebtClaimAcct" disabled></a-input>
              </a-form-model-item>
            </a-col>
            <a-col :span="8">
              <a-form-model-item label="债务账户" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="taxAmount">
                <a-input v-model="model.payerDebtLiabilityAcct" disabled></a-input>
              </a-form-model-item>
            </a-col>
          </a-card>
        </a-row>
        <a-row style="margin-top: 10px">
          <a-card title="付款方">
            <a-col :span="8">
              <a-form-model-item label="现金账户" :labelCol="labelCol" :wrapperCol="wrapperCol">
                <a-input v-model="model.payeeCashAcct" disabled></a-input>
              </a-form-model-item>
            </a-col>
            <a-col :span="8">
              <a-form-model-item label="债权账户" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="taxRate">
                <a-input v-model="model.payeeDebtClaimAcct" disabled></a-input>
              </a-form-model-item>
            </a-col>
            <a-col :span="8">
              <a-form-model-item label="债务账户" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="taxAmount">
                <a-input v-model="model.payeeDebtLiabilityAcct" disabled></a-input>
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
  name: 'BizTransferAcctForm',
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
      taxDisabled: false,
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
        payType: [
          {required: true, message: '请输入转账类型!'},
        ],
        payerId: [
          {required: true, message: '请输入付款人!'},
        ],
        payeeId: [
          {required: true, message: '请输入收款人!'},
        ],
        totalAmount: [
          {required: true, message: '请输入总金额!'},
          {pattern: /^(([0-9][0-9]*)|([0]\.\d{0,2}|[1-9][0-9]*\.\d{0,2}))$/, message: '请输入正确的金额!'},
        ],
        principal: [
          {required: false},
          {pattern: /^(([0-9][0-9]*)|([0]\.\d{0,2}|[1-9][0-9]*\.\d{0,2}))$/, message: '请输入正确的金额!'},
        ],
        income: [
          {required: false},
          {pattern: /^(([0-9][0-9]*)|([0]\.\d{0,2}|[1-9][0-9]*\.\d{0,2}))$/, message: '请输入正确的金额!'},
        ],
        taxType: [
          {required: true, message: '请输入征税类型!'},
        ],
      },
      url: {
        add: "/biz/bizTransferAcct/add",
        edit: "/biz/bizTransferAcct/edit",
        queryById: "/biz/bizTransferAcct/queryById"
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
    queryTeamInfo(type) {
      let puserId = "";
      if (type == "1") {
        puserId = this.model.payerId;
      } else {
        puserId = this.model.payeeId;
      }
      this.queryTaxs();
      if (puserId == undefined || puserId == "") return;
      var params = {userId: puserId}
      getAction("/biz/bizSubjectBalance/queryByUserId", params).then((res) => {
        if (res.success) {
          if (type == "1") {
            this.model.payerCashAcct = res.result.cashAcct;
            this.model.payerDebtClaimAcct = res.result.debtClaimAcct;
            this.model.payerDebtLiabilityAcct = res.result.debtLiabilityAcct;
          } else {
            this.model.payeeCashAcct = res.result.cashAcct;
            this.model.payeeDebtClaimAcct = res.result.debtClaimAcct;
            this.model.payeeDebtLiabilityAcct = res.result.debtLiabilityAcct;
          }
          this.$forceUpdate();
        } else {
          this.$message.warning(res.message)
        }
      }).finally(() => {
      });
    },
    setTaxRate() {
      if(this.model.payType == "JCZZ"){
        this.model.taxType = "NO";
        this.model.taxRate = 0;
        this.model.taxAmount = 0;
        this.taxDisabled = true;
      }else {
        this.model.taxType = "";
        this.taxDisabled = false;
      }
    },
    queryTaxs() {
      if (!this.model.payeeId || this.model.payeeId == "") return;
      if(this.model.taxType == 'NO'){
        this.model.taxRate = 0;
        this.model.taxAmount = 0;
        this.$forceUpdate();
        return;
      }else {
        let bizBankConfig = {
          userId: this.model.payeeId,
          taxAmount: (!this.model.income||this.model.income=="")?0:this.model.income,
          isTransnational: this.model.taxType == 'GS'
        }
        getAction("/biz/bizBankConfig/queryTaxs", bizBankConfig).then((res) => {
          if (res.success) {
            if (this.model.taxType == 'GS') {
              this.model.taxRate = res.result.tariffRate;
            } else {
              this.model.taxRate = res.result.saveRate;
            }
            this.model.taxAmount = res.result.taxAmount;
            this.$forceUpdate();
          } else {
            this.$message.warning(res.message)
          }
        }).finally(() => {
        });
      }
    },
  }
}
</script>