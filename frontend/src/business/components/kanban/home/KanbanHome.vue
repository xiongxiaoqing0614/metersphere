<template>
  <div>
    <el-table
      :data="tableData"
      border
      stripe
      show-summary
      :default-sort = "{prop: 'department', order: 'descending'}"
      style="width: 100%">
      <el-table-column
        prop="department"
        sortable
        :filters="orgList"
        :filter-method="filterHandler"
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
      tableData: null,
      orgList: null,
      wsList: null
    };
  },
  created() {
    this.getSummary();
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
    init() {

    },
    getSummary(){
      const _this = this;
      this.$get("/kanban/summary", response => {
        _this.tableData = response.data;
        _this.orgList = new Array();
        for(var i = 0, len = _this.tableData.length; i < len; i++){
          console.log(_this.tableData[i])
          var departName = _this.tableData[i].department
          if(!this.hasFilter(_this.orgList, departName))
          {
            console.log(_this.orgList.indexOf(departName))
            _this.orgList.push({text: departName, value: departName});
          }
        }
        console.log(_this.orgList)
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
    }
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
