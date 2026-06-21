<template>
  <div class="page-w page-main">
    <div class="book-layout">
      <div class="book-main">
        <div class="progress-bar">
          <div class="progress-step" v-for="(s,i) in steps" :key="i"
               :class="{ active:step===i, done:step>i }">
            <span class="dot">{{ step>i?'✓':i+1 }}</span>
            <span>{{ s }}</span>
            <span class="line" v-if="i<steps.length-1"/>
          </div>
        </div>

        <!-- Step 0: 选择科室 -->
        <div v-if="step===0" class="ani-up">
          <h2 class="sec-title">选择科室</h2>
          <div class="dept-grid">
            <div class="dept-card" v-for="d in departments" :key="d.id"
                 @click="pickDept(d)" :class="{active:selectedDept?.id===d.id}">
              {{ d.name }}
            </div>
          </div>
        </div>

        <!-- Step 1: 选择医生 -->
        <div v-if="step===1" class="ani-up">
          <div class="sec-head"><h2 class="sec-title">{{ selectedDept?.name }}</h2><button class="btn btn-ghost btn-sm" @click="step=0">重选科室</button></div>
          <div class="doc-list" v-loading="docLoading">
            <div class="doc-row" v-for="d in doctors" :key="d.userId" @click="pickDoctor(d)" :class="{active:selectedDoc?.userId===d.userId}">
              <div class="doc-av">{{ (d.realName||'?')[0] }}</div>
              <div class="doc-info">
                <h4>{{ d.realName }} <span class="tag tag-blue">{{ d.title }}</span></h4>
                <p>{{ (d.specialty||'').slice(0,50) }}</p>
              </div>
              <div class="doc-rate">&#9733; {{ d.avgRating||'5.0' }}<span class="fee">&#165;{{ d.consultationFee||0 }}</span></div>
            </div>
          </div>
        </div>

        <!-- Step 2: 选择时段 -->
        <div v-if="step===2" class="ani-up">
          <div class="sec-head"><h2 class="sec-title">{{ selectedDoc?.realName }} 的排班</h2><button class="btn btn-ghost btn-sm" @click="step=1">重选医生</button></div>
          <div class="date-tabs"><button v-for="d in dates" :key="d" :class="{active:d===selDate}" @click="selDate=d;selSlot=null;fetchSlots()">{{ fmtDate(d) }}</button></div>
          <div class="slot-grid" v-if="slots.length" v-loading="sLoading">
            <button class="capsule" v-for="s in slots" :key="s.id"
                    :class="{active:selSlot===s.id,full:s.bookedCount>=s.maxCount}"
                    :disabled="s.bookedCount>=s.maxCount" @click="selSlot=s.id">
              {{ s.timeSlot }} ({{ s.maxCount-s.bookedCount }}号)
            </button>
          </div>
          <div class="empty-state" v-else-if="!sLoading">该日期暂无排班</div>
        </div>

        <!-- Step 3: 确认预约 -->
        <div v-if="step===3" class="ani-up">
          <h2 class="sec-title">确认预约</h2>
          <div class="confirm-card card">
            <div class="cf-row"><span>就诊科室</span><span>{{ selectedDept?.name }}</span></div>
            <div class="cf-row"><span>就诊医生</span><span>{{ selectedDoc?.realName }} · {{ selectedDoc?.title }}</span></div>
            <div class="cf-row"><span>就诊时间</span><span>{{ selDate }} {{ slotInfo() }}</span></div>
            <div class="cf-row"><span>挂号费</span><span class="fee">&#165;{{ selectedDoc?.consultationFee||0 }}</span></div>
            <div class="cf-row" v-if="companions.length"><span>就诊人</span>
              <select v-model="cid"><option :value="null">本人</option><option v-for="c in companions" :key="c.id" :value="c.id">{{ c.realName }}</option></select>
            </div>
          </div>
          <div class="cf-actions"><button class="btn btn-primary btn-lg" :disabled="submitting" @click="submit">{{ submitting?'提交中…':'确认预约' }}</button><button class="btn btn-ghost" @click="step=2">返回修改</button></div>
        </div>
      </div>

      <aside class="book-side">
        <div class="side-card card">
          <h4>就诊人</h4>
          <p v-if="!companions.length" class="empty-hint">未添加就诊人</p>
          <p v-for="c in companions" :key="c.id">{{ c.realName }} · {{ c.relationship }}</p>
        </div>
        <div class="side-card card">
          <h4>预约须知</h4>
          <ul><li>可预约未来7天内号源</li><li>就诊前24小时可取消</li><li>3次爽约30天内禁止预约</li><li>请按时就诊，过号需重新排队</li></ul>
        </div>
        <div class="side-card card" v-if="announcements.length">
          <h4>停诊公告</h4>
          <p v-for="a in announcements.slice(0,3)" :key="a.id" class="ann">{{ a.title }}</p>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
const router=useRouter();const route=useRoute()
const step=ref(0)
const steps=['选择科室','选择医生','选择时段','确认预约']
const departments=ref([]);const selectedDept=ref(null)
const doctors=ref([]);const docLoading=ref(false);const selectedDoc=ref(null)
const dates=ref([]);const selDate=ref('');const slots=ref([]);const sLoading=ref(false);const selSlot=ref(null)
const companions=ref([]);const cid=ref(null);const submitting=ref(false)
const announcements=ref([])

const fmtDate=d=>{const dt=new Date(d);return (dt.getMonth()+1)+'/'+dt.getDate()+' '+['周日','周一','周二','周三','周四','周五','周六'][dt.getDay()]}
function slotInfo(){const s=slots.value.find(s=>s.id===selSlot.value);return s?s.timeSlot:''}

