<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="box-card card-fix">
          <PieChart ref="pieChart1" id="pieChart1" title="服务接入现状" :data="serviceInData" style="height: 400px;"/>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="box-card card-fix">
          <PieChart ref="pieChart2" id="pieChart2" title="团队接入现状" :data="teamInData" style="height: 400px;"/>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="box-card card-fix">
          <PieChart ref="pieChart3" id="pieChart3" title="API完成率" :data="apiDoneData" style="height: 400px;"/>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="box-card card-fix">
          <PieChart ref="pieChart4" id="pieChart4" title="执行成功率" :data="apiPassRateData" style="height: 400px;"/>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="box-card card-fix">
          <PieChart ref="pieChart5" id="pieChart5" title="API覆盖率" :data="apiCovRateData" style="height: 400px;"/>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="box-card card-fix">
          <PieChart ref="pieChart6" id="pieChart6" title="场景覆盖率" :data="scenCovRateData" style="height: 400px;"/>
        </el-card>
      </el-col>
    </el-row>

  <el-row :gutter="20">
    <el-col :span="24">
      <el-card class="box-card card-fix">
        <BarChart ref="barChart1" id="barChart1" title="近7日失败用例Top 20" :data="caseFailData" style="height: 500px;"/>
      </el-card>
    </el-col>
  </el-row>
  </div>
</template>

<script>

import MsMainContainer from "@/business/components/common/components/MsMainContainer";
import MsContainer from "@/business/components/common/components/MsContainer";
import PieChart from './components/PieChart.vue';
import BarChart from './components/BarChart.vue';

export default {
  name: "KanbanGraph",
  components: {
    MsMainContainer,
    MsContainer,
    PieChart,
    BarChart
  },
  data() {
    return {
      serviceInData: [
        {value: 0, name: '已接入服务数量: 0'},
        {value: 0, name: '未接入服务数量: 0'}
      ],
      teamInData: [
        {value: 0, name: '已接入团队数量: 0'},
        {value: 0, name: '未接入团队数量: 0'}
      ],
      apiDoneData: [
        {value: 0, name: '已完成接口数量: 0'},
        {value: 0, name: '未完成接口数量: 0'}
      ],
      apiPassRateData: [
        {value: 0, name: '定时任务执行成功次数: 0'},
        {value: 0, name: '定时任务执行失败次数: 0'}
      ],
      apiCovRateData: [
        {value: 0, name: '已有用例覆盖的接口数量: 0'},
        {value: 0, name: '未有用例覆盖的接口数量: 0'}
      ],
      scenCovRateData: [
        {value: 0, name: '已有场景覆盖的接口数量: 0'},
        {value: 0, name: '未有场景覆盖的接口数量: 0'}
      ],
      caseFailData: {
        x: [0, 0, 0, 0, 0, 0, 0, 0, 0, 0],
        y: ['case1', 'case2', 'case3', 'case4', 'case5', 
          'case6', 'case7', 'case8', 'case9', 'case10']
      }
    };
  },
  computed: {

  },
  created() {
    this.getGraphData();
    this.$nextTick(() => {
      // DOM 操作延迟执行代码
    })
  },
  mounted () {
  },
  updated () {
    this.$nextTick(() => {
      // 数据更新后 DOM 操作延迟执行代码
    }) 
  },
  activated() {
  },
  methods: {
    getGraphData(){
      this.result = this.$get("/tuhu/kanban/graph", response => {
        if (response.success) {
          this.$refs.pieChart1.updateChart(response.data.serviceInData);
          this.$refs.pieChart2.updateChart(response.data.teamInData);
          this.$refs.pieChart3.updateChart(response.data.apiDoneData);
          this.$refs.pieChart4.updateChart(response.data.apiPassRateData);
          this.$refs.pieChart5.updateChart(response.data.apiCovRateData);
          this.$refs.pieChart6.updateChart(response.data.scenCovRateData);
          this.$refs.barChart1.updateChart(response.data.caseFailData);
        }
      });
    }
  }
}
</script>

<style scoped>
.card-fix {
  height: 100%; 
  margin: 5px 0;
}
</style>
