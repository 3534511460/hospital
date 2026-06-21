<template>
  <div class="page-w page-main">
    <div class="back-bar"><button class="btn btn-ghost btn-sm" @click="$router.back()">&#8592; 返回列表</button></div>

    <div class="detail-card card" v-loading="loading">
      <div class="doc-header">
        <div class="doc-av">{{ (doctor.realName||'?')[0] }}</div>
        <div class="doc-info">
          <h1>{{ doctor.realName }} <span class="tag tag-blue">{{ doctor.title }}</span></h1>
          <p class="dept">{{ doctor.departmentName }}</p>
          <p class="rating">&#9733; {{ doctor.avgRating||'5.0' }} ({{ doctor.ratingCount||0 }}条评价)</p>
        </div>
      </div>

      <div class="section">
        <h3>擅长领域</h3>
        <p>{{ doctor.specialty||'暂无信息' }}</p>
      </div>
      <div class="section" v-if="doctor.introduction">
        <h3>医生简介</h3>
        <p>{{ doctor.introduction }}</p>
      </div>

      <div class="doc-actions">
        <div class="fee-line">挂号费 <strong>&#165;{{ doctor.consultationFee||0 }}</strong></div>
        <div class="btn-row">
          <button class="btn btn-primary btn-lg" @click="goBook">预约挂号</button>
          <button class="btn btn-outline btn-lg" @click="goChat">线上问诊</button>
        </div>
      </div>
    </div>

    <!-- 评价列表 -->
    <div class="eval-section" v-if="evaluations.length>0">
      <h2>患者评价</h2>
      <div class="eval-card card" v-for="e in evaluations" :key="e.id" style="margin-bottom:12px">
        <div class="eval-top"><span class="eval-patient">{{ e.isAnonymous?'匿名用户':(e.patientName||'患者') }}</span><span class="eval-stars">{{ '★'.repeat(e.rating) }}{{ '☆'.repeat(5-e.rating) }}</span></div>
        <p class="eval-content" v-if="e.content">{{ e.content }}</p>
        <span class="eval-time">{{ e.createTime }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref,onMounted } from 'vue'
import { useRoute,useRouter } from 'vue-router'
import request from '../../utils/request'
const route=useRoute();const router=useRouter()
const doctor=ref({});const evaluations=ref([]);const loading=ref(false)

async function fetch(){
  loading.value=true;const id=route.params.id
  try{
    const r=await request.get(`/hospital/doctors/${id}`)
    doctor.value=r.data||{}
    const er=await request.get(`/medical/evaluations/doctor/${id}`)
    evaluations.value=er.data||[]
  }catch{}finally{loading.value=false}
}
function goBook(){router.push(`/appointment?doctorId=${doctor.value.userId}&doctorName=${doctor.value.realName}`)}
async function goChat(){
  try{
    const r=await request.post('/chat/sessions',{doctorId:doctor.value.userId})
    router.push('/chat')
  }catch{router.push('/chat')}
}

onMounted(fetch)
</script>

<style scoped>
.back-bar { margin-bottom:16px; }
.doc-header { display:flex;align-items:center;gap:20px;margin-bottom:28px; }
.doc-av { width:72px;height:72px;border-radius:50%;background:var(--primary-light);color:var(--primary);display:flex;align-items:center;justify-content:center;font-size:30px;font-weight:600;flex-shrink:0; }
.doc-info h1 { font-size:24px;color:var(--title);margin-bottom:6px; }
.dept { font-size:14px;color:var(--caption);margin-bottom:4px; }
.rating { font-size:14px;color:#D4A853; }
.section { margin-bottom:20px; }
.section h3 { font-size:15px;color:var(--title);margin-bottom:8px; }
.section p { font-size:14px;color:var(--body);line-height:1.8; }
.doc-actions { display:flex;align-items:center;justify-content:space-between;padding-top:20px;border-top:1px solid var(--border-light); }
.fee-line { font-size:14px;color:var(--caption); }
.fee-line strong { font-size:24px;color:var(--primary);font-weight:700; }
.btn-row { display:flex;gap:12px; }

.eval-section { margin-top:32px; }
.eval-section h2 { font-size:20px;color:var(--title);margin-bottom:16px; }
.eval-top { display:flex;justify-content:space-between;margin-bottom:8px; }
.eval-patient { font-size:14px;font-weight:500;color:var(--title); }
.eval-stars { color:#F2C94C;font-size:14px; }
.eval-content { font-size:14px;color:var(--body);margin-bottom:8px; }
.eval-time { font-size:12px;color:var(--caption); }
</style>
