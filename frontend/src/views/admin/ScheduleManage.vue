<template>
  <div class="page-w page-main">
    <div class="admin-layout">
      <div class="admin-sidebar">
        <h3>系统管理</h3>
        <a :class="{active:$route.path==='/admin/users'}" @click="$router.push('/admin/users')">用户管理</a>
        <a :class="{active:$route.path==='/admin/doctors'}" @click="$router.push('/admin/doctors')">医生管理</a>
        <a :class="{active:$route.path==='/admin/schedules'}" @click="$router.push('/admin/schedules')">排班管理</a>
        <a :class="{active:$route.path==='/admin/departments'}" @click="$router.push('/admin/departments')">科室管理</a>
        <a :class="{active:$route.path==='/admin/announcements'}" @click="$router.push('/admin/announcements')">公告管理</a>
        <a :class="{active:$route.path==='/admin/statistics'}" @click="$router.push('/admin/statistics')">数据统计</a>
      </div>
      <div class="admin-main">
        <div class="page-head" style="display:flex;justify-content:space-between;align-items:center">
          <div style="display:flex;align-items:center;gap:12px">
            <h1>医生排班管理</h1>
            <div class="view-toggle">
              <button :class="{active:viewMode==='table'}" @click="viewMode='table'">表格</button>
              <button :class="{active:viewMode==='timetable'}" @click="viewMode='timetable'">课表</button>
            </div>
          </div>
          <div class="btn-row"><button class="btn btn-primary btn-sm" @click="openAdd">+ 新增排班</button></div>
        </div>

        <!-- 医生选择 + 周切换 + 复制按钮 -->
        <div class="toolbar">
          <div class="capsule-row">
            <button :class="['capsule',{active:!selDoctor}]" @click="selDoctor=null;fetchScheds()">全部</button>
            <button v-for="d in docs" :key="d.userId" :class="['capsule',{active:selDoctor===d.userId}]" @click="selDoctor=d.userId;fetchScheds()">{{ d.realName }}</button>
          </div>
          <div class="week-row">
            <button class="btn btn-outline btn-sm" @click="prevWeek">上一周</button>
            <span class="week-label">{{ weekLabel }}</span>
            <button class="btn btn-outline btn-sm" @click="nextWeek">下一周</button>
            <button v-if="selDoctor" class="btn btn-primary btn-sm" @click="copyLastWeek" :disabled="copying">
              {{ copying ? '复制中...' : '📋 复制上周排班' }}
            </button>
          </div>
        </div>

        <!-- 排班表格 -->
        <div class="card" style="padding:0;overflow:hidden" v-loading="loading">
          <table class="tbl"><thead><tr><th>医生</th><th>日期</th><th>星期</th><th>时段</th><th>号源</th><th>已约</th><th>状态</th><th>操作</th></tr></thead>
          <tbody><tr v-for="s in pagedScheds" :key="s.id">
            <td>{{ s.doctorName||'-' }}</td>
            <td>{{ s.workDate }}</td>
            <td>{{ dayOfWeek(s.workDate) }}</td>
            <td>{{ s.timeSlot }}</td>
            <td>{{ s.maxCount }}</td>
            <td>{{ s.bookedCount }}</td>
            <td><span class="tag" :class="s.status===1?'tag-green':s.status===0?'tag-orange':'tag-red'">{{ s.status===1?'正常':s.status===0?'审核中':'停诊' }}</span></td>
            <td>
              <button v-if="s.status===0" class="btn btn-primary btn-sm" @click="approve(s.id)">通过</button>
              <button v-if="s.status===0" class="btn btn-outline btn-sm" @click="reject(s.id)">驳回</button>
              <button v-if="s.status!==0" class="btn btn-ghost btn-sm" @click="del(s.id)" :disabled="s.bookedCount>0">删除</button>
            </td>
          </tr></tbody></table>
          <div class="empty-state" v-if="!loading&&schedules.length===0"><p>暂无排班</p></div>
        </div>

        <!-- 课表视图 -->
        <div class="timetable card" v-if="viewMode==='timetable'&&selDoctor" style="overflow-x:auto">
          <table class="tt-table">
            <thead><tr><th class="tt-th">时段</th><th v-for="d in ttDays" :key="d.label" class="tt-th" :class="{today:d.isToday}">{{ d.label }}<br>{{ d.date }}</th></tr></thead>
            <tbody><tr v-for="slot in slotOptions" :key="slot">
              <td class="tt-slot">{{ slot }}</td>
              <td v-for="d in ttDays" :key="d.date" class="tt-cell" :class="cellClass(slot,d.date)" @click="clickCell(slot,d.date)">
                <template v-if="getCell(slot,d.date)">
                  <span class="tt-count">{{ getCell(slot,d.date).bookedCount }}/{{ getCell(slot,d.date).maxCount }}</span>
                  <button class="tt-del" @click.stop="del(getCell(slot,d.date).id)" :disabled="getCell(slot,d.date).bookedCount>0" title="删除">×</button>
                </template>
                <span v-else class="tt-add-hint">+</span>
              </td>
            </tr></tbody>
          </table>
        </div>
        <div class="empty-state" v-if="viewMode==='timetable'&&!selDoctor"><p>请先选择一个医生查看课表</p></div>

        <!-- 分页 -->
        <div class="pager" v-if="viewMode==='table'&&totalPages>1">
          <button class="btn btn-ghost btn-sm" :disabled="page===1" @click="page--">上一页</button>
          <span>{{ page }} / {{ totalPages }}</span>
          <button class="btn btn-ghost btn-sm" :disabled="page===totalPages" @click="page++">下一页</button>
        </div>
      </div>
    </div>

    <!-- 新增排班弹窗 -->
    <div class="modal-bg" v-if="showAdd" @click.self="showAdd=false">
      <div class="modal"><h3>新增排班</h3>
        <div class="f"><label>医生</label><select v-model="form.doctorId"><option :value="null">请选择</option><option v-for="d in docs" :key="d.userId" :value="d.userId">{{ d.realName }} - {{ d.title }}</option></select></div>
        <div class="f"><label>日期</label><input v-model="form.workDate" type="date"/></div>
        <div class="f"><label>时段</label><select v-model="form.timeSlot"><option v-for="s in slotOptions" :key="s" :value="s">{{ s }}</option></select></div>
        <div class="f"><label>号源数</label><input v-model.number="form.maxCount" type="number" min="1" max="100"/></div>
        <div class="mact"><button class="btn btn-primary" :disabled="adding" @click="addSched">{{ adding?'添加中…':'确认添加' }}</button><button class="btn btn-ghost" @click="showAdd=false">取消</button></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref,reactive,computed,onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
