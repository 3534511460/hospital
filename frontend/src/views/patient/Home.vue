<template>
  <div class="home">
    <section class="hero page-w">
      <div class="hero-text ani-blur-stagger">
        <h1 class="ani-blur-item">专业医疗<span class="hl gradient-text">用心服务</span></h1>
        <p class="ani-blur-item">在线预约 · 智能导诊 · 线上咨询 — 让就医更简单</p>
        <div class="hero-btns ani-blur-item">
          <a class="btn btn-primary btn-lg" @click="$router.push('/doctors')">预约挂号</a>
          <a class="btn btn-outline btn-lg" @click="$router.push('/ai')">智能导诊</a>
        </div>
      </div>
      <div class="hero-stats">
        <div class="stat"><span class="n"><span ref="count1">0</span>+</span><span>临床科室</span></div>
        <div class="stat"><span class="n"><span ref="count2">0</span>+</span><span>执业医师</span></div>
        <div class="stat"><span class="n"><span ref="count3">0</span>k+</span><span>服务患者</span></div>
      </div>
      <div class="line-reveal" style="margin-top:40px"></div>
    </section>

    <section class="features page-w">
      <div class="feat-grid ani-blur-stagger">
        <div class="feat-card ani-blur-item" v-for="f in feats" :key="f.t">
          <div class="feat-icon ani-float">{{ f.i }}</div>
          <h4>{{ f.t }}</h4><p>{{ f.d }}</p>
        </div>
      </div>
    </section>

    <section class="queue-board page-w" v-if="queueData.length">
      <div class="qb-head"><h2>实时叫号</h2><a class="qb-link" @click="$router.push('/queue/board')">大屏 →</a></div>
      <div class="qb-grid ani-blur-stagger">
        <div class="qb-card ani-blur-item" v-for="(q,i) in queueData.slice(0,4)" :key="q.doctorId" :class="{live:q.queue&&q.queue.find(x=>x.status===1)}">
          <div class="qb-doc">{{ q.doctorName }} <span class="qb-dept">{{ q.departmentName }}</span></div>
          <div class="qb-now">
            <span class="qb-num" :class="{pulse:q.queue&&q.queue.find(x=>x.status===1)}">{{ q.queue&&q.queue.find(x=>x.status===1) ? q.queue.find(x=>x.status===1).queueNumber : '—' }}</span>
            <span class="qb-name">{{ q.queue&&q.queue.find(x=>x.status===1) ? q.queue.find(x=>x.status===1).patientName+' 就诊中' : '待叫号' }}</span>
          </div>
        </div>
      </div>
    </section>

    <section class="depts page-w">
      <h2>临床科室</h2>
      <div class="dept-grid ani-stagger">
        <div class="dept-card ani-item" v-for="d in depts" :key="d.n" @click="$router.push('/doctors?departmentId='+d.id)">
          <span class="dept-ico">{{ d.i }}</span><span class="dept-name">{{ d.n }}</span>
          <span class="dept-arrow">→</span>
        </div>
      </div>
    </section>

    <footer class="footer">
      <div class="page-w foot-grid">
        <div><h4 class="gradient-text">医预约</h4><p>专业医疗预约服务平台</p></div>
        <div><h5>服务</h5><a>在线预约</a><a>智能导诊</a><a>线上咨询</a></div>
        <div><h5>联系</h5><a>电话 010-12345678</a><a>地址 XX市XX路100号</a></div>
        <div><p>&copy; 2026 MedReserve</p></div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref,onMounted,nextTick } from 'vue'
import request from '../../utils/request'

const count1=ref(null);const count2=ref(null);const count3=ref(null)

function animateCount(el, target, suffix){
  if(!el) return
  let cur=0;const step=Math.ceil(target/40)
  const t=setInterval(()=>{cur+=step;if(cur>=target){cur=target;clearInterval(t)}el.textContent=cur+suffix},30)
}

const queueData=ref([])
async function fetchQueue(){
  try{
    const r=await request.get('/hospital/doctors',{params:{page:1,size:100}})
    const docs=(r.data&&r.data.records)||r.data||[]
    for(const d of docs.filter(d=>d.status===1).slice(0,8)){
      try{
        const qr=await request.get(`/queue/doctor/${d.userId}/today`)
        const active=qr.data?.filter(q=>q.status===0||q.status===1)||[]
        if(active.length) queueData.value.push({doctorId:d.userId,doctorName:d.realName,departmentName:d.departmentName,queue:qr.data})
      }catch{}
    }
  }catch{}
}

const feats=[
  {i:'📋',t:'全流程线上化',d:'从预约到就诊记录一站式完成'},
  {i:'🎯',t:'精准匹配专家',d:'按科室、疾病精准筛选最适合的医生'},
  {i:'🤖',t:'AI智能导诊',d:'描述症状，AI推荐科室和医生'},
  {i:'💬',t:'诊后无忧',d:'就诊记录永久保存，复诊一键发起'}
]
const depts=[
  {id:1,n:'内科',i:'🫀'},{id:2,n:'外科',i:'🔬'},{id:3,n:'妇产科',i:'🌸'},
  {id:4,n:'儿科',i:'👶'},{id:5,n:'皮肤科',i:'🔬'},{id:6,n:'眼科',i:'👁'},{id:7,n:'中医科',i:'🌿'}
]
onMounted(async ()=>{
  await nextTick()
  // Stats counter fires after hero animation settles
  setTimeout(()=>{
    animateCount(count1.value,20,'+')
    animateCount(count2.value,200,'+')
    animateCount(count3.value,50,'k+')
  },800)
  fetchQueue()
})
</script>

