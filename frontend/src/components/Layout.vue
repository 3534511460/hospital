<template>
  <div class="layout">
    <header class="top-nav">
      <div class="nav-inner page-w">
        <a class="logo" @click="$router.push('/home')">
          <span class="logo-icon">+</span>
          <span class="logo-text">医预约</span>
        </a>
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
          <a class="theme-btn" @click="toggleTheme" :title="dark?'亮色':'暗色'">{{ dark ? '☀' : '☾' }}</a>
          <div class="msg-bell" v-if="isLoggedIn" @click.stop="bellOpen=!bellOpen;if(bellOpen)loadBell()" title="消息中心">
            <span style="font-size:16px">🔔</span>
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
          <a class="burger" @click="drawer=true" v-if="isLoggedIn"><span/><span/><span/></a>
        </div>
      </div>
    </header>
    <div class="drawer-bg" v-if="drawer" @click="drawer=false"/>
    <aside class="drawer" :class="{open:drawer}">
      <a class="drawer-close" @click="drawer=false">&times;</a>
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
.top-nav { position:sticky;top:0;z-index:100;height:var(--nav-h);background:var(--surface);border-bottom:1px solid var(--border-light);display:flex;align-items:center; }
.nav-inner { display:flex;align-items:center;justify-content:space-between;width:100%; }
.logo { display:flex;align-items:center;gap:6px;cursor:pointer; }
.logo-icon { width:28px;height:28px;border-radius:6px;background:var(--primary);color:#fff;display:flex;align-items:center;justify-content:center;font-size:16px;font-weight:700; }
.logo-text { font-size:17px;font-weight:600;color:var(--title);letter-spacing:.02em; }
.nav-links { display:flex;gap:32px; }
.nav-links a { font-size:14px;color:var(--body);cursor:pointer;padding:4px 0;border-bottom:2px solid transparent;transition:all .15s; }
.nav-links a:hover,.nav-links a.active { color:var(--primary);border-color:var(--primary); }
.nav-right { display:flex;align-items:center;gap:12px; }
.theme-btn { font-size:16px;cursor:pointer;padding:4px;user-select:none; }
.user-dd { position:relative;display:flex;align-items:center;gap:8px;cursor:pointer; }
.user-name { font-size:13px;color:var(--body);max-width:80px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap; }
.avatar-sm { width:30px;height:30px;border-radius:50%;background:var(--primary-light);color:var(--primary);display:flex;align-items:center;justify-content:center;font-size:13px;font-weight:600; }
.dd-panel { position:absolute;top:40px;right:0;min-width:140px;background:var(--surface);border-radius:var(--radius);box-shadow:var(--shadow-md);border:1px solid var(--border);padding:6px 0;z-index:10; }
.dd-panel a { display:block;padding:8px 16px;font-size:13px;color:var(--body);cursor:pointer;transition:background .1s; }
.dd-panel a:hover { background:var(--bg); }
.dd-panel hr { border:none;border-top:1px solid var(--border-light);margin:4px 0; }
.burger { display:none;flex-direction:column;gap:4px;cursor:pointer; }
.burger span { width:18px;height:2px;background:var(--body);border-radius:1px; }
.drawer-bg { position:fixed;inset:0;background:rgba(0,0,0,.35);z-index:200; }
.drawer { position:fixed;top:0;right:0;width:280px;height:100vh;z-index:201;background:var(--surface);padding:24px;transform:translateX(100%);transition:transform .25s ease; }
.drawer.open { transform:translateX(0); }
.drawer-close { font-size:22px;cursor:pointer;display:block;text-align:right;margin-bottom:24px;color:var(--body); }
.drawer nav a { display:block;padding:12px 0;font-size:15px;color:var(--body);cursor:pointer;border-bottom:1px solid var(--border-light); }
.drawer nav hr { margin:12px 0;border:none;border-top:1px solid var(--border); }
.nav-badge { position:absolute;top:-8px;right:-18px;background:var(--error,#f56c6c);color:#fff;font-size:10px;min-width:16px;height:16px;line-height:16px;text-align:center;border-radius:8px;padding:0 4px; }
.msg-bell { position:relative;cursor:pointer;display:flex;align-items:center; }
.msg-badge { top:-6px;right:-10px; }
.bell-panel { position:absolute;top:38px;right:-12px;width:360px;max-height:420px;background:var(--surface);border-radius:var(--radius);box-shadow:var(--shadow-md);border:1px solid var(--border);z-index:110;overflow:hidden; }
.bell-head { display:flex;justify-content:space-between;align-items:center;padding:14px 16px;border-bottom:1px solid var(--border-light); }
.bell-head span { font-size:14px;font-weight:600;color:var(--title); }
.bell-head a { font-size:12px;color:var(--primary);cursor:pointer; }
.bell-body { max-height:340px;overflow-y:auto;padding:8px 0; }
.bell-section-title { font-size:11px;color:var(--warn);padding:6px 16px 4px;font-weight:500; }
.bell-leave-item { display:flex;justify-content:space-between;align-items:center;padding:8px 16px;transition:background .1s; }
.bell-leave-item:hover { background:var(--bg); }
.bell-leave-info { display:flex;flex-direction:column;gap:2px; }
.bell-leave-doc { font-size:13px;color:var(--title);font-weight:500; }
.bell-leave-date { font-size:11px;color:var(--caption); }
.bell-leave-acts { display:flex;gap:6px; }
.bell-divider { height:1px;background:var(--border-light);margin:4px 12px; }
.bell-msg-item { display:flex;align-items:flex-start;gap:8px;padding:8px 16px;cursor:pointer;transition:background .1s; }
.bell-msg-item:hover { background:var(--bg); }
.bell-msg-dot { width:6px;height:6px;border-radius:50%;background:var(--primary);margin-top:7px;flex-shrink:0; }
.bell-msg-c { flex:1;min-width:0; }
.bell-msg-title { font-size:13px;color:var(--title);white-space:nowrap;overflow:hidden;text-overflow:ellipsis; }
.bell-msg-content { font-size:11px;color:var(--caption);margin-top:1px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis; }
.bell-empty { padding:24px;text-align:center;font-size:13px;color:var(--caption); }
@media(max-width:768px){ .nav-links{display:none}.burger{display:flex} }
</style>