const docs=ref([]);const schedules=ref([]);const selDoctor=ref(null);const loading=ref(false)
const showAdd=ref(false);const adding=ref(false);const copying=ref(false);const viewMode=ref('table')
const page=ref(1);const pageSize=15
const slotOptions=['08:00-09:00','09:00-10:00','10:00-11:00','11:00-12:00','14:00-15:00','15:00-16:00','16:00-17:00']
const form=reactive({doctorId:null,workDate:'',timeSlot:'08:00-09:00',maxCount:30})

// Week navigation
const weekOffset=ref(0)
const weekLabel=computed(()=>{
  const mon=getMonday(weekOffset.value);const sun=new Date(mon);sun.setDate(sun.getDate()+6)
  return `${fmt(mon)} ~ ${fmt(sun)}`
})

function fmt(d){const y=d.getFullYear();const m=String(d.getMonth()+1).padStart(2,'0');const dd=String(d.getDate()).padStart(2,'0');return `${y}-${m}-${dd}`}
function getMonday(offset){
  const t=new Date();const dow=t.getDay()||7; // 1=Mon...7=Sun
  t.setDate(t.getDate()-(dow-1)+offset*7);t.setHours(0,0,0,0);return t
}
function dayOfWeek(d){const n=new Date(d+'T00:00:00').getDay();return ['日','一','二','三','四','五','六'][n]}

