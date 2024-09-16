<template>
  <a-card :bordered="false">
    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="状态">
              <j-dict-select-tag placeholder="请选择状态" v-model="queryParam.status" dictCode="fiscal_year_status"/>
            </a-form-item>
          </a-col>
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <span style="float: left;overflow: hidden;" class="table-page-search-submitButtons">
              <a-button type="primary" @click="searchQuery" icon="search">查询</a-button>
              <a-button type="primary" @click="searchReset" icon="reload" style="margin-left: 8px">重置</a-button>
            </span>
          </a-col>
        </a-row>
      </a-form>
    </div>
    <!-- 查询区域-END -->

    <!-- 操作按钮区域 -->
    <div class="table-operator">
      <a-button @click="handleAdd" type="primary" icon="plus">新增</a-button>
      <a-button type="primary" icon="download" @click="handleExportXls('财年信息')">导出</a-button>
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
        :rowSelection="{selectedRowKeys: selectedRowKeys, onChange: onSelectChange}"
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
          <a @click="handleEnd(record.id)" v-if="record.status == 1">清算</a>
          <a @click="handleStart(record.id)" v-if="record.status == 0">开始</a>
          <a-divider type="vertical" v-if="record.status == 1"/>
          <a-divider type="vertical" v-if="record.status == 0"/>
          <a-dropdown>
            <a class="ant-dropdown-link">更多 <a-icon type="down" /></a>
            <a-menu slot="overlay">
              <a-menu-item>
                <a @click="handleDetail(record)">详情</a>
              </a-menu-item>
            </a-menu>
          </a-dropdown>
        </span>

      </a-table>
    </div>

    <biz-fiscal-year-modal ref="modalForm" @ok="modalFormOk"></biz-fiscal-year-modal>
  </a-card>
</template>

<script>

  import '@/assets/less/TableExpand.less'
  import { mixinDevice } from '@/utils/mixin'
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  import BizFiscalYearModal from './modules/BizFiscalYearModal'
  import { getAction } from '@/api/manage'

  export default {
    name: 'BizFiscalYearList',
    mixins:[JeecgListMixin, mixinDevice],
    components: {
      BizFiscalYearModal
    },
    data () {
      return {
        description: '财年信息管理页面',
        isorter:{
          column: 'yearCode',
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
            title:'财年名称',
            align:"center",
            dataIndex: 'yearName'
          },
          {
            title:'开始时间',
            align:"center",
            dataIndex: 'startTime'
          },
          {
            title:'结束时间',
            align:"center",
            dataIndex: 'endTime'
          },
          {
            title:'状态',
            align:"center",
            dataIndex: 'status_dictText'
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
          list: "/biz/bizFiscalYear/list",
          delete: "/biz/bizFiscalYear/delete",
          end: "/biz/bizFiscalYear/end",
          start: "/biz/bizFiscalYear/start",
          deleteBatch: "/biz/bizFiscalYear/deleteBatch",
          exportXlsUrl: "/biz/bizFiscalYear/exportXls",
          importExcelUrl: "biz/bizFiscalYear/importExcel",
          
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
      handleEnd: function (id) {
        var that = this;
        this.$confirm({
          title: "确认操作",
          content: "是否对该财年进行清算?",
          onOk: function () {
            that.loading = true;
            getAction(that.url.end, {id: id}).then((res) => {
              if (res.success) {
                //重新计算分页问题
                console.log("SUCCESS End!!!!");
                console.log(res);
                that.reCalculatePage(1)
                that.$message.success(res.message);
                that.loadData();
              } else {
                that.$message.warning(res.message);
              }
            }).finally(() => {
              that.loading = false;
            });
          }
        });
      },
      handleStart: function (id) {
        var that = this;
        this.$confirm({
          title: "确认操作",
          content: "是否开启该财年?",
          onOk: function () {
            that.loading = true;
            getAction(that.url.start, {id: id}).then((res) => {
              if (res.success) {
                //重新计算分页问题
                console.log("SUCCESS Start!!!!!!!!!!!!!!");
                console.log(res);
                that.reCalculatePage(1)
                that.$message.success(res.message);
                that.loadData();
              } else {
                that.$message.warning(res.message);
              }
            }).finally(() => {
              that.loading = false;
            });
          }
        });
      },
      getSuperFieldList(){
        let fieldList=[];
        fieldList.push({type:'string',value:'yearName',text:'财年名称',dictCode:''})
        fieldList.push({type:'datetime',value:'startTime',text:'开始时间'})
        fieldList.push({type:'datetime',value:'endTime',text:'结束时间'})
        fieldList.push({type:'string',value:'status',text:'状态',dictCode:'fiscal_year_status'})
        this.superFieldList = fieldList
      }
    }
  }
</script>
<style scoped>
  @import '~@assets/less/common.less';
</style>