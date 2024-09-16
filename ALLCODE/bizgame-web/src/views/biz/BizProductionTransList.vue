<!--成品交易页面-->
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
      <a-button type="primary" icon="download" @click="handleExportXls('成品交易')">导出</a-button>
      <a-upload name="file" :showUploadList="false" :multiple="false" :headers="tokenHeader" :action="importExcelUrl" @change="handleImportExcel">
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

    <biz-production-trans-modal ref="modalForm" @ok="modalFormOk"></biz-production-trans-modal>
  </a-card>
</template>

<script>

  import '@/assets/less/TableExpand.less'
  import { mixinDevice } from '@/utils/mixin'
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  import BizProductionTransModal from './modules/BizProductionTransModal'
  import {filterMultiDictText} from '@/components/dict/JDictSelectUtil'

  export default {
    name: 'BizProductionTransList',
    mixins:[JeecgListMixin, mixinDevice],
    components: {
      BizProductionTransModal
    },
    data () {
      return {
        description: '成品交易管理页面',
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
            title:'卖方',
            align:"center",
            dataIndex: 'sellerId_dictText'
          },
          {
            title:'买方',
            align:"center",
            dataIndex: 'buyerId_dictText'
          },
          {
            title:'交易价格',
            align:"center",
            dataIndex: 'transPrice'
          },
          {
            title:'税率',
            align:"center",
            dataIndex: 'taxRate'
          },
          {
            title:'税额',
            align:"center",
            dataIndex: 'transTax'
          },
          {
            title:'芯片数量',
            align:"center",
            dataIndex: 'chipNumber'
          },
          {
            title:'汽车数量',
            align:"center",
            dataIndex: 'cardNumber'
          },
          {
            title:'新能源数量',
            align:"center",
            dataIndex: 'energyNumber'
          },
          {
            title:'玩具数量',
            align:"center",
            dataIndex: 'toyNumber'
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
          list: "/biz/bizProductionTrans/list",
          delete: "/biz/bizProductionTrans/delete",
          deleteBatch: "/biz/bizProductionTrans/deleteBatch",
          exportXlsUrl: "/biz/bizProductionTrans/exportXls",
          importExcelUrl: "biz/bizProductionTrans/importExcel",
          
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
        fieldList.push({type:'sel_search',value:'sellerId',text:'卖方',dictTable:"sys_user", dictText:'realname', dictCode:'username'})
        fieldList.push({type:'sel_search',value:'buyerId',text:'买方',dictTable:"sys_user", dictText:'realname', dictCode:'username'})
        fieldList.push({type:'string',value:'isTransnational',text:'是否跨国交易',dictCode:'yes_or_no'})
        fieldList.push({type:'double',value:'transPrice',text:'交易价格',dictCode:''})
        fieldList.push({type:'double',value:'taxRate',text:'税率',dictCode:''})
        fieldList.push({type:'double',value:'transTax',text:'税额',dictCode:''})
        fieldList.push({type:'double',value:'chipNumber',text:'芯片数量',dictCode:''})
        fieldList.push({type:'double',value:'cardNumber',text:'汽车数量',dictCode:''})
        fieldList.push({type:'double',value:'energyNumber',text:'新能源数量',dictCode:''})
        fieldList.push({type:'double',value:'toyNumber',text:'玩具数量',dictCode:''})
        fieldList.push({type:'string',value:'remark',text:'备注',dictCode:''})
        this.superFieldList = fieldList
      }
    }
  }
</script>
<style scoped>
  @import '~@assets/less/common.less';
</style>