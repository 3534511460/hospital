<template>
  <div class="home">
    <section class="hero page-w">
      <div class="hero-text">
        <h1>专业医疗<span class="hl">用心服务</span></h1>
        <p>在线预约 · 智能导诊 · 线上咨询 — 让就医更简单</p>
        <div class="hero-btns"><a class="btn btn-primary btn-lg" @click="$router.push('/doctors')">预约挂号</a><a class="btn btn-outline btn-lg" @click="$router.push('/ai')">智能导诊</a></div>
      </div>
      <div class="hero-stats">
        <div class="stat"><span class="n">20+</span><span>临床科室</span></div>
        <div class="stat"><span class="n">200+</span><span>执业医师</span></div>
        <div class="stat"><span class="n">50k+</span><span>服务患者</span></div>
      </div>
    </section>

    <section class="features page-w">
      <div class="feat-grid">
        <div class="feat-card" v-for="f in feats" :key="f.t">
          <div class="feat-icon">{{ f.i }}</div>
          <h4>{{ f.t }}</h4><p>{{ f.d }}</p>
        </div>
      </div>
    </section>

    <section class="depts page-w">
      <h2>临床科室</h2>
      <div class="dept-grid">
        <div class="dept-card" v-for="d in depts" :key="d.n" @click="$router.push('/doctors?departmentId='+d.id)">
          <span class="dept-ico">{{ d.i }}</span><span class="dept-name">{{ d.n }}</span>
          <span class="dept-arrow">→</span>
        </div>
      </div>
    </section>

    <footer class="footer">
      <div class="page-w foot-grid">
        <div><h4>医预约</h4><p>专业医疗预约服务平台</p></div>
        <div><h5>服务</h5><a>在线预约</a><a>智能导诊</a><a>线上咨询</a></div>
        <div><h5>联系</h5><a>电话 010-12345678</a><a>地址 XX市XX路100号</a></div>
        <div><p>&copy; 2026 MedReserve</p></div>
      </div>
    </footer>
  </div>
</template>

<script setup>
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
</script>

<style scoped>
.hero { padding: 72px 0 48px; text-align: center; }
.hero-text h1 { font-size: 44px; font-weight: 700; color: var(--title); margin-bottom: 12px; }
.hero-text h1 .hl { color: var(--primary); }
.hero-text p { font-size: 17px; color: var(--caption); margin-bottom: 32px; }
.hero-btns { display: flex; gap: 12px; justify-content: center; margin-bottom: 48px; }
.hero-stats { display: flex; justify-content: center; gap: 48px; }
.stat .n { display: block; font-size: 32px; font-weight: 700; color: var(--title); font-family: var(--serif); }
.stat span:last-child { font-size: 13px; color: var(--caption); }

.features { padding: 48px 0; }
.feat-grid { display: grid; grid-template-columns: repeat(4,1fr); gap: 16px; }
.feat-card { padding: 32px 24px; background: var(--surface); border-radius: var(--radius); box-shadow: var(--shadow); text-align: center; transition: box-shadow .2s; }
.feat-card:hover { box-shadow: var(--shadow-md); }
.feat-icon { font-size: 28px; margin-bottom: 12px; }
.feat-card h4 { font-size: 16px; color: var(--title); margin-bottom: 6px; }
.feat-card p { font-size: 13px; color: var(--caption); }

.depts { padding: 40px 0 60px; }
.depts h2 { font-size: 22px; font-weight: 600; color: var(--title); margin-bottom: 20px; }
.dept-grid { display: grid; grid-template-columns: repeat(4,1fr); gap: 12px; }
.dept-card { display: flex; align-items: center; gap: 12px; padding: 20px; background: var(--surface); border-radius: var(--radius); box-shadow: var(--shadow); cursor: pointer; transition: all .15s; }
.dept-card:hover { box-shadow: var(--shadow-md); color: var(--primary); }
.dept-ico { font-size: 24px; }
.dept-name { font-size: 15px; font-weight: 500; color: var(--title); }
.dept-arrow { margin-left: auto; color: var(--border); transition: color .15s; }
.dept-card:hover .dept-arrow { color: var(--primary); }

.footer { background: var(--surface); border-top: 1px solid var(--border-light); padding: 48px 0 24px; margin-top: 40px; }
.foot-grid { display: grid; grid-template-columns:2fr 1fr 1fr 1fr; gap: 40px; }
.foot-grid h4 { font-size: 18px; color: var(--title); margin-bottom: 6px; font-family: var(--serif); }
.foot-grid h5 { font-size: 13px; color: var(--title); margin-bottom: 10px; text-transform: uppercase; letter-spacing: .08em; }
.foot-grid p,.foot-grid a { font-size: 13px; color: var(--caption); display: block; margin-bottom: 4px; }

@media(max-width:768px){ .hero-text h1{font-size:28px}.feat-grid{grid-template-columns:repeat(2,1fr)}.dept-grid{grid-template-columns:repeat(2,1fr)}.foot-grid{grid-template-columns:1fr 1fr} .hero-stats{gap:24px} }
</style>
