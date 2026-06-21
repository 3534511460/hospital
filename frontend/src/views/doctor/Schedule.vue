<template>
  <div class="page-w page-main">
    <div class="page-head"><h1>排班管理</h1></div>
    <div style="margin-bottom:16px"><button class="btn btn-primary btn-sm" @click="showAdd=true">+ 新增排班</button></div>
    <div class="card" style="padding:0;overflow:hidden">
      <table class="tbl"><thead><tr><th>日期</th><th>时段</th><th>号源</th><th>已约</th><th>状态</th><th>操作</th></tr></thead>
      <tbody><tr v-for="s in schedules" :key="s.id"><td>{{ s.workDate }}</td><td>{{ s.timeSlot }}</td><td>{{ s.maxCount }}</td><td>{{ s.bookedCount }}</td><td><span :class="'tag '+(s.status===1?'tag-green':'tag-red')">{{ s.status===1?'正常':'停诊' }}</span></td><td><button class="btn btn-ghost btn-sm" @click="del(s.id)" :disabled="s.bookedCount>0">删除</button></td></tr></tbody></table>
      <div class="empty-hint" v-if="schedules.length===0">暂无排班</div>
    </div>
    <div class="modal-bg" v-if="showAdd" @click.self="showAdd=false">
      <div class="modal c"><h3>新增排班</h3><div class="f"><label>日期</label><input v-model="a.date" type="date"/></div><div class="f"><label>时段</label><select v-model="a.slot"><option v-for="s in slots" :key="s" :value="s">{{ s }}</option></select></div><div class="f"><label>号源数</label><input v-model="a.max" type="number" min="1" max="50"/></div><div class="mact"><button class="btn btn-primary" @click="add">确认</button><button class="btn btn-ghost" @click="showAdd=false">取消</button></div></div>
    </div>
  </div>
</template>

<script setup>
import { ref,onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
const schedules=ref([]);const showAdd=ref(false);const a=reactive({date:'',slot:'08:00-09:00',max:30})
import { reactive } from 'vue'
const slots=['08:00-09:00','09:00-10:00','10:00-11:00','11:00-12:00','14:00-15:00','15:00-16:00','16:00-17:00']
async function fetch(){try{const r=await request.get('/schedule/all');schedules.value=r.data||[]}catch{}}
async function add(){try{await request.post('/schedule/add',{workDate:a.date,timeSlot:a.slot,maxCount:a.max});ElMessage.success('添加成功');showAdd.value=false;fetch()}catch{}}
async function del(id){try{await request.delete('/schedule/'+id);ElMessage.success('已删除');fetch()}catch{}}
onMounted(fetch)
</script>

<style scoped>
.tbl { width:100%;border-collapse:collapse; }
.tbl th,.tbl td { padding:12px 16px;text-align:left;font-size:13px;border-bottom:1px solid var(--border-light); }
.tbl th { font-weight:500;color:var(--caption);font-size:12px; }
.tbl tbody tr:hover { background:var(--bg); }
.modal-bg { position:fixed;inset:0;background:rgba(0,0,0,.35);z-index:300;display:flex;align-items:center;justify-content:center; }
.modal { background:var(--surface);border-radius:var(--radius);padding:24px;width:400px;max-width:90vw;box-shadow:var(--shadow-md); }
.modal h3 { font-size:17px;color:var(--title);margin-bottom:16px; }
.f { margin-bottom:12px; }
.f label { display:block;font-size:13px;color:var(--body);margin-bottom:4px; }
.f input,.f select { width:100%;height:38px;padding:0 12px;border:1px solid var(--border);border-radius:var(--radius-sm);font-size:14px;font-family:inherit;color:var(--title);outline:none; }
.f input:focus,.f select:focus { border-color:var(--primary); }
.mact { display:flex;gap:8px;margin-top:16px; }
.empty-hint { padding:40px;text-align:center;color:var(--caption); }
</style>
