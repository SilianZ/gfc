<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container :disabled="formDisabled">
      <a-form-model ref="form" :model="model" :rules="validatorRules" slot="detail">
        <a-row>
          <a-card title="生产信息">
            <a-col :span="8">
              <a-form-model-item label="所属团队" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="userId">
                <j-search-select-tag v-model="model.userId" dict="sys_user,realname,username,post='TEM'"
                                     @change="queryTeamInfo"/>
              </a-form-model-item>
            </a-col>
            <a-col :span="8">
              <a-form-model-item label="加工厂" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="resourceId">
                <j-search-select-tag v-model="model.resourceId" :dict="factoryData" @change="queryFactoryRate"/>
              </a-form-model-item>
            </a-col>
            <a-col :span="8">
              <a-form-model-item label="生产速率" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="processRate">
                <a-input-number v-model="model.processRate" placeholder="加工厂生产速率" style="width: 100%" disabled=""/>
              </a-form-model-item>
            </a-col>
            <a-col :span="12">
              <a-form-model-item label="芯片数量" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="chipNumber">
                <a-input-number v-model="model.chipNumber" placeholder="请输入芯片数量" style="width: 100%"
                                @change="setConsume"/>
              </a-form-model-item>
            </a-col>
            <a-col :span="12">
              <a-form-model-item label="汽车数量" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="cardNumber">
                <a-input-number v-model="model.cardNumber" placeholder="请输入汽车数量" style="width: 100%"
                                @change="setConsume"/>
              </a-form-model-item>
            </a-col>
            <a-col :span="12">
              <a-form-model-item label="新能源数量" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="energyNumber">
                <a-input-number v-model="model.energyNumber" placeholder="请输入新能源数量" style="width: 100%"
                                @change="setConsume"/>
              </a-form-model-item>
            </a-col>
            <a-col :span="12">
              <a-form-model-item label="玩具数量" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="toyNumber">
                <a-input-number v-model="model.toyNumber" placeholder="请输入玩具数量" style="width: 100%"
                                @change="setConsume"/>
              </a-form-model-item>
            </a-col>
            <a-col :span="24">
              <a-form-model-item label="备注" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="remark">
                <a-textarea v-model="model.remark" rows="4" placeholder="请输入备注"/>
              </a-form-model-item>
            </a-col>
          </a-card>
        </a-row>
        <a-row v-if="formDisabled" style="margin-top: 15px">
          <a-card title="消耗资源">
            <a-col :span="6">
              <a-form-model-item label="钢铁数量" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="gtNumber">
                <a-input-number v-model="model.gtNumber" placeholder="消耗钢铁数量" style="width: 100%"/>
              </a-form-model-item>
            </a-col>
            <a-col :span="6">
              <a-form-model-item label="硅石数量" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="gsNumber">
                <a-input-number v-model="model.gsNumber" placeholder="消耗硅石数量" style="width: 100%"/>
              </a-form-model-item>
            </a-col>
            <a-col :span="6">
              <a-form-model-item label="石油数量" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="syNumber">
                <a-input-number v-model="model.syNumber" placeholder="消耗石油数量" style="width: 100%"/>
              </a-form-model-item>
            </a-col>
            <a-col :span="6">
              <a-form-model-item label="塑料数量" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="slNumber">
                <a-input-number v-model="model.slNumber" placeholder="消耗塑料数量" style="width: 100%"/>
              </a-form-model-item>
            </a-col>
          </a-card>
        </a-row>
        <a-row v-if="!formDisabled" style="margin-top: 15px">
          <a-col :span="10" style="padding-right: 5px">
            <a-card title="现有固资">
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
          <a-col :span="7" style="padding-left: 5px">
            <a-card title="现有材料">
              <a-col :span="22">
                <a-form-model-item label="钢铁数量" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="gtNumber">
                  <a-input-number v-model="model.ngtNumber" placeholder="现有钢铁数量" style="width: 100%" disabled/>
                </a-form-model-item>
              </a-col>
              <a-col :span="22">
                <a-form-model-item label="硅石数量" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="gsNumber">
                  <a-input-number v-model="model.ngsNumber" placeholder="现有硅石数量" style="width: 100%" disabled/>
                </a-form-model-item>
              </a-col>
              <a-col :span="22">
                <a-form-model-item label="石油数量" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="syNumber">
                  <a-input-number v-model="model.nsyNumber" placeholder="现有石油数量" style="width: 100%" disabled/>
                </a-form-model-item>
              </a-col>
              <a-col :span="22">
                <a-form-model-item label="塑料数量" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="slNumber">
                  <a-input-number v-model="model.nslNumber" placeholder="现有塑料数量" style="width: 100%" disabled/>
                </a-form-model-item>
              </a-col>
            </a-card>
          </a-col>
          <a-col :span="7" style="padding-left: 5px">
            <a-card title="本次消耗资源">
              <a-col :span="22">
                <a-form-model-item label="硅石数量" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="gsNumber">
                  <a-input-number v-model="model.gsNumber" placeholder="消耗硅石数量" style="width: 100%" disabled/>
                </a-form-model-item>
              </a-col>
              <a-col :span="22">
                <a-form-model-item label="钢铁数量" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="gtNumber">
                  <a-input-number v-model="model.gtNumber" placeholder="消耗钢铁数量" style="width: 100%" disabled/>
                </a-form-model-item>
              </a-col>
              <a-col :span="22">
                <a-form-model-item label="石油数量" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="syNumber">
                  <a-input-number v-model="model.syNumber" placeholder="消耗石油数量" style="width: 100%" disabled/>
                </a-form-model-item>
              </a-col>
              <a-col :span="22">
                <a-form-model-item label="塑料数量" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="slNumber">
                  <a-input-number v-model="model.slNumber" placeholder="消耗塑料数量" style="width: 100%" disabled/>
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

