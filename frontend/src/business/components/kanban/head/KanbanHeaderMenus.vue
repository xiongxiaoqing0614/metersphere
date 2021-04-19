<template>
  <div id="menu-bar" v-if="isRouterAlive">
    <el-row type="flex">
      <project-change :project-name="currentProject"/>
      <el-col :span="14">
        <el-menu class="header-menu" :unique-opened="true" mode="horizontal" router
                 :default-active='$route.path'>
          <el-menu-item :index="'/kanban/home'">
            {{ $t("test_kanban.kanban_summary") }}
          </el-menu-item>
        </el-menu>
      </el-col>
      <el-col :span="8"/>
    </el-row>
  </div>

</template>
<script>

import MsShowAll from "../../common/head/ShowAll";

export default {
  name: "KanbanHeaderMenus",
  components: {MsShowAll},
  data() {
    return {
      testPlanViewPath: '',
      isRouterAlive: true,
      testCaseEditPath: '',
      testCaseReviewEditPath: '',
      testCaseProjectPath: '',
      isProjectActivation: true,
      currentProject: '',
    }
  }
}

</script>

<style scoped>
.el-divider--horizontal {
  margin: 0;
}

#menu-bar {
  border-bottom: 1px solid #E6E6E6;
  background-color: #FFF;
}

.blank_item {
  display: none;
}

.el-divider--horizontal {
  margin: 0;
}

.deactivation >>> .el-submenu__title {
  border-bottom: white !important;
}

/*.project-change {*/
/*  height: 40px;*/
/*  line-height: 40px;*/
/*  color: inherit;*/
/*  margin-left: 20px;*/
/*}*/

</style>
