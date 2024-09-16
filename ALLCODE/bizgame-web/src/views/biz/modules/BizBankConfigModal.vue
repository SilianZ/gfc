<template>
  <j-modal
    :title="title"
    :width="1200"
    :visible="visible"
    :maskClosable="false"
    switchFullscreen
    @ok="handleOk"
    :okButtonProps="{ class:{'jee-hidden': disableSubmit} }"
    @cancel="handleCancel">
    <biz-bank-config-form ref="realForm" @ok="submitCallback" :disabled="disableSubmit"/>
  </j-modal>
</template>

<script>

  import BizBankConfigForm from './BizBankConfigForm'

  export default {
    name: 'BizBankConfigModal',
    components: {
      BizBankConfigForm
    },
    data() {
      return {
        title:'',
        width:800,
        visible: false,
        disableSubmit: false
      }
    },
    methods:{
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
        this.$refs.realForm.handleOk();
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
  color: rgba(0, 0, 0, 0.65);
}
</style>