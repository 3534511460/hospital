<template>
  <div class="page-w page-main">
    <div class="admin-layout">
      <div class="admin-sidebar">
        <h3>系统管理</h3>
        <a :class="{active:$route.path==='/admin/users'}" @click="$router.push('/admin/users')">用户管理</a>
        <a :class="{active:$route.path==='/admin/doctors'}" @click="$router.push('/admin/doctors')">医生管理</a>
        <a :class="{active:$route.path==='/admin/departments'}" @click="$router.push('/admin/departments')">科室管理</a>
        <a :class="{active:$route.path==='/admin/announcements'}" @click="$router.push('/admin/announcements')">公告管理</a>
        <a :class="{active:$route.path==='/admin/statistics'}" @click="$router.push('/admin/statistics')">数据统计</a>
      </div>
      <div class="admin-main">
        <div class="page-head"><h1>用户管理</h1></div>
        <div class="card" style="padding:0;overflow:hidden" v-loading="loading">
          <table class="tbl"><thead><tr><th>ID</th><th>用户名</th><th>姓名</th><th>手机</th><th>角色</th><th>状态</th><th>操作</th></tr></thead>
          <tbody><tr v-for="u in users" :key="u.id"><td>{{ u.id }}</td><td>{{ u.username }}</td><td>{{ u.realName||'-' }}</td><td>{{ u.phone||'-' }}</td><td><span class="tag" :class="roleTag(u.role)">{{ u.role }}</span></td><td><span class="tag" :class="u.status===1?'tag-green':'tag-red'">{{ u.status===1?'启用':'禁用' }}</span></td><td>
            <button class="btn btn-ghost btn-sm" @click="toggle(u)">{{ u.status===1?'禁用':'启用' }}</button>
            <button class="btn btn-ghost btn-sm" @click="resetPwd(u)">重置密码</button>
          </td></tr></tbody></table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref,onMounted } from 'vue'
import { ElMessage,ElMessageBox } from 'element-plus'
import request from '../../utils/request'
const users=ref([]);const loading=ref(false)
function roleTag(r){return r==='SYS_ADMIN'?'tag-blue':r==='DOCTOR'?'tag-green':r==='DEPT_ADMIN'?'tag-orange':'tag'}
async function fetch(){loading.value=true;try{const r=await request.get('/admin/users');users.value=r.data?.records||r.data||[]}catch{}finally{loading.value=false}}
async function toggle(u){try{await request.put(`/admin/users/${u.id}/status`,{status:u.status===1?0:1});ElMessage.success('已更新');fetch()}catch{}}
async function resetPwd(u){try{await ElMessageBox.confirm(`确认重置 ${u.username} 密码？`);await request.put(`/admin/users/${u.id}/reset-pwd`,{password:'123456'});ElMessage.success('已重置为123456')}catch{}}
onMounted(fetch)
</script>

<style scoped>
.admin-layout { display:grid;grid-template-columns:180px 1fr;gap:24px; }
.admin-sidebar { background:var(--surface);border-radius:var(--radius);box-shadow:var(--shadow);padding:16px;position:sticky;top:80px;height:fit-content; }
.admin-sidebar h3 { font-size:14px;color:var(--title);margin-bottom:12px;padding-bottom:8px;border-bottom:1px solid var(--border-light); }
.admin-sidebar a { display:block;padding:8px 12px;font-size:13px;color:var(--body);cursor:pointer;border-radius:var(--radius-sm);margin-bottom:2px;transition:all .15s; }
.admin-sidebar a:hover,.admin-sidebar a.active { background:var(--primary-light);color:var(--primary); }
.admin-main { min-width:0; }
.tbl { width:100%;border-collapse:collapse; }
.tbl th,.tbl td { padding:10px 14px;text-align:left;font-size:13px;border-bottom:1px solid var(--border-light); }
.tbl th { font-weight:500;color:var(--caption);font-size:12px; }
.tbl tbody tr:hover { background:var(--bg); }
@media(max-width:768px){ .admin-layout{grid-template-columns:1fr}.admin-sidebar{position:static;display:flex;gap:4px;flex-wrap:wrap;padding:8px}.admin-sidebar h3{display:none}.admin-sidebar a{font-size:12px;padding:4px 10px} }
</style>
