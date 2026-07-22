<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-side">
        <div class="brand"><span class="brand-mark"><i></i><i></i></span><strong>医预约</strong></div>
        <div class="side-copy"><p>MEDRESERVE</p><h2>建立你的<br>个人健康服务入口。</h2></div>
        <div class="side-steps"><div><span>01</span> 填写基本信息</div><div><span>02</span> 创建就诊账号</div><div><span>03</span> 开始预约服务</div></div>
      </div>
      <div class="auth-form">
        <p class="form-kicker">创建账号</p><h3>注册</h3><p class="form-note">请填写真实信息，以便完成后续就诊服务</p>
        <div class="row"><div class="field"><label>用户名</label><input v-model="f.username" placeholder="3-50位"/></div><div class="field"><label>姓名</label><input v-model="f.realName" placeholder="真实姓名"/></div></div>
        <div class="field"><label>手机号</label><input v-model="f.phone" placeholder="11位手机号"/></div>
        <div class="row"><div class="field"><label>密码</label><input v-model="f.password" type="password" placeholder="6-30位"/></div><div class="field"><label>确认</label><input v-model="f.cpwd" type="password" placeholder="再次输入"/></div></div>
        <button class="btn btn-primary submit-button" :disabled="loading" @click="register">{{ loading?'注册中…':'创建账号' }}<el-icon v-if="!loading"><ArrowRight /></el-icon></button>
        <p class="auth-foot">已有账号？<a @click="$router.push('/login')">立即登录</a></p>
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
const f=reactive({username:'',realName:'',phone:'',password:'',cpwd:''})
const loading=ref(false)
async function register(){
  console.log('register clicked')
  if(!f.username||!f.realName||!f.phone||!f.password) return ElMessage.warning('请填写完整信息')
  if(f.password!==f.cpwd) return ElMessage.warning('两次密码不一致')
  if(f.password.length<6) return ElMessage.warning('密码至少6位')
  loading.value=true
  console.log('calling API...')
  try{
    await request.post('/auth/register',{username:f.username,realName:f.realName,phone:f.phone,password:f.password})
    ElMessage.success('注册成功')
    router.push('/login')
  }catch(e){
    console.error('register error:', e)
  }finally{
    loading.value=false
  }
}
</script>

<style scoped>
.auth-page { min-height: 100vh; display: grid; place-items: center; padding: 32px; background: #eef2ed; }
.auth-card { width: min(980px, 100%); min-height: 640px; display: grid; grid-template-columns: 1fr 1.1fr; overflow: hidden; border: 1px solid #d5ddd7; border-radius: 8px; background: var(--surface); box-shadow: 0 24px 70px rgba(23, 63, 49, .12); }
.auth-side { position: relative; overflow: hidden; display: flex; flex-direction: column; justify-content: space-between; padding: 42px; color: #fff; background: #173f31; }
.auth-side::before, .auth-side::after { content: ''; position: absolute; background: rgba(255,255,255,.07); }
.auth-side::before { right: -90px; top: 185px; width: 340px; height: 76px; }
.auth-side::after { right: 38px; top: 54px; width: 76px; height: 340px; }
.brand { position: relative; z-index: 1; display: flex; align-items: center; gap: 10px; }
.brand-mark { position: relative; width: 30px; height: 30px; background: #f0f5ed; }
.brand-mark i { position: absolute; left: 6px; top: 13px; width: 18px; height: 4px; background: #173f31; }
.brand-mark i:last-child { left: 13px; top: 6px; width: 4px; height: 18px; }
.brand strong { font-size: 16px; }
.side-copy { position: relative; z-index: 1; }
.side-copy p { margin-bottom: 16px; color: #9ec5aa; font-size: 11px; font-weight: 700; }
.side-copy h2 { font-size: clamp(32px, 3.3vw, 44px); font-weight: 580; line-height: 1.35; letter-spacing: 0; }
.side-steps { position: relative; z-index: 1; padding-top: 18px; border-top: 1px solid rgba(255,255,255,.22); }
.side-steps div { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; color: #b8cec0; font-size: 12px; }
.side-steps span { color: #7ea98c; font-size: 11px; font-weight: 700; }
.auth-form { display: flex; flex-direction: column; justify-content: center; padding: clamp(38px, 5vw, 62px); background: var(--surface); }
.form-kicker { margin-bottom: 7px; color: var(--primary); font-size: 12px; font-weight: 700; }
.auth-form h3 { color: var(--title); font-size: 30px; font-weight: 650; letter-spacing: 0; }
.form-note { margin: 5px 0 26px; color: var(--caption); font-size: 13px; }
.field { margin-bottom: 15px; }
.field label { display: block; margin-bottom: 7px; color: var(--body); font-size: 12px; font-weight: 600; }
.field input { width: 100%; height: 44px; padding: 0 13px; border: 1px solid var(--border); border-radius: var(--radius-sm); color: var(--title); background: var(--surface); font: inherit; font-size: 14px; outline: none; transition: border-color .18s, box-shadow .18s; }
.field input:focus { border-color: var(--primary); box-shadow: 0 0 0 3px var(--primary-light); }
.field input::placeholder { color: var(--caption); }
.row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.submit-button { width: 100%; height: 44px; margin-top: 3px; justify-content: space-between; padding: 0 16px; font-size: 14px; }
.auth-foot { margin-top: 20px; color: var(--caption); font-size: 13px; text-align: center; }
.auth-foot a { color: var(--primary); font-weight: 600; cursor: pointer; }
@media (max-width: 760px) { .auth-page { padding: 0; background: var(--surface); } .auth-card { min-height: 100vh; grid-template-columns: 1fr; border: 0; box-shadow: none; } .auth-side { min-height: 210px; padding: 26px 24px; } .side-copy h2 { font-size: 26px; } .side-steps { display: none; } .auth-form { padding: 38px 24px 48px; } .row { grid-template-columns: 1fr; gap: 0; } }
</style>
