<template>
  <div class="home-experience">
    <MedicalHero />

    <section class="care-flow page-w reveal-block">
      <div class="section-heading">
        <p class="eyebrow">就医流程</p>
        <h2>把复杂留给系统，<br>把从容还给你。</h2>
        <p>从初次查找医生，到就诊后的健康记录，常用服务都在一个清晰的流程里。</p>
      </div>
      <div class="flow-list">
        <button v-for="(feature, index) in features" :key="feature.title" @click="$router.push(feature.path)">
          <span class="flow-index">0{{ index + 1 }}</span>
          <el-icon><component :is="feature.icon" /></el-icon>
          <strong>{{ feature.title }}</strong>
          <small>{{ feature.description }}</small>
          <el-icon class="flow-arrow"><ArrowRight /></el-icon>
        </button>
      </div>
    </section>

    <section v-if="queueData.length" class="queue-band reveal-block">
      <div class="page-w">
        <div class="section-bar">
          <div><p class="eyebrow">实时服务</p><h2>今日叫号</h2></div>
          <button class="text-button" @click="$router.push('/queue/board')">查看叫号大屏 <el-icon><ArrowRight /></el-icon></button>
        </div>
        <div class="queue-grid">
          <article v-for="q in queueData.slice(0, 4)" :key="q.doctorId" :class="{ live: currentQueue(q) }">
            <div class="queue-doctor"><strong>{{ q.doctorName }}</strong><span>{{ q.departmentName }}</span></div>
            <div class="queue-number">{{ currentQueue(q)?.queueNumber || '—' }}</div>
            <p><i></i>{{ currentQueue(q) ? `${currentQueue(q).patientName} 就诊中` : '等待叫号' }}</p>
          </article>
        </div>
      </div>
    </section>

    <section class="trust-band reveal-block">
      <div class="page-w trust-inner">
        <div class="section-heading compact">
          <p class="eyebrow">服务能力</p>
          <h2>每一个数字，<br>都来自真实服务。</h2>
        </div>
        <div class="numbers-list">
          <div v-for="item in stats" :key="item.label" class="number-item">
            <strong><span ref="statEls">0</span>{{ item.suffix }}</strong>
            <span>{{ item.label }}</span>
          </div>
        </div>
      </div>
    </section>

    <section class="departments page-w reveal-block">
      <div class="section-bar">
        <div><p class="eyebrow">临床科室</p><h2>按科室查找医生</h2></div>
        <button class="text-button" @click="$router.push('/doctors')">全部医生 <el-icon><ArrowRight /></el-icon></button>
      </div>
      <div class="department-list">
        <button v-for="dept in departments" :key="dept.id" @click="$router.push('/doctors?departmentId=' + dept.id)">
          <span>{{ String(dept.id).padStart(2, '0') }}</span>
          <strong>{{ dept.name }}</strong>
          <small>{{ dept.description }}</small>
          <el-icon><ArrowRight /></el-icon>
        </button>
      </div>
    </section>

    <footer class="experience-footer">
      <div class="page-w">
        <div><strong>医预约</strong><span>让医疗服务，回归清晰与关怀。</span></div>
        <p>预约咨询 010-12345678</p>
        <small>© 2026 MedReserve</small>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import MedicalHero from '../../components/MedicalHero.vue'
import request from '../../utils/request'

const statEls = ref([])
const queueData = ref([])
const stats = [
  { value: 20, suffix: '+', label: '临床科室' },
  { value: 200, suffix: '+', label: '执业医师' },
  { value: 50, suffix: 'k+', label: '累计服务患者' }
]
const features = [
  { icon: 'Search', title: '找到合适医生', description: '按科室、姓名与专长筛选', path: '/doctors' },
  { icon: 'Calendar', title: '选择就诊时间', description: '查看余号并在线完成预约', path: '/doctors' },
  { icon: 'ChatDotRound', title: '获得持续照护', description: '病历、消息与复诊统一管理', path: '/my-appointments' }
]
const departments = ref([])

async function fetchDepartments() {
  try {
    const res = await request.get('/hospital/departments')
    departments.value = res.data || []
  } catch {}
}

function currentQueue(item) {
  return item.queue?.find(queue => queue.status === 1)
}

