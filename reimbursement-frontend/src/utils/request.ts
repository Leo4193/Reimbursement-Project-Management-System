import axios from 'axios'
import type { AxiosInstance, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

const request: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    } else if (res.code === 401) {
      ElMessage.error(res.message || '请重新登录')
      localStorage.removeItem('token')
      window.location.href = '/login'
      return Promise.reject(res)
    } else {
      // 显示后端返回的具体错误信息
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(res)
    }
  },
  (error) => {
    // 网络错误或其他错误
    if (error.response) {
      const status = error.response.status
      if (status === 403) {
        ElMessage.error('权限不足，无法执行此操作')
      } else if (status === 401) {
        ElMessage.error('请重新登录')
        localStorage.removeItem('token')
        window.location.href = '/login'
      } else if (error.response.data && error.response.data.message) {
        ElMessage.error(error.response.data.message)
      } else {
        ElMessage.error('网络错误，请稍后重试')
      }
    } else {
      ElMessage.error('网络连接失败')
    }
    return Promise.reject(error)
  }
)

export default request