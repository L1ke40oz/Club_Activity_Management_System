import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/explore/clubs',
    name: 'ExploreClubs',
    component: () => import('../views/explore/ExploreClubs.vue')
  },
  {
    path: '/explore/clubs/:id',
    name: 'ExploreClubDetail',
    component: () => import('../views/explore/ExploreClubDetail.vue')
  },
  {
    path: '/explore/activities',
    name: 'ExploreActivities',
    component: () => import('../views/explore/ExploreActivities.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/admin',
    component: () => import('../layout/MainLayout.vue'),
    redirect: '/admin/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '首页仪表盘' }
      },
      {
        path: 'clubs',
        name: 'Clubs',
        component: () => import('../views/club/ClubList.vue'),
        meta: { title: '社团管理' }
      },
      {
        path: 'clubs/:id',
        name: 'ClubDetail',
        component: () => import('../views/club/ClubDetail.vue'),
        meta: { title: '社团详情' }
      },
      {
        path: 'activities',
        name: 'Activities',
        component: () => import('../views/activity/ActivityList.vue'),
        meta: { title: '活动管理' }
      },
      {
        path: 'activities/:id',
        name: 'ActivityDetail',
        component: () => import('../views/activity/ActivityDetail.vue'),
        meta: { title: '活动详情' }
      },
      {
        path: 'registrations',
        name: 'Registrations',
        component: () => import('../views/registration/RegistrationList.vue'),
        meta: { title: '报名签到' }
      },
      {
        path: 'my-clubs',
        name: 'MyClubs',
        component: () => import('../views/user/MyClubs.vue'),
        meta: { title: '我的社团' }
      },
      {
        path: 'club-view/:id',
        name: 'ClubView',
        component: () => import('../views/user/ClubView.vue'),
        meta: { title: '社团详情' }
      },
      {
        path: 'my-activities',
        name: 'MyActivities',
        component: () => import('../views/user/MyActivities.vue'),
        meta: { title: '我的活动' }
      },
      {
        path: 'messages',
        name: 'Messages',
        component: () => import('../views/user/MyMessages.vue'),
        meta: { title: '站内信' }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('../views/statistics/StatisticsView.vue'),
        meta: { title: '数据统计', requiresAdmin: true }
      },
      {
        path: 'ai-reports',
        name: 'AiReports',
        component: () => import('../views/ai/AiReport.vue'),
        meta: { title: 'AI分析报告', requiresAdmin: true }
      },
      {
        path: 'ai-reports/:id',
        name: 'ReportDetail',
        component: () => import('../views/ai/ReportDetail.vue'),
        meta: { title: '报告详情', requiresAdmin: true }
      },
      {
        path: 'ai-chat',
        name: 'AiChat',
        component: () => import('../views/ai/AiChat.vue'),
        meta: { title: 'AI助手', requiresAdmin: true }
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('../views/admin/UserManagement.vue'),
        meta: { title: '用户管理', requiresAdmin: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/Profile.vue'),
        meta: { title: '个人中心' }
      }
    ]
  },
  // 兼容旧路由：将 /dashboard, /clubs 等重定向到 /admin 下
  { path: '/dashboard', redirect: '/admin/dashboard' },
  { path: '/clubs', redirect: '/admin/clubs' },
  { path: '/clubs/:id', redirect: to => `/admin/clubs/${to.params.id}` },
  { path: '/activities', redirect: '/admin/activities' },
  { path: '/activities/:id', redirect: to => `/admin/activities/${to.params.id}` },
  { path: '/registrations', redirect: '/admin/registrations' },
  { path: '/ai-reports', redirect: '/admin/ai-reports' },
  { path: '/ai-reports/:id', redirect: to => `/admin/ai-reports/${to.params.id}` },
  { path: '/users', redirect: '/admin/users' },
  { path: '/profile', redirect: '/admin/profile' }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to) {
    if (to.hash) {
      return { el: to.hash, behavior: 'smooth' }
    }
    return { top: 0 }
  }
})

router.beforeEach((to, from, next) => {
  // 首页、登录页和浏览页不需要登录
  if (to.path === '/' || to.path === '/login' || to.path.startsWith('/explore')) {
    next()
    return
  }

  const userStore = useUserStore()

  // 需要登录的页面
  if (to.meta.requiresAuth || to.path.startsWith('/admin')) {
    if (!userStore.isLoggedIn) {
      next('/login')
      return
    }
  }

  // 管理员权限页面
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    ElMessage && ElMessage.warning('无权限访问')
    next('/admin/dashboard')
    return
  }

  next()
})

export default router
