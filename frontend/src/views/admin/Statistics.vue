<template>
  <div class="page-w page-main"><div class="admin-layout">
    <div class="admin-sidebar">
      <h3>系统管理</h3>
      <a :class="{active:$route.path==='/admin/users'}" @click="$router.push('/admin/users')">用户管理</a>
      <a :class="{active:$route.path==='/admin/doctors'}" @click="$router.push('/admin/doctors')">医生管理</a>
      <a :class="{active:$route.path==='/admin/departments'}" @click="$router.push('/admin/departments')">科室管理</a>
      <a :class="{active:$route.path==='/admin/announcements'}" @click="$router.push('/admin/announcements')">公告管理</a>
      <a :class="{active:$route.path==='/admin/statistics'}" @click="$router.push('/admin/statistics')">数据统计</a>
    </div>
    <div class="admin-main">
      <div class="page-head"><h1>数据统计</h1></div>
      <div class="stat-grid">
        <div class="card stat-card"><span class="n">{{ d.todayAppointments||0 }}</span><span>今日挂号</span></div>
        <div class="card stat-card"><span class="n">{{ d.totalPatients||0 }}</span><span>患者总数</span></div>
        <div class="card stat-card"><span class="n">{{ d.totalDoctors||0 }}</span><span>医生总数</span></div>
        <div class="card stat-card"><span class="n">{{ d.monthAppointments||0 }}</span><span>本月挂号</span></div>
      </div>
      <div class="card" v-if="d.topDoctors&&d.topDoctors.length" style="margin-top:16px">
        <h4 style="margin-bottom:12px;color:var(--title)">Top 医生</h4>
        <div class="rank" v-for="(r,i) in d.topDoctors" :key="i" style="display:flex;align-items:center;gap:12px;padding:8px 0;border-bottom:1px solid var(--border-light)">
          <span style="font-weight:600;color:var(--primary);min-width:20px">{{ i+1 }}</span><span style="flex:1;color:var(--title)">{{ r.name }}</span><span style="color:var(--caption);font-size:13px">{{ r.cnt }} 诊次</span>
        </div>
      </div>
    </div>
  </div></div>
</template>

<script setup>
import { ref,onMounted } from 'vue'
import request from '../../utils/request'
const d=ref({})
onMounted(async()=>{try{const r=await request.get('/admin/statistics/dashboard');d.value=r.data||{}}catch{}})
</script>

<style scoped>
.admin-layout{display:grid;grid-template-columns:180px 1fr;gap:24px}.admin-sidebar{background:var(--surface);border-radius:var(--radius);box-shadow:var(--shadow);padding:16px;position:sticky;top:80px;height:fit-content}.admin-sidebar h3{font-size:14px;color:var(--title);margin-bottom:12px;padding-bottom:8px;border-bottom:1px solid var(--border-light)}.admin-sidebar a{display:block;padding:8px 12px;font-size:13px;color:var(--body);cursor:pointer;border-radius:var(--radius-sm);margin-bottom:2px;transition:all .15s}.admin-sidebar a:hover,.admin-sidebar a.active{background:var(--primary-light);color:var(--primary)}.admin-main{min-width:0}.stat-grid{display:grid;grid-template-columns:repeat(4,1fr);gap:12px}.stat-card{text-align:center;padding:24px}.stat-card .n{display:block;font-size:32px;font-weight:700;color:var(--primary);margin-bottom:4px;font-family:var(--serif)}.stat-card span:last-child{font-size:13px;color:var(--caption)}@media(max-width:768px){.admin-layout{grid-template-columns:1fr}.admin-sidebar{position:static;display:flex;gap:4px;flex-wrap:wrap;padding:8px}.admin-sidebar h3{display:none}.admin-sidebar a{font-size:12px;padding:4px 10px}.stat-grid{grid-template-columns:repeat(2,1fr)}}
</style>
