<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container :disabled="formDisabled">
      <!-- 主表单区域 -->
      <a-form-model ref="form" :model="model" :rules="validatorRules" slot="detail">
        <a-row>
          <a-col :span="8" >
            <a-form-model-item label="所属团队" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="userId">
              <j-search-select-tag v-model="model.userId" dict="sys_user,realname,username"/>
            </a-form-model-item>
          </a-col>
          <a-col :span="8" >
            <a-form-model-item label="资源类型" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="resourceType">
              <j-dict-select-tag type="list" v-model="model.resourceType" dictCode="resource_type" placeholder="请选择资源类型" />
            </a-form-model-item>
          </a-col>
          <a-col :span="8" >
            <a-form-model-item label="资源名称" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="resourceName">
              <a-input v-model="model.resourceName" placeholder="请输入资源名称" ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="8" >
            <a-form-model-item label="所有权" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="isOwnership">
              <j-dict-select-tag type="list" v-model="model.isOwnership" dictCode="yes_or_no" placeholder="请选择所有权" />
            </a-form-model-item>
          </a-col>
          <a-col :span="8" >
            <a-form-model-item label="购买价格" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="price">
              <a-input-number v-model="model.price" placeholder="请输入购买价格" style="width: 100%" />
            </a-form-model-item>
          </a-col>
          <a-col :span="8" >
            <a-form-model-item label="状态" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="status">
              <j-dict-select-tag type="list" v-model="model.status" dictCode="resource_status" placeholder="请选择状态" />
            </a-form-model-item>
          </a-col>
          <a-col :span="24" >
            <a-form-model-item label="备注" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="remark">
              <a-textarea v-model="model.remark" rows="4" placeholder="请输入备注" />
            </a-form-model-item>
          </a-col>
        </a-row>
      </a-form-model>
    </j-form-container>
      <!-- 子表单区域 -->
    <a-tabs v-model="activeKey" @change="handleChangeTabs">
      <a-tab-pane tab="使用权" :key="refKeys[0]" :forceRender="true">
        <j-vxe-table
          keep-source
          :ref="refKeys[0]"
          :loading="bizResourceRightsTable.loading"
          :columns="bizResourceRightsTable.columns"
          :dataSource="bizResourceRightsTable.dataSource"
          :maxHeight="300"
          :disabled="formDisabled"
          :rowNumber="true"
          :rowSelection="true"
          :toolbar="true"
          />
      </a-tab-pane>
    </a-tabs>
  </a-spin>
</template>

<script>

  import { getAction } from '@/api/manage'
  import { JVxeTableModelMixin } from '@/mixins/JVxeTableModelMixin.js'
  import { JVXETypes } from '@/components/jeecg/JVxeTable'
  import { getRefPromise,VALIDATE_FAILED} from '@/components/jeecg/JVxeTable/utils/vxeUtils.js'
  import { validateDuplicateValue } from '@/utils/util'
  import JFormContainer from '@/components/jeecg/JFormContainer'

  export default {
    name: 'BizTeamResourceForm',
    mixins: [JVxeTableModelMixin],
    components: {
      JFormContainer,
    },
    data() {
      return {
        labelCol: {
          xs: { span: 24 },
          sm: { span: 5 },
        },
        wrapperCol: {
          xs: { span: 24 },
          sm: { span: 16 },
        },
        model:{
         },
        // 新增时子表默认添加几行空数据
        addDefaultRowNum: 1,
        validatorRules: {
           userId: [
              { required: true, message: '请输入所属团队!'},
           ],
           resourceType: [
              { required: true, message: '请输入资源类型!'},
           ],
           isOwnership: [
              { required: true, message: '请输入所有权!'},
           ],
           status: [
              { required: true, message: '请输入状态!'},
           ],
           price: [
              { required: true, message: '请输入购买价格!'},
              { pattern: /^(([0-9][0-9]*)|([0]\.\d{0,2}|[1-9][0-9]*\.\d{0,2}))$/, message: '请输入正确的金额!'},
           ],
        },
        refKeys: ['bizResourceRights', ],
        tableKeys:['bizResourceRights', ],
        activeKey: 'bizResourceRights',
        // 使用权
        bizResourceRightsTable: {
          loading: false,
          dataSource: [],
          columns: [
            {
              title: '所属财年',
              key: 'yearCode',
              type: JVXETypes.select,
              options:[],
              dictCode:"biz_fiscal_year,year_name,year_code",
              width:"280px",
              placeholder: '请输入${title}',
              defaultValue:'',
            },
            {
              title: '使用权属',
              key: 'userId',
              type: JVXETypes.userSelect,
              width:"200px",
              placeholder: '请输入${title}',
              defaultValue:'',
            },
          ]
        },
        url: {
          add: "/biz/bizTeamResource/add",
          edit: "/biz/bizTeamResource/edit",
          queryById: "/biz/bizTeamResource/queryById",
          bizResourceRights: {
            list: '/biz/bizTeamResource/queryBizResourceRightsByMainId'
          },
        }
      }
    },
    props: {
      //表单禁用
      disabled: {
        type: Boolean,
        default: false,
        required: false
      }
    },
    computed: {
      formDisabled(){
        return this.disabled
      },
    },
    created () {
    },
    methods: {
      addBefore(){
        this.bizResourceRightsTable.dataSource=[]
      },
      getAllTable() {
        let values = this.tableKeys.map(key => getRefPromise(this, key))
        return Promise.all(values)
      },
      /** 调用完edit()方法之后会自动调用此方法 */
      editAfter() {
        this.$nextTick(() => {
        })
        // 加载子表数据
        if (this.model.id) {
          let params = { id: this.model.id }
          this.requestSubTableData(this.url.bizResourceRights.list, params, this.bizResourceRightsTable)
        }
      },
      //校验所有一对一子表表单
        validateSubForm(allValues){
            return new Promise((resolve,reject)=>{
              Promise.all([
              ]).then(() => {
                resolve(allValues)
              }).catch(e => {
                if (e.error === VALIDATE_FAILED) {
                  // 如果有未通过表单验证的子表，就自动跳转到它所在的tab
                  this.activeKey = e.index == null ? this.activeKey : this.refKeys[e.index]
                } else {
                  console.error(e)
                }
              })
            })
        },
      /** 整理成formData */
      classifyIntoFormData(allValues) {
        let main = Object.assign(this.model, allValues.formValue)
        return {
          ...main, // 展开
          bizResourceRightsList: allValues.tablesValue[0].tableData,
        }
      },
      validateError(msg){
        this.$message.error(msg)
      },

    }
  }
</script>

<style scoped>
</style>