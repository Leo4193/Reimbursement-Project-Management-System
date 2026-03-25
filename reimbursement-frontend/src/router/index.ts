import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/auth/Login.vue')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/auth/Register.vue')
    },
    {
      path: '/',
      redirect: '/project'
    },
    {
      path: '/project',
      name: 'project',
      component: () => import('@/views/project/ProjectList.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/user',
      name: 'user',
      component: () => import('@/views/admin/UserManage.vue'),
      meta: { requiresAuth: true, roles: ['A'] }
    },
    {
      path: '/detail/:projectId',
      name: 'detail',
      component: () => import('@/views/detail/DetailList.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/statistics',
      name: 'statistics',
      component: () => import('@/views/statistics/StatisticsView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/views/profile/Profile.vue'),
      meta: { requiresAuth: true }
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userStore = useUserStore()
  const isLogin = !!token
  
  if (to.meta.requiresAuth && !isLogin) {
    next('/login')
  } else if (to.path === '/login' && isLogin) {
    next('/')
  } else {
    // 角色权限检查
    if (to.meta.roles && userStore.userInfo) {
      const roles = to.meta.roles as string[]
      if (!roles.includes(userStore.userInfo.roleCode)) {
        next('/project')
        return
      }
    }
    next()
  }
})

export default router