<template>
  <div class="med-hero" ref="containerRef" @mousemove="onMouseMove" @wheel="onWheel">
    <canvas ref="canvasRef" class="med-canvas"></canvas>

    <div class="med-overlay" :class="{ visible: showText }">
      <h1 class="med-title">Time to redefine<br><span class="med-accent">healthcare experience</span></h1>
      <p class="med-sub">重新定义医疗体验</p>
      <div class="med-btns">
        <button class="med-btn primary" @click="$router.push('/doctors')">预约挂号</button>
        <button class="med-btn outline" @click="$router.push('/ai')">智能导诊</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as THREE from 'three'

const canvasRef = ref(null)
const containerRef = ref(null)
const showText = ref(false)

let scene, camera, renderer
let dnaGroup, ambientCloud
let animationId
let mouseNDC = new THREE.Vector2(999, 999)
let scrollOff = 0
let targetScrollOff = 0

const RADIUS = 1.3
const HEIGHT = 7
const TURNS = 3.5
const BALL_RADIUS = 0.04
const BALLS_PER_STRAND = 500
const RUNG_EVERY = 8

function init() {
  const w = containerRef.value.clientWidth
  const h = containerRef.value.clientHeight

  renderer = new THREE.WebGLRenderer({ canvas: canvasRef.value, alpha: true, antialias: true })
  renderer.setSize(w, h)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  renderer.toneMapping = THREE.ACESFilmicToneMapping
  renderer.toneMappingExposure = 1.2

  scene = new THREE.Scene()
  scene.background = new THREE.Color('#0a0e27')
  scene.fog = new THREE.Fog('#0a0e27', 10, 35)

  camera = new THREE.PerspectiveCamera(45, w / h, 0.05, 60)
  camera.position.set(0, 1.2, 8.5)
  camera.lookAt(0, 0, 0)

  const amb = new THREE.AmbientLight(0x112244, 0.5)
  scene.add(amb)
  const dir = new THREE.DirectionalLight(0xffffff, 0.6)
  dir.position.set(5, 8, 5)
  scene.add(dir)

  buildDNA()
  buildAmbient()

  animationId = requestAnimationFrame(animate)
  setTimeout(() => { showText.value = true }, 1500)
}

function helixPos(strand, t) {
  const angle = t * Math.PI * 2 * TURNS + strand * Math.PI
  const y = (t - 0.5) * HEIGHT
  return new THREE.Vector3(Math.cos(angle) * RADIUS, y, Math.sin(angle) * RADIUS)
}

