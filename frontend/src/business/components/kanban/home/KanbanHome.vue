<template>
  <div>
    <div>
      <label>请选择需要展示的数据：</label>
      <el-checkbox-group v-model="checkedTableColumns" class="select-column">
        <el-checkbox
          v-for="column in tableColumns"
          :key="column.prop"
          :label="column.prop"
          >{{ column.label }}</el-checkbox>
      </el-checkbox-group>
    </div>
    <el-table
      :data="tableData"
      min-height="100"
      border
      stripe
      show-summary
      ref=“table”
      style="width: 100%; max-height: 700px;">
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
        prop="project"
        sortable
        label="项目">
      </el-table-column>
      <el-table-column
        align="center"
        prop="p0APICount"
        sortable
        label="P0接口数"
        v-if="showColumn.p0APICount">
      </el-table-column>
      <el-table-column
        align="center"
        prop="nonP0APICount"
        sortable
        label="非P0接口数"
        v-if="showColumn.nonP0APICount">
      </el-table-column>
      <el-table-column
        align="center"
        prop="apiCount"
        sortable
        label="接口总数"
        v-if="showColumn.apiCount">
      </el-table-column>
      <el-table-column
        align="center"
        prop="singleCount"
        sortable
        label="接口用例总数"
        v-if="showColumn.singleCount">
      </el-table-column>
      <el-table-column
        align="center"
        prop="scenarioCount"
        sortable
        label="场景用例总数"
        v-if="showColumn.scenarioCount">
      </el-table-column>
      <el-table-column
        align="center"
        prop="completedAPICount"
        sortable
        label="已完成接口总数"
        v-if="showColumn.completedAPICount">
      </el-table-column>
      <el-table-column
        align="center"
        prop="completedSingleCount"
        sortable
        label="已完成接口用例数"
        v-if="showColumn.completedSingleCount">
      </el-table-column>
      <el-table-column
        align="center"
        prop="completedScenarioCount"
        sortable
        label="已完成场景用例数"
        v-if="showColumn.completedScenarioCount">
      </el-table-column>
      <el-table-column
        align="center"
        prop="apiCountThisWeek"
        sortable
        label="本周新增接口数"
        v-if="showColumn.apiCountThisWeek">
      </el-table-column>
      <el-table-column
        align="center"
        prop="singleCountThisWeek"
        sortable
        label="本周新增接口用例数"
        v-if="showColumn.singleCountThisWeek">
      </el-table-column>
      <el-table-column
        align="center"
        prop="scenarioCountThisWeek"
        sortable
        label="本周新增场景用例数"
        v-if="showColumn.scenarioCountThisWeek">
      </el-table-column>
    </el-table>
  </div>
</template>

<script>

import MsMainContainer from "@/business/components/common/components/MsMainContainer";
import MsContainer from "@/business/components/common/components/MsContainer";

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
      wsList: null,
      tableColumns:null
    };
  },
  created() {
    this.getSummary();
    this.showColumn = {
      "p0APICount":true,
      "nonP0APICount":true,
      "apiCount":true,
      "singleCount":true,
      "scenarioCount":true,
      "completedAPICount":true,
      "completedSingleCount":true,
      "completedScenarioCount":true,
      "apiCountThisWeek":true,
      "singleCountThisWeek":true,
      "scenarioCountThisWeek":true,
    };
    this.tableColumns = [
      {
        prop: "p0APICount",
        label: "P0接口数",
        show: true,
      },
      {
        prop: "nonP0APICount",
        label: "非P0接口数",
        show: true,
      },
      {
        prop: "apiCount",
        label: "接口总数",
        show: true,
      },
      {
        prop: "singleCount",
        label: "接口用例总数",
        show: true,
      },
      {
        prop: "scenarioCount",
        label: "场景用例总数",
        show: true,
      },
      {
        prop: "completedAPICount",
        label: "已完成接口总数",
        show: true,
      },
      {
        prop: "completedSingleCount",
        label: "已完成接口用例数",
        show: true,
      },
      {
        prop: "completedScenarioCount",
        label: "已完成场景用例数",
        show: true,
      },
      {
        prop: "apiCountThisWeek",
        label: "本周新增接口数",
        show: true,
      },
      {
        prop: "singleCountThisWeek",
        label: "本周新增单接口用例数",
        show: true,
      },
      {
        prop: "scenarioCountThisWeek",
        label: "本周新增场景用例数",
        show: true,
      },
    ];
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
    projectId() {
      return this.$store.state.projectId
    },
    bindTableColumns() {
      return this.tableColumns.filter((column) => column.show);
    },
    /* 这里使用了getter和setter，这样写的好处不用自己手动监听复选框的选中事件 */
    checkedTableColumns: {
        get() {
          // 返回选中的列名
					return this.bindTableColumns.map(column => column.prop);
        },
        set(checked) {
          // 设置表格列的显示与隐藏
          this.tableColumns.forEach((column, index) => {
            // 如果选中，则设置列显示
            if(checked.includes(column.prop)) {
              console.log(column)
              column.show = true;
              this.showColumn[column.prop] = true;
            } else {
              // 如果未选中，则设置列隐藏
              //console.log(column)
              column.show = false;
              this.showColumn[column.prop] = false;
            }
          })
        }
      },
  },
  methods: {
    init() {

    },
    getSummary(){
      const _this = this;
      this.$get("/tuhu/kanban/summary", response => {
        _this.tableData = response.data;
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
    showColumn(colKey) {
        for(var column in this.tableColumns){
          if(column.prop == colKey){
            console.log(colKey)
            return false;
          }
        }
        return true;
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
.select-column {
  margin: 10px 5px;
}
</style>
