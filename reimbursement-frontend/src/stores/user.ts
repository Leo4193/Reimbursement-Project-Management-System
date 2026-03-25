import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { User } from '@/types'
import request from '@/utils/request'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<User | null>(null)

  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  const setUserInfo = (info: User) => {
    userInfo.value = info
  }

  const logout = () => {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  const getCurrentUser = async () => {
    try {
      const data = await request.get('/auth/me')
      userInfo.value = data
      return data
    } catch (error) {
      return null
    }
  }

  return {
    token,
    userInfo,
    setToken,
    setUserInfo,
    logout,
    getCurrentUser
  }
})