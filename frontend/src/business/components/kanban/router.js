
const Kanban = () => import('@/business/components/kanban/Kanban')
const KanbanHome = () => import('@/business/components/kanban/home/KanbanHome')
const ExecutionHome = () => import('@/business/components/kanban/execution/ExeSummary')
const OKRHome = () => import('@/business/components/kanban/okr/OKR')

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
    },
    {
      path: 'exeSummary',
      name: 'ExeSummary',
      component: ExecutionHome,
    },
    {
      path: 'okr',
      name: 'OKR',
      component: OKRHome,
    }
  ]
}
