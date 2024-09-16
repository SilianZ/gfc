<!--账户转账页面-->
<template>
  <a-card :bordered="false">
    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="所属财年">
              <j-dict-select-tag placeholder="请选择所属财年" v-model="queryParam.yearCode" dictCode="biz_fiscal_year,year_name,year_code"/>
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="转账类型">
              <j-dict-select-tag placeholder="请选择转账类型" v-model="queryParam.payType" dictCode="transfer_type"/>
            </a-form-item>
          </a-col>
          <template v-if="toggleSearchStatus">
            <a-col :xl="6" :lg="7" :md="8" :sm="24">
              <a-form-item label="付款人">
                <j-search-select-tag placeholder="请选择付款人" v-model="queryParam.payerId" dict="sys_user,realname,username"/>
              </a-form-item>
            </a-col>
            <a-col :xl="6" :lg="7" :md="8" :sm="24">
              <a-form-item label="收款人">
                <j-search-select-tag placeholder="请选择收款人" v-model="queryParam.payeeId" dict="sys_user,realname,username"/>
              </a-form-item>
            </a-col>
            <a-col :xl="6" :lg="7" :md="8" :sm="24">
              <a-form-item label="征税类型">
                <j-dict-select-tag placeholder="请选择征税类型" v-model="queryParam.taxType" dictCode="tax_type"/>
              </a-form-item>
            </a-col>
          </template>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <span style="float: left;overflow: hidden;" class="table-page-search-submitButtons">
              <a-button type="primary" @click="searchQuery" icon="search">查询</a-button>
              <a-button type="primary" @click="searchReset" icon="reload" style="margin-left: 8px">重置</a-button>
              <a @click="handleToggleSearch" style="margin-left: 8px">
                {{ toggleSearchStatus ? '收起' : '展开' }}
                <a-icon :type="toggleSearchStatus ? 'up' : 'down'"/>
              </a>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>
    <!-- 查询区域-END -->

    <!-- 操作按钮区域 -->
    <div class="table-operator">
      <a-button @click="handleAdd" type="primary" icon="plus">新增</a-button>
      <a-button type="primary" icon="download" @click="handleExportXls('账户转账')">导出</a-button>
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

        <template slot="htmlSlot" slot-scope="text">
          <div v-html="text"></div>
        </template>
        <template slot="imgSlot" slot-scope="text,record">
          <span v-if="!text" style="font-size: 12px;font-style: italic;">无图片</span>
          <img v-else :src="getImgView(text)" :preview="record.id" height="25px" alt="" style="max-width:80px;font-size: 12px;font-style: italic;"/>
        </template>
        <template slot="fileSlot" slot-scope="text">
          <span v-if="!text" style="font-size: 12px;font-style: italic;">无文件</span>
          <a-button
            v-else
            :ghost="true"
            type="primary"
            icon="download"
            size="small"
            @click="downloadFile(text)">
            下载
          </a-button>
        </template>

        <span slot="action" slot-scope="text, record">
          <a @click="handleDetail(record)">详情</a>
          <a-divider type="vertical" />
          <a-popconfirm title="确定删除吗?" @confirm="() => handleDelete(record.id)">
            <a>删除</a>
          </a-popconfirm>
        </span>

      </a-table>
    </div>

    <biz-transfer-acct-modal ref="modalForm" @ok="modalFormOk"></biz-transfer-acct-modal>
  </a-card>
</template>

<script>

  import '@/assets/less/TableExpand.less'
  import { mixinDevice } from '@/utils/mixin'
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  import BizTransferAcctModal from './modules/BizTransferAcctModal'
  import {filterMultiDictText} from '@/components/dict/JDictSelectUtil'

  export default {
    name: 'BizTransferAcctList',
    mixins:[JeecgListMixin, mixinDevice],
    components: {
      BizTransferAcctModal
    },
    data () {
      return {
        description: '账户转账管理页面',
        // 表头
        columns: [
          {
            title: '#',
            dataIndex: '',
            key:'rowIndex',
            width:60,
            align:"center",
            customRender:function (t,r,index) {
              return parseInt(index)+1;
            }
          },
          {
            title:'所属财年',
            align:"center",
            dataIndex: 'yearCode_dictText'
          },
          {
            title:'转账类型',
            align:"center",
            sorter: true,
            dataIndex: 'payType_dictText'
          },
          {
            title:'付款人',
            align:"center",
            sorter: true,
            dataIndex: 'payerId_dictText'
          },
          {
            title:'收款人',
            align:"center",
            sorter: true,
            dataIndex: 'payeeId_dictText'
          },
          {
            title:'总金额',
            align:"center",
            sorter: true,
            dataIndex: 'totalAmount'
          },
          {
            title:'本金',
            align:"center",
            sorter: true,
            dataIndex: 'principal'
          },
          {
            title:'收入',
            align:"center",
            sorter: true,
            dataIndex: 'income'
          },
          {
            title:'税额',
            align:"center",
            sorter: true,
            dataIndex: 'taxAmount'
          },
          {
            title: '操作',
            dataIndex: 'action',
            align:"center",
            fixed:"right",
            width:147,
            scopedSlots: { customRender: 'action' }
          }
        ],
        url: {
          list: "/biz/bizTransferAcct/list",
          delete: "/biz/bizTransferAcct/delete",
          deleteBatch: "/biz/bizTransferAcct/deleteBatch",
          exportXlsUrl: "/biz/bizTransferAcct/exportXls",
          importExcelUrl: "biz/bizTransferAcct/importExcel",
          
        },
        dictOptions:{},
        superFieldList:[],
      }
    },
    created() {
      this.getSuperFieldList();
    },
    computed: {
      importExcelUrl: function(){
        return `${window._CONFIG['domianURL']}/${this.url.importExcelUrl}`;
      },
    },
    methods: {
      initDictConfig(){
      },
      getSuperFieldList(){
        let fieldList=[];
        fieldList.push({type:'int',value:'yearCode',text:'所属财年',dictCode:"biz_fiscal_year,year_name,year_code"})
        fieldList.push({type:'string',value:'payType',text:'转账类型',dictCode:'transfer_type'})
        fieldList.push({type:'sel_search',value:'payerId',text:'付款人',dictTable:"sys_user", dictText:'realname', dictCode:'username'})
        fieldList.push({type:'sel_search',value:'payeeId',text:'收款人',dictTable:"sys_user", dictText:'realname', dictCode:'username'})
        fieldList.push({type:'string',value:'totalAmount',text:'总金额',dictCode:''})
        fieldList.push({type:'string',value:'principal',text:'本金',dictCode:''})
        fieldList.push({type:'string',value:'income',text:'收入',dictCode:''})
        fieldList.push({type:'string',value:'taxType',text:'征税类型',dictCode:'tax_type'})
        fieldList.push({type:'string',value:'taxRate',text:'适用税率',dictCode:''})
        fieldList.push({type:'string',value:'taxAmount',text:'税额',dictCode:''})
        fieldList.push({type:'string',value:'remark',text:'备注',dictCode:''})
        this.superFieldList = fieldList
      }
    }
  }
</script>
<style scoped>
  @import '~@assets/less/common.less';
</style>