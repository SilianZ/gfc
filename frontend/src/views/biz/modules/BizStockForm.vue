<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container :disabled="formDisabled">
      <a-form-model ref="form" :model="model" :rules="validatorRules" slot="detail">
        <a-row>
          <a-col :span="8">
            <a-form-model-item label="存入方" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="sellerId">
              <j-search-select-tag v-model="model.sellerId" dict="sys_user,realname,username"/>
            </a-form-model-item>
          </a-col>
          <a-col :span="8">
            <a-form-model-item label="存入资金" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="transPrice">
              <a-input-number v-model="model.transPrice" placeholder="请输入存入资金" style="width: 100%"/>
            </a-form-model-item>
          </a-col>
          <a-col :span="8">
            <a-form-model-item label="存入财年" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="yearCode">
              <a-input-number v-model="model.yearCode" placeholder="请输入存入财年" style="width: 100%"/>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="备注" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="remark">
              <a-textarea v-model="model.remark" rows="4" placeholder="请输入备注"/>
            </a-form-model-item>
          </a-col>
        </a-row>
      </a-form-model>
    </j-form-container>
  </a-spin>
</template>

<script>

import {httpAction, getAction} from '@/api/manage'
import {validateDuplicateValue} from '@/utils/util'

export default {
  name: 'BizStockForm',
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
        yearCode: [
          {required: true, message: '请输入所属财年!'},
        ],
        sellerId: [
          {required: true, message: '请输入存入方!'},
        ],
        transPrice: [
          {required: true, message: '请输入存入资金!'},
        ]
      },
      url: {
        add: "/biz/bizStock/add",
        edit: "/biz/bizStock/edit",
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
    queryTeamInfo() {
    //查询团队信息，其中含有账户资金
      if (!this.model.sellerId) return;
      var params = {userId: this.model.sellerId}
      getAction("/biz/bizSubjectBalance/queryByUserId", params).then((res) => {
        if (res.success) {
          this.model.ngtNumber = res.result.steelAcct;
          this.model.ngsNumber = res.result.silicaAcct;
          this.model.nsyNumber = res.result.crudeAcct;
          this.model.nslNumber = res.result.plasticsAcct;
          this.$forceUpdate();
        } else {
          this.$message.warning(res.message)
        }
      }).finally(() => {
      })
    }
  }
}
</script>