async function fetchQueue() {
  try {
    const [doctorResponse, queueResponse] = await Promise.all([
      request.get('/hospital/doctors', { params: { page: 1, size: 100 } }),
      request.get('/queue/board/today')
    ])
    const doctors = doctorResponse.data?.records || doctorResponse.data || []
    const queues = queueResponse.data || []
    queueData.value = doctors
      .filter(doctor => doctor.status === 1)
      .map(doctor => ({
        doctorId: doctor.userId,
        doctorName: doctor.realName,
        departmentName: doctor.departmentName,
        queue: queues.filter(item => item.doctorId === doctor.userId)
      }))
      .filter(item => item.queue.some(queue => queue.status === 0 || queue.status === 1))
      .slice(0, 8)
  } catch {}
}

function animateCount(el, target) {
  let start
  const tick = now => {
    if (!start) start = now
    const progress = Math.min((now - start) / 900, 1)
    el.textContent = Math.round(target * (1 - Math.pow(1 - progress, 3)))
    if (progress < 1) requestAnimationFrame(tick)
  }
  requestAnimationFrame(tick)
}

let observer
onMounted(async () => {
  await nextTick()
  observer = new IntersectionObserver(entries => entries.forEach(entry => {
    if (!entry.isIntersecting) return
    entry.target.classList.add('revealed')
    if (entry.target.classList.contains('trust-band')) statEls.value.forEach((el, index) => animateCount(el, stats[index].value))
    observer.unobserve(entry.target)
  }), { threshold: 0.02 })
  document.querySelectorAll('.reveal-block').forEach(el => observer.observe(el))
  fetchQueue()
  fetchDepartments()
})
onUnmounted(() => observer?.disconnect())
</script>

