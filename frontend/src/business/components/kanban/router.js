
const Kanban = () => import('@/business/components/kanban/Kanban')
const CaseSummary = () => import('@/business/components/kanban/case/CaseSummary')
const ExecutionSummary = () => import('@/business/components/kanban/execution/ExeSummary')
const OKRHome = () => import('@/business/components/kanban/okr/OKR')
const GraphHome = () => import('@/business/components/kanban/graph/index')

export default {
  path: "/kanban",
  name: "kanban",
  redirect: "/kanban/okr",
  components: {
    content: Kanban
  },
  children: [
    {
      path: 'caseSummary',
      name: 'CaseSummary',
      component: CaseSummary,
    },
    {
      path: 'exeSummary',
      name: 'ExeSummary',
      component: ExecutionSummary,
    },
    {
      path: 'okr',
      name: 'OKR',
      component: OKRHome,
    },
    {
      path: 'graph',
      name: 'graph',
      component: GraphHome,
    }
  ]
}
