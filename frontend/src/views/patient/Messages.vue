<template>
  <div class="page-w page-main">
    <div class="page-head"><h1>消息中心</h1></div>
    <div class="card" v-loading="loading" style="padding:0;overflow:hidden">
      <div class="msg-row" v-for="m in messages" :key="m.id" :class="{unread:!m.isRead}" @click="read(m)">
        <div class="unread-dot" v-if="!m.isRead"/>
        <div class="msg-c"><h4>{{ m.title }}</h4><p>{{ m.content }}</p><span class="t">{{ fmt(m.createTime) }}</span></div>
      </div>
      <div class="empty-hint" v-if="!loading&&messages.length===0">暂无消息</div>
    </div>
  </div>
</template>

<script setup>
import { ref,onMounted } from 'vue'
import request from '../../utils/request'
const messages=ref([]);const loading=ref(false)
async function fetch(){loading.value=true;try{const r=await request.get('/message/messages');messages.value=r.data||[]}catch{}finally{loading.value=false}}
async function read(m){if(m.isRead)return;try{await request.put(`/message/read/${m.id}`);m.isRead=1}catch{}}
function fmt(t){return t?new Date(t).toLocaleDateString('zh-CN',{month:'short',day:'numeric',hour:'2-digit',minute:'2-digit'}):''}
onMounted(fetch)
</script>

<style scoped>
.msg-row { display:flex;align-items:flex-start;gap:14px;padding:18px 20px;cursor:pointer;border-bottom:1px solid var(--border-light);transition:background .15s; }
.msg-row:hover { background:var(--bg); }
.msg-row.unread { border-left:3px solid var(--primary);background:var(--primary-light); }
.unread-dot { width:8px;height:8px;border-radius:50%;background:var(--primary);margin-top:6px;flex-shrink:0; }
.msg-c { flex:1; }
.msg-c h4 { font-size:14px;color:var(--title);margin-bottom:4px; }
.msg-c p { font-size:13px;color:var(--caption);display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden; }
.t { font-size:12px;color:var(--caption);margin-top:6px;display:block; }
.empty-hint { padding:40px;text-align:center;color:var(--caption); }
</style>
