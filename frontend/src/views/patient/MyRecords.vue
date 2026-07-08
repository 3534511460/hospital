<template>
  <div class="page-w page-main">
    <div class="page-head"><h1>我的病历</h1></div>
    <div class="record-list" v-loading="loading">
      <div class="record-card card" v-for="r in records" :key="r.id">
        <div class="record-top" @click="toggle(r)">
          <div>
            <h4>{{ r.diagnosis||'未诊断' }} <span class="tag tag-blue">病历</span></h4>
            <p>{{ r.doctorName||'' }} · {{ r.appointmentDate||r.createTime }}</p>
            <p class="chief" v-if="r.chiefComplaint">{{ r.chiefComplaint }}</p>
          </div>
          <span class="expand-arrow">{{ r._open ? '▲' : '▼' }}</span>
        </div>
        <div class="record-detail" v-if="r._open" v-loading="r._loadingRx">
          <div class="detail-grid">
            <div class="detail-item" v-if="r.chiefComplaint"><label>主诉</label><span>{{ r.chiefComplaint }}</span></div>
            <div class="detail-item" v-if="r.presentIllness"><label>现病史</label><span>{{ r.presentIllness }}</span></div>
            <div class="detail-item" v-if="r.pastHistory"><label>既往史</label><span>{{ r.pastHistory }}</span></div>
            <div class="detail-item" v-if="r.physicalExamination"><label>体格检查</label><span>{{ r.physicalExamination }}</span></div>
            <div class="detail-item" v-if="r.advice"><label>医嘱</label><span>{{ r.advice }}</span></div>
            <div class="detail-item" v-if="r.remark"><label>备注</label><span>{{ r.remark }}</span></div>
          </div>
          <div class="rx-section" v-if="r._prescriptions&&r._prescriptions.length">
            <h5>处方</h5>
            <table class="rx-tbl">
              <thead><tr><th>药品名称</th><th>规格</th><th>用量</th><th>频率</th><th>疗程</th></tr></thead>
              <tbody><tr v-for="rx in r._prescriptions" :key="rx.id">
                <td>{{ rx.medicationName }}</td>
                <td>{{ rx.specification||'-' }}</td>
                <td>{{ rx.dosage||'-' }}</td>
                <td>{{ rx.frequency||'-' }}</td>
                <td>{{ rx.duration||'-' }}</td>
              </tr></tbody>
            </table>
          </div>
        </div>
      </div>
      <div class="empty-state" v-if="!loading&&records.length===0"><p>暂无病历记录</p></div>
    </div>
  </div>
</template>

<script setup>
import { ref,onMounted } from 'vue'
import request from '../../utils/request'
const records=ref([]);const loading=ref(false)
async function fetch(){
  loading.value=true
  try{const r=await request.get('/medical/records/patient');records.value=(r.data||[]).map(x=>({...x,_open:false,_prescriptions:[],_loadingRx:false}))}catch{}
  finally{loading.value=false}
}
async function toggle(r){
  r._open=!r._open
  if(r._open&&!r._prescriptions.length){
    r._loadingRx=true
    try{const res=await request.get(`/medical/prescriptions/record/${r.id}`);r._prescriptions=res.data||[]}catch{}
    finally{r._loadingRx=false}
  }
}
onMounted(fetch)
</script>

<style scoped>
.record-list { display:flex;flex-direction:column;gap:12px; }
.record-top { display:flex;justify-content:space-between;align-items:flex-start;cursor:pointer;padding:16px; }
.record-top h4 { font-size:16px;color:var(--title);margin-bottom:4px; }
.record-top p { font-size:13px;color:var(--caption); }
.chief { color:var(--body)!important;margin-top:4px; }
.expand-arrow { font-size:12px;color:var(--caption);padding-top:4px;flex-shrink:0; }
.record-detail { padding:0 16px 16px;border-top:1px solid var(--border-light); }
.detail-grid { display:grid;grid-template-columns:1fr 1fr;gap:10px 24px;margin-top:14px; }
.detail-item { }
.detail-item label { display:block;font-size:11px;color:var(--caption);margin-bottom:2px; }
.detail-item span { font-size:14px;color:var(--body); }
.rx-section { margin-top:16px; }
.rx-section h5 { font-size:14px;color:var(--title);margin-bottom:8px; }
.rx-tbl { width:100%;border-collapse:collapse;font-size:13px; }
.rx-tbl th,.rx-tbl td { padding:6px 10px;text-align:left;border-bottom:1px solid var(--border-light); }
.rx-tbl th { font-weight:500;color:var(--caption);font-size:12px; }
@media(max-width:768px){ .detail-grid{grid-template-columns:1fr} }
</style>
