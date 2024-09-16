<template>
  <a-card :bordered="false">
    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="国家银行">
              <j-search-select-tag placeholder="请选择国家银行" v-model="queryParam.userId" dict="sys_user,realname,username,post='BNK'"/>
            </a-form-item>
          </a-col>
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
      <a-button type="primary" icon="download" @click="handleExportXls('银行管理')">导出</a-button>
    </div>

    <!-- table区域-begin -->
    <div>

      <a-table
        ref="table"
        size="middle"
        bordered
        rowKey="id"
        class="j-table-force-nowrap"
        :scroll="{x:true}"
        :columns="columns"
        :dataSource="dataSource"
        :pagination="ipagination"
        :loading="loading"
        :rowSelection="{selectedRowKeys: selectedRowKeys, onChange: onSelectChange}"
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
          <a @click="handleEdit(record)">编辑</a>
          <a-divider type="vertical" />
          <a @click="handleDetail(record)">详情</a>
        </span>

      </a-table>
    </div>

    <biz-bank-config-modal ref="modalForm" @ok="modalFormOk"/>
  </a-card>
</template>

<script>

  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  import BizBankConfigModal from './modules/BizBankConfigModal'
  import {filterMultiDictText} from '@/components/dict/JDictSelectUtil'
  import '@/assets/less/TableExpand.less'

  export default {
    name: "BizBankConfigList",
    mixins:[JeecgListMixin],
    components: {
      BizBankConfigModal
    },
    data () {
      return {
        description: '银行管理管理页面',
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
            title:'国家银行',
            align:"center",
            dataIndex: 'userId_dictText'
          },
          {
            title:'所属国家',
            align:"center",
            dataIndex: 'deptId_dictText'
          },
          {
            title:'储蓄率',
            align:"center",
            dataIndex: 'saveRate'
          },
          {
            title:'关税',
            align:"center",
            dataIndex: 'tariffRate'
          },
          {
            title:'是否投资',
            align:"center",
            dataIndex: 'isInvest_dictText'
          },
          {
            title:'投资计划',
            align:"center",
            dataIndex: 'investPlan_dictText'
          },
          {
            title: '操作',
            dataIndex: 'action',
            align:"center",
            fixed:"right",
            width:147,
            scopedSlots: { customRender: 'action' },
          }
        ],
        url: {
          list: "/biz/bizBankConfig/list",
          delete: "/biz/bizBankConfig/delete",
          deleteBatch: "/biz/bizBankConfig/deleteBatch",
          exportXlsUrl: "/biz/bizBankConfig/exportXls",
          importExcelUrl: "biz/bizBankConfig/importExcel",
          
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
      }
    },
    methods: {
      initDictConfig(){
      },
      getSuperFieldList(){
        let fieldList=[];
        fieldList.push({type:'sel_search',value:'userId',text:'国家银行',dictTable:"sys_user", dictText:'realname', dictCode:'username'})
        fieldList.push({type:'sel_depart',value:'deptId',text:'所属国家'})
        fieldList.push({type:'double',value:'saveRate',text:'储蓄率',dictCode:''})
        fieldList.push({type:'double',value:'tariffRate',text:'关税',dictCode:''})
        fieldList.push({type:'string',value:'isInvest',text:'是否投资',dictCode:'yes_or_no'})
        fieldList.push({type:'string',value:'investPlan',text:'投资计划',dictCode:'invest_plan'})
        this.superFieldList = fieldList
      }
    }
  }
</script>
<style scoped>
  @import '~@assets/less/common.less';
</style>