
const Kanban = () => import('@/business/components/kanban/Kanban')
const KanbanHome = () => import('@/business/components/kanban/home/KanbanHome')
// const reportListView = () => import('@/business/components/track/plan/TestPlan')

export default {
  path: "/kanban",
  name: "kanban",
  redirect: "/kanban/home",
  components: {
    content: Kanban
  },
  children: [
    {
      path: 'home',
      name: 'KanbanHome',
      component: KanbanHome,
    }
  ]
}
