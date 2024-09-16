<template>
  <a-card :bordered="false">
    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">
          <a-col :xl="6" :lg="7" :md="8" :sm="24">
            <a-form-item label="所属团队">
              <j-search-select-tag v-model="queryParam.userId" dict="sys_user,realname,username,post='TEM'"/>
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
      <a-button type="primary" icon="download" @click="handleExportXls('团队账户')">导出</a-button>
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
          <a @click="handleEdit(record)">编辑</a>
          <a-divider type="vertical" />
          <a @click="handleDetail(record)">详情</a>
        </span>

      </a-table>
    </div>

    <biz-team-balance-modal ref="modalForm" @ok="modalFormOk"></biz-team-balance-modal>
  </a-card>
</template>

<script>

  import '@/assets/less/TableExpand.less'
  import { mixinDevice } from '@/utils/mixin'
  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  import BizTeamBalanceModal from './modules/BizTeamBalanceModal'

  export default {
    name: 'BizTeamBalanceList',
    mixins:[JeecgListMixin, mixinDevice],
    components: {
      BizTeamBalanceModal
    },
    data () {
      return {
        description: '团队账户管理页面',
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
            title:'所属主体',
            align:"center",
            sorter: true,
            dataIndex: 'userId_dictText'
          },
          {
            title:'现金账户',
            align:"center",
            sorter: true,
            dataIndex: 'cashAcct'
          },
          {
            title:'债权账户',
            align:"center",
            sorter: true,
            dataIndex: 'debtClaimAcct'
          },
          {
            title:'债务账户',
            align:"center",
            sorter: true,
            dataIndex: 'debtLiabilityAcct'
          },
          {
            title:'钢铁库存',
            align:"center",
            sorter: true,
            dataIndex: 'steelAcct'
          },
          {
            title:'硅石库存',
            align:"center",
            sorter: true,
            dataIndex: 'silicaAcct'
          },
          {
            title:'石油库存',
            align:"center",
            sorter: true,
            dataIndex: 'crudeAcct'
          },
          {
            title:'塑料库存',
            align:"center",
            sorter: true,
            dataIndex: 'plasticsAcct'
          },
          {
            title:'芯片库存',
            align:"center",
            sorter: true,
            dataIndex: 'chipAcct'
          },
          {
            title:'汽车库存',
            align:"center",
            sorter: true,
            dataIndex: 'cardAcct'
          },
          {
            title:'新能源库存',
            align:"center",
            sorter: true,
            dataIndex: 'energyAcct'
          },
          {
            title:'玩具库存',
            align:"center",
            sorter: true,
            dataIndex: 'toyAcct'
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
          list: "/biz/bizSubjectBalance/list",
          delete: "/biz/bizSubjectBalance/delete",
          deleteBatch: "/biz/bizSubjectBalance/deleteBatch",
          exportXlsUrl: "/biz/bizSubjectBalance/exportXls",
          importExcelUrl: "biz/bizSubjectBalance/importExcel",
          
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
        fieldList.push({type:'sel_user',value:'userId',text:'所属主体'})
        fieldList.push({type:'double',value:'cashAcct',text:'现金账户',dictCode:''})
        fieldList.push({type:'double',value:'debtClaimAcct',text:'债权账户',dictCode:''})
        fieldList.push({type:'double',value:'debtLiabilityAcct',text:'债务账户',dictCode:''})
        fieldList.push({type:'double',value:'steelAcct',text:'钢铁库存',dictCode:''})
        fieldList.push({type:'double',value:'silicaAcct',text:'硅石库存',dictCode:''})
        fieldList.push({type:'double',value:'crudeAcct',text:'石油库存',dictCode:''})
        fieldList.push({type:'double',value:'plasticsAcct',text:'塑料库存',dictCode:''})
        fieldList.push({type:'double',value:'chipAcct',text:'芯片库存',dictCode:''})
        fieldList.push({type:'double',value:'cardAcct',text:'汽车库存',dictCode:''})
        fieldList.push({type:'double',value:'energyAcct',text:'新能源库存',dictCode:''})
        fieldList.push({type:'double',value:'toyAcct',text:'玩具库存',dictCode:''})
        this.superFieldList = fieldList
      }
    }
  }
</script>
<style scoped>
  @import '~@assets/less/common.less';
</style>