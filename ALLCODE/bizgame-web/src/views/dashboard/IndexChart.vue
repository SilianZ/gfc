<template>
  <div class="page-header-index-wide">
    <a-row :gutter="24">
      <a-col :sm="8" :md="8" :xl="8" :style="{ marginBottom: '24px' }">
        <chart-card :loading="loading" title="A国" :total="aBalance">
          <a-tooltip title="A国公共储蓄进度" slot="action">
            <a-icon type="info-circle-o" />
          </a-tooltip>
          <div>
            <mini-progress color="rgb(19, 194, 194)" :target="1" :percentage="aBalanceRate" :height="8" />
          </div>
          <template slot="footer">团队数量  <span>{{ aTeams }}</span></template>
        </chart-card>
      </a-col>
      <a-col :sm="8" :md="8" :xl="8" :style="{ marginBottom: '24px' }">
        <chart-card :loading="loading" title="B国" :total="bBalance">
          <a-tooltip title="B国公共储蓄进度" slot="action">
            <a-icon type="info-circle-o"/>
          </a-tooltip>
          <div>
            <mini-progress color="rgb(19, 194, 194)" :target="1" :percentage="cBalanceRate" :height="8" />
          </div>
          <template slot="footer">团队数量  <span>{{ bTeams }}</span></template>
        </chart-card>
      </a-col>
      <a-col :sm="8" :md="8" :xl="8" :style="{ marginBottom: '24px' }">
        <chart-card :loading="loading" title="C国" :total="cBalance">
          <a-tooltip title="C国公共储蓄进度" slot="action">
            <a-icon type="info-circle-o"/>
          </a-tooltip>
          <div>
            <mini-progress color="rgb(19, 194, 194)" :target="1" :percentage="cBalanceRate" :height="8" />
          </div>
          <template slot="footer">团队数量  <span>{{ cTeams }}</span></template>
        </chart-card>
      </a-col>
    </a-row>

    <a-row :gutter="24">
      <a-col :sm="8" :md="8" :xl="8" :style="{ marginBottom: '5px' }">
        <a-card title="团队资源" style="height: 625px">
          <a-table
            ref="table1"
            size="middle"
            :scroll="{ y: 475 }"
            rowKey="id"
            :showHeader="false"
            :columns="table1.columns"
            :dataSource="table1.dataSource"
            :pagination="table1.pagination"
            :loading="table1.loading"
            class="j-table-force-nowrap"
            @change="handlePage1Change"
          />
        </a-card>
      </a-col>
      <a-col :sm="8" :md="8" :xl="8" :style="{ marginBottom: '5px' }">
        <a-card title="团队资源" style="height: 625px">
          <a-table
            ref="table2"
            size="middle"
            :scroll="{ y: 475 }"
            rowKey="id"
            :showHeader="false"
            :columns="table2.columns"
            :dataSource="table2.dataSource"
            :pagination="table2.pagination"
            :loading="table2.loading"
            class="j-table-force-nowrap"
            @change="handlePage2Change"
          />
        </a-card>
      </a-col>
      <a-col :sm="8" :md="8" :xl="8" :style="{ marginBottom: '5px' }">
        <a-card title="团队资源" style="height: 625px">
          <a-table
            ref="table3"
            size="middle"
            :scroll="{ y: 475 }"
            rowKey="id"
            :showHeader="false"
            :columns="table3.columns"
            :dataSource="table3.dataSource"
            :pagination="table3.pagination"
            :loading="table3.loading"
            class="j-table-force-nowrap"
            @change="handlePage3Change"
          />
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script>
import ChartCard from '@/components/ChartCard'
import ACol from "ant-design-vue/es/grid/Col"
import ATooltip from "ant-design-vue/es/tooltip/Tooltip"
import MiniArea from '@/components/chart/MiniArea'
import MiniBar from '@/components/chart/MiniBar'
import MiniProgress from '@/components/chart/MiniProgress'
import RankList from '@/components/chart/RankList'
import Bar from '@/components/chart/Bar'
import LineChartMultid from '@/components/chart/LineChartMultid'
import HeadInfo from '@/components/tools/HeadInfo.vue'

