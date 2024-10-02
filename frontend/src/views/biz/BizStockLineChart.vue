<template>
    <div :style="{ padding: '0 0 32px 32px' }">
      <h4 :style="{ marginBottom: '20px' }">{{ title }}</h4>
      <v-chart :force-fit="true" :height="height" :data="data" :scale="scale" :onClick="handleClick">
        <v-tooltip/>
        <v-axis/>
        <v-legend/>
        <v-line position="type*y" color="x"/>
        <v-point position="type*y" color="x" :size="4" :v-style="style" :shape="'circle'"/>
      </v-chart>
    </div>
  </template>
  
  <script>
    import { DataSet } from '@antv/data-set'
    import { ChartEventMixins } from '@/mixins/ChartMixin'
  
    export default {
      name: 'LineChartMultid',
      mixins: [ChartEventMixins],
      props: {
        title: {
          type: String,
          default: '股票'
        },
        dataSource: {
          type: Array,
          default: () => []
        },
        fields: {
          type: Array,
          default: () => ['Percentage']
        },
        // 别名，需要的格式：[{field:'name',alias:'姓名'}, {field:'sex',alias:'性别'}]
        aliases:{
          type: Array,
          default: () => []
        },
        height: {
          type: Number,
          default: 254
        }
      },
      data() {
        return {
          scale: [{
            type: 'cat',
            dataKey: 'x',
            min: -1,
            max: 1
          }],
          style: { stroke: '#fff', lineWidth: 1 }
        }
      },
      computed: {
        data() {
          const dv = new DataSet.View().source(this.dataSource)
          dv.transform({
            type: 'fold',
            fields: this.fields,
            key: 'x',
            value: 'y'
          })
          let rows =  dv.rows
          // 替换别名
          rows.forEach(row => {
            for (let item of this.aliases) {
              if (item.field === row.x) {
                row.x = item.alias
                break
              }
            }
          })
          return rows
        }
      },
      methods: {
        updateChartData(newData) {
          this.dataSource = newData;
        }
      }
    }
  </script>
  
  <style scoped>
  
  </style>