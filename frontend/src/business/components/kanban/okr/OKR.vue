<template>
  <div>
    <vxe-toolbar ref="xToolbar" export>
      <template #buttons>
        <vxe-button content="选择季度">
          <template #dropdowns>
            <vxe-button type="text" content="21Q2"></vxe-button>
            <vxe-button type="text" content="21Q3"></vxe-button>
            <vxe-button type="text" content="21Q4"></vxe-button>
          </template>
        </vxe-button>
      </template>
    </vxe-toolbar> 
    <vxe-table
      border
      resizable
      show-footer
      show-overflow
      stripe
      highlight-current-row
      highlight-hover-row
      ref="xTable"
      class="editable-footer"
      max-height="600"
      :export-config="{}"
      :loading="result.loading"
      :data="tableData"
      :footer-method="footerMethod"
      :footer-cell-class-name="footerCellClassName"
      :edit-config="{trigger: 'dblclick', mode: 'row'}">
      <vxe-table-column type="seq" width="60"></vxe-table-column>
      <vxe-table-column 
        field="department" 
        title="部门"
        sortable
        :filters="[{label:'门店与供应链中心',value:'门店与供应链中心'},{label:'线下业务与CRM技术中心',value:'线下业务与CRM技术中心'},{label:'交易履约与营销平台中心',value:'交易履约与营销平台中心'},{label:'业务开发第二中心',value:'业务开发第二中心'},{label:'业务开发第一中心',value:'业务开发第一中心'}]"
        :filter-method="filterDepartmentMethod"></vxe-table-column>
      <vxe-table-column field="team" title="团队"></vxe-table-column>
      <vxe-table-column field="okrApiTotal" title="季度OKR-接口总数" :edit-render="{name: '$input', props: {type: 'number'}}"></vxe-table-column>
      <vxe-table-column field="okrApiP0" title="季度OKR-P0接口数" :edit-render="{name: '$input', props: {type: 'number'}}"></vxe-table-column>
      <vxe-table-column field="okrApiP0N" title="季度OKR-非P0接口数" >
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
    
      <vxe-table-column field="apiCount" title="已完成接口总数"></vxe-table-column>
      <vxe-table-column field="p0APICount" title="已完成P0接口总数"></vxe-table-column>
      <vxe-table-column field="singleCount" title="已完成接口用例数"></vxe-table-column>
      <vxe-table-column field="scenarioCount" title="已完成场景用例数"></vxe-table-column>

      <vxe-table-column field="apiCountThisWeek" title="本周新增接口总数"></vxe-table-column>
      <vxe-table-column field="p0APICountThisWeek" title="本周新增P0接口总数"></vxe-table-column>
      <vxe-table-column field="singleCountThisWeek" title="本周新增接口用例数"></vxe-table-column>
      <vxe-table-column field="scenarioCountThisWeek" title="本周新增场景用例数"></vxe-table-column>
    
      <vxe-table-column field="apiCompleteRate" title="接口总数完成率">
            <template #default="{ row }">
              <span>{{ calapiCompleteRate (row) }} %</span>
            </template>  
      </vxe-table-column>
      <vxe-table-column field="p0apiCompleteRate" title="P0接口总数完成率">
            <template #default="{ row }">
              <span>{{ calp0apiCompleteRate(row) }} %</span>
            </template>    
      </vxe-table-column>
      <vxe-table-column field="caseCompleteRate" title="接口用例数完成率">
            <template #default="{ row }">
              <span>{{ calcaseCompleteRate(row) }} %</span>
            </template>       
      </vxe-table-column>
      <vxe-table-column field="scenarioCompleteRate" title="场景用例数完成率">
            <template #default="{ row }">
              <span>{{ calscenarioCompleteRate(row) }} %</span>
            </template>        
      </vxe-table-column>
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
            _this.orgList.push({label: departName, value: departName});
          }
        }
        _this.departmentOptions = (JSON.stringify(this.orgList).replaceAll("\"label\"", "label").replaceAll("\"value\"", "value").replaceAll("\"","'")).toString();
      });
    },
    filterTag(value, row) {
        return row.tag === value;
    },
    filterDepartmentMethod ({ value, row, column }) {
      return row.department === value
    },
    hasFilter(orgList, orgName) {
      for(var i = 0, len = orgList.length; i < len; i++){
        if(orgName == orgList[i].label)
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
      if(row.okrApiTotal > 0) {
        row.okrApiP0N = row.okrApiTotal - row.okrApiP0
      }else{
        row.okrApiP0N = 0
      }
      return row.okrApiP0N
    },
    countokrApiTestP0 (row) {
      if(row.okrApiP0 > 0) {
        row.okrApiTestP0 = row.okrApiP0 * 4
      }else{
        row.okrApiTestP0 = 0
      }
      return row.okrApiTestP0
    },
    countokrApiTestP0N (row) {
      if (row.okrApiP0N > 0) {
      row.okrApiTestP0N = row.okrApiP0N
      }else{
        row.okrApiTestP0N = 0
      }
      return row.okrApiTestP0N
    },
    countokrApiTestTotal (row) {
      row.okrApiTestTotal = row.okrApiTestP0 + row.okrApiTestP0N
      return row.okrApiTestTotal
    },
    calapiCompleteRate (row) {
      if(row.okrApiTotal == 0) {
        row.apiCompleteRate = 0
      }else{
        row.apiCompleteRate = (row.apiCount/row.okrApiTotal).toFixed(4) * 100 
      }
      return row.apiCompleteRate
    },
    calp0apiCompleteRate (row) {
      if(row.okrApiP0 == 0) {
        row.p0apiCompleteRate = 0
      }else{
        row.p0apiCompleteRate = (row.p0APICount/row.okrApiP0).toFixed(4) * 100
      }
      return row.p0apiCompleteRate
    },
    calcaseCompleteRate (row) {
      if(row.okrApiTestTotal == 0) {
        row.caseCompleteRate = 0
      }else{
        row.caseCompleteRate = (row.singleCount/row.okrApiTestTotal).toFixed(4) * 100
      }
      return row.caseCompleteRate
    },
    calscenarioCompleteRate (row) {
      if(row.okrScenarioTestTotal == 0) {
        row.scenarioCompleteRate = 0
      }else{
        row.scenarioCompleteRate = (row.scenarioCount/row.okrScenarioTestTotal).toFixed(4) * 100
      }
      return row.scenarioCompleteRate
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
    updateFooterEvent () {
      const $table = this.$refs.xTable
      $table.updateFooter()
    },
    footerCellClassName ({ $rowIndex, columnIndex }) {
      if (columnIndex === 0) {
        if ($rowIndex === 0) {
          return 'col-blue'
        } else {
          return 'col-red'
        }
      }
    },
    footerMethod ({ columns, data }) {
      let returnArray = [
        columns.map((column, columnIndex) => {
          if (columnIndex === 0) {
            return '合计'
          }
          
          if (columnIndex > 2 && columnIndex < 18) {
            return `${this.sumNum(data, column.property)}`
          }
          return '-'
        })
      ]

      if(returnArray[0][3] > 0)
        returnArray[0][18] = ((returnArray[0][10]/returnArray[0][3]).toFixed(4) * 100).toString() + " %"

      if(returnArray[0][4] > 0)
        returnArray[0][19] = ((returnArray[0][11]/returnArray[0][4]).toFixed(4) * 100).toString() + " %"

      if(returnArray[0][8] > 0)
        returnArray[0][20] = ((returnArray[0][12]/returnArray[0][8]).toFixed(4) * 100).toString() + " %"

      if(returnArray[0][9] > 0)
        returnArray[0][21] = ((returnArray[0][13]/returnArray[0][9]).toFixed(4) * 100).toString() + " %"

      for (let i = 0; i < returnArray[0].length; ++i) {
        if(returnArray[0][i] === "NaN")
          returnArray[0][i] = 0
      }
      return returnArray
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