<style scoped>
.hero { padding: 72px 0 48px; text-align: center; }
.hero-text h1 { font-size: 44px; font-weight: 700; color: var(--title); margin-bottom: 12px; }
.hero-text h1 .hl { color: var(--primary); }
.hero-text p { font-size: 17px; color: var(--caption); margin-bottom: 32px; }
.hero-btns { display: flex; gap: 12px; justify-content: center; margin-bottom: 40px; }
.hero-btns .btn { transition: all .25s ease; }
.hero-btns .btn:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(22,93,255,.3); }
.hero-stats { display: flex; justify-content: center; gap: 48px; }
.stat .n { display: block; font-size: 32px; font-weight: 700; color: var(--title); font-family: var(--serif); }
.stat span:last-child { font-size: 13px; color: var(--caption); }

.features { padding: 56px 0; }
.feat-grid { display: grid; grid-template-columns: repeat(4,1fr); gap: 16px; }
.feat-card { padding: 32px 24px; background: var(--surface); border-radius: var(--radius); box-shadow: var(--shadow); text-align: center; transition: transform .3s ease, box-shadow .3s ease; }
.feat-card:hover { box-shadow: var(--shadow-md); transform: translateY(-4px); }
.feat-icon { font-size: 28px; margin-bottom: 12px; display: inline-block; }
.feat-card h4 { font-size: 16px; color: var(--title); margin-bottom: 6px; }
.feat-card p { font-size: 13px; color: var(--caption); }

.queue-board { padding: 40px 0; }
.qb-head { display:flex;justify-content:space-between;align-items:center;margin-bottom:16px; }
.qb-head h2 { font-size:22px;font-weight:600;color:var(--title); }
.qb-link { font-size:13px;color:var(--primary);cursor:pointer;transition: transform .15s;display:inline-block; }
.qb-link:hover { transform: translateX(4px); }
.qb-grid { display:grid;grid-template-columns:repeat(4,1fr);gap:12px; }
.qb-card { padding:20px;background:var(--surface);border-radius:var(--radius);box-shadow:var(--shadow);transition: all .3s ease;position:relative;overflow:hidden; }
.qb-card.live { box-shadow: 0 0 0 1px rgba(22,93,255,.2), var(--shadow); }
.qb-card::before { content:'';position:absolute;top:0;left:0;right:0;height:3px;background:var(--border-light);transition: background .3s; }
.qb-card.live::before { background:var(--primary); }
.qb-doc { font-size:14px;font-weight:600;color:var(--title);margin-bottom:10px; }
.qb-dept { font-size:11px;color:var(--caption);font-weight:400;margin-left:4px; }
.qb-now { display:flex;align-items:center;gap:12px; }
.qb-num { font-size:36px;font-weight:700;color:var(--primary);font-family:var(--serif);line-height:1;transition: transform .3s ease;display:inline-block; }
.qb-num.pulse { animation: pulseRing 2s infinite; }
.qb-name { font-size:14px;color:var(--body); }

.depts { padding: 40px 0 60px; }
.depts h2 { font-size: 22px; font-weight: 600; color: var(--title); margin-bottom: 20px; }
.dept-grid { display: grid; grid-template-columns: repeat(4,1fr); gap: 12px; }
.dept-card { display: flex; align-items: center; gap: 12px; padding: 20px; background: var(--surface); border-radius: var(--radius); box-shadow: var(--shadow); cursor: pointer; transition: all .25s ease; }
.dept-card:hover { box-shadow: var(--shadow-md); transform: translateY(-2px); }
.dept-card:hover .dept-name { color: var(--primary); }
.dept-ico { font-size: 24px; transition: transform .3s ease; }
.dept-card:hover .dept-ico { transform: scale(1.15); }
.dept-name { font-size: 15px; font-weight: 500; color: var(--title); transition: color .15s; }
.dept-arrow { margin-left: auto; color: var(--border); transition: all .25s; }
.dept-card:hover .dept-arrow { color: var(--primary); transform: translateX(4px); }

.footer { background: var(--surface); border-top: 1px solid var(--border-light); padding: 48px 0 24px; margin-top: 40px; }
.foot-grid { display: grid; grid-template-columns:2fr 1fr 1fr 1fr; gap: 40px; }
.foot-grid h4 { font-size: 18px; color: var(--title); margin-bottom: 6px; }
.foot-grid h5 { font-size: 13px; color: var(--title); margin-bottom: 10px; text-transform: uppercase; letter-spacing: .08em; }
.foot-grid p,.foot-grid a { font-size: 13px; color: var(--caption); display: block; margin-bottom: 4px; }

@media(max-width:768px){ .hero-text h1{font-size:28px}.feat-grid{grid-template-columns:repeat(2,1fr)}.dept-grid{grid-template-columns:repeat(2,1fr)}.foot-grid{grid-template-columns:1fr 1fr} .hero-stats{gap:24px}.qb-grid{grid-template-columns:repeat(2,1fr)} }
</style>
