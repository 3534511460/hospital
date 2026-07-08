<template>
  <div class="page-w page-main">
    <div class="page-head"><h1>预约管理</h1></div>

    <div class="status-filters">
      <button v-for="f in filters" :key="f.v" :class="{active:filter===f.v}" @click="filter=f.v;fetch()">{{ f.l }}</button>
    </div>

    <div class="apt-list" v-loading="loading">
      <div class="apt-card card" v-for="a in appointments" :key="a.id">
        <div class="apt-top">
          <div>
            <h4>{{ a.patientName }} <span class="tag" :class="statusTag(a.status)">{{ statusText(a.status) }}</span></h4>
            <p>{{ a.departmentName }} · {{ a.timeSlot }} · {{ a.appointmentDate }}</p>
            <p v-if="a.symptomDesc" style="font-size:12px;color:var(--caption);margin-top:4px">{{ a.symptomDesc }}</p>
          </div>
          <div class="apt-date">{{ a.appointmentDate }} <span class="apt-no">{{ a.appointmentNo }}</span></div>
        </div>
        <div class="apt-actions" v-if="a.status===2">
          <button class="btn btn-primary btn-sm" @click="openRecord(a)">写病历</button>
        </div>
      </div>
      <div class="empty-state" v-if="!loading&&appointments.length===0"><p>暂无预约记录</p></div>
    </div>

    <!-- 写病历弹窗 -->
    <div class="modal-bg" v-if="showRecord" @click.self="showRecord=false">
      <div class="modal-card modal-wide" v-loading="recordLoading">
        <h3>病历 — {{ curPatient }}</h3>
        <div class="form-grid">
          <div class="form-item full">
            <label>主诉</label>
            <textarea v-model="form.chiefComplaint" rows="2" placeholder="患者主要不适症状及持续时间"/>
          </div>
          <div class="form-item full">
            <label>现病史</label>
            <textarea v-model="form.presentIllness" rows="3" placeholder="发病情况、症状演变、诊疗经过等"/>
          </div>
          <div class="form-item full">
            <label>既往史</label>
            <textarea v-model="form.pastHistory" rows="2" placeholder="既往疾病史、手术史、过敏史等"/>
          </div>
          <div class="form-item full">
            <label>体格检查</label>
            <textarea v-model="form.physicalExamination" rows="2" placeholder="生命体征及系统检查结果"/>
          </div>
          <div class="form-item full">
            <label>诊断 <span style="color:var(--danger)">*</span></label>
            <input v-model="form.diagnosis" placeholder="临床诊断"/>
          </div>
          <div class="form-item full">
            <label>医嘱</label>
            <textarea v-model="form.advice" rows="2" placeholder="治疗建议及注意事项"/>
          </div>
          <div class="form-item full">
            <label>备注</label>
            <input v-model="form.remark" placeholder="其他补充说明"/>
          </div>
        </div>

        <!-- 处方 -->
        <div class="rx-area">
          <h5>处方 <button class="btn btn-ghost btn-sm" @click="addRx">+ 添加药品</button></h5>
          <div class="rx-row" v-for="(rx,i) in form.prescriptions" :key="i">
            <input v-model="rx.medicationName" placeholder="药品名称"/>
            <input v-model="rx.specification" placeholder="规格"/>
            <input v-model="rx.dosage" placeholder="用量"/>
            <input v-model="rx.frequency" placeholder="频率"/>
            <input v-model="rx.duration" placeholder="疗程"/>
            <button class="btn btn-ghost btn-sm" @click="form.prescriptions.splice(i,1)">删</button>
          </div>
        </div>

        <div class="modal-act">
          <button class="btn btn-primary" @click="submitRecord" :disabled="saving">{{ saving?'保存中…':'保存病历' }}</button>
          <button class="btn btn-ghost" @click="showRecord=false">取消</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref,onMounted } from 'vue'
import { ElMessage,ElMessageBox } from 'element-plus'
import request from '../../utils/request'
const filter=ref('all');const loading=ref(false);const appointments=ref([])
const filters=[{v:'all',l:'全部'},{v:'1',l:'已预约'},{v:'2',l:'已就诊'},{v:'3',l:'已取消'},{v:'4',l:'爽约'}]
function statusTag(s){return s===1?'tag-blue':s===2?'tag-green':s===3?'tag-orange':'tag-red'}
function statusText(s){return s===1?'已预约':s===2?'已就诊':s===3?'已取消':'爽约'}
async function fetch(){
  loading.value=true
  try{
    const r=await request.get('/appointment/doctor',{params:{status:filter.value==='all'?null:filter.value}})
    appointments.value=r.data||[]
  }catch{}
  finally{loading.value=false}
}

