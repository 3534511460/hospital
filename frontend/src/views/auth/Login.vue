<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-side">
        <div class="brand"><span class="brand-mark"><i></i><i></i></span><strong>医预约</strong></div>
        <div class="side-copy"><p>MEDRESERVE</p><h2>安心就医，<br>从一次清晰的预约开始。</h2></div>
        <div class="side-meta"><span>20+ 临床科室</span><span>安全 · 专业 · 便捷</span></div>
      </div>
      <div class="auth-form">
        <p class="form-kicker">用户登录</p><h3>欢迎回来</h3><p class="form-note">登录后继续管理预约与就诊记录</p>
        <div class="field"><label>用户名</label><input v-model="form.username" placeholder="请输入用户名" @keyup.enter="login"/></div>
        <div class="field"><label>密码</label><input v-model="form.password" type="password" placeholder="请输入密码" @keyup.enter="login"/></div>
        <button class="btn btn-primary submit-button" :disabled="loading" @click="login">{{ loading?'登录中…':'登录' }}<el-icon v-if="!loading"><ArrowRight /></el-icon></button>
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
.auth-page { min-height: 100vh; display: grid; place-items: center; padding: 32px; background: #eef2ed; }
.auth-card { width: min(940px, 100%); min-height: 590px; display: grid; grid-template-columns: 1.05fr .95fr; overflow: hidden; border: 1px solid #d5ddd7; border-radius: 8px; background: var(--surface); box-shadow: 0 24px 70px rgba(23, 63, 49, .12); }
.auth-side { position: relative; overflow: hidden; display: flex; flex-direction: column; justify-content: space-between; padding: 42px; color: #fff; background: #173f31; }
.auth-side::before, .auth-side::after { content: ''; position: absolute; background: rgba(255,255,255,.07); }
.auth-side::before { right: -80px; top: 165px; width: 320px; height: 76px; }
.auth-side::after { right: 42px; top: 43px; width: 76px; height: 320px; }
.brand { position: relative; z-index: 1; display: flex; align-items: center; gap: 10px; }
.brand-mark { position: relative; width: 30px; height: 30px; background: #f0f5ed; }
.brand-mark i { position: absolute; left: 6px; top: 13px; width: 18px; height: 4px; background: #173f31; }
.brand-mark i:last-child { left: 13px; top: 6px; width: 4px; height: 18px; }
.brand strong { font-size: 16px; }
.side-copy { position: relative; z-index: 1; max-width: 360px; }
.side-copy p { margin-bottom: 16px; color: #9ec5aa; font-size: 11px; font-weight: 700; }
.side-copy h2 { font-size: clamp(32px, 3.3vw, 44px); font-weight: 580; line-height: 1.35; letter-spacing: 0; }
.side-meta { position: relative; z-index: 1; display: flex; justify-content: space-between; padding-top: 20px; border-top: 1px solid rgba(255,255,255,.22); color: #b8cec0; font-size: 11px; }
.auth-form { display: flex; flex-direction: column; justify-content: center; padding: clamp(42px, 6vw, 72px); background: var(--surface); }
.form-kicker { margin-bottom: 8px; color: var(--primary); font-size: 12px; font-weight: 700; }
.auth-form h3 { color: var(--title); font-size: 30px; font-weight: 650; letter-spacing: 0; }
.form-note { margin: 6px 0 32px; color: var(--caption); font-size: 13px; }
.field { margin-bottom: 18px; }
.field label { display: block; margin-bottom: 7px; color: var(--body); font-size: 12px; font-weight: 600; }
.field input { width: 100%; height: 44px; padding: 0 13px; border: 1px solid var(--border); border-radius: var(--radius-sm); color: var(--title); background: var(--surface); font: inherit; font-size: 14px; outline: none; transition: border-color .18s, box-shadow .18s; }
.field input:focus { border-color: var(--primary); box-shadow: 0 0 0 3px var(--primary-light); }
.field input::placeholder { color: var(--caption); }
.submit-button { width: 100%; height: 44px; margin-top: 4px; justify-content: space-between; padding: 0 16px; font-size: 14px; }
.auth-foot { margin-top: 22px; color: var(--caption); font-size: 13px; text-align: center; }
.auth-foot a { color: var(--primary); font-weight: 600; cursor: pointer; }
@media (max-width: 760px) { .auth-page { padding: 0; background: var(--surface); } .auth-card { min-height: 100vh; grid-template-columns: 1fr; border: 0; box-shadow: none; } .auth-side { min-height: 220px; padding: 28px 24px; } .side-copy h2 { font-size: 27px; } .side-meta { display: none; } .auth-form { padding: 42px 24px 52px; } }
</style>
