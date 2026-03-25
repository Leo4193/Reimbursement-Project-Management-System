<template>
  <div v-if="isLoggedIn" class="app-container">
    <el-container>
      <el-aside width="200px" class="sidebar">
        <div class="logo">
          <h3>报销管理系统</h3>
        </div>
        <el-menu
          :default-active="activeMenu"
          router
          class="menu"
        >
          <el-menu-item index="/project">
            <el-icon><Folder /></el-icon>
            <span>项目管理</span>
          </el-menu-item>
          <el-menu-item v-if="userRole === 'A'" index="/user">
            <el-icon><User /></el-icon>
            <span>人员管理</span>
          </el-menu-item>
          <el-menu-item index="/statistics">
            <el-icon><DataAnalysis /></el-icon>
            <span>统计报表</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <el-container>
        <el-header class="header">
          <div class="user-info">
            <el-dropdown @command="handleCommand">
              <span class="user-name">{{ userInfo?.realName || userInfo?.username || '用户' }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
  
  <router-view v-else />
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { Folder, DataAnalysis, ArrowDown, User } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const isLoggedIn = computed(() => !!userStore.token)
const userInfo = computed(() => userStore.userInfo)
const userRole = computed(() => userStore.userInfo?.roleCode)
const activeMenu = computed(() => route.path)

const handleCommand = (command: string) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}

// 初始化加载用户信息
onMounted(async () => {
  if (userStore.token && !userStore.userInfo) {
    await userStore.getCurrentUser()
  }
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.app-container {
  height: 100vh;
  display: flex;
}

.sidebar {
  background-color: #304156;
  color: #fff;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  border-bottom: 1px solid #4a5a6e;
}

.menu {
  border-right: none;
  background-color: #304156;
}

.menu .el-menu-item {
  color: #bfcbd9;
}

.menu .el-menu-item.is-active {
  color: #409eff;
  background-color: #263445;
}

.menu .el-menu-item:hover {
  background-color: #263445;
  color: #fff;
}

.header {
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 0 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-name {
  cursor: pointer;
  font-size: 14px;
  color: #333;
}
</style>