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
      <a-button type="primary" icon="download" @click="handleExportXls('原材料开采计划')">导出</a-button>
    </div>

    <!-- table区域-begin -->
    <div>
      <a-table
        ref="table"
        size="middle"
        :scroll="{x:true}"
        bordered
        rowKey="id"
        :isorter="isorter"
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
          <a @click="handleEdit(record)">编辑</a>
          <a-divider type="vertical" />
           <a @click="handleDetail(record)">详情</a>
        </span>

      </a-table>
    </div>

    <biz-mine-plan-modal ref="modalForm" @ok="modalFormOk"></biz-mine-plan-modal>
  </a-card>
</template>

<script>

  import '@/assets/less/TableExpand.less'
  import { mixinDevice } from '@/utils/mixin'
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  import BizMinePlanModal from './modules/BizMinePlanModal'

  export default {
    name: 'BizMinePlanList',
    mixins:[JeecgListMixin, mixinDevice],
    components: {
      BizMinePlanModal
    },
    data () {
      return {
        description: '原材料开采计划管理页面',
        isorter:{
          column: 'planCode',
          order: 'asc',
        },
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
            title:'计划编号',
            align:"center",
            dataIndex: 'planCode'
          },
          {
            title:'计划名称',
            align:"center",
            dataIndex: 'planName'
          },
          {
            title:'财年限制',
            align:"center",
            dataIndex: 'yearLimit'
          },
          {
            title:'钢铁产量',
            align:"center",
            dataIndex: 'gtProduction'
          },
          {
            title:'硅石产量',
            align:"center",
            dataIndex: 'gsProduction'
          },
          {
            title:'石油产量',
            align:"center",
            dataIndex: 'syProduction'
          },
          {
            title:'塑料产量',
            align:"center",
            dataIndex: 'slProduction'
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
          list: "/biz/bizMinePlan/list",
          delete: "/biz/bizMinePlan/delete",
          deleteBatch: "/biz/bizMinePlan/deleteBatch",
          exportXlsUrl: "/biz/bizMinePlan/exportXls",
          importExcelUrl: "biz/bizMinePlan/importExcel",
          
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
        fieldList.push({type:'string',value:'planCode',text:'计划编号',dictCode:''})
        fieldList.push({type:'string',value:'planName',text:'计划名称',dictCode:''})
        fieldList.push({type:'int',value:'yearLimit',text:'财年限制',dictCode:''})
        fieldList.push({type:'double',value:'gtProduction',text:'钢铁产量',dictCode:''})
        fieldList.push({type:'double',value:'gsProduction',text:'硅石产量',dictCode:''})
        fieldList.push({type:'double',value:'syProduction',text:'石油产量',dictCode:''})
        fieldList.push({type:'double',value:'slProduction',text:'塑料产量',dictCode:''})
        fieldList.push({type:'string',value:'remark',text:'备注',dictCode:''})
        this.superFieldList = fieldList
      }
    }
  }
</script>
<style scoped>
  @import '~@assets/less/common.less';
</style>