async function fetchDepts(){try{const r=await request.get('/hospital/departments');departments.value=r.data||[]}catch{}}
async function fetchCompanions(){try{const r=await request.get('/user/companions');companions.value=r.data||[]}catch{}}
async function fetchAnnouncements(){try{const r=await request.get('/hospital/announcements');announcements.value=(r.data&&r.data.records)||r.data||[]}catch{}}

function pickDept(d){selectedDept.value=d;step.value=1;selectedDoc.value=null;loadDoctors(d.id)}
async function loadDoctors(deptId){
  docLoading.value=true
  try{const r=await request.get('/hospital/doctors',{params:{departmentId:deptId,page:1,size:50}});doctors.value=(r.data&&r.data.records)||r.data||[]}catch{}
  finally{docLoading.value=false}
}
function pickDoctor(d){selectedDoc.value=d;step.value=2;genDates();selSlot.value=null}
function genDates(){dates.value=[];for(let i=0;i<7;i++){const d=new Date();d.setDate(d.getDate()+i);dates.value.push(d.toISOString().split('T')[0])}selDate.value=dates.value[0];fetchSlots()}
async function fetchSlots(){
  sLoading.value=true
  try{const r=await request.get(`/schedule/available/${selectedDoc.value.userId}`,{params:{startDate:selDate.value,endDate:selDate.value}});slots.value=r.data||[]}catch{}
  finally{sLoading.value=false}
}
function pickSlot(){step.value=3}

async function submit(){
  if(!seleSlot.value) return
  submitting.value=true
  try{await request.post('/appointment/book',{doctorId:selectedDoc.value.userId,scheduleId:selSlot.value,companionId:cid.value});ElMessage.success('预约成功');router.push('/my-appointments')}
  catch{}finally{submitting.value=false}
}

onMounted(()=>{fetchDepts();fetchCompanions();fetchAnnouncements()})
watch(step,v=>{if(v===2&&!dates.value.length)genDates()})
</script>

<style scoped>
.book-layout { display:grid;grid-template-columns:1fr 320px;gap:24px; }
.book-main { min-width:0; }
.book-side { display:flex;flex-direction:column;gap:16px; }
.sec-title { font-size:20px;font-weight:600;color:var(--title);margin-bottom:16px; }
.sec-head { display:flex;align-items:center;justify-content:space-between;margin-bottom:16px; }
.sec-head .sec-title { margin-bottom:0; }

.dept-grid { display:grid;grid-template-columns:repeat(4,1fr);gap:12px; }
.dept-card { padding:20px;border:1px solid var(--border);border-radius:var(--radius);text-align:center;font-size:15px;color:var(--body);cursor:pointer;transition:all .15s; }
.dept-card:hover,.dept-card.active { border-color:var(--primary);color:var(--primary);background:var(--primary-light); }

.doc-list { display:flex;flex-direction:column;gap:8px; }
.doc-row { display:flex;align-items:center;gap:14px;padding:16px;border:1px solid var(--border);border-radius:var(--radius);cursor:pointer;transition:all .15s; }
.doc-row:hover,.doc-row.active { border-color:var(--primary);box-shadow:var(--shadow); }
.doc-av { width:44px;height:44px;border-radius:50%;background:var(--primary-light);color:var(--primary);display:flex;align-items:center;justify-content:center;font-size:18px;font-weight:600;flex-shrink:0; }
.doc-info { flex:1;min-width:0; }
.doc-info h4 { font-size:15px;color:var(--title);margin-bottom:4px; }
.doc-info p { font-size:13px;color:var(--caption);white-space:nowrap;overflow:hidden;text-overflow:ellipsis; }
.doc-rate { text-align:right;font-size:14px;color:var(--title); }
.doc-rate .fee { display:block;font-size:12px;color:var(--caption);margin-top:2px; }

.date-tabs { display:flex;gap:8px;margin-bottom:16px;overflow-x:auto; }
.date-tabs button { padding:8px 16px;border:1px solid var(--border);border-radius:20px;background:none;font-size:13px;cursor:pointer;white-space:nowrap;font-family:inherit;transition:all .15s; }
.date-tabs button.active { background:var(--primary);color:#fff;border-color:var(--primary); }
.slot-grid { display:grid;grid-template-columns:repeat(3,1fr);gap:10px; }

.confirm-card { margin-bottom:20px; }
.cf-row { display:flex;justify-content:space-between;padding:12px 0;border-bottom:1px solid var(--border-light);font-size:14px; }
.cf-row span:first-child { color:var(--caption); }
.cf-row span:last-child { color:var(--title);font-weight:500; }
.cf-row .fee { font-size:18px;font-weight:600;color:var(--primary); }
.cf-row select { padding:4px 8px;border:1px solid var(--border);border-radius:4px;font-size:13px;font-family:inherit; }
.cf-actions { display:flex;gap:12px; }

.side-card h4 { font-size:15px;color:var(--title);margin-bottom:12px; }
.side-card ul { padding-left:18px;font-size:13px;color:var(--caption);line-height:2; }
.side-card .ann { font-size:12px;color:var(--primary);margin-bottom:6px;cursor:pointer; }
.empty-hint { font-size:13px;color:var(--caption); }

@media(max-width:768px){ .book-layout{grid-template-columns:1fr}.dept-grid{grid-template-columns:repeat(2,1fr)}.slot-grid{grid-template-columns:repeat(2,1fr)} }
</style>
