<template>
  <section class="medical-hero">
    <div class="hero-grid" aria-hidden="true"></div>
    <div class="hero-mark" aria-hidden="true"><span></span><span></span></div>

    <div class="hero-content page-w" :class="{ visible: ready }">
      <div class="hero-copy">
        <p class="hero-kicker"><span></span> 线上预约服务</p>
        <h1>预约挂号，<br>从这里开始。</h1>
        <p class="hero-sub">按科室、医生或症状快速找到合适的就诊安排，让每一步都清楚、安心。</p>
      </div>

      <div class="hero-actions" aria-label="快捷服务">
        <button class="action-primary" @click="$router.push('/doctors')">
          <span class="action-icon"><el-icon><Calendar /></el-icon></span>
          <span><small>在线服务</small><strong>预约挂号</strong></span>
          <el-icon class="action-arrow"><ArrowRight /></el-icon>
        </button>
        <button @click="$router.push('/ai')">
          <span class="action-icon"><el-icon><MagicStick /></el-icon></span>
          <span><small>不清楚挂什么科</small><strong>AI 智能导诊</strong></span>
          <el-icon class="action-arrow"><ArrowRight /></el-icon>
        </button>
        <button @click="$router.push('/my-records')">
          <span class="action-icon"><el-icon><Document /></el-icon></span>
          <span><small>诊后随时查看</small><strong>我的病历</strong></span>
          <el-icon class="action-arrow"><ArrowRight /></el-icon>
        </button>
      </div>

      <div class="hero-status">
        <span><i></i> 今日服务正常</span>
        <span>覆盖 20+ 临床科室</span>
        <span>预约咨询热线 010-12345678</span>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'

const ready = ref(false)
onMounted(() => requestAnimationFrame(() => { ready.value = true }))
</script>

<style scoped>
.medical-hero {
  position: relative;
  min-height: 560px;
  height: min(680px, calc(100svh - var(--nav-h) - 96px));
  overflow: hidden;
  color: #f5f7f2;
  background: #173f31;
}
.hero-grid {
  position: absolute;
  inset: 0;
  opacity: .12;
  background-size: 72px 72px;
  background-image: linear-gradient(#dce9df 1px, transparent 1px), linear-gradient(90deg, #dce9df 1px, transparent 1px);
  mask-image: linear-gradient(90deg, #000, transparent 78%);
}
.hero-mark { position: absolute; right: 3vw; top: 50%; width: min(38vw, 520px); aspect-ratio: 1; transform: translateY(-50%); opacity: .09; }
.hero-mark span { position: absolute; left: 0; top: 40%; width: 100%; height: 20%; background: #fff; }
.hero-mark span:last-child { left: 40%; top: 0; width: 20%; height: 100%; }
.hero-content { position: relative; z-index: 1; height: 100%; display: flex; flex-direction: column; justify-content: center; padding-top: 54px; padding-bottom: 32px; }
.hero-copy { max-width: 760px; opacity: 0; transform: translateY(20px); transition: opacity .7s ease, transform .7s ease; }
.visible .hero-copy { opacity: 1; transform: translateY(0); }
.hero-kicker { display: flex; align-items: center; gap: 12px; margin-bottom: 22px; color: #b9d5c3; font-size: 12px; font-weight: 600; }
.hero-kicker span { width: 30px; height: 2px; background: #8fc59f; }
.hero-copy h1 { color: #fff; font-size: clamp(52px, 6.3vw, 88px); font-weight: 650; line-height: 1.08; letter-spacing: 0; }
.hero-sub { max-width: 560px; margin-top: 24px; color: #c8d9ce; font-size: 17px; line-height: 1.8; }
.hero-actions { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); max-width: 880px; margin-top: 48px; border: 1px solid rgba(230, 240, 232, .28); opacity: 0; transform: translateY(18px); transition: opacity .7s ease .14s, transform .7s ease .14s; }
.visible .hero-actions { opacity: 1; transform: translateY(0); }
.hero-actions button { min-width: 0; height: 92px; display: grid; grid-template-columns: 38px 1fr 20px; align-items: center; gap: 13px; padding: 0 20px; border: 0; border-right: 1px solid rgba(230, 240, 232, .28); background: rgba(10, 32, 24, .38); color: #fff; text-align: left; font: inherit; cursor: pointer; transition: background .2s ease; }
.hero-actions button:last-child { border-right: 0; }
.hero-actions button:hover { background: rgba(255, 255, 255, .1); }
.hero-actions .action-primary { background: #f0f5ed; color: #173f31; }
.hero-actions .action-primary:hover { background: #fff; }
.hero-actions button:nth-child(2) .action-icon { color: #a9c7ff; }
.action-icon { width: 38px; height: 38px; display: grid; place-items: center; border: 1px solid currentColor; font-size: 18px; }
.hero-actions small, .hero-actions strong { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.hero-actions small { margin-bottom: 2px; opacity: .65; font-size: 11px; }
.hero-actions strong { font-size: 15px; font-weight: 600; }
.action-arrow { justify-self: end; transition: transform .2s ease; }
.hero-actions button:hover .action-arrow { transform: translateX(4px); }
.hero-status { margin-top: auto; padding-top: 26px; display: flex; gap: 30px; color: #b7cbbd; font-size: 11px; opacity: 0; transition: opacity .7s ease .28s; }
.visible .hero-status { opacity: 1; }
.hero-status span { display: flex; align-items: center; gap: 8px; }
.hero-status i { width: 7px; height: 7px; border-radius: 50%; background: #78d49a; box-shadow: 0 0 0 4px rgba(120, 212, 154, .15); }
@media (max-width: 760px) {
  .medical-hero { min-height: 600px; height: calc(100svh - var(--nav-h) - 92px); }
  .hero-grid { background-size: 48px 48px; }
  .hero-mark { width: 76vw; right: -24vw; top: 26%; }
  .hero-content { justify-content: flex-start; padding-top: 72px; padding-bottom: 20px; }
  .hero-copy h1 { font-size: 48px; line-height: 1.12; }
  .hero-sub { margin-top: 18px; font-size: 15px; }
  .hero-actions { grid-template-columns: 1fr; width: 100%; margin-top: 34px; }
  .hero-actions button { height: 70px; border-right: 0; border-bottom: 1px solid rgba(230, 240, 232, .28); }
  .hero-actions button:last-child { border-bottom: 0; }
  .hero-status { gap: 10px; justify-content: space-between; }
  .hero-status span:nth-child(n+2) { display: none; }
}
@media (prefers-reduced-motion: reduce) {
  .hero-copy, .hero-actions, .hero-status { opacity: 1; transform: none; transition: none; }
}
</style>