const pagedScheds=computed(()=>{
  const start=(page.value-1)*pageSize
  return schedules.value.slice(start,start+pageSize)
})
const totalPages=computed(()=>Math.max(1,Math.ceil(schedules.value.length/pageSize)))

// Timetable computed
const ttDays=computed(()=>{
  const days=[];const today=fmt(new Date());const mon=getMonday(weekOffset.value)
  for(let i=0;i<7;i++){const d=new Date(mon);d.setDate(d.getDate()+i);const ds=fmt(d);days.push({date:ds,label:['一','二','三','四','五','六','日'][i],isToday:ds===today})}
  return days
})
function getCell(slot,date){return schedules.value.find(s=>s.timeSlot===slot&&s.workDate===date)}
function cellClass(slot,date){
  const s=getCell(slot,date);if(!s) return 'empty'
  if(s.bookedCount>=s.maxCount) return 'full'
  if(s.status===0) return 'pending'
  return 'available'
}
function clickCell(slot,date){
  if(getCell(slot,date)) return
  form.doctorId=selDoctor.value;form.workDate=date;form.timeSlot=slot;form.maxCount=30;showAdd.value=true
}

function prevWeek(){weekOffset.value--;fetchScheds()}
function nextWeek(){weekOffset.value++;fetchScheds()}

async function fetchDocs(){try{const r=await request.get('/admin/doctors');docs.value=r.data||[]}catch{}}
async function fetchScheds(){
  loading.value=true;page.value=1
  try{
    const mon=getMonday(weekOffset.value);const sun=new Date(mon);sun.setDate(sun.getDate()+6)
    const start=fmt(mon);const end=fmt(sun)
    if(selDoctor.value){
      const r=await request.get('/schedule/week',{params:{doctorId:selDoctor.value,start,end}})
      schedules.value=r.data||[]
    }else{
      const r=await request.get('/schedule/all')
      schedules.value=(r.data||[]).filter(s=>s.workDate>=start&&s.workDate<=end)
    }
  }catch{}finally{loading.value=false}
}

async function copyLastWeek(){
  if(!selDoctor.value) return ElMessage.warning('请先选择一个医生')
  copying.value=true
  const targetMon=fmt(getMonday(weekOffset.value))
  try{const r=await request.post(`/schedule/copy-last-week/${selDoctor.value}?targetMonday=${targetMon}`);ElMessage.success(`已复制 ${r.data.copied} 条排班`);fetchScheds()}
  catch{}finally{copying.value=false}
}

function openAdd(){form.doctorId=selDoctor.value||null;form.workDate='';form.timeSlot='08:00-09:00';form.maxCount=30;showAdd.value=true}
async function addSched(){
  if(!form.doctorId||!form.workDate) return ElMessage.warning('请选择医生和日期')
  adding.value=true
  try{await request.post('/schedule',{doctorId:form.doctorId,workDate:form.workDate,timeSlot:form.timeSlot,maxCount:form.maxCount,status:1});ElMessage.success('添加成功');showAdd.value=false;fetchScheds()}catch{}
  finally{adding.value=false}
}
async function approve(id){try{await request.put(`/schedule/${id}`,{status:1});ElMessage.success('已通过');fetchScheds()}catch{}}
async function reject(id){try{await request.delete(`/schedule/${id}`);ElMessage.success('已驳回');fetchScheds()}catch{}}
async function del(id){try{await request.delete(`/schedule/${id}`);ElMessage.success('已删除');fetchScheds()}catch{}}
onMounted(()=>{fetchDocs();fetchScheds()})
</script>

