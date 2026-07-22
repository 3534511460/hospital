<template>
  <div class="layout">
    <header class="top-nav">
      <div class="nav-inner page-w">
        <button class="logo" aria-label="返回首页" @click="$router.push('/home')">
          <span class="logo-icon" aria-hidden="true"><i></i><i></i></span>
          <span class="logo-text">医预约<small>MEDRESERVE</small></span>
        </button>
        <nav class="nav-links" v-if="isLoggedIn">
          <a :class="{ active:$route.path==='/home' }" @click="$router.push('/home')">首页</a>
          <a v-if="userRole!=='DOCTOR'" :class="{ active:$route.path==='/doctors' }" @click="$router.push('/doctors')">找医生</a>
          <a v-if="userRole==='PATIENT'" :class="{ active:$route.path==='/my-appointments' }" @click="$router.push('/my-appointments')">我的预约</a>
          <a v-if="userRole==='PATIENT'" :class="{ active:$route.path==='/my-records' }" @click="$router.push('/my-records')">我的病历</a>
          <a v-if="userRole!=='DOCTOR'" :class="{ active:$route.path==='/ai' }" @click="$router.push('/ai')">AI导诊</a>
          <a v-if="userRole==='DOCTOR'" :class="{ active:$route.path==='/schedule' }" @click="$router.push('/schedule')">排班</a>
          <a v-if="userRole==='DOCTOR'" :class="{ active:$route.path==='/doctor-appointments' }" @click="$router.push('/doctor-appointments')">预约管理</a>
          <a v-if="userRole==='DOCTOR'||userRole==='DEPT_ADMIN'" :class="{ active:$route.path==='/queue' }" @click="$router.push('/queue')">叫号</a>
          <a :class="{ active:$route.path==='/chat' }" @click="$router.push('/chat')" style="position:relative">{{ userRole==='DOCTOR'?'患者咨询':'问医生' }}<span class="nav-badge" v-if="unreadChatCount>0">{{ unreadChatCount>99?'99+':unreadChatCount }}</span></a>
          <a v-if="userRole==='SYS_ADMIN'" :class="{ active:$route.path.startsWith('/admin') }" @click="$router.push('/admin')" style="color:var(--primary)">管理</a>
        </nav>
        <div class="nav-right">
          <button class="icon-btn theme-btn" @click="toggleTheme" :title="dark?'切换亮色模式':'切换暗色模式'">
            <el-icon><Sunny v-if="dark"/><Moon v-else/></el-icon>
          </button>
          <div class="msg-bell" v-if="isLoggedIn" @click.stop="bellOpen=!bellOpen;if(bellOpen)loadBell()" title="消息中心">
            <button class="icon-btn" aria-label="消息中心"><el-icon><Bell /></el-icon></button>
            <span class="nav-badge msg-badge" v-if="unreadMsgCount>0">{{ unreadMsgCount>99?'99+':unreadMsgCount }}</span>
            <div class="bell-panel" v-if="bellOpen" @click.stop>
              <div class="bell-head">
                <span>消息中心</span>
                <a @click="bellOpen=false;$router.push('/messages')">查看全部 →</a>
              </div>
              <div class="bell-body" v-loading="bellLoading">
                <!-- Pending leave requests for admins -->
                <template v-if="isAdmin&&pendingLeaves.length">
                  <div class="bell-section-title">待审核停诊申请</div>
                  <div class="bell-leave-item" v-for="s in pendingLeaves" :key="s.id">
                    <div class="bell-leave-info">
                      <span class="bell-leave-doc">{{ s.doctorName }}</span>
                      <span class="bell-leave-date">{{ s.workDate }} {{ s.timeSlot }}</span>
                    </div>
                    <div class="bell-leave-acts">
                      <button class="btn btn-primary btn-sm" @click="approveLeave(s)" :disabled="s._acting">通过</button>
                      <button class="btn btn-ghost btn-sm" @click="rejectLeave(s)" :disabled="s._acting">驳回</button>
                    </div>
                  </div>
                  <div class="bell-divider"></div>
                </template>
                <!-- Recent messages -->
                <div class="bell-msg-item" v-for="m in bellMsgs" :key="m.id" @click="bellOpen=false;$router.push('/messages')">
                  <div class="bell-msg-dot" v-if="!m.isRead"></div>
                  <div class="bell-msg-c">
                    <div class="bell-msg-title">{{ m.title }}</div>
                    <div class="bell-msg-content">{{ m.content }}</div>
                  </div>
                </div>
                <div class="bell-empty" v-if="!bellLoading&&!pendingLeaves.length&&!bellMsgs.length">暂无新消息</div>
              </div>
            </div>
          </div>
          <template v-if="isLoggedIn">
            <div class="user-dd" @click.stop="menu=!menu">
              <span class="user-name">{{ userInfo?.realName || '用户' }}</span>
              <span class="avatar-sm">{{ (userInfo?.realName||'?')[0] }}</span>
              <div class="dd-panel" v-if="menu">
                <a @click="go('/profile')">个人中心</a>
                <a v-if="userRole==='SYS_ADMIN'" @click="go('/admin')">系统管理</a>
                <hr><a @click="logout">退出登录</a>
              </div>
            </div>
          </template>
          <template v-else>
            <a class="btn btn-outline btn-sm" @click="$router.push('/login')">登录</a>
            <a class="btn btn-primary btn-sm" @click="$router.push('/register')">注册</a>
          </template>
          <button class="icon-btn burger" aria-label="打开导航" @click="drawer=true" v-if="isLoggedIn"><el-icon><Menu /></el-icon></button>
        </div>
      </div>
    </header>
    <div class="drawer-bg" v-if="drawer" @click="drawer=false"/>
    <aside class="drawer" :class="{open:drawer}">
      <button class="icon-btn drawer-close" aria-label="关闭导航" @click="drawer=false"><el-icon><Close /></el-icon></button>
      <nav><a @click="go('/home');drawer=false">首页</a><a v-if="userRole!=='DOCTOR'" @click="go('/doctors');drawer=false">找医生</a><a v-if="userRole==='PATIENT'" @click="go('/my-appointments');drawer=false">我的预约</a><a v-if="userRole==='PATIENT'" @click="go('/my-records');drawer=false">我的病历</a><a v-if="userRole!=='DOCTOR'" @click="go('/ai');drawer=false">AI导诊</a><a v-if="userRole==='DOCTOR'" @click="go('/schedule');drawer=false">排班</a><a v-if="userRole==='DOCTOR'||userRole==='DEPT_ADMIN'" @click="go('/queue');drawer=false">叫号</a><a @click="go('/chat');drawer=false" style="position:relative">{{ userRole==='DOCTOR'?'患者咨询':'问医生' }}<span class="nav-badge" v-if="unreadChatCount>0">{{ unreadChatCount>99?'99+':unreadChatCount }}</span></a><hr><a v-if="userRole==='SYS_ADMIN'" @click="go('/admin');drawer=false">系统管理</a><a @click="go('/profile');drawer=false">个人中心</a><a @click="logout">退出</a></nav>
    </aside>
    <main class="page-main"><router-view v-slot="{ Component }"><transition name="page-fade" mode="out-in"><component :is="Component"/></transition></router-view></main>
  </div>
