<template>
  <div>
    <vxe-toolbar ref="xToolbar" custom export>
      <template #buttons>
        <vxe-select v-model="okrName" @change="getOKRByName">
          <vxe-option v-for="item in okrNames" :key="item" :value="item" :label="`${item}`"></vxe-option>
        </vxe-select>
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
      :max-height="height"
      :header-cell-style="headerCellStyle"
      :cell-style="cellStyle"
      :export-config="{}"
      :loading="result.loading"
      :data="tableData"
      :footer-method="footerMethod"
      :footer-cell-class-name="footerCellClassName"
      :edit-config="{trigger: 'dblclick', mode: 'row', showStatus: true}"
      @edit-closed="editClosedEvent">
      <vxe-table-column type="seq" width="60"></vxe-table-column>
      <vxe-table-column field="id" :visible="false" width="60"></vxe-table-column>
      <vxe-table-column 
        field="department" 
        title="部门"
        sortable
        :filters="[{label:'门店与供应链中心',value:'门店与供应链中心'},{label:'线下业务与CRM技术中心',value:'线下业务与CRM技术中心'},{label:'交易履约与营销平台中心',value:'交易履约与营销平台中心'},{label:'业务开发第二中心',value:'业务开发第二中心'},{label:'业务开发第一中心',value:'业务开发第一中心'}]"
        :filter-method="filterDepartmentMethod"></vxe-table-column>
      <vxe-table-column field="team" title="团队"></vxe-table-column>
      <vxe-table-column field="okrApiTotal" title="季度OKR-接口总数" :edit-render="{name: '$input', props: {type: 'number',placeholder: '请输入OKR'}}"></vxe-table-column>
      <vxe-table-column field="okrApiP0" title="季度OKR-P0接口数" :edit-render="{name: '$input', props: {type: 'number',placeholder: '请输入OKR'}}"></vxe-table-column>
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
      <vxe-table-column field="okrScenarioTestTotal" title="季度OKR-场景用例总数" :edit-render="{name: '$input', props: {type: 'number',placeholder: '请输入OKR'}}"></vxe-table-column>
    
      <vxe-table-column field="completedAPICount" title="已完成接口总数"></vxe-table-column>
      <vxe-table-column field="completedP0APICount" title="已完成P0接口总数"></vxe-table-column>
      <vxe-table-column field="completedSingleCount" title="已完成接口用例数"></vxe-table-column>
      <vxe-table-column field="completedScenarioCount" title="已完成场景用例数"></vxe-table-column>

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
      <vxe-table-column field="appIdCoverage" title="AppId服务接入率">
            <template #default="{ row }">
              <span>{{ row.appIdCoverage * 100 }} %</span>
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
      okrName: null,
      okrNames: null,
      okrList: null,
      tableColumns: null,
      height: 100
    };
  },
  created() {
    this.getOKRNames();
    this.getSummary();
    this.$nextTick(() => {
      // 将表格和工具栏进行关联
      const $table = this.$refs.xTable
      $table.connect(this.$refs.xToolbar)
    })
  },
  updated () {
    this.height = document.body.clientHeight - 180;
    this.$nextTick(() => {
      // this.$refs['table'].doLayout();
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
    getOKRNames(){
      this.result = this.$get("/tuhu/okr/getOKRNames", response => {
        this.okrNames = response.data;
        if(this.okrName == null && this.okrNames.length > 0)
          this.okrName = this.okrNames[0];
      });
    },
    getSummary(){
      const _this = this;
      this.result = this.$get("/tuhu/okr/getCurrentOKR", response => {
        _this.tableData = response.data;
        _this.orgList = new Array();
        for(var i = 0, len = _this.tableData.length; i < len; i++){
          if(this.okrName ==null && i == 0) {
            this.okrName = _this.tableData[i].name;
          }

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
        row.apiCompleteRate = 0.0
      }else{
        row.apiCompleteRate = ((row.apiCount/row.okrApiTotal) * 100).toFixed(1)
      }
      return row.apiCompleteRate
    },
    calp0apiCompleteRate (row) {
      if(row.okrApiP0 == 0) {
        row.p0apiCompleteRate = 0.0
      }else{
        row.p0apiCompleteRate = ((row.p0APICount/row.okrApiP0) * 100).toFixed(1)
      }
      return row.p0apiCompleteRate
    },
    calcaseCompleteRate (row) {
      if(row.okrApiTestTotal == 0) {
        row.caseCompleteRate = 0.0
      }else{
        row.caseCompleteRate = ((row.singleCount/row.okrApiTestTotal) * 100).toFixed(1)
      }
      return row.caseCompleteRate
    },
    calscenarioCompleteRate (row) {
      if(row.okrScenarioTestTotal == 0) {
        row.scenarioCompleteRate = 0.0
      }else{
        row.scenarioCompleteRate = ((row.scenarioCount/row.okrScenarioTestTotal)* 100).toFixed(1) 
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

      if(returnArray[0][3] > 0){
        let rate = ((returnArray[0][10]/returnArray[0][3])* 100).toString().match(/^\d+(?:\.\d{0,1})?/)
        returnArray[0][18] = rate + " %"
      }

      if(returnArray[0][4] > 0){
        let rate = ((returnArray[0][11]/returnArray[0][4]) * 100).toString().match(/^\d+(?:\.\d{0,1})?/)
        returnArray[0][19] = rate + " %"
      }

      if(returnArray[0][8] > 0){
        let rate = ((returnArray[0][12]/returnArray[0][8]) * 100).toString().match(/^\d+(?:\.\d{0,1})?/)
        returnArray[0][20] = rate + " %"
      }

      if(returnArray[0][9] > 0){
        let rate = ((returnArray[0][13]/returnArray[0][9]) * 100).toString().match(/^\d+(?:\.\d{0,1})?/)
        returnArray[0][21] = rate + " %"
      }

      for (let i = 0; i < returnArray[0].length; ++i) {
        if(returnArray[0][i] === "NaN")
          returnArray[0][i] = 0
      }
      return returnArray
    },
    headerCellStyle ({ column, columnIndex }) {
      if (column.property === 'okrApiTotal' || column.property === 'okrApiP0' || column.property === 'okrScenarioTestTotal'  ) {
        return {
          backgroundColor: '#D1E9E9',
          color: '#272727'
        }
      } else if (column.property === 'okrApiP0N' || column.property === 'okrApiTestP0' ||
       column.property === 'okrApiTestP0N' || column.property === 'okrApiTestTotal' ||
       column.property === 'apiCompleteRate' || column.property === 'p0apiCompleteRate' ||
       column.property === 'caseCompleteRate' || column.property === 'scenarioCompleteRate' || column.property === 'appIdCoverage') {
        return {
          backgroundColor: '#E1C4C4',
          color: '#272727'
        }
       } else if (column.property === 'apiCountThisWeek' || column.property === 'p0APICountThisWeek' ||
       column.property === 'singleCountThisWeek' || column.property === 'scenarioCountThisWeek' ) {
        return {
          backgroundColor: '#E8E8D0',
          color: '#272727'
        }
       } else if (column.property === 'apiCount' || column.property === 'p0APICount' ||
       column.property === 'singleCount' || column.property === 'scenarioCount' ) {
        return {
          backgroundColor: '#FFF3EE',
          color: '#272727'
        }
       } else {
         return {
          backgroundColor: '#FFFCEC',
          color: '#272727'
         }
       }
    },
    cellStyle ({ row, rowIndex, column }) {
      // if (column.property === 'sex') {
      //   if (row.sex === 'Women') {
      //     return {
      //       backgroundColor: '#187',
      //       color: '#ffffff'
      //     }
      //   } else if (row.age === 24) {
      //     return {
      //       backgroundColor: '#2db7f5',
      //       color: '#000'
      //     }
      //   }
      // }
      // if ([2, 3, 5].includes(rowIndex)) {
      //   return {
      //     backgroundColor: 'red',
      //     color: '#ffffff'
      //   }
      // }
    },
    editClosedEvent ({ row, column }) {
      row.name = this.okrName;
      this.result = this.$post('/tuhu/okr/updateOKR', row, response => {
        row.id = response.data
        this.$success(this.$t('commons.save_success'));
        this.$emit('refresh');
      });
    },
    buildParam(row) {
      return JSON.parse(row)
    },
    getOKRByName() {
      this.result = this.$get("/tuhu/okr/getOKR/" + this.okrName , response => {
        this.tableData = response.data;
        this.orgList = new Array();
        for(var i = 0, len = this.tableData.length; i < len; i++){
          var departName = this.tableData[i].department
          if(!this.hasFilter(this.orgList, departName))
          {
            this.orgList.push({label: departName, value: departName});
          }
        }
        this.departmentOptions = (JSON.stringify(this.orgList).replaceAll("\"label\"", "label").replaceAll("\"value\"", "value").replaceAll("\"","'")).toString();
        this.$emit('refresh');
      });
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