// 病历弹窗
const showRecord=ref(false);const recordLoading=ref(false);const saving=ref(false)
const curAptId=ref(null);const curPatientId=ref(null);const curPatient=ref('');const recordId=ref(null)
const emptyForm=()=>({chiefComplaint:'',presentIllness:'',pastHistory:'',physicalExamination:'',diagnosis:'',advice:'',remark:'',prescriptions:[]})
const form=ref(emptyForm())
function addRx(){form.value.prescriptions.push({medicationName:'',specification:'',dosage:'',frequency:'',duration:''})}
async function openRecord(a){
  curAptId.value=a.id;curPatientId.value=a.patientId;curPatient.value=a.patientName
  form.value=emptyForm();recordId.value=null
  showRecord.value=true;recordLoading.value=true
  try{
    const r=await request.get(`/medical/records/appointment/${a.id}`)
    const rec=r.data
    if(rec){
      recordId.value=rec.id
      form.value={chiefComplaint:rec.chiefComplaint||'',presentIllness:rec.presentIllness||'',pastHistory:rec.pastHistory||'',physicalExamination:rec.physicalExamination||'',diagnosis:rec.diagnosis||'',advice:rec.advice||'',remark:rec.remark||'',prescriptions:[]}
      try{
        const rx=await request.get(`/medical/prescriptions/record/${rec.id}`)
        form.value.prescriptions=(rx.data||[]).map(x=>({medicationName:x.medicationName||'',specification:x.specification||'',dosage:x.dosage||'',frequency:x.frequency||'',duration:x.duration||''}))
      }catch{}
    }
  }catch{}
  finally{recordLoading.value=false}
}
async function submitRecord(){
  if(!form.value.diagnosis){ElMessage.warning('请填写诊断');return}
  saving.value=true
  try{
    const payload={appointmentId:curAptId.value,patientId:curPatientId.value,...form.value}
    let recId=recordId.value
    if(recId){
      await request.put(`/medical/records/${recId}`,payload)
    }else{
      const r=await request.post('/medical/records',payload)
      recId=r.data?.id
    }
    // 保存处方
    const rxs=form.value.prescriptions.filter(x=>x.medicationName)
    for(const rx of rxs){
      await request.post('/medical/prescriptions',{medicalRecordId:recId,patientId:curPatientId.value,...rx})
    }
    ElMessage.success('病历已保存');showRecord.value=false
  }catch{}
  finally{saving.value=false}
}
onMounted(fetch)
</script>

<style scoped>
.status-filters { display:flex;gap:8px;margin-bottom:20px; }
.status-filters button { padding:6px 18px;border:1px solid var(--border);border-radius:20px;background:none;font-size:13px;color:var(--body);cursor:pointer;font-family:inherit;transition:all .15s; }
.status-filters button.active { background:var(--primary);color:#fff;border-color:var(--primary); }
.apt-list { display:flex;flex-direction:column;gap:12px; }
.apt-top { display:flex;justify-content:space-between;align-items:flex-start; }
.apt-top h4 { font-size:16px;color:var(--title);margin-bottom:4px; }
.apt-top p { font-size:13px;color:var(--caption); }
.apt-date { text-align:right;font-size:13px;color:var(--body);white-space:nowrap; }
.apt-no { display:block;font-size:11px;color:var(--caption); }
.apt-actions { display:flex;gap:8px;padding:0 0 14px 0; }

.modal-bg { position:fixed;inset:0;background:rgba(0,0,0,.35);z-index:300;display:flex;align-items:flex-start;justify-content:center;padding-top:40px;overflow-y:auto; }
.modal-card { background:var(--surface);border-radius:var(--radius);padding:28px;width:640px;max-width:94vw;box-shadow:var(--shadow-md);margin-bottom:40px; }
.modal-card h3 { font-size:18px;color:var(--title);margin-bottom:20px; }
.form-grid { display:grid;grid-template-columns:1fr 1fr;gap:14px 20px; }
.form-item.full { grid-column:1/-1; }
.form-item label { display:block;font-size:13px;color:var(--body);margin-bottom:4px;font-weight:500; }
.form-item input,.form-item textarea { width:100%;border:1px solid var(--border);border-radius:var(--radius-sm);padding:8px 10px;font-size:14px;font-family:inherit;outline:none;box-sizing:border-box; }
.form-item input:focus,.form-item textarea:focus { border-color:var(--primary); }
.rx-area { margin-top:18px;border-top:1px solid var(--border-light);padding-top:16px; }
.rx-area h5 { font-size:14px;color:var(--title);margin-bottom:10px;display:flex;align-items:center;gap:12px; }
.rx-row { display:flex;gap:6px;margin-bottom:8px;flex-wrap:wrap; }
.rx-row input { width:100px;border:1px solid var(--border);border-radius:var(--radius-sm);padding:6px 8px;font-size:13px;font-family:inherit;outline:none; }
.rx-row input:focus { border-color:var(--primary); }
.modal-act { display:flex;gap:8px;margin-top:20px; }
</style>
