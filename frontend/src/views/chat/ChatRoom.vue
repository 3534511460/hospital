<template>
  <div class="page-w page-main">
    <div class="chat-layout">
      <div class="sidebar">
        <h3>咨询记录</h3>
        <div class="session-list">
          <div class="session-item" v-for="s in sessions" :key="s.id" :class="{active:currentSession?.id===s.id}" @click="openExisting(s)">
            <h4>{{ userRole==='DOCTOR'?s.patientName:s.doctorName }}</h4>
            <p>{{ s.lastMessageContent||'暂无消息' }}</p>
            <span class="badge" v-if="unread(s)>0">{{ unread(s) }}</span>
          </div>
          <div class="empty-hint" v-if="sessions.length===0">暂无咨询记录</div>
        </div>
        <button class="btn btn-outline btn-sm" style="width:100%;margin-top:12px" @click="showDoctorList=true;currentSession=null">+ 发起新咨询</button>
      </div>

      <div class="main">
        <template v-if="showDoctorList">
          <div class="page-head" style="text-align:center;padding:20px 20px 0">
            <p style="font-size:12px;color:var(--caption);margin-bottom:4px">Online Consultation</p>
            <h1>线上问诊</h1>
            <p style="font-size:14px;color:var(--caption);margin-top:4px">选择医生发起咨询。就诊后7天内可复诊。</p>
          </div>
          <div class="tabs"><button :class="{active:tab==='recommend'}" @click="tab='recommend'">推荐医生</button><button :class="{active:tab==='visited'}" @click="tab='visited'">可复诊医生</button></div>
          <div class="doc-grid" v-loading="loading">
            <template v-if="tab==='recommend'">
              <div class="doc-card" v-for="d in doctors" :key="d.userId" @click="startChat(d)">
                <div class="doc-top"><div class="doc-av">{{ (d.realName||'?')[0] }}</div><div><h4>{{ d.realName }} <em>{{ d.title }}</em></h4><p>{{ d.departmentName }}</p></div></div>
                <p class="intro">{{ (d.specialty||'').slice(0,60) }}</p>
                <div class="doc-foot"><span class="star">&#9733; {{ d.avgRating||'5.0' }}</span><span class="fee">&#165;{{ d.consultationFee||0 }}</span><button class="btn btn-primary btn-sm">开始咨询</button></div>
              </div>
            </template>
            <template v-else>
              <div class="doc-card" v-for="d in visitedDoctors" :key="d.userId" @click="startChat(d)">
                <div class="doc-top"><div class="doc-av">{{ (d.realName||'?')[0] }}</div><div><h4>{{ d.realName }}</h4><p>{{ d.departmentName }}</p></div></div>
                <p class="intro">上次就诊：{{ d.lastVisitDate }}</p>
                <div class="doc-foot"><span :class="d.daysLeft>0?'tag tag-green':'tag tag-red'">{{ d.daysLeft>0?'剩余'+d.daysLeft+'天':'已过期' }}</span><button class="btn btn-primary btn-sm" :disabled="d.daysLeft<=0">开始咨询</button></div>
              </div>
              <div class="empty-hint" v-if="visitedDoctors.length===0" style="grid-column:1/-1;padding:40px">暂无可复诊医生</div>
            </template>
          </div>
        </template>

        <template v-else-if="currentSession">
          <div class="chat-top"><button class="btn btn-ghost btn-sm" @click="showDoctorList=true;currentSession=null">&#8592; 返回</button><h3>{{ userRole==='DOCTOR'?currentSession.patientName:currentSession.doctorName }}</h3></div>
          <div class="chat-msgs" ref="msgBox"><div class="msg" v-for="m in messages" :key="m.id" :class="{mine:m.senderId===myId}"><div class="bubble">{{ m.content }}</div></div></div>
          <div class="chat-send"><input v-model="input" placeholder="输入消息..." @keyup.enter="sendMsg"/><button class="btn btn-primary" :disabled="!input.trim()" @click="sendMsg">发送</button></div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref,computed,onMounted,onUnmounted,nextTick } from 'vue'
