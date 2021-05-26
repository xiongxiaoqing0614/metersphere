export function pullAndMergeCodeCoverageRate(vueObj, tableData) {
  let ids = []
  tableData.forEach(item => {
    ids.push(item.id)
  })

  let data = {testReportIds: ids}
  vueObj.result = vueObj.$post('/tuhu/testplan/coveragerate', data, response => {
    if (response.data != null) {
      let coverageData = response.data;
      for (let n = 0; n < tableData.length; n++){
        let item = tableData[n]
        for (let i = 0; i < coverageData.length; i++){
          if (item.id === coverageData[i].testReportId) {
            item.coverageRate = coverageData[i].coverageRate * 100 + '%'
            item._appId = coverageData[i].appId
            item._branchName = coverageData[i].branchName
            item._commitId = coverageData[i].commitId
            item._stage = coverageData[i].stage
          }
        }
      }
    }
    vueObj.tableData = tableData
  })
}