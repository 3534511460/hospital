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
      <div class="page-head" style="display:flex;justify-content:space-between;align-items:center"><h1>公告管理</h1><button class="btn btn-primary btn-sm" @click="openAdd">+ 发布公告</button></div>
      <div class="card" style="padding:0;overflow:hidden"><table class="tbl"><thead><tr><th>标题</th><th>状态</th><th>时间</th><th>操作</th></tr></thead>
      <tbody><tr v-for="a in list" :key="a.id"><td>{{ a.title }}</td><td><span :class="'tag '+(a.publishStatus===1?'tag-green':'tag-orange')">{{ a.publishStatus===1?'已发布':'草稿' }}</span></td><td>{{ a.createTime }}</td><td><button class="btn btn-ghost btn-sm" v-if="a.publishStatus!==1" @click="publish(a)">发布</button><button class="btn btn-ghost btn-sm" @click="del(a)">删除</button></td></tr></tbody></table></div>
    </div>
  </div>
  <div class="modal-bg" v-if="showAdd" @click.self="showAdd=false"><div class="modal"><h3>发布公告</h3>
    <div class="f"><label>标题</label><input v-model="f.title"/></div>
    <div class="f"><label>内容</label><textarea v-model="f.content" rows="4"/></div>
    <div class="mact"><button class="btn btn-primary" @click="add">发布</button><button class="btn btn-ghost" @click="showAdd=false">取消</button></div>
  </div></div>
</div></template>

<script setup>
import { ref,reactive,onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
const list=ref([]);const showAdd=ref(false);const f=reactive({title:'',content:''})
async function fetch(){try{const r=await request.get('/admin/announcements');list.value=r.data?.records||r.data||[]}catch{}}
function openAdd(){f.title='';f.content='';showAdd.value=true}
async function add(){await request.post('/admin/announcements',f);ElMessage.success('发布成功');showAdd.value=false;fetch()}
async function publish(a){await request.put(`/admin/announcements/${a.id}`,{...a,publishStatus:1});fetch()}
async function del(a){await request.delete(`/admin/announcements/${a.id}`);ElMessage.success('已删除');fetch()}
onMounted(fetch)
</script>

<style scoped>
.admin-layout{display:grid;grid-template-columns:180px 1fr;gap:24px}.admin-sidebar{background:var(--surface);border-radius:var(--radius);box-shadow:var(--shadow);padding:16px;position:sticky;top:80px;height:fit-content}.admin-sidebar h3{font-size:14px;color:var(--title);margin-bottom:12px;padding-bottom:8px;border-bottom:1px solid var(--border-light)}.admin-sidebar a{display:block;padding:8px 12px;font-size:13px;color:var(--body);cursor:pointer;border-radius:var(--radius-sm);margin-bottom:2px;transition:all .15s}.admin-sidebar a:hover,.admin-sidebar a.active{background:var(--primary-light);color:var(--primary)}.admin-main{min-width:0}.tbl{width:100%;border-collapse:collapse}.tbl th,.tbl td{padding:10px 14px;text-align:left;font-size:13px;border-bottom:1px solid var(--border-light)}.tbl th{font-weight:500;color:var(--caption);font-size:12px}.tbl tbody tr:hover{background:var(--bg)}.modal-bg{position:fixed;inset:0;background:rgba(0,0,0,.35);z-index:300;display:flex;align-items:center;justify-content:center}.modal{background:var(--surface);border-radius:var(--radius);padding:24px;width:400px;max-width:90vw;box-shadow:var(--shadow-md)}.modal h3{font-size:17px;color:var(--title);margin-bottom:16px}.f{margin-bottom:12px}.f label{display:block;font-size:13px;color:var(--body);margin-bottom:4px}.f input,.f textarea{width:100%;padding:8px 12px;border:1px solid var(--border);border-radius:var(--radius-sm);font-size:14px;font-family:inherit;color:var(--title);outline:none;background:var(--surface);resize:vertical}.f input:focus,.f textarea:focus{border-color:var(--primary)}.mact{display:flex;gap:8px;margin-top:16px}@media(max-width:768px){.admin-layout{grid-template-columns:1fr}.admin-sidebar{position:static;display:flex;gap:4px;flex-wrap:wrap;padding:8px}.admin-sidebar h3{display:none}.admin-sidebar a{font-size:12px;padding:4px 10px}}
</style>
