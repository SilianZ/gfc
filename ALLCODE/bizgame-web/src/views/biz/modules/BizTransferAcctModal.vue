<template>
  <j-modal
    :title="title"
    :width="width"
    :visible="visible"
    switchFullscreen
    @ok="handleOk"
    :okButtonProps="{ class:{'jee-hidden': disableSubmit} }"
    @cancel="handleCancel"
    cancelText="关闭">
    <biz-transfer-acct-form ref="realForm" @ok="submitCallback" :disabled="disableSubmit"></biz-transfer-acct-form>
  </j-modal>
</template>

<script>

  import BizTransferAcctForm from './BizTransferAcctForm'
  export default {
    name: 'BizTransferAcctModal',
    components: {
      BizTransferAcctForm
    },
    data () {
      return {
        title:'',
        width:1280,
        visible: false,
        disableSubmit: false
      }
    },
    methods: {
      add () {
        this.visible=true
        this.$nextTick(()=>{
          this.$refs.realForm.add();
        })
      },
      edit (record) {
        this.visible=true
        this.$nextTick(()=>{
          this.$refs.realForm.edit(record);
        })
      },
      close () {
        this.$emit('close');
        this.visible = false;
      },
      handleOk () {
        this.$refs.realForm.submitForm();
      },
      submitCallback(){
        this.$emit('ok');
        this.visible = false;
      },
      handleCancel () {
        this.close()
      }
    }
  }
</script>
<style lang="less">
.j-modal-box {
  .ant-form-item {
    margin-bottom: 5px;
  }
  .ant-form-item-label {
    width: 120px;
  }
  & .ant-form-item-control-wrapper{
    width: calc(100% - 120px)
  }
}
.ant-input-number-disabled {
  color: rgba(0, 0, 0, 0.75) !important;
  background-color: #f5f5f5a8 !important;;
}
.ant-input-disabled {
  color: rgba(0, 0, 0, 0.75) !important;;
  background-color: #f5f5f5a8 !important;;
}
</style>