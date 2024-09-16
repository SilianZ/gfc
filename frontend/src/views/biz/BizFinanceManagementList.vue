<template>
  <a-card :bordered="false">
    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">
        </a-row>
      </a-form>
    </div>
    <!-- 查询区域-END -->

    <!-- 操作按钮区域 -->
    <div class="table-operator">
      <a-button @click="handleAdd" type="primary" icon="plus">新增</a-button>
      <a-button type="primary" icon="download" @click="handleExportXls('原材交易')">导出</a-button>
      <a-upload name="file" :showUploadList="false" :multiple="false" :headers="tokenHeader" :action="importExcelUrl"
                @change="handleImportExcel">
        <a-button type="primary" icon="import">导入</a-button>
      </a-upload>
    </div>

    <!-- table区域-begin -->
    <div>
      <a-table
        ref="table"
        size="middle"
        :scroll="{x:true}"
        bordered
        rowKey="id"
        :columns="columns"
        :dataSource="dataSource"
        :pagination="ipagination"
        :loading="loading"
        class="j-table-force-nowrap"
        @change="handleTableChange">
        <span slot="action" slot-scope="text, record">
          <a @click="handleDetail(record)">详情</a>

          <a-divider type="vertical"/>
          <a-popconfirm title="确定删除吗?" @confirm="() => handleDelete(record.id)">
            <a>删除</a>
          </a-popconfirm>
        </span>
      </a-table>
    </div>

    <biz-finance-management-modal ref="modalForm" @ok="modalFormOk"></biz-finance-management-modal>
  </a-card>
</template>

<script>

import '@/assets/less/TableExpand.less'
import {mixinDevice} from '@/utils/mixin'
import {JeecgListMixin} from '@/mixins/JeecgListMixin'
import BizFinanceManagementModal from './modules/BizFinanceManagementModal'
import {filterMultiDictText} from '@/components/dict/JDictSelectUtil'

export default {
  name: 'BizFinanceManagementList',
  mixins: [JeecgListMixin, mixinDevice],
  components: {
    BizFinanceManagementModal
  },
  data() {
    return {
      description: '理财管理页面',
      // 表头
      columns: [
        {
          title: '#',
          dataIndex: '',
          key: 'rowIndex',
          width: 60,
          align: "center",
          customRender: function (t, r, index) {
            return parseInt(index) + 1;
          }
        },
        {
          title: '存入财年',
          align: "center",
          dataIndex: 'yearCode_dictText'
        },
        {
          title: '存入方',
          align: "center",
          dataIndex: 'buyerId_dictText'
        },
        {
          title: '存入资金',
          align: "center",
          dataIndex: 'yearCode_dictText'
        },
        {
          title: '预计收益',
          align: "center",
          dataIndex: ''
        },
        {
          title: '操作',
          dataIndex: 'action',
          align: "center",
          fixed: "right",
          width: 147,
          scopedSlots: {customRender: 'action'}
        }
      ],
      url: {
        list: "/biz/bizFinanceManagement/list",
        delete: "/biz/bizFinanceManagement/delete",
        deleteBatch: "/biz/bizFinanceManagement/deleteBatch",
        exportXlsUrl: "/biz/bizFinanceManagement/exportXls",
        importExcelUrl: "biz/bizFinanceManagement/importExcel",

      },
      dictOptions: {},
      superFieldList: [],
    }
  },
  created() {
    this.getSuperFieldList();
  },
  computed: {
    importExcelUrl: function () {
      return `${window._CONFIG['domianURL']}/${this.url.importExcelUrl}`;
    },
  },
  methods: {
    initDictConfig() {
    },
    getSuperFieldList() {
      let fieldList = [];
      fieldList.push({type: 'int', value: 'yearCode', text: '所属财年', dictCode: "biz_fiscal_year,year_name,year_code"})
      fieldList.push({
        type: 'sel_search',
        value: 'buyerId',
        text: '存入方',
        dictTable: "sys_user",
        dictText: 'realname',
        dictCode: 'username'
      })
      fieldList.push({type: 'double', value: 'transPrice', text: '存入资金', dictCode: ''})
      fieldList.push({type: 'string', value: 'remark', text: '备注', dictCode: ''})
      this.superFieldList = fieldList
    }
  }
}
</script>
<style scoped>
@import '~@assets/less/common.less';
</style>