import Trend from '@/components/Trend'
import {getLoginfo, getVisitInfo} from '@/api/api'
import {getAction} from "@api/manage";

export default {
  name: "IndexChart",
  components: {
    ATooltip,
    ACol,
    ChartCard,
    MiniArea,
    MiniBar,
    MiniProgress,
    RankList,
    Bar,
    Trend,
    LineChartMultid,
    HeadInfo
  },
  data() {
    return {
      loading: true,
      center: null,
      visitFields: ['ip', 'visit'],
      visitInfo: [],
      aTeams: "0",
      bTeams: "0",
      cTeams: "0",
      aBalance: "0",
      aBalanceRate: "0",
      bBalance: "0",
      bBalanceRate: "0",
      cBalance: "0",
      cBalanceRate: "0",
      indicator: <a-icon type="loading" style="font-size: 24px" spin/>,

      table1: {
        loading: false,
        pagination: {
          current: 1,
          pageSize: 10,
          pageSizeOptions: ['10', '20', '30', '50'],
          total: 0,
        },
        dataSource: [],
        columns: [
          {dataIndex: 'username_dictText', title: '团队名称'},
          {dataIndex: 'resourceName', title: '资源名称'},
          {dataIndex: 'status_dictText', title: '状态'}
        ],
      },
      table2: {
        loading: false,
        pagination: {
          current: 1,
          pageSize: 10,
          pageSizeOptions: ['10', '20', '30', '50'],
          total: 0,
        },
        dataSource: [],
        columns: [
          {dataIndex: 'username_dictText', title: '团队名称'},
          {dataIndex: 'resourceName', title: '资源名称'},
          {dataIndex: 'status_dictText', title: '状态'}
        ],
      },
      table3: {
        loading: false,
        pagination: {
          current: 1,
          pageSize: 10,
          pageSizeOptions: ['10', '20', '30', '50'],
          total: 0,
        },
        dataSource: [],
        columns: [
          {dataIndex: 'username_dictText', title: '团队名称'},
          {dataIndex: 'resourceName', title: '资源名称'},
          {dataIndex: 'status_dictText', title: '状态'}
        ],
      }
    }
  },
  created() {
    setTimeout(() => {
      this.loading = !this.loading
    }, 1000)
    this.initLogInfo();
    this.queryTable1();
    this.queryTable2();
    this.queryTable3();
    //获取参数队伍数量getTeamNumber
    let params = {};
    getAction("/sys/user/getTeamNumber", params).then((res) => {
      if (res.success) {
        this.aTeams = res.result.A国;
        this.bTeams = res.result.B国;
        this.cTeams = res.result.C国;
      } else {
        this.$message.warning(res.message)
      }
    }).finally(() => {
      this.table1.loading = false
    });
    getAction("/sys/user/getBankBalance", params).then((res) => {
      if (res.success) {
        this.aBalance = res.result.A国 == 0 ? 0.00 : "￥" + res.result.A国 + "亿";
        this.aBalanceRate = res.result.A国 == 0 ? 0.00 : res.result.A国/60 * 100;
        this.bBalance = res.result.B国 == 0 ? 0.00 : "￥" + res.result.B国 + "亿";
        this.bBalanceRate = res.result.B国 == 0 ? 0.00 : res.result.B国/60 * 100;
        this.cBalance = res.result.C国 == 0 ? 0.00 : "￥" + res.result.C国 + "亿";
        this.cBalanceRate = res.result.C国 == 0 ? 0.00 : res.result.C国/60 * 100;
      } else {
        this.$message.warning(res.message)
      }
    }).finally(() => {
      this.table1.loading = false
    });
  },
  methods: {
    initLogInfo() {
      getLoginfo(null).then((res) => {
        if (res.success) {
          Object.keys(res.result).forEach(key => {
            res.result[key] = res.result[key] + ""
          })
          this.loginfo = res.result;
        }
      })
      getVisitInfo().then(res => {
        if (res.success) {
          this.visitInfo = res.result;
        }
      })
    },
    // 当分页参数变化时触发的事件
    handlePage1Change(event) {
      // 重新赋值
      this.table1.pagination.current = event.current
      this.table1.pagination.pageSize = event.pageSize
      // 查询数据
      this.queryTable1()
    },
    // 当分页参数变化时触发的事件
    handlePage2Change(event) {
      // 重新赋值
      this.table2.pagination.current = event.current
      this.table2.pagination.pageSize = event.pageSize
      // 查询数据
      this.queryTable2()
    },
    // 当分页参数变化时触发的事件
    handlePage3Change(event) {
      // 重新赋值
      this.table2.pagination.current = event.current
      this.table2.pagination.pageSize = event.pageSize
      // 查询数据
      this.queryTable3()
    },
    queryTable1() {
      var params = {
        deptId: '1705269193837809666',
        pageNo: this.table1.pagination.current,
        pageSize: this.table1.pagination.pageSize
      }
      this.table1.loading = true;
      getAction("/biz/vCountryResource/list", params).then((res) => {
        if (res.success) {
          this.table1.dataSource = res.result.records || res.result;
          if (res.result.total) {
            this.table1.pagination.total = res.result.total;
          } else {
            this.table1.pagination.total = 0;
          }
        } else {
          this.$message.warning(res.message)
        }
      }).finally(() => {
        this.table1.loading = false
      })
    },
    queryTable2() {
      var params = {
        deptId: '1705269225303478273',
        pageNo: this.table2.pagination.current,
        pageSize: this.table2.pagination.pageSize
      }
      this.table2.loading = true;
      getAction("/biz/vCountryResource/list", params).then((res) => {
        if (res.success) {
          this.table2.dataSource = res.result.records || res.result;
          if (res.result.total) {
            this.table2.pagination.total = res.result.total;
          } else {
            this.table2.pagination.total = 0;
          }
        } else {
          this.$message.warning(res.message)
        }
      }).finally(() => {
        this.table2.loading = false
      })
    },
    queryTable3() {
      var params = {
        deptId: '1705269249806602241',
        pageNo: this.table2.pagination.current,
        pageSize: this.table2.pagination.pageSize
      }
      this.table2.loading = true;
      getAction("/biz/vCountryResource/list", params).then((res) => {
        if (res.success) {
          this.table2.dataSource = res.result.records || res.result;
          if (res.result.total) {
            this.table2.pagination.total = res.result.total;
          } else {
            this.table2.pagination.total = 0;
          }
        } else {
          this.$message.warning(res.message)
        }
      }).finally(() => {
        this.table2.loading = false
      })
    }
  }
}
</script>

<style lang="less" scoped>
.circle-cust {
  position: relative;
  top: 28px;
  left: -100%;
}

.extra-wrapper {
  line-height: 55px;
  padding-right: 24px;

  .extra-item {
    display: inline-block;
    margin-right: 24px;

    a {
      margin-left: 24px;
    }
  }
}

/* 首页访问量统计 */
.head-info {
  position: relative;
  text-align: left;
  padding: 0 32px 0 0;
  min-width: 125px;

  &.center {
    text-align: center;
    padding: 0 32px;
  }

  span {
    color: rgba(0, 0, 0, .45);
    display: inline-block;
    font-size: .95rem;
    line-height: 42px;
    margin-bottom: 4px;
  }

  p {
    line-height: 42px;
    margin: 0;

    a {
      font-weight: 600;
      font-size: 1rem;
    }
  }
}
</style>