import request from '../../utils/request'

const userRole=computed(()=>{const u=localStorage.getItem('user');return u?JSON.parse(u).role:''})
const myId=computed(()=>{const u=localStorage.getItem('user');return u?JSON.parse(u).userId:0})
const tab=ref('recommend');const loading=ref(false)
const doctors=ref([]);const visitedDoctors=ref([]);const sessions=ref([])
const currentSession=ref(null);const showDoctorList=ref(true)
const messages=ref([]);const input=ref('');const msgBox=ref(null)
let ws=null

function unread(s){return userRole.value==='DOCTOR'?(s.unreadCountDoctor||0):(s.unreadCountPatient||0)}

async function fetchD(){loading.value=true;try{const r=await request.get('/hospital/doctors',{params:{page:1,size:20}});doctors.value=(r.data?.records||r.data||[])}catch{}finally{loading.value=false}}
async function fetchV(){try{const r=await request.get('/appointment/my',{params:{status:2}});const seen=new Set();visitedDoctors.value=[];for(const a of(r.data||[])){if(seen.has(a.doctorId))continue;seen.add(a.doctorId);visitedDoctors.value.push({userId:a.doctorId,realName:a.doctorName,departmentName:a.departmentName,lastVisitDate:a.appointmentDate,daysLeft:7-Math.floor((Date.now()-new Date(a.appointmentDate))/86400000)})}}catch{}}
async function fetchS(){try{const r=await request.get('/chat/sessions');sessions.value=r.data||[]}catch{}}

async function startChat(doc){
  try{const exist=sessions.value.find(s=>s.doctorId===doc.userId);if(exist){openExisting(exist);return}
  const body={doctorId:doc.userId};const ar=await request.get('/appointment/my',{params:{status:2}});const appt=(ar.data||[]).find(a=>a.doctorId===doc.userId);if(appt)body.appointmentId=appt.id
  const res=await request.post('/chat/sessions',body);sessions.value.unshift(res.data);openExisting(res.data)}catch{}
}
async function openExisting(s){currentSession.value=s;showDoctorList.value=false;try{const r=await request.get(`/chat/sessions/${s.id}/messages`);messages.value=r.data||[]}catch{};await nextTick();if(msgBox.value)msgBox.value.scrollTop=msgBox.value.scrollHeight;fetchS()}
async function sendMsg(){
  if(!input.value.trim()||!currentSession.value) return
  const c=input.value.trim();input.value=''
  // Optimistic: show message immediately
  messages.value.push({id:Date.now(),senderId:myId.value,content:c})
  await nextTick();if(msgBox.value)msgBox.value.scrollTop=msgBox.value.scrollHeight
  try{
    await request.post(`/chat/sessions/${currentSession.value.id}/messages`,{content:c,msgType:'TEXT'})
    await openExisting(currentSession.value)
  }catch(e){console.error('send failed',e);messages.value.pop()}
}
function connectWs(){const t=localStorage.getItem('token');if(!t)return;ws=new WebSocket(`ws://localhost:8080/ws/chat?token=${t}`);ws.onmessage=e=>{try{const d=JSON.parse(e.data);if(d.type==='MESSAGE'&&d.sessionId===currentSession.value?.id){messages.value.push({id:d.id,senderId:d.senderId,content:d.content});nextTick(()=>{if(msgBox.value)msgBox.value.scrollTop=msgBox.value.scrollHeight})}if(d.type==='MESSAGE')fetchS()}catch{}};ws.onclose=()=>setTimeout(connectWs,3000)}
onMounted(()=>{fetchD();fetchV();fetchS();connectWs()})
onUnmounted(()=>{if(ws)ws.close()})
</script>