</template>

<script setup>
import { ref,computed,onMounted,onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { unreadChatCount,updateUnread } from '../utils/chatStore'
import { unreadMsgCount,fetchUnreadMsg } from '../utils/messageStore'
import request from '../utils/request'
const router=useRouter()
const menu=ref(false),drawer=ref(false),dark=ref(false)
const isLoggedIn=computed(()=>!!localStorage.getItem('token'))
const userRole=computed(()=>{const u=localStorage.getItem('user');return u?JSON.parse(u).role:''})
const userInfo=computed(()=>{const u=localStorage.getItem('user');return u?JSON.parse(u):null})
function go(p){menu.value=false;router.push(p)}
function logout(){localStorage.clear();router.push('/login')}
function toggleTheme(){dark.value=!dark.value;document.documentElement.setAttribute('data-theme',dark.value?'dark':'');localStorage.setItem('theme',dark.value?'dark':'light')}
function clk(e){if(menu.value&&!e.target.closest('.user-dd'))menu.value=false;if(bellOpen.value&&!e.target.closest('.msg-bell'))bellOpen.value=false}
let chatTimer=null
async function pollUnread(){if(!isLoggedIn.value)return;try{const r=await request.get('/chat/sessions');updateUnread(r.data||[],userRole.value)}catch{}}

// Bell panel
const bellOpen=ref(false);const bellLoading=ref(false);const bellMsgs=ref([]);const pendingLeaves=ref([])
const isAdmin=computed(()=>{const r=userRole.value;return r==='SYS_ADMIN'||r==='DEPT_ADMIN'})
async function loadBell(){
  bellLoading.value=true
  try{
    const r=await request.get('/message/messages',{params:{page:1,size:5}})
    bellMsgs.value=(r.data||[]).filter(m=>!m.isRead)
    fetchUnreadMsg(request)
  }catch{}
  if(isAdmin.value){
    try{
      const r=await request.get('/schedule/all')
      const today=new Date().toISOString().slice(0,10)
      pendingLeaves.value=(r.data||[]).filter(s=>s.status===0&&s.workDate>=today).map(s=>({...s,_acting:false}))
    }catch{}
  }
  bellLoading.value=false
}
async function approveLeave(s){
  s._acting=true
  try{await request.put(`/schedule/${s.id}`,{status:1});ElMessage.success('已通过');pendingLeaves.value=pendingLeaves.value.filter(x=>x.id!==s.id);fetchUnreadMsg(request)}catch{}

  finally{s._acting=false}
}
async function rejectLeave(s){
  s._acting=true
  try{await request.delete(`/schedule/${s.id}`);ElMessage.success('已驳回');pendingLeaves.value=pendingLeaves.value.filter(x=>x.id!==s.id);fetchUnreadMsg(request)}catch{}
  finally{s._acting=false}
}
onMounted(()=>{dark.value=localStorage.getItem('theme')==='dark';if(dark.value)document.documentElement.setAttribute('data-theme','dark');document.addEventListener('click',clk);pollUnread();fetchUnreadMsg(request);chatTimer=setInterval(()=>{pollUnread();fetchUnreadMsg(request)},30000)})
onUnmounted(()=>{document.removeEventListener('click',clk);clearInterval(chatTimer)})
</script>

<style scoped>
.top-nav { position: sticky; top: 0; z-index: 100; height: var(--nav-h); display: flex; align-items: center; border-bottom: 1px solid var(--border); background: color-mix(in srgb, var(--surface) 94%, transparent); backdrop-filter: blur(16px); }
.nav-inner { width: 100%; display: flex; align-items: center; justify-content: space-between; gap: 28px; }
.logo { flex: 0 0 auto; display: flex; align-items: center; gap: 10px; padding: 0; border: 0; background: none; font: inherit; cursor: pointer; }
.logo-icon { position: relative; width: 30px; height: 30px; background: var(--primary); }
.logo-icon i { position: absolute; left: 6px; top: 13px; width: 18px; height: 4px; background: #fff; }
.logo-icon i:last-child { left: 13px; top: 6px; width: 4px; height: 18px; }
.logo-text { color: var(--title); font-size: 16px; font-weight: 700; line-height: 1.05; letter-spacing: 0; text-align: left; }
.logo-text small { display: block; margin-top: 4px; color: var(--caption); font-size: 7px; font-weight: 600; letter-spacing: 0; }
.nav-links { min-width: 0; display: flex; align-self: stretch; align-items: center; justify-content: center; gap: 24px; }
.nav-links a { position: relative; height: 100%; display: flex; align-items: center; color: var(--body); font-size: 13px; cursor: pointer; white-space: nowrap; transition: color .18s ease; }
.nav-links a::after { content: ''; position: absolute; left: 0; right: 0; bottom: -1px; height: 2px; background: var(--primary); transform: scaleX(0); transition: transform .2s ease; }
.nav-links a:hover, .nav-links a.active { color: var(--primary); }
.nav-links a.active::after { transform: scaleX(1); }
.nav-right { flex: 0 0 auto; display: flex; align-items: center; gap: 8px; }
.icon-btn { width: 36px; height: 36px; display: inline-grid; place-items: center; border: 0; color: var(--body); background: transparent; font-size: 17px; cursor: pointer; transition: color .18s ease, background .18s ease; }
.icon-btn:hover { color: var(--primary); background: var(--primary-light); }
.user-dd { position: relative; display: flex; align-items: center; gap: 9px; margin-left: 4px; cursor: pointer; }
.user-name { max-width: 76px; overflow: hidden; color: var(--body); font-size: 13px; text-overflow: ellipsis; white-space: nowrap; }
.avatar-sm { width: 32px; height: 32px; display: grid; place-items: center; border: 1px solid color-mix(in srgb, var(--primary) 25%, var(--border)); border-radius: 50%; color: var(--primary); background: var(--primary-light); font-size: 13px; font-weight: 700; }
.dd-panel { position: absolute; top: 44px; right: 0; min-width: 150px; padding: 6px; border: 1px solid var(--border); border-radius: var(--radius); background: var(--surface); box-shadow: var(--shadow-md); z-index: 10; }
.dd-panel a { display: block; padding: 9px 11px; border-radius: 4px; color: var(--body); font-size: 13px; cursor: pointer; }
.dd-panel a:hover { color: var(--primary); background: var(--primary-light); }
.dd-panel hr { margin: 5px 0; border: 0; border-top: 1px solid var(--border-light); }
.burger { display: none; }
.drawer-bg { position: fixed; inset: 0; z-index: 200; background: rgba(14, 25, 19, .45); backdrop-filter: blur(2px); }
.drawer { position: fixed; top: 0; right: 0; z-index: 201; width: min(320px, 86vw); height: 100vh; padding: 22px; border-left: 1px solid var(--border); background: var(--surface); transform: translateX(100%); transition: transform .25s ease; }
.drawer.open { transform: translateX(0); }
.drawer-close { margin: 0 0 24px auto; }
.drawer nav a { display: block; padding: 13px 8px; border-bottom: 1px solid var(--border-light); color: var(--body); font-size: 14px; cursor: pointer; }
.drawer nav hr { margin: 12px 0; border: 0; border-top: 1px solid var(--border); }
.nav-badge { position: absolute; top: 10px; right: -15px; min-width: 16px; height: 16px; padding: 0 4px; border: 2px solid var(--surface); border-radius: 9px; background: var(--error); color: #fff; font-size: 9px; line-height: 12px; text-align: center; }
.msg-bell { position: relative; display: flex; align-items: center; cursor: pointer; }
.msg-badge { top: -2px; right: -3px; }
.bell-panel { position: absolute; top: 44px; right: -12px; z-index: 110; width: min(370px, calc(100vw - 28px)); max-height: 430px; overflow: hidden; border: 1px solid var(--border); border-radius: var(--radius); background: var(--surface); box-shadow: var(--shadow-lg); }
.bell-head { display: flex; justify-content: space-between; align-items: center; padding: 15px 17px; border-bottom: 1px solid var(--border-light); }
.bell-head span { color: var(--title); font-size: 14px; font-weight: 650; }
.bell-head a { color: var(--primary); font-size: 12px; cursor: pointer; }
.bell-body { max-height: 350px; overflow-y: auto; padding: 8px 0; }
.bell-section-title { padding: 7px 17px 4px; color: var(--warn); font-size: 11px; font-weight: 600; }
.bell-leave-item { display: flex; justify-content: space-between; align-items: center; padding: 9px 17px; }
.bell-leave-item:hover, .bell-msg-item:hover { background: var(--bg); }
.bell-leave-info { display: flex; flex-direction: column; gap: 2px; }
.bell-leave-doc { color: var(--title); font-size: 13px; font-weight: 600; }
.bell-leave-date, .bell-msg-content { color: var(--caption); font-size: 11px; }
.bell-leave-acts { display: flex; gap: 6px; }
.bell-divider { height: 1px; margin: 4px 12px; background: var(--border-light); }
.bell-msg-item { display: flex; align-items: flex-start; gap: 9px; padding: 9px 17px; cursor: pointer; }
.bell-msg-dot { flex: 0 0 auto; width: 6px; height: 6px; margin-top: 7px; border-radius: 50%; background: var(--primary); }
.bell-msg-c { flex: 1; min-width: 0; }
.bell-msg-title, .bell-msg-content { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.bell-msg-title { color: var(--title); font-size: 13px; }
.bell-empty { padding: 30px; color: var(--caption); font-size: 13px; text-align: center; }
@media (max-width: 1080px) { .nav-links { gap: 16px; } .nav-links a:nth-child(4) { display: none; } }
@media (max-width: 820px) { .nav-links { display: none; } .user-name { display: none; } .burger { display: inline-grid; } }
@media (max-width: 480px) { .logo-text small { display: none; } .nav-right { gap: 2px; } }
</style>
