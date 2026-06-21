<template>
  <div class="page-w page-main">
    <div class="admin-layout">
      <div class="admin-sidebar">
        <h3>系统管理</h3>
        <a :class="{active:$route.path==='/admin/users'}" @click="$router.push('/admin/users')">用户管理</a>
        <a :class="{active:$route.path==='/admin/doctors'}" @click="$router.push('/admin/doctors')">医生管理</a>
        <a :class="{active:$route.path==='/admin/schedules'}" @click="$router.push('/admin/schedules')">排班管理</a>
        <a :class="{active:$route.path==='/admin/departments'}" @click="$router.push('/admin/departments')">科室管理</a>
        <a :class="{active:$route.path==='/admin/announcements'}" @click="$router.push('/admin/announcements')">公告管理</a>
        <a :class="{active:$route.path==='/admin/statistics'}" @click="$router.push('/admin/statistics')">数据统计</a>
      </div>
      <div class="admin-main">
        <div class="page-head" style="display:flex;justify-content:space-between;align-items:center">
          <h1>医生管理</h1>
          <button class="btn btn-primary btn-sm" @click="openAdd">+ 添加医生</button>
        </div>
        <div class="card" style="padding:0;overflow:hidden" v-loading="loading">
          <table class="tbl"><thead><tr><th>姓名</th><th>科室</th><th>职称</th><th>挂号费</th><th>评分</th><th>状态</th><th>操作</th></tr></thead>
          <tbody><tr v-for="d in doctors" :key="d.userId"><td>{{ d.realName }}</td><td>{{ d.departmentName||'-' }}</td><td>{{ d.title }}</td><td>&#165;{{ d.consultationFee||0 }}</td><td>&#9733; {{ d.avgRating||'5.0' }}</td><td><span class="tag" :class="d.status===1?'tag-green':'tag-red'">{{ d.status===1?'在岗':'停诊' }}</span></td><td>
            <button class="btn btn-ghost btn-sm" @click="editDoctor(d)">编辑</button>
            <button class="btn btn-ghost btn-sm" @click="toggle(d)">{{ d.status===1?'停诊':'启用' }}</button>
            <button class="btn btn-ghost btn-sm" @click="del(d)">删除</button>
          </td></tr></tbody></table>
        </div>
      </div>
    </div>
    <!-- Add Doctor Dialog -->
    <div class="modal-bg" v-if="showAdd" @click.self="showAdd=false">
      <div class="modal"><h3>添加医生</h3>
        <div class="f"><label>用户名</label><input v-model="form.username"/></div>
        <div class="f"><label>姓名</label><input v-model="form.realName"/></div>
        <div class="f"><label>手机号</label><input v-model="form.phone"/></div>
        <div class="f"><label>科室</label><select v-model="form.departmentId"><option :value="null">请选择</option><option v-for="dp in depts" :key="dp.id" :value="dp.id">{{ dp.name }}</option></select></div>
        <div class="f"><label>职称</label><select v-model="form.title"><option>主任医师</option><option>副主任医师</option><option>主治医师</option><option>住院医师</option></select></div>
        <div class="f"><label>擅长领域</label><input v-model="form.specialty"/></div>
        <div class="f"><label>挂号费</label><input v-model="form.consultationFee" type="number"/></div>
        <div class="mact"><button class="btn btn-primary" :disabled="saving" @click="addDoctor">{{ saving?'保存中…':'确认添加' }}</button><button class="btn btn-ghost" @click="showAdd=false">取消</button></div>
      </div>
    </div>
    <!-- Edit Doctor Dialog -->
    <div class="modal-bg" v-if="showEdit" @click.self="showEdit=false">
      <div class="modal"><h3>编辑医生</h3>
        <div class="f"><label>科室</label><select v-model="editForm.departmentId"><option v-for="dp in depts" :key="dp.id" :value="dp.id">{{ dp.name }}</option></select></div>
        <div class="f"><label>职称</label>
          <div style="display:flex;gap:8px">
            <select v-model="editForm.title" style="flex:1"><option v-for="t in titleOptions" :key="t" :value="t">{{ t }}</option></select>
            <input v-model="newTitle" placeholder="新职称" style="width:120px;font-size:12px"/>
            <button class="btn btn-outline btn-sm" @click="addTitle" :disabled="!newTitle.trim()">添加</button>
          </div>
        </div>
        <div class="f"><label>挂号费 (元)</label><input v-model.number="editForm.consultationFee" type="number" min="0"/></div>
        <div class="f"><label>擅长领域</label><input v-model="editForm.specialty"/></div>
        <div class="f"><label>个人简介</label><textarea v-model="editForm.introduction" rows="2"/></div>
        <div class="mact"><button class="btn btn-primary" :disabled="saving" @click="saveEdit">{{ saving?'保存中…':'保存修改' }}</button><button class="btn btn-ghost" @click="showEdit=false">取消</button></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref,reactive,onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../utils/request'