<style scoped>
.chat-layout { display:grid;grid-template-columns:220px 1fr;gap:1px;background:var(--border);min-height:560px;border-radius:var(--radius);overflow:hidden;box-shadow:var(--shadow); }
.sidebar { background:var(--surface);padding:16px;display:flex;flex-direction:column; }
.sidebar h3 { font-size:14px;color:var(--title);margin-bottom:12px; }
.session-list { flex:1;overflow-y:auto; }
.session-item { padding:10px;cursor:pointer;border-bottom:1px solid var(--border-light);position:relative;border-radius:var(--radius-sm); }
.session-item:hover,.session-item.active { background:var(--bg); }
.session-item h4 { font-size:13px;color:var(--title); }
.session-item p { font-size:11px;color:var(--caption);margin-top:2px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap; }
.badge { position:absolute;right:10px;top:10px;background:var(--error);color:#fff;font-size:10px;min-width:16px;height:16px;border-radius:8px;display:flex;align-items:center;justify-content:center; }
.empty-hint { padding:16px 0;text-align:center;font-size:13px;color:var(--caption); }

.main { background:var(--surface);display:flex;flex-direction:column;overflow:hidden; }
.tabs { display:flex;justify-content:center;gap:8px;margin:8px 0 16px; }
.tabs button { padding:7px 22px;border:1px solid var(--border);border-radius:20px;background:none;font-size:13px;cursor:pointer;font-family:inherit;color:var(--body);transition:all .15s; }
.tabs button.active { background:var(--primary);color:#fff;border-color:var(--primary); }
.doc-grid { display:grid;grid-template-columns:repeat(2,1fr);gap:12px;padding:0 20px 20px;overflow-y:auto; }
.doc-card { padding:16px;border:1px solid var(--border);border-radius:var(--radius);cursor:pointer;transition:all .15s; }
.doc-card:hover { border-color:var(--primary);box-shadow:var(--shadow); }
.doc-top { display:flex;align-items:center;gap:10px;margin-bottom:10px; }
.doc-av { width:40px;height:40px;border-radius:50%;background:var(--primary-light);color:var(--primary);display:flex;align-items:center;justify-content:center;font-size:16px;font-weight:600;flex-shrink:0; }
.doc-top h4 { font-size:14px;color:var(--title); } .doc-top h4 em { font-style:normal;font-size:11px;color:var(--primary);background:var(--primary-light);padding:1px 5px;margin-left:4px;border-radius:3px; }
.doc-top p { font-size:12px;color:var(--caption); }
.intro { font-size:12px;color:var(--caption);margin-bottom:10px;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden; }
.doc-foot { display:flex;align-items:center;justify-content:space-between; }
.star { font-size:12px;color:#F2C94C; }
.fee { font-size:14px;font-weight:600;color:var(--title); }

.chat-top { display:flex;align-items:center;gap:12px;padding:12px 20px;border-bottom:1px solid var(--border-light); }
.chat-top h3 { font-size:15px;color:var(--title); }
.chat-msgs { flex:1;overflow-y:auto;padding:20px; }
.msg { margin-bottom:14px;display:flex; }
.msg.mine { justify-content:flex-end; }
.bubble { max-width:70%;padding:10px 14px;font-size:14px;line-height:1.6;border-radius:var(--radius-sm);background:var(--bg);color:var(--title); }
.msg.mine .bubble { background:var(--primary-light); }
.chat-send { display:flex;border-top:1px solid var(--border-light);padding:12px 20px;gap:8px; }
.chat-send input { flex:1;height:38px;padding:0 12px;border:1px solid var(--border);border-radius:20px;background:var(--bg);font-size:14px;color:var(--title);font-family:inherit;outline:none;transition:border-color .15s; }
.chat-send input:focus { border-color:var(--primary); }
@media(max-width:768px){ .chat-layout{grid-template-columns:1fr}.doc-grid{grid-template-columns:1fr} }
</style>
