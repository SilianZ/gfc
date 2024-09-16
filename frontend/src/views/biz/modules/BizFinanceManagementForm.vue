<template>
    <a-spin :spinning="confirmLoading">
      <j-form-container :disabled="formDisabled">
        <a-form-model ref="form" :model="model" :rules="validatorRules" slot="detail">
          <a-row>
            <a-col :span="8">
              <a-form-model-item label="存入方" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="sellerId">
                <j-search-select-tag v-model="model.sellerId" dict="sys_user,realname,username" @change="querySellerAssets"/>
              </a-form-model-item>
            </a-col>
            <a-col :span="8">
              <a-form-model-item label="存入资金" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="transPrice">
                <a-input-number v-model="model.transPrice" placeholder="请输入存入资金" style="width: 100%" @blur="queryTaxs"/>
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
    name: 'BizFinanceManagementForm',
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
          add: "/biz/bizFinanceManagement/add",
          edit: "/biz/bizFinanceManagement/edit",
          queryById: "/biz/bizFinanceManagement/queryById"
        },
        table1: {
          loading: false,
          pagination: {
            current: 1,
            pageSize: 10,
            pageSizeOptions: ['10', '20', '30', '50'],
            total: 0,
          },
          dataSource: [],
          columns: [
            {dataIndex: 'resourceType_dictText', title: '资源类型'},
            {dataIndex: 'resourceName', title: '资源名称'},
            {dataIndex: 'status_dictText', title: '状态'},
            {dataIndex: 'limitYear_dictText', title: '使用权截止', customRender: (text, record) => {if(record.limitYear=="99") return '无限期'; else return text}},
          ],
        },
        table2: {
          loading: false,
          pagination: {
            current: 1,
            pageSize: 10,
            pageSizeOptions: ['10', '20', '30', '50'],
            total: 0,
          },
          dataSource: [],
          columns: [
            {dataIndex: 'resourceType_dictText', title: '资源类型'},
            {dataIndex: 'resourceName', title: '资源名称'},
            {dataIndex: 'status_dictText', title: '状态'},
            {dataIndex: 'limitYear_dictText', title: '使用权截止', customRender: (text, record) => {if(record.limitYear=="99") return '无限期'; else return text}},
          ],
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
      handlePage1Change(event) {
        // 重新赋值
        this.table1.pagination.current = event.current
        this.table1.pagination.pageSize = event.pageSize
        // 查询数据
        this.querySellerAssets()
      },
      handlePage2Change(event) {
        // 重新赋值
        this.table1.pagination.current = event.current
        this.table1.pagination.pageSize = event.pageSize
        // 查询数据
        this.queryBuyerAssets()
      },
      querySellerAssets() {
        if (!this.model.sellerId) return;
        var params = {
          userId: this.model.sellerId,
          resourceType: "CN",
          pageNo: this.table1.pagination.current,
          pageSize: this.table1.pagination.pageSize
        }
        this.table1.loading = true;
        getAction("/biz/vTeamResource/listChannel", params).then((res) => {
          if (res.success) {
            this.table1.dataSource = res.result.records || res.result;
            if (res.result.total) {
              this.table1.pagination.total = res.result.total;
            } else {
              this.table1.pagination.total = 0;
            }
          } else {
            this.$message.warning(res.message)
          }
        }).finally(() => {
          this.table1.loading = false
        })
        this.queryTeamInfo();
      },
      queryBuyerAssets() {
        if (!this.model.buyerId) return;
        var params = {
          userId: this.model.buyerId,
          resourceType: "CN",
          pageNo: this.table2.pagination.current,
          pageSize: this.table2.pagination.pageSize
        }
        this.queryTaxs();
        this.table2.loading = true;
        getAction("/biz/vTeamResource/listChannel", params).then((res) => {
          if (res.success) {
            this.table2.dataSource = res.result.records || res.result;
            if (res.result.total) {
              this.table2.pagination.total = res.result.total;
            } else {
              this.table2.pagination.total = 0;
            }
          } else {
            this.$message.warning(res.message)
          }
        }).finally(() => {
          this.table2.loading = false
        })
      },
      queryTeamInfo() {
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
      },
      queryTaxs() {
        if (!this.model.buyerId || !this.model.transPrice || !this.model.isTransnational ) return;
        let bizBankConfig = {
          userId: this.model.buyerId,
          taxAmount: this.model.transPrice+1,
          isTransnational: this.model.isTransnational == "Y"
        }
        getAction("/biz/bizBankConfig/queryTaxs", bizBankConfig).then((res) => {
          if (res.success) {
            if (this.model.isTransnational == "Y") {
              this.model.taxRate = res.result.tariffRate;
            } else {
              this.model.taxRate = res.result.saveRate;
            }
            this.model.transTax = res.result.taxAmount;
            this.$forceUpdate();
          } else {
            this.$message.warning(res.message)
          }
        }).finally(() => {
        });
      }
    }
  }
  </script>