<style scoped>
.home-experience { margin: -24px 0; overflow: hidden; background: #fbfcfa; color: #17221d; }
.eyebrow { margin-bottom: 14px; color: #337558; font-size: 12px; font-weight: 700; letter-spacing: 0; }
.section-heading { max-width: 560px; }
.section-heading h2, .section-bar h2 { color: #17221d; font-size: clamp(34px, 4.2vw, 56px); font-weight: 620; line-height: 1.18; letter-spacing: 0; }
.section-heading > p:last-child { margin-top: 22px; color: #657169; font-size: 16px; line-height: 1.8; }
.care-flow { padding-top: 80px; padding-bottom: clamp(100px, 12vw, 160px); display: grid; grid-template-columns: .9fr 1.1fr; gap: clamp(60px, 9vw, 130px); align-items: start; }
.flow-list { border-top: 1px solid #cfd8d1; }
.flow-list button { width: 100%; min-height: 112px; display: grid; grid-template-columns: 34px 36px 1fr 1.2fr 22px; align-items: center; gap: 16px; padding: 18px 4px; border: 0; border-bottom: 1px solid #cfd8d1; color: #17221d; background: transparent; text-align: left; font: inherit; cursor: pointer; transition: padding .2s ease, background .2s ease; }
.flow-list button:hover { padding-left: 12px; background: #f1f5f1; }
.flow-list button > .el-icon { width: 36px; height: 36px; border: 1px solid #aebeb3; color: #337558; font-size: 17px; }
.flow-list .flow-arrow { width: auto; height: auto; border: 0; color: #637168; }
.flow-index, .flow-list small { color: #7d8981; font-size: 12px; }
.flow-list strong { font-size: 16px; font-weight: 600; }
.section-bar { display: flex; align-items: end; justify-content: space-between; gap: 30px; margin-bottom: 40px; }
.section-bar h2 { font-size: clamp(30px, 3.4vw, 44px); }
.text-button { display: inline-flex; align-items: center; gap: 8px; padding: 8px 0; border: 0; color: #356bc6; background: none; font: inherit; font-weight: 600; cursor: pointer; white-space: nowrap; }
.text-button .el-icon { transition: transform .2s ease; }
.text-button:hover .el-icon { transform: translateX(4px); }
.queue-band { padding: 88px 0; background: #eef3ef; }
.queue-grid { display: grid; grid-template-columns: repeat(4, 1fr); border-top: 1px solid #bfcac2; border-left: 1px solid #bfcac2; }
.queue-grid article { min-height: 190px; padding: 24px; border-right: 1px solid #bfcac2; border-bottom: 1px solid #bfcac2; background: rgba(255,255,255,.42); }
.queue-doctor { display: flex; justify-content: space-between; gap: 10px; }
.queue-doctor strong { font-size: 15px; }
.queue-doctor span { color: #758078; font-size: 12px; }
.queue-number { margin: 24px 0 16px; color: #235e44; font-size: 48px; font-weight: 650; line-height: 1; }
.queue-grid p { color: #6c786f; font-size: 12px; }
.queue-grid p i { display: inline-block; width: 6px; height: 6px; margin-right: 8px; border-radius: 50%; background: #9aa39c; }
.queue-grid article.live p i { background: #2eae65; box-shadow: 0 0 0 4px rgba(46,174,101,.14); }
.trust-band { padding: clamp(100px, 12vw, 160px) 0; background: #1d2923; color: #fff; }
.trust-inner { display: grid; grid-template-columns: 1fr 1.4fr; gap: clamp(60px, 9vw, 130px); align-items: end; }
.trust-band .eyebrow { color: #99c5a8; }
.trust-band .section-heading h2 { color: #f5f8f5; }
.numbers-list { display: grid; grid-template-columns: repeat(3, 1fr); gap: 28px; }
.number-item { padding: 0 0 20px; border-bottom: 1px solid #526158; }
.number-item strong { display: block; margin-bottom: 10px; font-size: clamp(44px, 5vw, 70px); font-weight: 560; line-height: 1; letter-spacing: 0; }
.number-item > span { color: #aebbb2; font-size: 12px; }
.departments { padding-top: clamp(100px, 12vw, 160px); padding-bottom: clamp(110px, 13vw, 170px); }
.department-list { display: grid; grid-template-columns: 1fr 1fr; border-top: 1px solid #cfd8d1; }
.department-list button { min-height: 84px; display: grid; grid-template-columns: 34px 90px 1fr 22px; align-items: center; gap: 16px; padding: 12px 8px; border: 0; border-bottom: 1px solid #cfd8d1; color: #17221d; background: transparent; text-align: left; font: inherit; cursor: pointer; transition: background .2s ease, padding .2s ease; }
.department-list button:nth-child(odd) { border-right: 1px solid #cfd8d1; }
.department-list button:hover { padding-left: 16px; background: #f0f4f0; }
.department-list button > span, .department-list small { color: #7b877f; font-size: 12px; }
.department-list strong { font-size: 17px; font-weight: 600; }
.department-list .el-icon { justify-self: end; color: #337558; }
.experience-footer { padding: 54px 0; border-top: 1px solid #31423a; background: #15211b; color: #f3f6f3; }
.experience-footer > div { display: grid; grid-template-columns: 1.5fr 1fr auto; gap: 30px; align-items: end; }
.experience-footer strong { display: block; font-size: 20px; }
.experience-footer span, .experience-footer p, .experience-footer small { color: #99a9a0; font-size: 12px; }
.reveal-block { opacity: 0; transform: translateY(24px); transition: opacity .65s ease, transform .65s ease; }
.reveal-block.revealed { opacity: 1; transform: translateY(0); }
@media (max-width: 760px) {
  .home-experience { margin-top: -24px; }
  .care-flow, .trust-inner { grid-template-columns: 1fr; gap: 48px; }
  .care-flow { padding-top: 48px; padding-left: 20px; padding-right: 20px; }
  .flow-list button { grid-template-columns: 26px 34px 1fr 20px; gap: 12px; min-height: 88px; }
  .flow-list small { display: none; }
  .queue-band { padding: 68px 0; }
  .queue-grid { grid-template-columns: 1fr 1fr; }
  .numbers-list { gap: 12px; }
  .number-item strong { font-size: 42px; }
  .department-list { grid-template-columns: 1fr; }
  .department-list button { grid-template-columns: 28px 76px 1fr 20px; }
  .department-list button:nth-child(odd) { border-right: 0; }
  .departments { padding-left: 20px; padding-right: 20px; }
  .section-bar { align-items: flex-start; }
  .experience-footer { padding: 42px 20px; }
  .experience-footer > div { grid-template-columns: 1fr; gap: 14px; }
}
@media (prefers-reduced-motion: reduce) {
  .reveal-block { opacity: 1; transform: none; transition: none; }
}
</style>