function buildDNA() {
  dnaGroup = new THREE.Group()

  // Glow texture for sprites
  const glowTex = (() => {
    const s = 64, c = document.createElement('canvas')
    c.width = s; c.height = s
    const ctx = c.getContext('2d')
    const g = ctx.createRadialGradient(s / 2, s / 2, 0, s / 2, s / 2, s / 2)
    g.addColorStop(0, 'rgba(255,255,255,1)')
    g.addColorStop(0.08, 'rgba(180,220,255,0.95)')
    g.addColorStop(0.3, 'rgba(80,160,255,0.4)')
    g.addColorStop(0.6, 'rgba(20,60,200,0.05)')
    g.addColorStop(1, 'rgba(0,0,0,0)')
    ctx.fillStyle = g; ctx.fillRect(0, 0, s, s)
    return new THREE.CanvasTexture(c)
  })()

  const ballGeo = new THREE.SphereGeometry(BALL_RADIUS, 8, 6)
  const glowGeo = new THREE.SphereGeometry(BALL_RADIUS * 2.5, 8, 6)

  const matA = new THREE.MeshStandardMaterial({ color: 0x3388dd, roughness: 0.2, metalness: 0.3, emissive: 0x112244 })
  const matB = new THREE.MeshStandardMaterial({ color: 0x55aadd, roughness: 0.2, metalness: 0.3, emissive: 0x112244 })
  const glowMatA = new THREE.MeshBasicMaterial({ color: 0x4499ee, transparent: true, opacity: 0.18, depthWrite: false })
  const glowMatB = new THREE.MeshBasicMaterial({ color: 0x66bbff, transparent: true, opacity: 0.15, depthWrite: false })
  const rungMat = new THREE.MeshStandardMaterial({ color: 0x6699cc, roughness: 0.3, metalness: 0.2, emissive: 0x112233, emissiveIntensity: 0.5 })

  const strandAmm = new THREE.InstancedMesh(ballGeo, matA, BALLS_PER_STRAND + rungAnchors())
  const strandBmm = new THREE.InstancedMesh(ballGeo, matB, BALLS_PER_STRAND + rungAnchors())
  const glowA = new THREE.InstancedMesh(glowGeo, glowMatA, BALLS_PER_STRAND)
  const glowB = new THREE.InstancedMesh(glowGeo, glowMatB, BALLS_PER_STRAND)

  const dummy = new THREE.Object3D()
  const colorA = new THREE.Color('#3388dd')
  const colorB = new THREE.Color('#55aadd')

  // Strand backbone balls
  for (let i = 0; i < BALLS_PER_STRAND; i++) {
    const t = i / (BALLS_PER_STRAND - 1)
    const p = helixPos(0, t)
    dummy.position.copy(p)
    dummy.updateMatrix()
    strandAmm.setMatrixAt(i, dummy.matrix)
    strandAmm.setColorAt(i, colorA)
    glowA.setMatrixAt(i, dummy.matrix)

    const q = helixPos(1, t)
    dummy.position.copy(q)
    dummy.updateMatrix()
    strandBmm.setMatrixAt(i, dummy.matrix)
    strandBmm.setColorAt(i, colorB)
    glowB.setMatrixAt(i, dummy.matrix)
  }

  // Rung cylinders + anchor balls
  const rungCount = Math.floor(BALLS_PER_STRAND / RUNG_EVERY)
  let si = BALLS_PER_STRAND
  for (let i = 0; i < rungCount; i++) {
    const t = i / (rungCount - 1)
    const a = helixPos(0, t)
    const b = helixPos(1, t)
    const mid = new THREE.Vector3().addVectors(a, b).multiplyScalar(0.5)
    const dir = new THREE.Vector3().subVectors(b, a)
    const len = dir.length()

    // Rung as cylinder
    const cylGeo = new THREE.CylinderGeometry(0.018, 0.018, len, 6)
    const cyl = new THREE.Mesh(cylGeo, rungMat)
    cyl.position.copy(mid)
    cyl.lookAt(b)
    cyl.rotateX(Math.PI / 2)
    dnaGroup.add(cyl)

    // Anchor balls at each end of rung
    dummy.position.copy(a); dummy.updateMatrix()
    strandAmm.setMatrixAt(si, dummy.matrix)
    strandAmm.setColorAt(si, colorA)
    si++

    dummy.position.copy(b); dummy.updateMatrix()
    strandBmm.setMatrixAt(si - (BALLS_PER_STRAND - si + 1), dummy.matrix)
    strandBmm.setColorAt(si - (BALLS_PER_STRAND - si + 1), colorB)
  }

  strandAmm.instanceMatrix.needsUpdate = true
  strandAmm.instanceColor.needsUpdate = true
  strandBmm.instanceMatrix.needsUpdate = true
  strandBmm.instanceColor.needsUpdate = true
  glowA.instanceMatrix.needsUpdate = true
  glowB.instanceMatrix.needsUpdate = true

  dnaGroup.add(strandAmm, strandBmm, glowA, glowB)
  scene.add(dnaGroup)
}

function rungAnchors() {
  return Math.floor(BALLS_PER_STRAND / RUNG_EVERY)
}

function buildAmbient() {
  const count = 300
  const pos = new Float32Array(count * 3)
  for (let i = 0; i < count; i++) {
    const th = Math.random() * Math.PI * 2
    const ph = Math.acos(2 * Math.random() - 1)
    const r = 7 + Math.random() * 12
    pos[i * 3] = Math.sin(ph) * Math.cos(th) * r
    pos[i * 3 + 1] = Math.sin(ph) * Math.sin(th) * r
    pos[i * 3 + 2] = Math.cos(ph) * r
  }
  const geo = new THREE.BufferGeometry()
  geo.setAttribute('position', new THREE.BufferAttribute(pos, 3))
  const mat = new THREE.PointsMaterial({
    size: 0.045, color: 0x3366aa, blending: THREE.AdditiveBlending,
    depthWrite: false, transparent: true, opacity: 0.4
  })
  ambientCloud = new THREE.Points(geo, mat)
  scene.add(ambientCloud)
}

function onMouseMove(e) {
  const r = containerRef.value.getBoundingClientRect()
  mouseNDC.x = ((e.clientX - r.left) / r.width) * 2 - 1
  mouseNDC.y = -((e.clientY - r.top) / r.height) * 2 + 1
}