const doctors=ref([]);const depts=ref([]);const loading=ref(false);const showAdd=ref(false);const showEdit=ref(false);const saving=ref(false)
const form=reactive({username:'',realName:'',phone:'',departmentId:null,title:'主治医师',specialty:'',consultationFee:20})
const editForm=reactive({userId:null,departmentId:null,title:'',consultationFee:0,specialty:'',introduction:''})
const newTitle=ref('')
const titleOptions=ref(['主任医师','副主任医师','主治医师','住院医师','实习医师','教授','副教授','研究员'])

function editDoctor(d){
  editForm.userId=d.userId;editForm.departmentId=d.departmentId;editForm.title=d.title||''
  editForm.consultationFee=d.consultationFee||0;editForm.specialty=d.specialty||'';editForm.introduction=d.introduction||''
  showEdit.value=true
}
function addTitle(){const t=newTitle.value.trim();if(t&&!titleOptions.value.includes(t)){titleOptions.value.push(t)};newTitle.value=''}
async function saveEdit(){
  saving.value=true
  try{await request.put(`/admin/doctors/${editForm.userId}`,editForm);ElMessage.success('修改成功');showEdit.value=false;fetch()}catch{}
  finally{saving.value=false}
}
async function fetch(){loading.value=true;try{const r=await request.get('/admin/doctors');doctors.value=r.data?.records||r.data||[]}catch{}finally{loading.value=false}}
async function fetchDepts(){try{const r=await request.get('/hospital/departments');depts.value=r.data||[]}catch{}}
async function toggle(d){try{await request.put(`/admin/doctors/${d.userId}/status`,{status:d.status===1?0:1});ElMessage.success('已更新');fetch()}catch{}}
async function del(d){try{await request.delete(`/admin/doctors/${d.userId}`);ElMessage.success('已删除');fetch()}catch{}}
function openAdd(){form.username='';form.realName='';form.phone='';form.departmentId=null;form.title='主治医师';form.specialty='';form.consultationFee=20;showAdd.value=true}
async function addDoctor(){
  if(!form.username||!form.realName) return ElMessage.warning('请填写必填项')
  saving.value=true
  try{await request.post('/admin/doctors',form);ElMessage.success('添加成功');showAdd.value=false;fetch()}catch{}
  finally{saving.value=false}
}
onMounted(()=>{fetch();fetchDepts()})
</script>

<style scoped>
.admin-layout { display:grid;grid-template-columns:180px 1fr;gap:24px; }
.admin-sidebar { background:var(--surface);border-radius:var(--radius);box-shadow:var(--shadow);padding:16px;position:sticky;top:80px;height:fit-content; }
.admin-sidebar h3 { font-size:14px;color:var(--title);margin-bottom:12px;padding-bottom:8px;border-bottom:1px solid var(--border-light); }
.admin-sidebar a { display:block;padding:8px 12px;font-size:13px;color:var(--body);cursor:pointer;border-radius:var(--radius-sm);margin-bottom:2px;transition:all .15s; }
.admin-sidebar a:hover,.admin-sidebar a.active { background:var(--primary-light);color:var(--primary); }
.admin-main { min-width:0; }
.tbl { width:100%;border-collapse:collapse; }
.tbl th,.tbl td { padding:10px 14px;text-align:left;font-size:13px;border-bottom:1px solid var(--border-light); }
.tbl th { font-weight:500;color:var(--caption);font-size:12px; }
.tbl tbody tr:hover { background:var(--bg); }
.modal-bg { position:fixed;inset:0;background:rgba(0,0,0,.35);z-index:300;display:flex;align-items:center;justify-content:center; }
.modal { background:var(--surface);border-radius:var(--radius);padding:24px;width:440px;max-width:90vw;box-shadow:var(--shadow-md);max-height:80vh;overflow-y:auto; }
.modal h3 { font-size:17px;color:var(--title);margin-bottom:16px; }
.f { margin-bottom:12px; }
.f label { display:block;font-size:13px;color:var(--body);margin-bottom:4px; }
.f input,.f select,.f textarea { width:100%;padding:8px 12px;border:1px solid var(--border);border-radius:var(--radius-sm);font-size:14px;font-family:inherit;color:var(--title);outline:none;background:var(--surface);resize:vertical; }
.f input:focus,.f select:focus,.f textarea:focus { border-color:var(--primary); }
.f input,.f select { height:38px; }
.mact { display:flex;gap:8px;margin-top:16px; }
@media(max-width:768px){ .admin-layout{grid-template-columns:1fr}.admin-sidebar{position:static;display:flex;gap:4px;flex-wrap:wrap;padding:8px}.admin-sidebar h3{display:none}.admin-sidebar a{font-size:12px;padding:4px 10px} }
</style>
