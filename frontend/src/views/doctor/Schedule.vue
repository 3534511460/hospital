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

    <div class="card" style="padding:0;overflow:hidden" v-loading="loading">
      <table class="tbl"><thead><tr><th>日期</th><th>星期</th><th>时段</th><th>号源</th><th>已约</th><th>状态</th><th>操作</th></tr></thead>
      <tbody><tr v-for="s in schedules" :key="s.id">
        <td>{{ s.workDate }}</td>
        <td>{{ dayOfWeek(s.workDate) }}</td>
        <td>{{ s.timeSlot }}</td>
        <td>{{ s.maxCount }}</td>
        <td>{{ s.bookedCount }}</td>
        <td><span class="tag" :class="s.status===1?'tag-green':s.status===0?'tag-orange':'tag-red'">{{ s.status===1?'正常':s.status===0?'停诊':'满号' }}</span></td>
        <td>
          <button v-if="s.status===1&&s.bookedCount===0" class="btn btn-outline btn-sm" @click="requestCancel(s)">申请停诊</button>
          <span v-else-if="s.status===0" class="tag tag-orange">审核中</span>
          <span v-else-if="s.bookedCount>0" style="font-size:12px;color:var(--caption)">已有预约</span>
        </td>
      </tr></tbody></table>
      <div class="empty-state" v-if="!loading&&schedules.length===0"><p>暂无排班信息，请联系管理员添加</p></div>
    </div>
  </div>
</template>

<script setup>
import { ref,computed,onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
const schedules=ref([]);const loading=ref(false)

const pendingList=computed(()=>schedules.value.filter(s=>s.status===0))

function dayOfWeek(d){const n=new Date(d).getDay();return ['日','一','二','三','四','五','六'][n]}

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
.pending-card { padding:16px 20px; }
.pending-card h4 { font-size:14px;color:var(--title);margin-bottom:10px; }
.pending-row { display:flex;justify-content:space-between;align-items:center;padding:6px 0;font-size:13px;color:var(--body); }
.tbl { width:100%;border-collapse:collapse; }
.tbl th,.tbl td { padding:10px 14px;text-align:left;font-size:13px;border-bottom:1px solid var(--border-light); }
.tbl th { font-weight:500;color:var(--caption);font-size:12px; }
.tbl tbody tr:hover { background:var(--bg); }
</style>
