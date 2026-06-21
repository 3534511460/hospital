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
          <a :class="{ active:$route.path==='/doctors' }" @click="$router.push('/doctors')">找医生</a>
          <a v-if="userRole==='PATIENT'" :class="{ active:$route.path==='/my-appointments' }" @click="$router.push('/my-appointments')">我的预约</a>
          <a :class="{ active:$route.path==='/ai' }" @click="$router.push('/ai')">AI导诊</a>
          <a :class="{ active:$route.path==='/chat' }" @click="$router.push('/chat')">问医生</a>
          <a v-if="userRole==='SYS_ADMIN'" :class="{ active:$route.path.startsWith('/admin') }" @click="$router.push('/admin')" style="color:var(--primary)">管理</a>
        </nav>
        <div class="nav-right">
          <a class="theme-btn" @click="toggleTheme" :title="dark?'亮色':'暗色'">{{ dark ? '☀' : '☾' }}</a>
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
      <nav><a @click="go('/home');drawer=false">首页</a><a @click="go('/doctors');drawer=false">找医生</a><a v-if="userRole==='PATIENT'" @click="go('/my-appointments');drawer=false">我的预约</a><a @click="go('/ai');drawer=false">AI导诊</a><a @click="go('/chat');drawer=false">问医生</a><hr><a v-if="userRole==='SYS_ADMIN'" @click="go('/admin');drawer=false">系统管理</a><a @click="go('/profile');drawer=false">个人中心</a><a @click="logout">退出</a></nav>
    </aside>
    <main class="page-main"><router-view/></main>
  </div>
</template>

<script setup>
import { ref,computed,onMounted,onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
const router=useRouter()
const menu=ref(false),drawer=ref(false),dark=ref(false)
const isLoggedIn=computed(()=>!!localStorage.getItem('token'))
const userRole=computed(()=>{const u=localStorage.getItem('user');return u?JSON.parse(u).role:''})
const userInfo=computed(()=>{const u=localStorage.getItem('user');return u?JSON.parse(u):null})
function go(p){menu.value=false;router.push(p)}
function logout(){localStorage.clear();router.push('/login')}
function toggleTheme(){dark.value=!dark.value;document.documentElement.setAttribute('data-theme',dark.value?'dark':'');localStorage.setItem('theme',dark.value?'dark':'light')}
function clk(e){if(menu.value&&!e.target.closest('.user-dd'))menu.value=false}
onMounted(()=>{dark.value=localStorage.getItem('theme')==='dark';if(dark.value)document.documentElement.setAttribute('data-theme','dark');document.addEventListener('click',clk)})
onUnmounted(()=>document.removeEventListener('click',clk))
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
@media(max-width:768px){ .nav-links{display:none}.burger{display:flex} }
</style>
