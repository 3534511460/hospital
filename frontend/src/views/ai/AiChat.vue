<template>
  <div class="page-w page-main">
    <div class="ai-layout">
      <div class="ai-sidebar">
        <div class="sidebar-head">
          <h3>对话历史</h3>
          <button class="btn btn-ghost btn-sm" @click="startNewChat">+ 新导诊</button>
        </div>
        <div class="history-list" v-if="history.length>0">
          <div class="history-item" v-for="h in historyGroups" :key="h.sessionId"
               :class="{active:h.sessionId===sessionId}" @click="loadHistory(h.sessionId)">
            <p class="h-title">{{ h.firstMsg }}</p>
            <span class="h-time">{{ h.time }}</span>
          </div>
        </div>
        <div class="empty-hint" v-else>暂无历史对话</div>
      </div>

      <div class="ai-chat">
        <div class="page-head" style="text-align:center;padding:16px 0 0">
          <p style="font-size:12px;color:var(--caption);margin-bottom:4px">AI Triage</p>
          <h1>智能导诊</h1>
        </div>

        <div class="chat-panel">
          <div class="disclaimer">&#9888; 智能导诊仅供参考，不能替代专业医生诊断。紧急症状请前往急诊科。</div>

          <div class="messages" ref="msgBox">
            <div class="welcome" v-if="messages.length===0&&!streaming">
              <p>请描述您的症状：</p>
              <div class="quick-asks">
                <button v-for="q in quickQuestions" :key="q" @click="send(q)">{{ q }}</button>
              </div>
            </div>
            <div v-for="(m,i) in messages" :key="i" :class="['msg',m.role]">
              <div class="msg-avatar">{{ m.role==='user'?'我':'AI' }}</div>
              <div class="msg-text" v-html="renderMarkdown(m.content)"/>
            </div>
            <div v-if="streaming" class="msg assistant">
              <div class="msg-avatar">AI</div>
              <div class="msg-text">{{ streamText }}<span class="cursor">|</span></div>
            </div>
          </div>

          <div class="input-area">
            <input v-model="input" placeholder="描述您的症状..." @keyup.enter="send(input)" :disabled="streaming"/>
            <button class="btn btn-primary" :disabled="!input.trim()||streaming" @click="send(input)">{{ streaming?'...':'发送' }}</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref,computed,nextTick,onMounted } from 'vue'
import request from '../../utils/request'

const input=ref(''),messages=ref([]),streaming=ref(false),streamText=ref(''),msgBox=ref(null)
const sessionId=ref('sess_'+Date.now()),history=ref([])

const historyGroups=computed(()=>{
  const map=new Map()
  for(const h of history.value){
    if(!map.has(h.sessionId)) map.set(h.sessionId,{sessionId:h.sessionId,firstMsg:(h.userMessage||'').slice(0,30),time:h.createTime?new Date(h.createTime).toLocaleDateString('zh-CN'):''})
  }
  return [...map.values()]
})

const quickQuestions=['头痛发热应该挂什么科？','胃痛消化不良挂哪个科室？','皮肤起红疹很痒怎么办？','儿童感冒咳嗽挂什么科？']

async function send(text){
  if(!text.trim()||streaming.value) return
  const msg=text.trim();input.value='';messages.value.push({role:'user',content:msg});await scrollBottom()
  streaming.value=true;streamText.value=''
  try{
    const res=await request.post('/ai/chat',{message:msg,sessionId:sessionId.value,type:'TRIAGE'})
    const reply=res.data.reply;let idx=0
    while(idx<reply.length){streamText.value+=reply[idx];idx++;await new Promise(r=>setTimeout(r,15))}
  }catch{streamText.value='抱歉，AI服务暂时不可用。'}
  if(streamText.value) messages.value.push({role:'assistant',content:streamText.value})
  streamText.value='';streaming.value=false;await scrollBottom();fetchHistory()
}

