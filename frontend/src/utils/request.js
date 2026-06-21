import axios from 'axios'
import { ElMessage, ElNotification } from 'element-plus'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 15000
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code === 200) {
      return res
    }
    if (res.code === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
      window.location.href = '/login'
      return Promise.reject(new Error(res.msg || '认证失败'))
    }
    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  error => {
    if (error.response) {
      const { status } = error.response
      if (status === 401) {
        localStorage.clear()
        window.location.href = '/login'
      } else if (status === 403) {
        ElMessage.error('没有操作权限')
      } else if (status === 500) {
        ElMessage.error('服务器内部错误')
      }
    } else {
      ElMessage.error('网络连接异常')
    }
    return Promise.reject(error)
  }
)

export default request
