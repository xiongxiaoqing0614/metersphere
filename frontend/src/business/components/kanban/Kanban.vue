<template>
  <el-col>
    <kanban-header-menus/>
    <div>
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
        baseUrl: "kanban"
      }
    },
    activated() {
      this.$refs.table.doLayout()
    }
  }
</script>


<style>
  .node-tree {
    margin: 3%;
  }
</style>

<style scoped>


</style>
