<template>
  <div class="page-w page-main" style="max-width:540px">
    <div class="page-head"><h1>个人中心</h1></div>
    <div class="card" v-loading="loading">
      <div class="field"><label>头像</label><div class="av-row"><div class="av">{{ (profile.realName||'?')[0] }}</div><label class="btn btn-outline btn-sm" style="cursor:pointer">更换头像<input type="file" hidden accept="image/*" @change="uploadAvatar"/></label></div></div>
      <div class="field"><label>用户名</label><div class="val">{{ profile.username }}</div></div>
      <div class="field"><label>角色</label><div class="val">{{ profile.role }}</div></div>
      <div class="field"><label>真实姓名</label><input v-model="e.realName"/></div>
      <div class="row"><div class="field"><label>性别</label><select v-model="e.gender"><option :value="0">未知</option><option :value="1">男</option><option :value="2">女</option></select></div><div class="field"><label>年龄</label><input v-model="e.age" type="number"/></div></div>
      <div class="row"><div class="field"><label>手机</label><input v-model="e.phone"/></div><div class="field"><label>邮箱</label><input v-model="e.email"/></div></div>
      <button class="btn btn-primary" @click="save">保存修改</button>
    </div>
    <div class="card" style="margin-top:16px">
      <h4 style="margin-bottom:16px;color:var(--title)">修改密码</h4>
      <div class="field"><label>原密码</label><input v-model="p.old" type="password"/></div>
      <div class="field"><label>新密码</label><input v-model="p.new" type="password"/></div>
      <button class="btn btn-primary" @click="changePwd">修改密码</button>
    </div>
  </div>
</template>

<script setup>
import { ref,reactive,onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
const loading=ref(false);const profile=ref({});const e=reactive({realName:'',gender:0,age:null,phone:'',email:''});const p=reactive({old:'',new:''})
async function fetch(){loading.value=true;try{const r=await request.get('/user/profile');profile.value=r.data;Object.assign(e,{realName:r.data.realName,gender:r.data.gender||0,age:r.data.age,phone:r.data.phone,email:r.data.email})}catch{}finally{loading.value=false}}
async function save(){try{await request.put('/user/profile',e);ElMessage.success('保存成功')}catch{}}
async function changePwd(){if(!p.old||!p.new)return ElMessage.warning('请填写密码');try{await request.put('/user/password',{oldPassword:p.old,newPassword:p.new});ElMessage.success('密码修改成功');p.old='';p.new=''}catch{}}
async function uploadAvatar(ev){const f=ev.target.files[0];if(!f)return;const fd=new FormData();fd.append('file',f);try{const r=await request.post('/file/upload',fd,{headers:{'Content-Type':'multipart/form-data'}});await request.put('/user/avatar',{avatarUrl:r.data.url});profile.value.avatar=r.data.url;ElMessage.success('头像已更新')}catch{}}
onMounted(fetch)
</script>

<style scoped>
.field { margin-bottom:14px; }
.field label { display:block;font-size:13px;color:var(--body);margin-bottom:5px; }
.field input,.field select { width:100%;height:38px;padding:0 12px;border:1px solid var(--border);border-radius:var(--radius-sm);font-size:14px;color:var(--title);background:var(--surface);font-family:inherit;outline:none; }
.field input:focus,.field select:focus { border-color:var(--primary); }
.val { padding:8px 0;font-size:14px;color:var(--caption);border-bottom:1px solid var(--border-light); }
.row { display:grid;grid-template-columns:1fr 1fr;gap:12px; }
.av-row { display:flex;align-items:center;gap:14px; }
.av { width:48px;height:48px;border-radius:50%;background:var(--primary-light);color:var(--primary);display:flex;align-items:center;justify-content:center;font-size:20px;font-weight:600; }
</style>
