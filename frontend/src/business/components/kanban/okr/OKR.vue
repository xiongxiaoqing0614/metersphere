<template>
  <div>
    <vxe-table
      border
      resizable
      show-overflow
      :loading="result.loading"
      :data="tableData"
      :footer-method="footerMethod"
      :edit-config="{trigger: 'dblclick', mode: 'row'}">
      <vxe-table-column type="seq" width="60"></vxe-table-column>
      <vxe-table-column field="department" title="部门"></vxe-table-column>
      <vxe-table-column field="team" title="团队"></vxe-table-column>
      <vxe-table-column field="okrApiTotal" title="季度OKR-接口总数" :edit-render="{name: '$input', props: {type: 'number'}}"></vxe-table-column>
      <vxe-table-column field="okrApiP0" title="季度OKR-P0接口数" :edit-render="{name: '$input', props: {type: 'number'}}"></vxe-table-column>
      <vxe-table-column field="okrApiP0N" title="季度OKR-非P0接口数">
            <template #default="{ row }">
              <span>{{ countokrApiP0N(row) }}</span>
            </template>
      </vxe-table-column>
      <vxe-table-column field="okrApiTestP0" title="季度OKR-P0接口用例数">
            <template #default="{ row }">
              <span>{{ countokrApiTestP0(row) }}</span>
            </template>
      </vxe-table-column>
      <vxe-table-column field="okrApiTestP0N" title="季度OKR-非P0接口用例数">
            <template #default="{ row }">
              <span>{{ countokrApiTestP0N(row) }}</span>
            </template>
      </vxe-table-column>
      <vxe-table-column field="okrApiTestTotal" title="季度OKR-接口用例总数">
            <template #default="{ row }">
              <span>{{ countokrApiTestTotal(row) }}</span>
            </template>
      </vxe-table-column>
      <vxe-table-column field="okrScenarioTestTotal" title="季度OKR-场景用例总数" :edit-render="{name: '$input', props: {type: 'number'}}"></vxe-table-column>
    
      <vxe-table-column field="apicompleted" title="已完成接口总数"></vxe-table-column>
      <vxe-table-column field="p0apicompleted" title="已完成P0接口总数"></vxe-table-column>
      <vxe-table-column field="casecompleted" title="已完成接口用例数"></vxe-table-column>
      <vxe-table-column field="scenariocompleted" title="已完成场景用例数"></vxe-table-column>

      <vxe-table-column field="apiaddedweek" title="本周新增接口总数"></vxe-table-column>
      <vxe-table-column field="p0apiaddedweek" title="本周新增P0接口总数"></vxe-table-column>
      <vxe-table-column field="caseaddedweek" title="本周新增接口用例数"></vxe-table-column>
      <vxe-table-column field="scenarioaddedweek" title="本周新增场景用例数"></vxe-table-column>
    
      <vxe-table-column field="apicompleterate" title="接口总数完成率"></vxe-table-column>
      <vxe-table-column field="p0apicompleterate" title="P0接口总数完成率"></vxe-table-column>
      <vxe-table-column field="casecompleterate" title="接口用例数完成率"></vxe-table-column>
      <vxe-table-column field="scenariocompleterate" title="场景用例数完成率"></vxe-table-column>
    </vxe-table>
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
      this.result = this.$get("/tuhu/okr/getOKR", response => {
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
    sumNum (list, field) {
      let count = 0
      list.forEach(item => {
        count += Number(item[field])
      })
      return count
    },
    countokrApiP0N (row) {
      return row.okrApiTotal - row.okrApiP0
    },
    countokrApiTestP0 (row) {
      return row.okrApiP0 * 4
    },
    countokrApiTestP0N (row) {
      return row.okrApiTotal - row.okrApiP0
    },
    countokrApiTestTotal (row) {
      return row.okrApiP0*3 + row.okrApiTotal *1
    },
    countAmount (row) {
      return row.amount * row.number
    },
    countAllAmount (data) {
      let count = 0
      data.forEach(row => {
        count += this.countAmount(row)
      })
      return count
    },
    footerMethod ({ columns, data }) {
      return [
        columns.map((column, columnIndex) => {
          if (columnIndex === 0) {
            return '合计'
          }
          if (columnIndex === 3) {
            return `${this.sumNum(data, 'number')} 本`
          } else if (columnIndex === 4) {
            return `共 ${this.countAllAmount(data)} 元`
          }
          return '-'
        })
      ]
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
