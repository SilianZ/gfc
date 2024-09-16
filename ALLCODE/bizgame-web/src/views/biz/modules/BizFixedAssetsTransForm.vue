<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container :disabled="formDisabled">
      <a-form-model ref="form" :model="model" :rules="validatorRules" slot="detail">
        <a-row>
          <a-col :span="8">
            <a-form-model-item label="交易类型" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="transType">
              <j-dict-select-tag type="list" v-model="model.transType" dictCode="trans_type" placeholder="请选择交易类型"/>
            </a-form-model-item>
          </a-col>
          <a-col :span="16">
            <a-form-model-item label="注：" :labelCol="labelCol" :wrapperCol="wrapperCol">
              合资建造伽马工厂，成员提前转账到指定股东账户，买方选择所有合资方，资金和资源从第一个买方账户扣除
            </a-form-model-item>
          </a-col>
          <a-col :span="8">
            <a-form-model-item label="卖方" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="sellerId">
              <j-search-select-tag v-model="model.sellerId" dict="sys_user,realname,username"
                                   @change="querySellerAssets"/>
            </a-form-model-item>
          </a-col>
          <a-col :span="8">
            <a-form-model-item label="买方" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="buyerId">
              <j-search-select-tag v-model="model.buyerId" dict="sys_user,realname,username" mode="multiple"
                                   @change="queryBuyerAssets"/>
            </a-form-model-item>
          </a-col>
          <a-col :span="8">
            <a-form-model-item label="所有权/使用权" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="rightType">
              <j-dict-select-tag type="list" v-model="model.rightType" dictCode="right_type" placeholder="请选择所有权/使用权"
                                 @change="getDictInfo"/>
            </a-form-model-item>
          </a-col>
          <a-col :span="8">
            <a-form-model-item label="使用年限" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="yearLimit">
              <a-input-number v-model="model.yearLimit" placeholder="请输入使用年限" style="width: 100%"/>
            </a-form-model-item>
          </a-col>
          <a-col :span="8">
            <a-form-model-item label="交易标的" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="transObject">
              <j-dict-select-tag type="list" v-model="model.transObject" :dictCode="dictCode"
                                 placeholder="请选择交易标的"/>
            </a-form-model-item>
          </a-col>
          <a-col :span="8">
            <a-form-model-item label="交易价格" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="transPrice">
              <a-input-number v-model="model.transPrice" placeholder="请输入交易价格" style="width: 100%"/>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="交易附言" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="remark">
              <a-textarea v-model="model.remark" rows="4" placeholder="请输入交易附言"/>
            </a-form-model-item>
          </a-col>
        </a-row>
        <a-row v-if="!formDisabled">
          <a-col :span="12" style="padding-right: 10px">
            <a-card title="卖方拥有资源">
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
          <a-col :span="12" style="padding-left: 10px">
            <a-card title="买方拥有资源">
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
        </a-row>
      </a-form-model>
    </j-form-container>
  </a-spin>
</template>

<script>

import {httpAction, getAction} from '@/api/manage'
import {filterObj, validateDuplicateValue} from '@/utils/util'
import {JVXETypes} from "@comp/jeecg/JVxeTable";

export default {
  name: 'BizFixedAssetsTransForm',
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
      dictCode: 'v_team_resource,resource_name,id,1=2',
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
        transType: [
          {required: true, message: '请输入交易类型!'},
        ],
        transPrice: [
          {required: true, message: '请输入交易价格!'},
        ],
        sellerId: [
          {required: true, message: '请输入卖方!'},
        ],
        buyerId: [
          {required: true, message: '请输入买方!'},
        ],
        transObject: [
          {required: true, message: '请输入交易标的!'},
        ],
        rightType: [
          {required: true, message: '请输入所有权/使用权!'},
        ],
      },
      url: {
        add: "/biz/bizFixedAssetsTrans/add",
        edit: "/biz/bizFixedAssetsTrans/edit",
        queryById: "/biz/bizFixedAssetsTrans/queryById"
      },
      isorter: {
        column: 'yearCode',
        order: 'asc',
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
    this.getDictInfo();
  },
  methods: {
    add() {
      this.edit(this.modelDefault);
    },
    edit(record) {
      this.model = Object.assign({}, record);
      this.visible = true;
    },
    // 当分页参数变化时触发的事件
    handlePage1Change(event) {
      // 重新赋值
      this.table1.pagination.current = event.current
      this.table1.pagination.pageSize = event.pageSize
      // 查询数据
      this.querySellerAssets()
    },
    // 当分页参数变化时触发的事件
    handlePage2Change(event) {
      // 重新赋值
      this.table2.pagination.current = event.current
      this.table2.pagination.pageSize = event.pageSize
      // 查询数据
      this.queryBuyerAssets()
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
    querySellerAssets() {
      if (!this.model.sellerId) return;
      var params = {
        userId: this.model.sellerId,
        pageNo: this.table1.pagination.current,
        pageSize: this.table1.pagination.pageSize
      }
      this.table1.loading = true;
      this.getDictInfo();
      getAction("/biz/vTeamResource/list", params).then((res) => {
        if (res.success) {
          this.model.yearCode = res.message;
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
    },
    queryBuyerAssets() {
      if (!this.model.buyerId) return;
      var params = {
        userId: this.model.buyerId,
        pageNo: this.table2.pagination.current,
        pageSize: this.table2.pagination.pageSize
      }
      this.table2.loading = true;
      getAction("/biz/vTeamResource/list", params).then((res) => {
        if (res.success) {
          this.model.yearCode = res.message;
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
    getDictInfo() {
      let str = ''
      if (this.model.rightType == "SYOU") {//交易所有权，必须是个人拥有的资产，合资工厂不可以交易
        str = 'v_team_resource,resource_name,id,data_type=\'SYOU\' and user_id = \'' + this.model.sellerId + '\'';
      } else if (this.model.rightType == "SYON") {//交易使用权
        str = 'v_team_resource,resource_name,id,data_type=\'SYON\' and user_id = \'' + this.model.sellerId + '\'';
      } else {
        str = 'v_team_resource,resource_name,id';
      }
      this.dictCode = str
    },
  }
}
</script>