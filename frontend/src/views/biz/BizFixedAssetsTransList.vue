<template>
  <a-card :bordered="false">
    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="交易类型">
              <j-dict-select-tag placeholder="请选择交易类型" v-model="queryParam.transType" dictCode="trans_type"/>
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="卖方">
              <j-select-user-by-dep placeholder="请选择卖方" v-model="queryParam.sellerId"/>
            </a-form-item>
          </a-col>
          <template v-if="toggleSearchStatus">
            <a-col :xl="6" :lg="7" :md="8" :sm="24">
              <a-form-item label="买方">
                <j-select-user-by-dep placeholder="请选择买方" v-model="queryParam.buyerId"/>
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
      <a-button type="primary" icon="download" @click="handleExportXls('固定资产交易')">导出</a-button>
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

    <biz-fixed-assets-trans-modal ref="modalForm" @ok="modalFormOk"></biz-fixed-assets-trans-modal>
  </a-card>
</template>

<script>

  import '@/assets/less/TableExpand.less'
  import { mixinDevice } from '@/utils/mixin'
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  import BizFixedAssetsTransModal from './modules/BizFixedAssetsTransModal'
  import {filterMultiDictText} from '@/components/dict/JDictSelectUtil'

  export default {
    name: 'BizFixedAssetsTransList',
    mixins:[JeecgListMixin, mixinDevice],
    components: {
      BizFixedAssetsTransModal
    },
    data () {
      return {
        description: '固定资产交易管理页面',
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
            title:'交易类型',
            align:"center",
            dataIndex: 'transType_dictText'
          },
          {
            title:'交易价格',
            align:"center",
            dataIndex: 'transPrice'
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
            title:'交易标的',
            align:"center",
            dataIndex: 'transObject_dictText'
          },
          {
            title:'所有权/使用权',
            align:"center",
            dataIndex: 'rightType_dictText'
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
          list: "/biz/bizFixedAssetsTrans/list",
          delete: "/biz/bizFixedAssetsTrans/delete",
          deleteBatch: "/biz/bizFixedAssetsTrans/deleteBatch",
          exportXlsUrl: "/biz/bizFixedAssetsTrans/exportXls",
          importExcelUrl: "biz/bizFixedAssetsTrans/importExcel",
          
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
        fieldList.push({type:'int',value:'yearCode',text:'财年编号',dictCode:''})
        fieldList.push({type:'string',value:'transType',text:'交易类型',dictCode:'trans_type'})
        fieldList.push({type:'double',value:'transPrice',text:'交易价格',dictCode:''})
        fieldList.push({type:'sel_user',value:'sellerId',text:'卖方'})
        fieldList.push({type:'sel_user',value:'buyerId',text:'买方'})
        fieldList.push({type:'string',value:'transObject',text:'交易标的',dictCode:'resource_type'})
        fieldList.push({type:'string',value:'rightType',text:'所有权/使用权',dictCode:'right_type'})
        fieldList.push({type:'int',value:'yearLimit',text:'使用年限',dictCode:''})
        fieldList.push({type:'string',value:'remark',text:'交易附言',dictCode:''})
        this.superFieldList = fieldList
      }
    }
  }
</script>
<style scoped>
  @import '~@assets/less/common.less';
</style>