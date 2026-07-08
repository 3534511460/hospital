import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('../views/auth/Login.vue'), meta: { public: true } },
  { path: '/register', name: 'Register', component: () => import('../views/auth/Register.vue'), meta: { public: true } },
  { path: '/', redirect: '/home' },
  { path: '/home', name: 'Home', component: () => import('../views/patient/Home.vue') },
  { path: '/doctors', name: 'DoctorList', component: () => import('../views/patient/DoctorList.vue') },
  { path: '/doctor/:id', name: 'DoctorDetail', component: () => import('../views/patient/DoctorDetail.vue') },
  { path: '/appointment', name: 'Appointment', component: () => import('../views/patient/Appointment.vue') },
  { path: '/my-appointments', name: 'MyAppointments', component: () => import('../views/patient/MyAppointments.vue') },
  { path: '/profile', name: 'Profile', component: () => import('../views/patient/Profile.vue') },
  { path: '/messages', name: 'Messages', component: () => import('../views/patient/Messages.vue') },
  { path: '/my-records', name: 'MyRecords', component: () => import('../views/patient/MyRecords.vue') },
  { path: '/ai', name: 'AiChat', component: () => import('../views/ai/AiChat.vue') },
  { path: '/queue', name: 'QueueCalling', component: () => import('../views/doctor/QueueCalling.vue') },
  { path: '/queue/board', name: 'QueueBoard', component: () => import('../views/doctor/QueueBoard.vue'), meta: { public: true } },
  { path: '/schedule', name: 'DoctorSchedule', component: () => import('../views/doctor/Schedule.vue') },
  { path: '/doctor-appointments', name: 'DoctorAppointments', component: () => import('../views/doctor/Appointments.vue') },
  { path: '/admin/users', name: 'AdminUsers', component: () => import('../views/admin/UserManage.vue'), meta: { role: 'SYS_ADMIN' } },
  { path: '/admin/doctors', name: 'AdminDoctors', component: () => import('../views/admin/DoctorManage.vue'), meta: { role: 'SYS_ADMIN,DEPT_ADMIN' } },
  { path: '/admin/schedules', name: 'AdminSchedules', component: () => import('../views/admin/ScheduleManage.vue'), meta: { role: 'SYS_ADMIN,DEPT_ADMIN' } },
  { path: '/admin/departments', name: 'AdminDepartments', component: () => import('../views/admin/DeptManage.vue'), meta: { role: 'SYS_ADMIN' } },
  { path: '/admin/announcements', name: 'AdminAnnouncements', component: () => import('../views/admin/AnnounceManage.vue'), meta: { role: 'SYS_ADMIN,DEPT_ADMIN' } },
  { path: '/admin/statistics', name: 'AdminStats', component: () => import('../views/admin/Statistics.vue'), meta: { role: 'SYS_ADMIN,DEPT_ADMIN' } },
  { path: '/chat', name: 'ChatRoom', component: () => import('../views/chat/ChatRoom.vue') },
  { path: '/admin', redirect: '/admin/users' },
]

const router = createRouter({ history: createWebHistory(), routes })

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  const role = user?.role || ''

  if (!to.meta.public && !token) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }
  if (to.meta.public && token && (to.path === '/login' || to.path === '/register')) {
    return next('/home')
  }
  if (to.meta.role && !to.meta.role.split(',').includes(role)) {
    return next('/home')
  }
  next()
})

export default router
