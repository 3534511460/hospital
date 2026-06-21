<template>
  <div class="board" ref="board">
    <div class="board-header">
      <h1>叫号大屏</h1>
      <p>{{ today }}</p>
    </div>

    <div class="dept-grid" v-loading="loading">
      <div class="dept-col" v-for="d in departments" :key="d.id">
        <div class="dept-head">{{ d.name }}</div>
        <div class="doc-cards">
          <div class="doc-card" v-for="doc in getDeptDoctors(d.id)" :key="doc.doctorId">
            <div class="doc-name">{{ doc.doctorName }} <span class="title-sm">{{ doc.title||'' }}</span></div>
            <div class="now-call" v-if="doc.queue&&doc.queue.length">
              <div class="now-row" v-for="q in doc.queue.filter(x=>x.status===1)" :key="q.id">
                <span class="now-num">{{ q.queueNumber }}</span>
                <span class="now-name">{{ q.patientName }}</span>
                <span class="now-slot">{{ q.timeSlot }}</span>
              </div>
              <div class="now-empty" v-if="!doc.queue.some(x=>x.status===1)">待叫号</div>
            </div>
            <div class="now-empty" v-else>暂无排队</div>
            <!-- Waiting list -->
            <div class="wait-list" v-if="doc.queue&&doc.queue.filter(x=>x.status===0).length">
              <div class="wait-row" v-for="q in doc.queue.filter(x=>x.status===0).slice(0,5)" :key="q.id">
                <span>{{ q.queueNumber }} {{ q.patientName }}</span>
              </div>
              <div class="wait-more" v-if="doc.queue.filter(x=>x.status===0).length>5">还有 {{ doc.queue.filter(x=>x.status===0).length-5 }} 人等待...</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref,onMounted } from 'vue'
import request from '../../utils/request'

const today=new Date().toLocaleDateString('zh-CN',{year:'numeric',month:'long',day:'numeric',weekday:'long'})
const departments=ref([]);const allQueues=ref([]);const loading=ref(false)

function getDeptDoctors(deptId){return allQueues.value.filter(d=>d.departmentId===deptId)}

async function fetch(){
  loading.value=true
  try{
    const dr=await request.get('/hospital/departments');departments.value=dr.data||[]
    const rr=await request.get('/hospital/doctors',{params:{page:1,size:100}})
    const docs=(rr.data&&rr.data.records)||rr.data||[]
    const result=[]
    for(const d of docs.filter(d=>d.status===1)){
      try{
        const qr=await request.get(`/queue/doctor/${d.userId}/today`)
        result.push({doctorId:d.userId,doctorName:d.realName,title:d.title,departmentId:d.departmentId,departmentName:d.departmentName,queue:qr.data||[]})
      }catch{result.push({doctorId:d.userId,doctorName:d.realName,title:d.title,departmentId:d.departmentId,departmentName:d.departmentName,queue:[]})}
    }
    allQueues.value=result
  }catch{}finally{loading.value=false}
}

onMounted(fetch)
// Auto refresh every 30s
setInterval(fetch,30000)
</script>

<style scoped>
.board { min-height:100vh;background:var(--bg);padding:20px 32px; }
.board-header { text-align:center;padding:16px 0 24px; }
.board-header h1 { font-size:32px;font-weight:700;color:var(--title); }
.board-header p { font-size:14px;color:var(--caption);margin-top:4px; }

.dept-grid { display:grid;grid-template-columns:repeat(auto-fit,minmax(280px,1fr));gap:20px; }
.dept-col { background:var(--surface);border-radius:var(--radius);box-shadow:var(--shadow);overflow:hidden; }
.dept-head { background:var(--primary);color:#fff;padding:14px 20px;font-size:18px;font-weight:600;text-align:center;letter-spacing:.05em; }
.doc-cards { padding:12px; }
.doc-card { padding:12px;border:1px solid var(--border-light);border-radius:var(--radius-sm);margin-bottom:10px;background:var(--bg); }
.doc-name { font-size:14px;font-weight:600;color:var(--title);margin-bottom:8px; }
.title-sm { font-size:11px;color:var(--caption);font-weight:400; }

.now-call { margin-bottom:8px; }
.now-row { display:flex;align-items:center;gap:10px;padding:10px 12px;background:var(--primary-light);border-radius:var(--radius-sm);border-left:3px solid var(--primary);animation:pulse .6s ease; }
.now-num { font-size:28px;font-weight:700;color:var(--primary);font-family:var(--serif); }
.now-name { font-size:15px;font-weight:600;color:var(--title); }
.now-slot { margin-left:auto;font-size:12px;color:var(--caption); }
.now-empty { padding:10px;text-align:center;font-size:13px;color:var(--caption); }

.wait-list { padding:4px 0; }
.wait-row { padding:4px 8px;font-size:12px;color:var(--body);border-bottom:1px solid var(--border-light); }
.wait-more { padding:4px 8px;font-size:11px;color:var(--caption);font-style:italic; }

@keyframes pulse { 0%,100%{transform:scale(1)}50%{transform:scale(1.02)} }
</style>
