<<<<<<< Updated upstream
<template>
  <view class="classroom-mobile">
    <view class="top-bar">
      <u-icon class="back" @click="$router.back()"><ArrowLeftBold /></u-icon>
      <u-input v-model="search" placeholder="搜索教学楼/教室" :clearable="true" @confirm="fetchRooms" />
      <u-button size="mini" type="primary" @click="openDialog()"><u-icon><Plus /></u-icon> 添加</u-button>
    </view>

    <view v-if="loading" class="loading"><u-skeleton rows="5" animated/></view>
    <u-empty v-else-if="rooms.length===0" description="暂无教室" />
    <u-collapse v-else>
      <u-collapse-item v-for="r in rooms" :key="r.id" :title="`${r.building}-${r.classroomName}`">
        <text>ID：{{ r.id }}</text>
        <text>容量：{{ r.capacity ?? '-' }}</text>
        <view class="btn-group">
          <u-button size="mini" @click="openDialog(r)">编辑</u-button>
          <u-button size="mini" type="error" @click="confirmDelete(r.id)">删除</u-button>
        </view>
      </u-collapse-item>
    </u-collapse>

    <!-- dialog -->
    <u-popup v-model="dialogVisible" :title="isEdit? '编辑教室':'添加教室'" width="90%" @close="resetForm">
      <u-form :model="form" ref="formRef" :rules="rules" label-width="90px">
        <u-form-item label="教学楼" prop="building"><u-input v-model="form.building" /></u-form-item>
        <u-form-item label="教室" prop="classroomName"><u-input v-model="form.classroomName" /></u-form-item>
        <u-form-item label="容量" prop="capacity"><u-input-number v-model="form.capacity" :min="1" /></u-form-item>
      </u-form>
      <template #footer>
        <u-button @click="dialogVisible=false">取消</u-button>
        <u-button type="primary" @click="submit">确定</u-button>
      </template>
    </u-popup>
  </view>
</template>

<script setup>

// 全局 uni 对象定义
const uni = {
  showToast: (options) => {
    if (options.icon === 'success') {
      alert('✅ ' + options.title);
    } else if (options.icon === 'error') {
      alert('❌ ' + options.title);
    } else {
      alert(options.title);
    }
  },
  showModal: (options) => {
    const result = confirm(options.content || options.title);
    if (options.success) {
      options.success({ confirm: result });
    }
  },
  navigateTo: (options) => {
    window.location.href = options.url;
  },
  navigateBack: () => {
    window.history.back();
  },
  redirectTo: (options) => {
    window.location.replace(options.url);
  },
  reLaunch: (options) => {
    window.location.href = options.url;
  }
};






import { ref, onMounted } from 'vue'
import { getClassrooms, createClassroom, updateClassroom, deleteClassroom } from '@/api/admin'

const loading = ref(false)
const rooms = ref([])
const search = ref('')

const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({ building:'', classroomName:'', capacity:null, id:null })
const formRef = ref(null)
const rules = { building:[{required:true,message:'必填',trigger:'blur'}], classroomName:[{required:true,message:'必填',trigger:'blur'}], capacity:[{required:true,message:'必填',trigger:'blur'}] }

const fetchRooms = async () => {
  loading.value = true
  try {
    const res = await getClassrooms()
    rooms.value = Array.isArray(res.data)? res.data.filter(r=>{
      if(!search.value) return true
      const q=search.value.toLowerCase()
      return r.building.toLowerCase().includes(q)||r.classroomName.toLowerCase().includes(q)
    }):[]
  }catch(e){uni.showToast({ title: '加载失败', icon: 'error' })} finally{loading.value=false}
}

const openDialog = (room=null)=>{
  isEdit.value=!!room
  if(room) form.value={...room}
  else form.value={building:'',classroomName:'',capacity:null,id:null}
  dialogVisible.value=true
}

const resetForm=()=>{form.value={building:'',classroomName:'',capacity:null,id:null}}

const submit=()=>{
  formRef.value.validate(async valid=>{
    if(!valid) return
    try{
      if(isEdit.value){ await updateClassroom(form.value.id, form.value); uni.showToast({ title: '更新成功', icon: 'success' }) }
      else { await createClassroom(form.value); uni.showToast({ title: '创建成功', icon: 'success' }) }
      dialogVisible.value=false; fetchRooms()
    }catch(e){uni.showToast({ title: '操作失败', icon: 'error' })}
  })
}

const confirmDelete = id => {
  uni.showModal({
    title: '警告',
    content: '确认删除?',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteClassroom(id)
          uni.showToast({ title: '删除成功', icon: 'success' })
          fetchRooms()
        } catch (e) {
          uni.showToast({ title: '删除失败', icon: 'error' })
        }
      }
    }
  })
}

