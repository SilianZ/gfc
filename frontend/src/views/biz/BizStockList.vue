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
      <a-button type="primary" @click="searchReset" icon="reload" style="margin-left: 8px">刷新</a-button>
    </div>

    <!-- 多行折线图 -->
    <a-tabs defaultActiveKey="1">
      <a-tab-pane tab="股票投资" key="6">
        <line-chart-multid ref="lineChart" title="股票投资" :height="height"/>
      </a-tab-pane>
    </a-tabs>
    
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
          <a-popconfirm title="确定取款吗?" @confirm="() => handleDelete(record.id)">
            <a>取款</a>
          </a-popconfirm>
        </span>
      </a-table>
    </div>

    <biz-stock-modal ref="modalForm" @ok="modalFormOk"></biz-stock-modal>
  </a-card>
</template>

<script>
import '@/assets/less/TableExpand.less'
import {mixinDevice} from '@/utils/mixin'
import {JeecgListMixin} from '@/mixins/JeecgListMixin'
import BizStockModal from './modules/BizStockModal'
import {filterMultiDictText} from '@/components/dict/JDictSelectUtil'
import LineChartMultid from './BizStockLineChart.vue'
import {httpAction, getAction} from '@/api/manage'

export default {
  name: 'BizStockList',
  mixins: [JeecgListMixin, mixinDevice],
  components: {
    BizStockModal, LineChartMultid
  },
  data() {
    return {
      description: '股票投资页面',
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
          dataIndex: 'sellerId_dictText'
        },
        {
          title: '存入资金',
          align: "center",
          dataIndex: 'transPrice'
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
        list: "/biz/bizStock/list",
        delete: "/biz/bizStock/delete",
      },
      dictOptions: {},
      superFieldList: [],
    }
  },
  created() {
    this.getSuperFieldList();
  },
  computed: {
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
    },
    fetchChartData() {
      const httpurl = '/biz/bizFiscalYear/getYearNameAndStockInc';  // 后端提供的获取数据的 URL
      const method = 'get';  // 使用 GET 方法请求数据
      var params = {}
      // 使用封装好的 httpAction 方法发送请求
      getAction(httpurl, params).then((res) => {
        if (res.success) {
          console.log("This is the data for line chart fetch from the backend");
          console.log(res);


          const chartData = res.result.map(item => ({
            type: item.type,  // 后端返回的字段是 type
            Percentage: item.Percentage
          }));
          console.log("This is the const chartData");
          console.log(chartData);
          if (this.$refs.lineChart) {
            this.$refs.lineChart.updateChartData(chartData);
          } else {
            console.error('LineChart component is not yet loaded');
          }  // 更新图表数据
        } else {
          this.$message.warning('图表数据加载失败: ' + res.message);
        }
      }).catch(error => {
        console.error('Error fetching chart data:', error);
        this.$message.error('图表数据加载失败');
      }).finally(() => {
        // 可以在这里处理加载状态的结束，如果你有相应的状态管理
      });
    },
    searchReset() {
      // 重置查询条件
      // 重新加载图表数据
      this.fetchChartData();
    }
  }
}
</script>
<style scoped>
@import '~@assets/less/common.less';
</style>