<template>
  <div>
    <el-table
      v-loading="result.loading"
      :data="tableData"
      height="700"
      border
      stripe
      ref=“table”
      style="width: 100%">
      <el-table-column
        align="center"
        prop="department"
        sortable
        :filters="orgList"
        :filter-method="filterHandler"
        label="部门">
      </el-table-column>
      <el-table-column
        align="center"
        prop="team"
        sortable
        label="团队">
      </el-table-column>
      <el-table-column
        align="center"
        prop="projectName"
        sortable
        label="项目">
      </el-table-column>
      <el-table-column
        align="center"
        prop="name"
        sortable
        label="测试计划">
      </el-table-column>
      <el-table-column
        align="center"
        prop="executionTimes"
        label="执行次数">
      </el-table-column>
      <el-table-column
        align="center"
        prop="passRate"
        label="用例最后通过率">
      </el-table-column>
            <el-table-column
        align="center"
        prop="total"
        label="测试计划用例总数">
      </el-table-column>
            <el-table-column
        align="center"
        prop="passed"
        label="执行通过用例总数">
      </el-table-column>
    </el-table>
  </div>
</template>

<script>

import MsMainContainer from "@/business/components/common/components/MsMainContainer";
import MsContainer from "@/business/components/common/components/MsContainer";

require('echarts/lib/component/legend');


export default {
  name: "KanbanExeSummary",
  components: {
    MsMainContainer,
    MsContainer,
  },
  data() {
    return {
      result:{},
      tableData: null,
      orgList: null,
      wsList: null,
      tableColumns:null
    };
  },
  created() {
    this.getSummary();
    this.$nextTick(() => {
      this.$refs['table'].doLayout();
    }) 
  },
  updated () {
    this.$nextTick(() => {
      this.$refs['table'].doLayout();
    }) 
  },
  activated() {
    this.init();
  },
  computed: {

  },
  methods: {
    init() {

    },
    getSummary(){
      const _this = this;
      this.result = this.$get("/tuhu/kanban/exeSummary", response => {
        _this.tableData = response.data;
        console.info(_this.tableData);
        console.table(_this.tableData)
        _this.orgList = new Array();
        for(var i = 0, len = _this.tableData.length; i < len; i++){
          var departName = _this.tableData[i].department
          if(!this.hasFilter(_this.orgList, departName))
          {
            _this.orgList.push({text: departName, value: departName});
          }
        }
      });
    },
    filterTag(value, row) {
        return row.tag === value;
    },
    filterHandler(value, row, column) {
      const property = row['department'];
      return property === value;
    },
    hasFilter(orgList, orgName) {
      for(var i = 0, len = orgList.length; i < len; i++){
        if(orgName == orgList[i].text)
          return true;
      }
      return false;
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
