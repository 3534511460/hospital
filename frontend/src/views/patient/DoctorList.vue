<template>
  <div class="page-w page-main">
    <div class="page-head"><h1>找医生</h1></div>
    <div class="filters">
      <button :class="{active:!selDept}" @click="selDept=null;fetch()">全部科室</button>
      <button v-for="d in depts" :key="d.id" :class="{active:selDept===d.id}" @click="selDept=d.id;fetch()">{{ d.name }}</button>
      <input v-model="keyword" placeholder="搜索医生姓名..." @input="fetch" style="margin-left:auto"/>
    </div>
    <div class="doc-grid" v-loading="loading">
      <div class="doc-card card" v-for="d in doctors" :key="d.userId" @click="$router.push('/appointment?doctorId='+d.userId+'&doctorName='+d.realName)">
        <div class="doc-top"><div class="doc-av">{{ (d.realName||'?')[0] }}</div><div><h4>{{ d.realName }} <span class="tag tag-blue">{{ d.title }}</span></h4><p>{{ d.departmentName }}</p></div></div>
        <p class="spec">{{ (d.specialty||'').slice(0,80) }}</p>
        <div class="doc-foot"><span class="star">&#9733; {{ d.avgRating||'5.0' }}</span><span class="fee">&#165;{{ d.consultationFee||0 }}</span><button class="btn btn-primary btn-sm">预约</button></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref,onMounted } from 'vue'
import request from '../../utils/request'
const depts=ref([]);const doctors=ref([]);const selDept=ref(null);const keyword=ref('');const loading=ref(false)
async function fetchD(){try{const r=await request.get('/hospital/departments');depts.value=r.data||[]}catch{}}
async function fetch(){loading.value=true;try{const r=await request.get('/hospital/doctors',{params:{departmentId:selDept.value,name:keyword.value||null,page:1,size:50}});doctors.value=(r.data?.records||r.data||[])}catch{}finally{loading.value=false}}
onMounted(()=>{fetchD();fetch()})
</script>

<style scoped>
.filters { display:flex;align-items:center;gap:8px;margin-bottom:20px;flex-wrap:wrap; }
.filters button { padding:6px 16px;border:1px solid var(--border);border-radius:20px;background:none;font-size:13px;color:var(--body);cursor:pointer;font-family:inherit;transition:all .15s; }
.filters button.active { background:var(--primary);color:#fff;border-color:var(--primary); }
.filters input { padding:6px 14px;border:1px solid var(--border);border-radius:20px;font-size:13px;outline:none;font-family:inherit;width:180px; }
.filters input:focus { border-color:var(--primary); }
.doc-grid { display:grid;grid-template-columns:repeat(2,1fr);gap:14px; }
.doc-top { display:flex;align-items:center;gap:12px;margin-bottom:10px; }
.doc-av { width:44px;height:44px;border-radius:50%;background:var(--primary-light);color:var(--primary);display:flex;align-items:center;justify-content:center;font-size:18px;font-weight:600;flex-shrink:0; }
.doc-top h4 { font-size:15px;color:var(--title);margin-bottom:3px; }
.doc-top p { font-size:12px;color:var(--caption); }
.spec { font-size:13px;color:var(--caption);margin-bottom:12px;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden; }
.doc-foot { display:flex;align-items:center;justify-content:space-between; }
.star { font-size:14px;color:#F2C94C; }
.fee { font-size:17px;font-weight:600;color:var(--title); }
@media(max-width:768px){ .doc-grid{grid-template-columns:1fr} }
</style>