onMounted(fetchRooms)
</script>

<style scoped>
.classroom-mobile{padding:1rem;}
.top-bar{display:flex;gap:.5rem;margin-bottom:1rem;align-items:center;}
.back{font-size:1.8rem;cursor:pointer;}
.top-bar .el-input{flex:1;}
.btn-group{display:flex;gap:.5rem;margin-top:.5rem;}
.loading{padding:1rem;}
=======
<template>
  <div class="classroom-mobile">
    <div class="top-bar">
      <el-icon class="back" @click="$router.back()"><ArrowLeftBold /></el-icon>
      <el-input v-model="search" placeholder="搜索教学楼/教室" clearable @keyup.enter="fetchRooms" />
      <el-button size="small" type="primary" @click="openDialog()"><el-icon><Plus /></el-icon> 添加</el-button>
    </div>

    <div v-if="loading" class="loading"><el-skeleton rows="5" animated/></div>
    <el-empty v-else-if="rooms.length===0" description="暂无教室" />
    <el-collapse v-else>
      <el-collapse-item v-for="r in rooms" :key="r.id" :title="`${r.building}-${r.classroomName}`">
        <p>ID：{{ r.id }}</p>
        <p>容量：{{ r.capacity ?? '-' }}</p>
        <div class="btn-group">
          <el-button size="small" @click="openDialog(r)">编辑</el-button>
          <el-button size="small" type="danger" @click="confirmDelete(r.id)">删除</el-button>
        </div>
      </el-collapse-item>
    </el-collapse>

    <!-- dialog -->
    <el-dialog v-model="dialogVisible" :title="isEdit? '编辑教室':'添加教室'" width="90%" @close="resetForm">
      <el-form :model="form" ref="formRef" :rules="rules" label-width="90px">
        <el-form-item label="教学楼" prop="building"><el-input v-model="form.building" /></el-form-item>
        <el-form-item label="教室" prop="classroomName"><el-input v-model="form.classroomName" /></el-form-item>
        <el-form-item label="容量" prop="capacity"><el-input-number v-model="form.capacity" :min="1" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ArrowLeftBold, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getClassrooms, createClassroom, updateClassroom, deleteClassroom } from '@/api/admin'

const loading = ref(false)
const rooms = ref([])
const search = ref('')

const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({ building:'', classroomName:'', capacity:null, id:null })
const formRef = ref(null)
const rules = { building:[{required:true,message:'必填',trigger:'blur'}], classroomName:[{required:true,message:'必填',trigger:'blur'}], capacity:[{required:true,message:'必填',trigger:'blur'}] }

const fetchRooms = async () => {
  loading.value = true
  try {
    const res = await getClassrooms()
    rooms.value = Array.isArray(res.data)? res.data.filter(r=>{
      if(!search.value) return true
      const q=search.value.toLowerCase()
      return r.building.toLowerCase().includes(q)||r.classroomName.toLowerCase().includes(q)
    }):[]
  }catch(e){ElMessage.error('加载失败')} finally{loading.value=false}
}

const openDialog = (room=null)=>{
  isEdit.value=!!room
  if(room) form.value={...room}
  else form.value={building:'',classroomName:'',capacity:null,id:null}
  dialogVisible.value=true
}

const resetForm=()=>{form.value={building:'',classroomName:'',capacity:null,id:null}}

const submit=()=>{
  formRef.value.validate(async valid=>{
    if(!valid) return
    try{
      if(isEdit.value){ await updateClassroom(form.value.id, form.value); ElMessage.success('更新成功') }
      else { await createClassroom(form.value); ElMessage.success('创建成功') }
      dialogVisible.value=false; fetchRooms()
    }catch(e){ElMessage.error('操作失败')}
  })
}

const confirmDelete=id=>{
  ElMessageBox.confirm('确认删除?','警告',{type:'warning'}).then(async()=>{ try{await deleteClassroom(id);ElMessage.success('删除成功');fetchRooms()}catch(e){ElMessage.error('删除失败')}})
}

onMounted(fetchRooms)
</script>

<style scoped>
.classroom-mobile{padding:1rem;}
.top-bar{display:flex;gap:.5rem;margin-bottom:1rem;align-items:center;}
.back{font-size:1.8rem;cursor:pointer;}
.top-bar .el-input{flex:1;}
.btn-group{display:flex;gap:.5rem;margin-top:.5rem;}
.loading{padding:1rem;}
>>>>>>> Stashed changes
</style> 