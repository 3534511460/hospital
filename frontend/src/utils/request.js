import axios from 'axios'
import { ElMessage, ElNotification } from 'element-plus'
import router from '../router'

let isRedirecting = false

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 15000
})

function handleAuthError() {
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('user')
  if (!isRedirecting) {
    isRedirecting = true
    router.push('/login').finally(() => { isRedirecting = false })
  }
}

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
      handleAuthError()
      return Promise.reject(new Error(res.msg || '认证失败'))
    }
    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  error => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        handleAuthError()
      } else if (status === 403) {
        ElMessage.error(data?.msg || '没有操作权限')
      } else if (status === 400) {
        ElMessage.error(data?.msg || '请求参数有误')
      } else if (status === 500) {
        ElMessage.error(data?.msg || '服务器内部错误')
      }
    } else {
      ElMessage.error('网络连接异常')
    }
    return Promise.reject(error)
  }
)

export default request
