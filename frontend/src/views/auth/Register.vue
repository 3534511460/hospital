<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-side">
        <h2>医预约</h2><p>创建账号开始使用</p>
        <div class="side-steps"><div><span>01</span> 填写信息</div><div><span>02</span> 完成注册</div><div><span>03</span> 开始预约</div></div>
      </div>
      <div class="auth-form">
        <h3>注册</h3>
        <div class="row"><div class="field"><label>用户名</label><input v-model="f.username" placeholder="3-50位"/></div><div class="field"><label>姓名</label><input v-model="f.realName" placeholder="真实姓名"/></div></div>
        <div class="field"><label>手机号</label><input v-model="f.phone" placeholder="11位手机号"/></div>
        <div class="row"><div class="field"><label>密码</label><input v-model="f.password" type="password" placeholder="6-30位"/></div><div class="field"><label>确认</label><input v-model="f.cpwd" type="password" placeholder="再次输入"/></div></div>
        <button class="btn btn-primary" style="width:100%;height:42px;font-size:15px" :disabled="loading" @click="register">{{ loading?'注册中…':'创建账号' }}</button>
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
.auth-page { min-height:100vh;display:grid;place-items:center;background:var(--bg);padding:24px; }
.auth-card { display:grid;grid-template-columns:280px 380px;border-radius:var(--radius);overflow:hidden;box-shadow:var(--shadow-md); }
.auth-side { background:var(--primary);color:#fff;padding:48px 36px;display:flex;flex-direction:column;justify-content:center; }
.auth-side h2 { font-size:26px;font-weight:700;margin-bottom:8px; }
.auth-side p { font-size:14px;opacity:.85;margin-bottom:24px; }
.side-steps div { font-size:13px;opacity:.8;margin-bottom:8px;display:flex;align-items:center;gap:8px; }
.side-steps span { font-size:18px;font-family:var(--serif);opacity:.5; }
.auth-form { background:var(--surface);padding:40px 32px; }
.auth-form h3 { font-size:22px;font-weight:600;color:var(--title);margin-bottom:24px; }
.field { margin-bottom:16px; }
.field label { display:block;font-size:13px;color:var(--body);margin-bottom:6px; }
.field input { width:100%;height:40px;padding:0 12px;border:1px solid var(--border);border-radius:var(--radius-sm);font-size:14px;color:var(--title);background:var(--surface);outline:none;transition:border-color .15s;font-family:inherit; }
.field input:focus { border-color:var(--primary); }
.field input::placeholder { color:var(--caption); }
.row { display:grid;grid-template-columns:1fr 1fr;gap:12px; }
.auth-foot { margin-top:20px;text-align:center;font-size:13px;color:var(--caption); }
.auth-foot a { color:var(--primary);cursor:pointer; }
@media(max-width:768px){ .auth-card{grid-template-columns:1fr}.auth-side{padding:28px 24px} }
</style>
