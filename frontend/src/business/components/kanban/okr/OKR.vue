<template>
  <div>
    <el-table
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
        prop="okrapitotal"
        label="季度OKR-接口总数">
      </el-table-column>
      <el-table-column
        align="center"
        prop="okrp0api"
        label="季度OKR-P0接口数">
      </el-table-column>
      <el-table-column
        align="center"
        prop="okrnp0api"
        label="季度OKR-非P0接口数">
      </el-table-column>
      <el-table-column
        align="center"
        prop="okrp0case"
        label="季度OKR-P0接口用例数">
      </el-table-column>
      <el-table-column
        align="center"
        prop="okrnp0case"
        label="季度OKR-非P0接口用例数">
      </el-table-column>
      <el-table-column
        align="center"
        prop="okrcase"
        label="季度OKR-接口用例总数">
      </el-table-column>
      <el-table-column
        align="center"
        prop="okrscenario"
        label="季度OKR-场景用例总数">
      </el-table-column>
      <el-table-column
        align="center"
        prop="apicompleted"
        label="已完成接口总数">
      </el-table-column>
      <el-table-column
        align="center"
        prop="p0apicompleted"
        label="已完成P0接口总数">
      </el-table-column>
      <el-table-column
        align="center"
        prop="casecompleted"
        label="已完成接口用例数">
      </el-table-column>
      <el-table-column
        align="center"
        prop="scenariocompleted"
        label="已完成场景用例数">
      </el-table-column>
      <el-table-column
        align="center"
        prop="apiaddedweek"
        label="本周新增接口总数">
      </el-table-column>
      <el-table-column
        align="center"
        prop="p0apiaddedweek"
        label="本周新增P0接口总数">
      </el-table-column>
      <el-table-column
        align="center"
        prop="caseaddedweek"
        label="本周新增接口用例数">
      </el-table-column>
      <el-table-column
        align="center"
        prop="scenarioaddedweek"
        label="本周新增场景用例数">
      </el-table-column>
      <el-table-column
        align="center"
        prop="apicompleterate"
        label="接口总数完成率">
      </el-table-column>
      <el-table-column
        align="center"
        prop="p0apicompleterate"
        label="P0接口总数完成率">
      </el-table-column>
      <el-table-column
        align="center"
        prop="casecompleterate"
        label="接口用例数完成率">
      </el-table-column>
      <el-table-column
        align="center"
        prop="scenariocompleterate"
        label="场景用例数完成率">
      </el-table-column>
    </el-table>
  </div>
</template>

<script>

import MsMainContainer from "@/business/components/common/components/MsMainContainer";
import MsContainer from "@/business/components/common/components/MsContainer";

require('echarts/lib/component/legend');


export default {
  name: "KanbanOKR",
  components: {
    MsMainContainer,
    MsContainer,
  },
  data() {
    return {
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
      this.$get("/tuhu/kanban/exeSummary", response => {
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
