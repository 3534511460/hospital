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
      <div class="page-head" style="display:flex;justify-content:space-between;align-items:center"><h1>科室管理</h1><button class="btn btn-primary btn-sm" @click="openAdd">+ 添加科室</button></div>
      <div class="card" style="padding:0;overflow:hidden"><table class="tbl"><thead><tr><th>ID</th><th>名称</th><th>编码</th><th>描述</th><th>操作</th></tr></thead>
      <tbody><tr v-for="d in list" :key="d.id"><td>{{ d.id }}</td><td>{{ d.name }}</td><td>{{ d.code }}</td><td class="desc">{{ (d.description||'').slice(0,60) }}</td><td><button class="btn btn-ghost btn-sm" @click="del(d)">删除</button></td></tr></tbody></table></div>
    </div>
  </div>
  <!-- Modal -->
  <div class="modal-bg" v-if="showAdd" @click.self="showAdd=false"><div class="modal"><h3>添加科室</h3>
    <div class="f"><label>名称</label><input v-model="f.name"/></div>
    <div class="f"><label>编码</label><input v-model="f.code"/></div>
    <div class="f"><label>描述</label><input v-model="f.description"/></div>
    <div class="mact"><button class="btn btn-primary" @click="add">确认</button><button class="btn btn-ghost" @click="showAdd=false">取消</button></div>
  </div></div>
</div></template>

<script setup>
import { ref,reactive,onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
const list=ref([]);const showAdd=ref(false);const f=reactive({name:'',code:'',description:''})
async function fetch(){try{const r=await request.get('/admin/departments');list.value=r.data||[]}catch{}}
function openAdd(){f.name='';f.code='';f.description='';showAdd.value=true}
async function add(){await request.post('/admin/departments',f);ElMessage.success('添加成功');showAdd.value=false;fetch()}
async function del(d){await request.delete(`/admin/departments/${d.id}`);ElMessage.success('已删除');fetch()}
onMounted(fetch)
</script>

<style scoped>
.admin-layout{display:grid;grid-template-columns:180px 1fr;gap:24px}.admin-sidebar{background:var(--surface);border-radius:var(--radius);box-shadow:var(--shadow);padding:16px;position:sticky;top:80px;height:fit-content}.admin-sidebar h3{font-size:14px;color:var(--title);margin-bottom:12px;padding-bottom:8px;border-bottom:1px solid var(--border-light)}.admin-sidebar a{display:block;padding:8px 12px;font-size:13px;color:var(--body);cursor:pointer;border-radius:var(--radius-sm);margin-bottom:2px;transition:all .15s}.admin-sidebar a:hover,.admin-sidebar a.active{background:var(--primary-light);color:var(--primary)}.admin-main{min-width:0}.tbl{width:100%;border-collapse:collapse}.tbl th,.tbl td{padding:10px 14px;text-align:left;font-size:13px;border-bottom:1px solid var(--border-light)}.tbl th{font-weight:500;color:var(--caption);font-size:12px}.tbl tbody tr:hover{background:var(--bg)}.desc{color:var(--caption);max-width:300px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}.modal-bg{position:fixed;inset:0;background:rgba(0,0,0,.35);z-index:300;display:flex;align-items:center;justify-content:center}.modal{background:var(--surface);border-radius:var(--radius);padding:24px;width:400px;max-width:90vw;box-shadow:var(--shadow-md)}.modal h3{font-size:17px;color:var(--title);margin-bottom:16px}.f{margin-bottom:12px}.f label{display:block;font-size:13px;color:var(--body);margin-bottom:4px}.f input{width:100%;height:38px;padding:0 12px;border:1px solid var(--border);border-radius:var(--radius-sm);font-size:14px;font-family:inherit;color:var(--title);outline:none;background:var(--surface)}.f input:focus{border-color:var(--primary)}.mact{display:flex;gap:8px;margin-top:16px}@media(max-width:768px){.admin-layout{grid-template-columns:1fr}.admin-sidebar{position:static;display:flex;gap:4px;flex-wrap:wrap;padding:8px}.admin-sidebar h3{display:none}.admin-sidebar a{font-size:12px;padding:4px 10px}}
</style>
