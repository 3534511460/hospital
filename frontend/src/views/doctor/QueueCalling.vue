<template>
  <div class="page-w page-main">
    <div class="page-head" style="display:flex;justify-content:space-between;align-items:center">
      <div><h1>叫号就诊</h1><p style="color:var(--caption);font-size:13px">今日排队管理</p></div>
      <a class="btn btn-outline btn-sm" @click="$router.push('/queue/board')">📺 叫号大屏</a>
    </div>

    <!-- 当前叫号 -->
    <div class="current-call card" v-if="currentCall">
      <div class="call-display">
        <span class="call-label">当前叫号</span>
        <span class="call-number">{{ currentCall.queueNumber }}</span>
        <span class="call-name">{{ currentCall.patientName }}</span>
        <span class="call-slot">{{ currentCall.timeSlot }}</span>
        <span v-if="isDeptAdmin" class="call-doc">{{ currentCall.doctorName }}</span>
      </div>
      <div class="call-actions">
        <button class="btn btn-primary" @click="completePatient">就诊完成</button>
        <button class="btn btn-outline" @click="missPatient">过号</button>
      </div>
    </div>
    <div class="current-call card empty" v-else>
      <div class="call-display"><span class="call-label">暂无叫号</span></div>
    </div>

    <!-- 操作区 -->
    <div v-if="!isDeptAdmin" style="margin:16px 0;display:flex;gap:12px">
      <button class="btn btn-primary btn-lg" @click="callNextPatient" :disabled="calling">
        {{ calling ? '叫号中...' : '叫号（下一个）' }}
      </button>
    </div>

    <!-- 排队列表 -->
    <div class="card" style="padding:0;overflow:hidden">
      <table class="tbl"><thead><tr><th>排队号</th><th>患者</th><th v-if="isDeptAdmin">医生</th><th>时段</th><th>状态</th><th>操作</th></tr></thead>
      <tbody><tr v-for="q in queue" :key="q.id" :class="{called:q.status===1,missed:q.status===2}">
        <td><span class="q-num">{{ q.queueNumber }}</span></td>
        <td>{{ q.patientName }}</td><td v-if="isDeptAdmin">{{ q.doctorName }}</td><td>{{ q.timeSlot }}</td>
        <td><span class="tag" :class="statusTag(q.status)">{{ statusText(q.status) }}</span></td>
        <td>
          <button v-if="q.status===1" class="btn btn-primary btn-sm" @click="completeById(q.appointmentId)">完成</button>
          <button v-if="q.status===1" class="btn btn-ghost btn-sm" @click="missById(q.appointmentId)">过号</button>
          <button v-if="q.status===2" class="btn btn-ghost btn-sm" @click="rewaitById(q.appointmentId)">重排</button>
        </td>
      </tr></tbody></table>
      <div class="empty-state" v-if="queue.length===0"><p>今日暂无排队患者</p></div>
    </div>
  </div>
</template>

<script setup>
import { ref,computed,onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
const queue=ref([])
const calling=ref(false)
const isDeptAdmin=computed(()=>{const u=localStorage.getItem('user');return u?JSON.parse(u).role==='DEPT_ADMIN':false})
const currentCall=computed(()=>queue.value.find(q=>q.status===1)||null)
function statusTag(s){return s===0?'tag-blue':s===1?'tag-green':s===2?'tag-orange':s===3?'tag':''}
function statusText(s){return s===0?'等待':s===1?'叫号中':s===2?'过号':s===3?'已就诊':''}
async function fetch(){try{const r=await request.get('/queue/today');queue.value=r.data||[]}catch{}}
async function callNextPatient(){calling.value=true;try{await request.post('/queue/call-next');await fetch()}catch{}finally{calling.value=false}}
async function completePatient(){if(!currentCall.value)return;try{await request.put(`/queue/${currentCall.value.appointmentId}/complete`);fetch()}catch{}}
async function missPatient(){if(!currentCall.value)return;try{await request.put(`/queue/${currentCall.value.appointmentId}/miss`);fetch()}catch{}}
async function completeById(aid){try{await request.put(`/queue/${aid}/complete`);fetch()}catch{}}
async function missById(aid){try{await request.put(`/queue/${aid}/miss`);fetch()}catch{}}
async function rewaitById(aid){try{await request.put(`/queue/${aid}/rewait`);fetch()}catch{}}
onMounted(fetch)
</script>

<style scoped>
.current-call { padding:28px 32px;display:flex;align-items:center;justify-content:space-between;margin-bottom:16px; }
.current-call.empty { opacity:.4; }
.call-display { display:flex;align-items:center;gap:20px; }
.call-label { font-size:13px;color:var(--caption); }
.call-number { font-size:48px;font-weight:700;color:var(--primary);font-family:var(--serif);line-height:1; }
.call-name { font-size:20px;font-weight:600;color:var(--title); }
.call-slot { font-size:13px;color:var(--caption); }
.call-doc { font-size:13px;color:var(--caption);margin-left:4px; }
.call-doc::before { content:'· '; }
.call-actions { display:flex;gap:8px; }
.tbl { width:100%;border-collapse:collapse; }
.tbl th,.tbl td { padding:12px 16px;text-align:left;font-size:13px;border-bottom:1px solid var(--border-light); }
.tbl th { font-weight:500;color:var(--caption);font-size:12px; }
.tbl tbody tr:hover { background:var(--bg); }
.tbl tr.called { background:var(--primary-light);animation:pulse .6s ease; }
.tbl tr.missed { opacity:.5; }
.q-num { font-weight:600;font-size:15px;color:var(--title); }
@keyframes pulse { 0%,100%{transform:scale(1)}50%{transform:scale(1.01)} }
</style>
