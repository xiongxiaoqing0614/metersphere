<template>
  <div>

    <el-drawer
      :visible.sync="showDialog"
      :with-header="false"
      :modal-append-to-body="false"
      size="100%"
      ref="drawer"
      v-loading="result.loading">
      <template v-slot:default="scope">

        <el-row type="flex" class="head-bar">
          <el-col :span="12">
            <div class="name-edit">
              <el-button plain size="mini" icon="el-icon-back" @click="handleClose">{{$t('test_track.return')}}
              </el-button>&nbsp;
              <span class="title">{{name}}</span>
            </div>
          </el-col>
          <el-col :span="12" class="head-right">
            <el-button plain size="mini" @click="handleExport()">
              {{$t('test_track.plan_view.export_report')}}
            </el-button>
          </el-col>
        </el-row>

        <div class="container" ref="resume" id="app">
          <iframe id="template-iframe" ref="iframe" :src="url" width="100%" height="100%" frameborder="no" border="0" marginwidth="0" marginheight="0" scrolling="yes" allowtransparency="yes"></iframe>
        </div>

      </template>
    </el-drawer>
  </div>
</template>

<script>
  export default {
    name: "TestPlanCodeCoverageRateReportView",
    data() {
      return {
        result: {},
        report: {},
        showDialog: false,
        name: '',
        url: ''
      }
    },
    methods: {
      listenGoBack() {
        //监听浏览器返回操作，关闭该对话框
        if (window.history && window.history.pushState) {
          history.pushState(null, null, document.URL);
          window.addEventListener('popstate', this.goBack, false);
        }
      },
      goBack() {
        this.handleClose();
      },
      open(name, url) {
        this.url = url;
        this.name = name;
        this.showDialog = true;   
        this.listenGoBack();
      },
      handleClose() {
        window.removeEventListener('popstate', this.goBack, false);
        this.$emit('refresh');
        this.showDialog = false;
      },
      handleExport() {
        window.open(this.url + '&zip=true');
      }
    }
  }
</script>

<style scoped>

  .el-main {
    height: calc(100vh - 70px);
    width: 100%;
  }

  .head-bar {
    background: white;
    height: 45px;
    line-height: 45px;
    padding: 0 10px;
    border: 1px solid #EBEEF5;
    box-shadow: 0 0 2px 0 rgba(31, 31, 31, 0.15), 0 1px 2px 0 rgba(31, 31, 31, 0.15);
  }

  .container {
    height: 100vh;
    background: #F5F5F5;
  }

  .el-card {
    width: 70%;
    margin: 5px auto;
  }

  .head-right {
    text-align: right;
  }

</style>
