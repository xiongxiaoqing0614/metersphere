<template>
  <el-col>
    <kanban-header-menus/>
    <div class="table-container">
      <transition>
        <keep-alive>
          <router-view :baseUrl="baseUrl"/>
        </keep-alive>
      </transition>
    </div>
  </el-col>
</template>

<script>

  import KanbanHeaderMenus from "./head/KanbanHeaderMenus";

  export default {
    name: "KanbanHome",
    components: {KanbanHeaderMenus},
    data() {
      return {
        baseUrl: "okr"
      }
    }
  }
</script>


<style>
  .node-tree {
    margin: 3%;
  }
  .table-container {
    margin: 10px;
  }
  .el-table th.gutter{

    display: table-cell!important;

  }
</style>

<style scoped>


</style>
