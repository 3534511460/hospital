<template>
  <div class="page-w page-main">
    <div class="page-head"><h1>我的排班</h1><p style="color:var(--caption);font-size:13px">如需停诊或改诊，请提交申请，由管理员审核处理</p></div>

    <!-- 审核中的申请 -->
    <div class="card pending-card" v-if="pendingList.length" style="margin-bottom:20px;border-left:3px solid var(--warn)">
      <h4>审核中的停诊申请（{{ pendingList.length }}条）</h4>
      <div class="pending-row" v-for="s in pendingList" :key="s.id">
        <span>{{ s.workDate }} · {{ s.timeSlot }}</span>
        <span class="tag tag-orange">审核中</span>
      </div>
    </div>

    <!-- 日期筛选 -->
    <div class="toolbar">
      <div class="status-filters">
        <button v-for="f in filters" :key="f.v" :class="{active:filter===f.v}" @click="filter=f.v">{{ f.l }}</button>
      </div>
      <div class="week-row">
        <button class="btn btn-outline btn-sm" @click="prevWeek">上一周</button>
        <span class="week-label">{{ weekLabel }}</span>
        <button class="btn btn-outline btn-sm" @click="nextWeek">下一周</button>
      </div>
    </div>

    <div class="card" style="padding:0;overflow:hidden" v-loading="loading">
      <table class="tbl"><thead><tr><th>日期</th><th>星期</th><th>时段</th><th>号源</th><th>已约</th><th>状态</th><th>操作</th></tr></thead>
      <tbody><tr v-for="s in filteredScheds" :key="s.id">
        <td>{{ s.workDate }}</td>
        <td>{{ dayOfWeek(s.workDate) }}</td>
        <td>{{ s.timeSlot }}</td>
        <td>{{ s.maxCount }}</td>
        <td>{{ s.bookedCount }}</td>
        <td><span class="tag" :class="s.status===1?'tag-green':s.status===0?'tag-orange':'tag-red'">{{ s.status===1?'正常':s.status===0?'停诊':'满号' }}</span></td>
        <td>
          <button v-if="s.status===1&&s.bookedCount===0&&!isPast(s)" class="btn btn-outline btn-sm" @click="requestCancel(s)">申请停诊</button>
          <span v-else-if="s.status===0" class="tag tag-orange">审核中</span>
          <span v-else-if="s.bookedCount>0" style="font-size:12px;color:var(--caption)">已有预约</span>
          <span v-else-if="isPast(s)" style="font-size:12px;color:var(--caption)">已过期</span>
        </td>
      </tr></tbody></table>
      <div class="empty-state" v-if="!loading&&filteredScheds.length===0"><p>该周暂无排班</p></div>
    </div>
  </div>
</template>

<script setup>
import { ref,computed,onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
const schedules=ref([]);const loading=ref(false);const weekOffset=ref(0);const filter=ref('week')

const filters=[{v:'week',l:'本周'},{v:'upcoming',l:'将来'},{v:'all',l:'全部'}]

const pendingList=computed(()=>schedules.value.filter(s=>s.status===0))

function fmt(d){const y=d.getFullYear();const m=String(d.getMonth()+1).padStart(2,'0');const dd=String(d.getDate()).padStart(2,'0');return `${y}-${m}-${dd}`}
function getMonday(offset){
  const t=new Date();const dow=t.getDay()||7;
  t.setDate(t.getDate()-(dow-1)+offset*7);t.setHours(0,0,0,0);return t
}
const weekLabel=computed(()=>{
  const mon=getMonday(weekOffset.value);const sun=new Date(mon);sun.setDate(sun.getDate()+6)
  return `${fmt(mon)} ~ ${fmt(sun)}`
})

function dayOfWeek(d){const n=new Date(d+'T00:00:00').getDay();return ['日','一','二','三','四','五','六'][n]}
function isPast(s){
  const now=new Date();const today=fmt(now)
  if(s.workDate<today) return true
  if(s.workDate===today&&s.timeSlot){
    const end=s.timeSlot.split('-')[1]
    if(end){const[h,m]=end.split(':');const endTime=new Date();endTime.setHours(+h,+m,0,0);if(now>=endTime) return true}
  }
  return false
}

const filteredScheds=computed(()=>{
  let list=schedules.value
  if(filter.value==='week'){
    const mon=fmt(getMonday(weekOffset.value))
    const sun=fmt(new Date(getMonday(weekOffset.value).getTime()+6*86400000))
    list=list.filter(s=>s.workDate>=mon&&s.workDate<=sun)
  }else if(filter.value==='upcoming'){
    const today=fmt(new Date())
    list=list.filter(s=>s.workDate>=today)
  }
  return list
})

function prevWeek(){weekOffset.value--}
function nextWeek(){weekOffset.value++}

async function fetch(){
  loading.value=true
  try{const r=await request.get('/schedule/doctor');schedules.value=r.data||[]}catch{}
  finally{loading.value=false}
}

async function requestCancel(s){
  try{await request.put(`/schedule/${s.id}`,{...s,status:0});ElMessage.success('已提交停诊申请，等待管理员审核');fetch()}catch{}
}

onMounted(fetch)
</script>

<style scoped>
.toolbar { display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;flex-wrap:wrap;gap:12px; }
.status-filters { display:flex;gap:8px; }
.status-filters button { padding:6px 18px;border:1px solid var(--border);border-radius:20px;background:none;font-size:13px;color:var(--body);cursor:pointer;font-family:inherit;transition:all .15s; }
.status-filters button.active { background:var(--primary);color:#fff;border-color:var(--primary); }
.week-row { display:flex;align-items:center;gap:8px; }
.week-label { font-size:13px;color:var(--body);min-width:200px;text-align:center; }
.pending-card { padding:16px 20px; }
.pending-card h4 { font-size:14px;color:var(--title);margin-bottom:10px; }
.pending-row { display:flex;justify-content:space-between;align-items:center;padding:6px 0;font-size:13px;color:var(--body); }
.tbl { width:100%;border-collapse:collapse; }
.tbl th,.tbl td { padding:10px 14px;text-align:left;font-size:13px;border-bottom:1px solid var(--border-light); }
.tbl th { font-weight:500;color:var(--caption);font-size:12px; }
.tbl tbody tr:hover { background:var(--bg); }
</style>