async function fetchHistory(){
  try{const res=await request.get('/ai/history');history.value=res.data||[]}catch{}
}
async function loadHistory(sid){
  sessionId.value=sid
  try{const res=await request.get(`/ai/history/${sid}`);const list=res.data||[]
  messages.value=[];for(const h of list){messages.value.push({role:'user',content:h.userMessage});messages.value.push({role:'assistant',content:h.aiResponse})}
  await scrollBottom()}catch{}
}
function startNewChat(){sessionId.value='sess_'+Date.now();messages.value=[]}
function renderMarkdown(t){return t.replace(/\*\*(.*?)\*\*/g,'<strong>$1</strong>').replace(/\n/g,'<br>').replace(/【(.+?)】/g,'<em class="note">【$1】</em>')}
async function scrollBottom(){await nextTick();if(msgBox.value) msgBox.value.scrollTop=msgBox.value.scrollHeight}
onMounted(fetchHistory)
</script>

<style scoped>
.ai-layout { display:grid;grid-template-columns:220px 1fr;gap:1px;background:var(--border);min-height:560px;border-radius:var(--radius);overflow:hidden;box-shadow:var(--shadow); }
.ai-sidebar { background:var(--surface);padding:16px;overflow-y:auto; }
.sidebar-head { display:flex;align-items:center;justify-content:space-between;margin-bottom:12px; }
.sidebar-head h3 { font-size:14px;color:var(--title); }
.history-item { padding:10px;cursor:pointer;border-bottom:1px solid var(--border-light);transition:background .15s;border-radius:var(--radius-sm); }
.history-item:hover,.history-item.active { background:var(--bg); }
.h-title { font-size:13px;color:var(--title);overflow:hidden;text-overflow:ellipsis;white-space:nowrap; }
.h-time { font-size:11px;color:var(--caption); }
.empty-hint { padding:16px 0;text-align:center;font-size:13px;color:var(--caption); }

.ai-chat { background:var(--surface);display:flex;flex-direction:column; }
.chat-panel { flex:1;display:flex;flex-direction:column;padding:12px 20px 16px;overflow:hidden; }
.disclaimer { padding:8px 12px;background:#FFF9E6;border:1px solid #F5E09A;border-radius:var(--radius-sm);font-size:12px;color:#B8860B;margin-bottom:12px;text-align:center; }
.messages { flex:1;overflow-y:auto;padding:4px 0; }
.welcome { text-align:center;padding:36px 0; }
.welcome p { font-size:14px;color:var(--caption);margin-bottom:12px; }
.quick-asks { display:flex;flex-wrap:wrap;gap:8px;justify-content:center; }
.quick-asks button { padding:7px 14px;border:1px solid var(--border);border-radius:20px;background:var(--surface);font-size:13px;cursor:pointer;font-family:inherit;color:var(--body);transition:all .15s; }
.quick-asks button:hover { border-color:var(--primary);color:var(--primary); }

.msg { display:flex;gap:10px;margin-bottom:16px; }
.msg.user { flex-direction:row-reverse; }
.msg-avatar { width:28px;height:28px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:11px;font-weight:600;flex-shrink:0; }
.msg.user .msg-avatar { background:var(--primary-light);color:var(--primary); }
.msg.assistant .msg-avatar { background:var(--bg);color:var(--body); }
.msg-text { max-width:75%;padding:10px 14px;font-size:14px;line-height:1.7;border-radius:var(--radius-sm);color:var(--title); }
.msg.user .msg-text { background:var(--primary-light); }
.msg.assistant .msg-text { background:var(--bg); }
.msg-text :deep(em.note) { color:var(--caption);font-size:12px;display:block;margin-top:6px; }

.cursor { animation:blink .7s infinite; }
@keyframes blink { 0%,100%{opacity:1}50%{opacity:0} }

.input-area { display:flex;gap:10px;margin-top:12px; }
.input-area input { flex:1;height:40px;padding:0 12px;border:1px solid var(--border);border-radius:20px;background:var(--bg);font-size:14px;color:var(--title);font-family:inherit;outline:none;transition:border-color .15s; }
.input-area input:focus { border-color:var(--primary); }

@media(max-width:768px){ .ai-layout{grid-template-columns:1fr}.ai-sidebar{max-height:140px} }
</style>
