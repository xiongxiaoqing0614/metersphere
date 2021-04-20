<template>
  <div>
    <el-table
      :data="tableData"
      border
      show-summary
      :default-sort = "{prop: 'department', order: 'descending'}"
      style="width: 100%">
      <el-table-column
        prop="department"
        sortable
        label="部门">
      </el-table-column>
      <el-table-column
        prop="team"
        sortable
        label="小组">
      </el-table-column>
      <el-table-column
        prop="project"
        sortable
        label="项目">
      </el-table-column>
      <el-table-column
        prop="apiCount"
        sortable
        label="接口数量">
      </el-table-column>
      <el-table-column
        prop="singleCount"
        sortable
        label="单接口用例数量">
      </el-table-column>
      <el-table-column
        prop="scenarioCount"
        sortable
        label="场景用例数量">
      </el-table-column>
    </el-table>
  </div>
</template>


<script>

import MsMainContainer from "@/business/components/common/components/MsMainContainer";
import MsContainer from "@/business/components/common/components/MsContainer";
import {COUNT_NUMBER, COUNT_NUMBER_SHALLOW} from "@/common/js/constants";

require('echarts/lib/component/legend');


export default {
  name: "KanbanHome",
  components: {
    MsMainContainer,
    MsContainer,
  },
  data() {
    return {
      tableData: null
    };
  },
    created() {
      const _this = this
      this.$get("/kanban/summary", response => {
        _this.tableData = response.data;
      });
  },
  activated() {
    this.init();
  },
  computed: {
    projectId() {
      return this.$store.state.projectId
    },
  },
  methods: {
    checkTipsType() {
      let random = Math.floor(Math.random() * (4 - 1 + 1)) + 1;
      this.tipsType = random + "";
    },
    init() {
      let selectProjectId = this.projectId;
      if (!selectProjectId) {
        return;
      }
      this.$get("/kanban/summary", response => {
        this.summaryData = response.data;
        console.table(this.summaryData)
      });

    },
    setBarOption(data) {
      let xAxis = [];
      data.map(d => {
        if (!xAxis.includes(d.xAxis)) {
          xAxis.push(d.xAxis);
        }
      });
      let yAxis1 = data.filter(d => d.groupName === 'FUNCTIONCASE').map(d => [d.xAxis, d.yAxis]);
      let yAxis2 = data.filter(d => d.groupName === 'RELEVANCECASE').map(d => [d.xAxis, d.yAxis]);
      let option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        xAxis: {
          type: 'category',
          data: xAxis
        },
        yAxis: {
          type: 'value',
          axisLine: {
            show: false
          },
          axisTick: {
            show: false
          }
        },
        legend: {
          data: ["功能用例数", "关联用例数"],
          orient: 'vertical',
          right: '80',
        },
        series: [{
          name: "功能用例数",
          data: yAxis1,
          type: 'bar',
          itemStyle: {
            normal: {
              color: this.$store.state.theme ? this.$store.state.theme : COUNT_NUMBER
            }
          }
        },
          {
            name: "关联用例数",
            data: yAxis2,
            type: 'bar',
            itemStyle: {
              normal: {
                color: this.$store.state.theme ? this.$store.state.theme : COUNT_NUMBER_SHALLOW
              }
            }
          }]
      };
      this.caseOption = option;
    },
    redirectPage(page, dataType, selectType) {
      //test_plan 页面跳转
      // this.$router.push('/track/plan/view/'+selectType);
      switch (page) {
        case "case":
          this.$router.push({
            name:'testCase',
            params:{
              dataType:dataType,dataSelectRange:selectType, projectId: this.projectId
            }
          });
          break;
      }
    },
  }
}
</script>

<style scoped>
.square {
  width: 100%;
  height: 400px;
}

.rectangle {
  width: 100%;
  height: 400px;
}

.el-row {
  margin-bottom: 20px;
  margin-left: 20px;
  margin-right: 20px;
}

.track-card {
  height: 100%;
}
</style>