<style scoped>
.admin-layout{display:grid;grid-template-columns:180px 1fr;gap:24px}
.admin-sidebar{background:var(--surface);border-radius:var(--radius);box-shadow:var(--shadow);padding:16px;position:sticky;top:80px;height:fit-content}
.admin-sidebar h3{font-size:14px;color:var(--title);margin-bottom:12px;padding-bottom:8px;border-bottom:1px solid var(--border-light)}
.admin-sidebar a{display:block;padding:8px 12px;font-size:13px;color:var(--body);cursor:pointer;border-radius:var(--radius-sm);margin-bottom:2px;transition:all .15s}
.admin-sidebar a:hover,.admin-sidebar a.active{background:var(--primary-light);color:var(--primary)}
.admin-main{min-width:0}
.toolbar{margin-bottom:16px;display:flex;flex-direction:column;gap:12px}
.capsule-row{display:flex;gap:6px;flex-wrap:wrap}
.view-toggle{display:flex;border:1px solid var(--border);border-radius:20px;overflow:hidden}
.view-toggle button{padding:4px 14px;border:none;background:none;font-size:12px;cursor:pointer;font-family:inherit;transition:all .15s}
.view-toggle button.active{background:var(--primary);color:#fff}

.timetable{padding:0;overflow:hidden}
.tt-table{width:100%;border-collapse:collapse;table-layout:fixed}
.tt-th{padding:10px 8px;text-align:center;font-size:12px;color:var(--caption);border-bottom:1px solid var(--border);font-weight:500}
.tt-th.today{color:var(--primary);font-weight:600;background:var(--primary-light)}
.tt-slot{padding:6px 8px;font-size:11px;color:var(--caption);text-align:center;border-bottom:1px solid var(--border-light);white-space:nowrap;width:90px}
.tt-cell{padding:6px;text-align:center;border:1px solid var(--border-light);min-width:80px;height:52px;transition:all .15s;position:relative;cursor:default}
.tt-cell.available{background:#E8FAF0}
.tt-cell.full{background:#FFECE8}
.tt-cell.pending{background:#FFF3E8}
.tt-cell.empty{cursor:pointer}
.tt-cell.empty:hover{background:var(--primary-light)}
.tt-count{font-size:13px;font-weight:600;color:var(--title)}
.tt-add-hint{font-size:16px;color:var(--border);opacity:0}
.tt-cell.empty:hover .tt-add-hint{opacity:1;color:var(--primary)}
.tt-del{position:absolute;top:2px;right:4px;border:none;background:none;font-size:14px;color:var(--caption);cursor:pointer;padding:0;line-height:1}
.tt-del:hover{color:var(--error)}
.tt-del:disabled{display:none}
.week-row{display:flex;align-items:center;gap:8px}
.week-label{font-size:13px;color:var(--body);min-width:200px;text-align:center}
.btn-row{display:flex;gap:8px}
.tbl{width:100%;border-collapse:collapse}
.tbl th,.tbl td{padding:10px 14px;text-align:left;font-size:13px;border-bottom:1px solid var(--border-light)}
.tbl th{font-weight:500;color:var(--caption);font-size:12px}
.tbl tbody tr:hover{background:var(--bg)}
.pager{display:flex;align-items:center;justify-content:center;gap:12px;margin-top:14px;font-size:13px;color:var(--body)}
.modal-bg{position:fixed;inset:0;background:rgba(0,0,0,.35);z-index:300;display:flex;align-items:center;justify-content:center}
.modal{background:var(--surface);border-radius:var(--radius);padding:24px;width:400px;max-width:90vw;box-shadow:var(--shadow-md)}
.modal h3{font-size:17px;color:var(--title);margin-bottom:16px}
.f{margin-bottom:12px}
.f label{display:block;font-size:13px;color:var(--body);margin-bottom:4px}
.f input,.f select{width:100%;height:38px;padding:0 12px;border:1px solid var(--border);border-radius:var(--radius-sm);font-size:14px;font-family:inherit;color:var(--title);outline:none;background:var(--surface)}
.f input:focus,.f select:focus{border-color:var(--primary)}
.mact{display:flex;gap:8px;margin-top:16px}
@media(max-width:768px){.admin-layout{grid-template-columns:1fr}.admin-sidebar{position:static;display:flex;gap:4px;flex-wrap:wrap;padding:8px}.admin-sidebar h3{display:none}.admin-sidebar a{font-size:12px;padding:4px 10px}}
</style>
