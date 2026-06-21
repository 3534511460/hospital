<template>
  <div class="page-w page-main">
    <div class="page-head"><h1>我的预约</h1></div>
    <div class="status-filters">
      <button v-for="f in filters" :key="f.v" :class="{active:filter===f.v}" @click="filter=f.v;fetch()">{{ f.l }}</button>
    </div>
    <div class="apt-list" v-loading="loading">
      <div class="apt-card card" v-for="a in appointments" :key="a.id">
        <div class="apt-top">
          <div>
            <h4>{{ a.doctorName }} <span class="tag" :class="statusTag(a.status)">{{ statusText(a.status) }}</span></h4>
            <p>{{ a.departmentName }} · {{ a.timeSlot }}</p>
          </div>
          <div class="apt-date">{{ a.appointmentDate }} <span class="apt-no">{{ a.appointmentNo }}</span></div>
        </div>
        <div class="apt-actions">
          <button v-if="a.status===1" class="btn btn-outline btn-sm" @click="cancel(a.id)">取消预约</button>
          <button v-if="a.status===2&&userRole==='PATIENT'" class="btn btn-primary btn-sm" @click="evaluate(a)">评价</button>
        </div>
      </div>
      <div class="empty-state" v-if="!loading&&appointments.length===0"><p>暂无预约记录</p></div>
    </div>

    <!-- 评价弹窗 -->
    <div class="modal-bg" v-if="showEval" @click.self="showEval=false">
      <div class="modal-card">
        <h3>评价 {{ evalDoc }}</h3>
        <div class="stars"><button v-for="i in 5" :key="i" @click="rating=i">{{ i<=rating?'★':'☆' }}</button></div>
        <textarea v-model="evalText" rows="3" placeholder="写下您的评价…"/>
        <div class="modal-act"><button class="btn btn-primary" @click="submitEval">提交</button><button class="btn btn-ghost" @click="showEval=false">取消</button></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref,computed,onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
const userRole=computed(()=>{const u=localStorage.getItem('user');return u?JSON.parse(u).role:''})
const filter=ref('all');const loading=ref(false);const appointments=ref([])
const filters=[{v:'all',l:'全部'},{v:'1',l:'已预约'},{v:'2',l:'已就诊'},{v:'3',l:'已取消'},{v:'4',l:'爽约'}]
function statusTag(s){return s===1?'tag-blue':s===2?'tag-green':s===3?'tag-orange':'tag-red'}
function statusText(s){return s===1?'已预约':s===2?'已就诊':s===3?'已取消':'爽约'}
async function fetch(){
  loading.value=true
  try{const r=await request.get('/appointment/my',{params:{status:filter.value==='all'?null:filter.value}});appointments.value=r.data||[]}catch{}
  finally{loading.value=false}
}
async function cancel(id){
  try{await request.put('/appointment/cancel/'+id);ElMessage.success('已取消');fetch()}catch{}
}
const showEval=ref(false);const evalAptId=ref(null);const evalDoc=ref('');const rating=ref(5);const evalText=ref('')
function evaluate(a){evalAptId.value=a.id;evalDoc.value=a.doctorName;rating.value=5;evalText.value='';showEval.value=true}
async function submitEval(){
  try{await request.post('/medical/evaluations',{appointmentId:evalAptId.value,doctorId:0,rating:rating.value,content:evalText.value});ElMessage.success('评价成功');showEval.value=false;fetch()}catch{}
}
onMounted(fetch)
</script>

<style scoped>
.status-filters { display:flex;gap:8px;margin-bottom:20px; }
.status-filters button { padding:6px 18px;border:1px solid var(--border);border-radius:20px;background:none;font-size:13px;color:var(--body);cursor:pointer;font-family:inherit;transition:all .15s; }
.status-filters button.active { background:var(--primary);color:#fff;border-color:var(--primary); }
.apt-list { display:flex;flex-direction:column;gap:12px; }
.apt-top { display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:12px; }
.apt-top h4 { font-size:16px;color:var(--title);margin-bottom:4px; }
.apt-top p { font-size:13px;color:var(--caption); }
.apt-date { text-align:right;font-size:13px;color:var(--body); }
.apt-no { display:block;font-size:11px;color:var(--caption); }
.apt-actions { display:flex;gap:8px; }

.modal-bg { position:fixed;inset:0;background:rgba(0,0,0,.35);z-index:300;display:flex;align-items:center;justify-content:center; }
.modal-card { background:var(--surface);border-radius:var(--radius);padding:32px;width:400px;max-width:90vw;box-shadow:var(--shadow-md); }
.modal-card h3 { font-size:18px;color:var(--title);margin-bottom:16px; }
.stars { margin-bottom:12px; }
.stars button { background:none;border:none;font-size:28px;cursor:pointer;color:#F2C94C; }
textarea { width:100%;border:1px solid var(--border);border-radius:var(--radius-sm);padding:10px;font-size:14px;font-family:inherit;resize:vertical;outline:none; }
textarea:focus { border-color:var(--primary); }
.modal-act { display:flex;gap:8px;margin-top:14px; }
</style>
