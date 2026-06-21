<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-side">
        <h2>医预约</h2><p>专业医疗预约服务平台</p>
        <div class="side-badge">V2.0</div>
      </div>
      <div class="auth-form">
        <h3>登录</h3>
        <div class="field"><label>用户名</label><input v-model="form.username" placeholder="请输入用户名" @keyup.enter="login"/></div>
        <div class="field"><label>密码</label><input v-model="form.password" type="password" placeholder="请输入密码" @keyup.enter="login"/></div>
        <button class="btn btn-primary" style="width:100%;height:42px;font-size:15px" :disabled="loading" @click="login">{{ loading?'登录中…':'登录' }}</button>
        <p class="auth-foot">还没有账号？<a @click="$router.push('/register')">立即注册</a></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive,ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
const router=useRouter()
const form=reactive({username:'',password:''})
const loading=ref(false)
async function login(){
  if(!form.username||!form.password) return ElMessage.warning('请输入用户名和密码')
  loading.value=true
  try{
    const res=await request.post('/auth/login',form)
    localStorage.setItem('token',res.data.accessToken)
    localStorage.setItem('user',JSON.stringify(res.data))
    ElMessage.success('登录成功');router.push('/home')
  }catch{}finally{loading.value=false}
}
</script>

<style scoped>
.auth-page { min-height:100vh;display:grid;place-items:center;background:var(--bg);padding:24px; }
.auth-card { display:grid;grid-template-columns:280px 360px;border-radius:var(--radius);overflow:hidden;box-shadow:var(--shadow-md); }
.auth-side { background:var(--primary);color:#fff;padding:48px 36px;display:flex;flex-direction:column;justify-content:center; }
.auth-side h2 { font-size:26px;font-weight:700;margin-bottom:8px; }
.auth-side p { font-size:14px;opacity:.85; }
.side-badge { margin-top:24px;display:inline-block;padding:4px 12px;border-radius:20px;border:1px solid rgba(255,255,255,.3);font-size:12px;width:fit-content; }
.auth-form { background:var(--surface);padding:48px 36px; }
.auth-form h3 { font-size:22px;font-weight:600;color:var(--title);margin-bottom:28px; }
.field { margin-bottom:18px; }
.field label { display:block;font-size:13px;color:var(--body);margin-bottom:6px; }
.field input { width:100%;height:40px;padding:0 12px;border:1px solid var(--border);border-radius:var(--radius-sm);font-size:14px;color:var(--title);background:var(--surface);outline:none;transition:border-color .15s;font-family:inherit; }
.field input:focus { border-color:var(--primary); }
.field input::placeholder { color:var(--caption); }
.auth-foot { margin-top:20px;text-align:center;font-size:13px;color:var(--caption); }
.auth-foot a { color:var(--primary);cursor:pointer; }
@media(max-width:768px){ .auth-card{grid-template-columns:1fr}.auth-side{padding:32px 24px} }
</style>