function onWheel(e) {
  e.preventDefault()
  targetScrollOff += e.deltaY * 0.0025
  targetScrollOff = THREE.MathUtils.clamp(targetScrollOff, -2.5, 2.5)
}

let clock = new THREE.Clock()
function animate() {
  animationId = requestAnimationFrame(animate)
  const dt = Math.min(clock.getDelta(), 0.1)
  const elapsed = performance.now() / 1000

  scrollOff += (targetScrollOff - scrollOff) * 0.06

  // Mouse attraction — rotate entire DNA group toward cursor
  const targetRotY = mouseNDC.x * 0.6
  const targetRotX = -mouseNDC.y * 0.25
  dnaGroup.rotation.y += (targetRotY - dnaGroup.rotation.y) * 0.03
  dnaGroup.rotation.x += (targetRotX - dnaGroup.rotation.x) * 0.03
  // Always add base auto-rotation
  dnaGroup.rotation.y += dt * 0.15

  // Scroll: unroll — stretch vertically, expand radius
  const sc = 1 + Math.abs(scrollOff) * 0.25
  dnaGroup.scale.set(1 + Math.abs(scrollOff) * 0.15, 1 - Math.abs(scrollOff) * 0.1, 1 + Math.abs(scrollOff) * 0.15)
  dnaGroup.position.y = scrollOff * 1.2

  // Camera orbit
  const camR = 8.5 + scrollOff * 1.8
  const camAngle = elapsed * 0.12
  camera.position.x = Math.sin(camAngle) * camR
  camera.position.z = Math.cos(camAngle) * camR
  camera.position.y = 1.2 + scrollOff * 0.6 + Math.sin(elapsed * 0.4) * 0.25
  camera.lookAt(0, scrollOff * 0.5, 0)

  // Ambient float
  ambientCloud.rotation.y += dt * 0.025
  ambientCloud.rotation.x += dt * 0.012

  renderer.render(scene, camera)
}

function onResize() {
  if (!containerRef.value || !renderer) return
  const w = containerRef.value.clientWidth
  const h = containerRef.value.clientHeight
  renderer.setSize(w, h)
  camera.aspect = w / Math.max(h, 1)
  camera.updateProjectionMatrix()
}

onMounted(async () => {
  await nextTick()
  init()
  window.addEventListener('resize', onResize)
})

onUnmounted(() => {
  cancelAnimationFrame(animationId)
  window.removeEventListener('resize', onResize)
  if (renderer) { renderer.dispose(); renderer.forceContextLoss() }
})
</script>

<style scoped>
.med-hero {
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  background: #0a0e27;
}

.med-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  display: block;
}

.med-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 10;
  opacity: 0;
  transform: translateY(12px);
  transition: opacity 1s ease 0.5s, transform 1s ease 0.5s;
  pointer-events: none;
}

.med-overlay.visible {
  opacity: 1;
  transform: translateY(0);
  pointer-events: auto;
}

.med-title {
  font-size: clamp(24px, 4vw, 44px);
  font-weight: 300;
  color: #fff;
  text-align: center;
  line-height: 1.35;
  letter-spacing: 0.03em;
  margin-bottom: 6px;
  text-shadow: 0 0 80px rgba(60, 140, 240, 0.3);
}

.med-accent {
  font-weight: 550;
  background: linear-gradient(120deg, #66b3ff, #aad4ff, #66b3ff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.med-sub {
  font-size: 15px;
  color: rgba(255,255,255,0.33);
  margin-bottom: 32px;
  letter-spacing: 0.18em;
}

.med-btns { display: flex; gap: 14px; }

.med-btn {
  padding: 11px 32px;
  border-radius: 28px;
  font-size: 14px;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.25s;
  border: none;
  letter-spacing: 0.03em;
}

.med-btn.primary { background: #1a6dff; color: #fff; }
.med-btn.primary:hover { background: #4080ff; box-shadow: 0 0 24px rgba(26,109,255,0.35); transform: translateY(-1px); }
.med-btn.outline { background: transparent; color: rgba(255,255,255,0.8); border: 1px solid rgba(255,255,255,0.2); }
.med-btn.outline:hover { border-color: rgba(255,255,255,0.45); background: rgba(255,255,255,0.05); }

@media (max-width: 768px) {
  .med-title { font-size: 20px; }
  .med-btns { flex-direction: column; gap: 10px; }
  .med-btn { padding: 10px 24px; font-size: 13px; }
}
</style>
