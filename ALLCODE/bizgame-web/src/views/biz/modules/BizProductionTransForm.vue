<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container :disabled="formDisabled">
      <a-form-model ref="form" :model="model" :rules="validatorRules" slot="detail">
        <a-row>
          <a-col :span="8">
            <a-form-model-item label="卖方" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="sellerId">
              <j-search-select-tag v-model="model.sellerId" dict="sys_user,realname,username"  @change="querySellerAssets" />
            </a-form-model-item>
          </a-col>
          <a-col :span="8">
            <a-form-model-item label="买方" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="buyerId">
              <j-search-select-tag v-model="model.buyerId" dict="sys_user,realname,username"  @change="queryBuyerAssets" />
            </a-form-model-item>
          </a-col>
          <a-col :span="8">
            <a-form-model-item label="跨国交易" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="isTransnational">
              <j-dict-select-tag type="radio" v-model="model.isTransnational" dictCode="yes_or_no" placeholder="请选择是否跨国交易" @change="queryTaxs"/>
            </a-form-model-item>
          </a-col>
          <a-col :span="8">
            <a-form-model-item label="交易价格" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="transPrice">
              <a-input-number v-model="model.transPrice" placeholder="请输入交易价格" style="width: 100%" @blur="queryTaxs"/>
            </a-form-model-item>
          </a-col>
          <a-col :span="8">
            <a-form-model-item label="税率" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="taxRate">
              <a-input-number v-model="model.taxRate" placeholder="请输入税率" style="width: 100%" />
            </a-form-model-item>
          </a-col>
          <a-col :span="8">
            <a-form-model-item label="税额" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="transTax">
              <a-input-number v-model="model.transTax" placeholder="请输入税额" style="width: 100%" />
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="芯片数量" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="chipNumber">
              <a-input-number v-model="model.chipNumber" placeholder="请输入芯片数量" style="width: 100%" />
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="汽车数量" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="cardNumber">
              <a-input-number v-model="model.cardNumber" placeholder="请输入汽车数量" style="width: 100%" />
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="新能源数量" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="energyNumber">
              <a-input-number v-model="model.energyNumber" placeholder="请输入新能源数量" style="width: 100%" />
            </a-form-model-item>
          </a-col>
          <a-col :span="12">
            <a-form-model-item label="玩具数量" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="toyNumber">
              <a-input-number v-model="model.toyNumber" placeholder="请输入玩具数量" style="width: 100%" />
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="备注" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="remark">
              <a-textarea v-model="model.remark" rows="4" placeholder="请输入备注" />
            </a-form-model-item>
          </a-col>
        </a-row>
        <a-row v-if="!formDisabled" style="margin-top: 15px">
          <a-col :span="9" style="padding-right: 5px">
            <a-card title="卖方持有通道">
              <a-table
                ref="table1"
                size="middle"
                :scroll="{x:true}"
                bordered
                rowKey="id"
                :columns="table1.columns"
                :dataSource="table1.dataSource"
                :pagination="table1.pagination"
                :loading="table1.loading"
                class="j-table-force-nowrap"
                @change="handlePage1Change"
              />
            </a-card>
          </a-col>
          <a-col :span="9" style="padding-right: 5px">
            <a-card title="买方持有通道">
              <a-table
                ref="table2"
                size="middle"
                :scroll="{x:true}"
                bordered
                rowKey="id"
                :columns="table2.columns"
                :dataSource="table2.dataSource"
                :pagination="table2.pagination"
                :loading="table2.loading"
                class="j-table-force-nowrap"
                @change="handlePage2Change"
              />
            </a-card>
          </a-col>
          <a-col :span="6" style="padding-left: 5px">
            <a-card title="卖方现有成品" style="height:320px">
              <a-col :span="22">
                <a-form-model-item label="芯片数量" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="nchipNumber">
                  <a-input-number v-model="model.nchipNumber" placeholder="现有芯片数量" style="width: 100%" disabled/>
                </a-form-model-item>
              </a-col>
              <a-col :span="22">
                <a-form-model-item label="汽车数量" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="ncardNumber">
                  <a-input-number v-model="model.ncardNumber" placeholder="现有汽车数量" style="width: 100%" disabled/>
                </a-form-model-item>
              </a-col>
              <a-col :span="22">
                <a-form-model-item label="新能源数量" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="nenergyNumber">
                  <a-input-number v-model="model.nenergyNumber" placeholder="现有新能源数量" style="width: 100%" disabled/>
                </a-form-model-item>
              </a-col>
              <a-col :span="22">
                <a-form-model-item label="玩具数量" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="ntoyNumber">
                  <a-input-number v-model="model.ntoyNumber" placeholder="现有玩具数量" style="width: 100%" disabled/>
                </a-form-model-item>
              </a-col>
            </a-card>
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
    name: 'BizProductionTransForm',
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
           sellerId: [
              { required: true, message: '请输入卖方!'},
           ],
           buyerId: [
              { required: true, message: '请输入买方!'},
           ],
           isTransnational: [
              { required: true, message: '请输入是否跨国交易!'},
           ],
           transPrice: [
              { required: true, message: '请输入交易价格!'},
           ],
           taxRate: [
              { required: true, message: '请输入税率!'},
           ],
           transTax: [
              { required: true, message: '请输入税额!'},
              { pattern: /^(([0-9][0-9]*)|([0]\.\d{0,2}|[1-9][0-9]*\.\d{0,2}))$/, message: '请输入正确的金额!'},
           ],
           chipNumber: [
              { required: false},
              { pattern: /^-?\d+\.?\d*$/, message: '请输入数字!'},
           ],
           cardNumber: [
              { required: false},
              { pattern: /^-?\d+\.?\d*$/, message: '请输入数字!'},
           ],
           energyNumber: [
              { required: false},
              { pattern: /^-?\d+\.?\d*$/, message: '请输入数字!'},
           ],
           toyNumber: [
              { required: false},
              { pattern: /^-?\d+\.?\d*$/, message: '请输入数字!'},
           ],
        },
        url: {
          add: "/biz/bizProductionTrans/add",
          edit: "/biz/bizProductionTrans/edit",
          queryById: "/biz/bizProductionTrans/queryById"
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
            this.model.nchipNumber = res.result.chipAcct;
            this.model.ncardNumber = res.result.cardAcct;
            this.model.nenergyNumber = res.result.energyAcct;
            this.model.ntoyNumber = res.result.toyAcct;
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