import {httpAction, getAction} from '@/api/manage'
import {validateDuplicateValue} from '@/utils/util'
import {JVXETypes} from "@comp/jeecg/JVxeTable";

export default {
  name: 'BizProductionProcessForm',
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
      factoryData: 'biz_team_resource,resource_name,id,1=2',
      ableSubmit: false,
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
          {required: true, message: '请输入所属团队!'},
        ],
        resourceId: [
          {required: true, message: '请输入加工厂!'},
        ],
        processRate: [
          {required: true, message: '请输入生产速率!'},
          {pattern: /^-?\d+\.?\d*$/, message: '请输入数字!'},
        ],
        chipNumber: [
          {required: false},
          {pattern: /^-?\d+$/, message: '请输入整数!'},
        ],
        cardNumber: [
          {required: false},
          {pattern: /^-?\d+$/, message: '请输入整数!'},
        ],
        energyNumber: [
          {required: false},
          {pattern: /^-?\d+$/, message: '请输入整数!'},
        ],
        toyNumber: [
          {required: false},
          {pattern: /^-?\d+$/, message: '请输入整数!'},
        ]
      },
      url: {
        add: "/biz/bizProductionProcess/add",
        edit: "/biz/bizProductionProcess/edit",
        queryById: "/biz/bizProductionProcess/queryById"
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
    }
  },
  computed: {
    formDisabled() {
      return this.disabled
    },
  },
  created() {
    //备份model原始值
    this.model.ngtNumber = 0;
    this.model.ngsNumber = 0;
    this.model.nsyNumber = 0;
    this.model.nslNumber = 0;
    this.getDictInfo();
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
      if (!this.model.ableSubmit) {
        this.$message.warning("原材料不足，不能提交！")
        return;
      }
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
    setConsume() {
      let gtNum = 0, gsNum = 0, syNum = 0, slNum = 0;

      gsNum = (this.model.chipNumber == null ? 0 : this.model.chipNumber * 20) +
        (this.model.cardNumber == null ? 0 : this.model.cardNumber * 5) +
        (this.model.energyNumber == null ? 0 : this.model.energyNumber * 5) +
        (this.model.toyNumber == null ? 0 : this.model.toyNumber * 10);

      gtNum = (this.model.chipNumber == null ? 0 : this.model.chipNumber * 10) +
        (this.model.cardNumber == null ? 0 : this.model.cardNumber * 20) +
        (this.model.energyNumber == null ? 0 : this.model.energyNumber * 5) +
        (this.model.toyNumber == null ? 0 : this.model.toyNumber * 5);

      syNum = (this.model.chipNumber == null ? 0 : this.model.chipNumber * 5) +
        (this.model.cardNumber == null ? 0 : this.model.cardNumber * 10) +
        (this.model.energyNumber == null ? 0 : this.model.energyNumber * 20) +
        (this.model.toyNumber == null ? 0 : this.model.toyNumber * 5);

      slNum = (this.model.chipNumber == null ? 0 : this.model.chipNumber * 5) +
        (this.model.cardNumber == null ? 0 : this.model.cardNumber * 5) +
        (this.model.energyNumber == null ? 0 : this.model.energyNumber * 10) +
        (this.model.toyNumber == null ? 0 : this.model.toyNumber * 20);

      this.model.gtNumber = Math.ceil(gtNum / this.model.processRate);
      this.model.gsNumber = Math.ceil(gsNum / this.model.processRate);
      this.model.syNumber = Math.ceil(syNum / this.model.processRate);
      this.model.slNumber = Math.ceil(slNum / this.model.processRate);
      let message = "";
      if (this.model.gtNumber > this.model.ngtNumber) {
        message += "钢铁资源不足：现有资源" + this.model.ngtNumber + "，需要" + this.model.gtNumber + "；";
      }
      if (this.model.gsNumber > this.model.ngsNumber) {
        message += "硅石资源不足：现有资源" + this.model.ngsNumber + "，需要" + this.model.gsNumber + "；";
      }
      if (this.model.syNumber > this.model.nsyNumber) {
        message += "石油资源不足：现有资源" + this.model.nsyNumber + "，需要" + this.model.syNumber + "；";
      }
      if (this.model.slNumber > this.model.nslNumber) {
        message += "塑料资源不足：现有资源" + this.model.nslNumber + "，需要" + this.model.slNumber + "；";
      }
      if (message != "") {
        this.model.ableSubmit = false;
        this.$message.warning(message)
      } else {
        this.model.ableSubmit = true;
      }
    },
    queryTeamInfo() {
      if (!this.model.userId) return;
      this.getDictInfo();
      var params = {userId: this.model.userId}
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
      this.queryAssets();
      this.queryFactoryRate();
    },
    queryFactoryRate() {
      if (!this.model.resourceId) return;
      var params = {id: this.model.resourceId}
      getAction("/biz/bizTeamResource/queryById", params).then((res) => {
        if (res.success) {
          this.model.yearCode = res.result.yearCode;
          if (res.result.isBankRate) {
            this.model.processRate = res.result.bankRate;
            this.$message.info("银行启动加工业投资，老式工厂损耗降低为35%")
          } else {
            this.model.processRate = res.result.productRate;
          }
          this.$forceUpdate();
        } else {
          this.$message.warning(res.message)
        }
      }).finally(() => {
      })
      this.setConsume();
    },
    getDictInfo() {
      let str = ''
      if (this.model.userId) {
        str = 'v_team_resource,resource_name,id,(data_type=\'SYON\' and resource_type=\'LS\') or (data_type=\'SYON\' and resource_type regexp \'JM|ML\' and user_id regexp \'' + this.model.userId + '\')';
      } else if(this.formDisabled){
        str = 'v_team_resource,resource_name,id';
      } else {
        str = 'v_team_resource,resource_name,id,data_type=\'SYON\' and resource_type=\'LS\'';
      }
      this.factoryData = str
    },
    // 当分页参数变化时触发的事件
    handlePage1Change(event) {
      // 重新赋值
      this.table1.pagination.current = event.current
      this.table1.pagination.pageSize = event.pageSize
      // 查询数据
      this.queryAssets()
    },
    queryAssets() {
      if (!this.model.userId) return;
      var params = {
        userId: this.model.userId,
        pageNo: this.table1.pagination.current,
        pageSize: this.table1.pagination.pageSize
      }
      this.table1.loading = true;
      this.getDictInfo();
      getAction("/biz/vTeamResource/list", params).then((res) => {
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
    }
  }
}
</script>