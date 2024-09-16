<template>
  <a-modal
    title="用户初始化"
    :width="800"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @ok="handleSubmit"
    @cancel="handleCancel"
    cancelText="关闭"
    style="top:20px;"
  >
    <a-spin :spinning="confirmLoading">
      <a-form :form="form" ref="form">
        <a-form-item label="每国团队账号数量" :labelCol="labelCol" :wrapperCol="wrapperCol">
          <a-input placeholder="请填写每国团队账号数量" v-model="model.number"/>
        </a-form-item>
        <a-form-item label="团队账户初始余额" :labelCol="labelCol" :wrapperCol="wrapperCol">
          <a-input placeholder="请填写团队账户初始余额" v-model="model.initAmount"/>
        </a-form-item>
      </a-form>
    </a-spin>
  </a-modal>
</template>

<script>
import {httpAction} from '@/api/manage'

export default {
  name: "InitModal",
  data() {
    return {
      visible: false,
      confirmLoading: false,
      model: {number: 5, initAmount: 100.00},
      labelCol: {
        xs: {span: 24},
        sm: {span: 5},
      },
      wrapperCol: {
        xs: {span: 24},
        sm: {span: 16},
      },
      form: this.$form.createForm(this)
    }
  },
  created() {
    console.log("created");
  },

  methods: {
    show() {
      this.form.resetFields();
      this.visible = true;
    },
    close() {
      this.$emit('close');
      this.visible = false;
      this.disableSubmit = false;
    },
    handleSubmit() {
      const that = this;
      this.$confirm({
        title: "确认操作",
        content: "该操作将会清空系统所有数据，并创建新的用户，重新开始游戏，请确认是否初始化?",
        onOk: function () {
          let httpurl = "/sys/user/init";
          that.confirmLoading = true;
          httpAction(httpurl, that.model, "post").then((res) => {
            if (res.success) {
              that.$message.success(res.message);
              that.visible = false;
              that.$emit('ok');
              that.visible = false;
            } else {
              that.$message.warning(res.message);
            }
          }).finally(() => {
            that.confirmLoading = false;
          });
        }
      });
    },
    handleCancel() {
      this.close()
    }
  }
}
</script>