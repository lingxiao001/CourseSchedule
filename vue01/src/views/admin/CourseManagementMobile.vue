<<<<<<< Updated upstream
<template>
  <view class="course-mgmt-mobile">
    <view class="top-bar">
      <u-icon class="back" @click="$router.back()"><ArrowLeftBold /></u-icon>
      <u-input v-model="search" placeholder="搜索课程名称/代码" :clearable="true" @confirm="fetchCourses" />
      <u-button type="primary" size="mini" @click="openDialog()">
        <u-icon><Plus /></u-icon> 添加
      </u-button>
    </view>

    <view v-if="loading" class="loading"><u-skeleton rows="5" animated/></view>
    <u-empty v-else-if="courses.length===0" description="暂无课程" />
    <u-collapse v-else>
      <u-collapse-item v-for="c in courses" :key="c.id" :title="`${c.courseName}(${c.classCode})`">
        <text>学分：{{ c.credit }} | 学时：{{ c.hours }}</text>
        <text>{{ c.description }}</text>
        <view v-if="classMap[c.id] && classMap[c.id].length">
          <text>教学班</text>
          <ul class="class-list">
            <li v-for="tc in classMap[c.id]" :key="tc.id">
              {{ tc.classCode }} - 教师: {{ tc.teacher?.realName || '未知' }}
            </li>
          </ul>
        </view>
        <view class="btn-group">
          <u-button size="mini" @click="openDialog(c)">编辑</u-button>
          <u-button size="mini" type="error" @click="confirmDelete(c.id)">删除</u-button>
        </view>
      </u-collapse-item>
    </u-collapse>

    <!-- 添加/编辑课程对话框 -->
    <u-popup v-model="dialogVisible" :title="isEdit ? '编辑课程' : '添加课程'" width="90%" @close="resetForm">
      <u-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <u-form-item label="课程名称" prop="courseName">
          <u-input v-model="form.courseName" />
        </u-form-item>
        <u-form-item label="课程代码" prop="classCode">
          <u-input v-model="form.classCode" />
        </u-form-item>
        <u-form-item label="描述" prop="description">
          <u-input type="textarea" v-model="form.description" />
        </u-form-item>
        <u-form-item label="学分" prop="credit">
          <u-input-number v-model="form.credit" :step="0.5" :min="0" />
        </u-form-item>
        <u-form-item label="学时" prop="hours">
          <u-input-number v-model="form.hours" :step="8" :min="0" />
        </u-form-item>
      </u-form>
      <template #footer>
        <u-button @click="dialogVisible=false">取消</u-button>
        <u-button type="primary" @click="submitForm">确定</u-button>
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
import { getAllCourses, deleteCourse } from '@/api/admin'
import { getTeachingClassesByCourse } from '@/api/teacher'

const loading = ref(false)
const courses = ref([])
const search = ref('')

const dialogVisible = ref(false)
const currentCourse = ref(null)
const isEdit = ref(false)
const form = ref({})
const rules = ref({})
const formRef = ref(null)

const classMap = ref({})

const fetchCourses = async () => {
  loading.value = true
  try {
    const res = await getAllCourses()
    courses.value = Array.isArray(res.data) ? res.data.filter(c=>{
      if(!search.value) return true
      const q = search.value.toLowerCase()
      return c.courseName.toLowerCase().includes(q) || c.classCode.toLowerCase().includes(q)
    }):[]
    // 获取每门课的教学班
    await Promise.all(courses.value.map(async course=>{
      try{
        const cls=await getTeachingClassesByCourse(course.id)
        classMap.value[course.id]=cls
      }catch(e){ classMap.value[course.id]=[] }
    }))
  } catch(e){ uni.showToast({ title: '加载课程失败', icon: 'error' }) } finally{ loading.value=false }
}

const openDialog = (course=null) => {
  currentCourse.value = course
  isEdit.value = !!course
  form.value = {
    courseName: course?.courseName || '',
    classCode: course?.classCode || '',
    description: course?.description || '',
    credit: course?.credit || 0,
    hours: course?.hours || 0
  }
  dialogVisible.value = true
}

const confirmDelete = (id) => {
  uni.showModal({
    title: '警告',
    content: '确认删除该课程?',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteCourse(id)
          uni.showToast({ title: '删除成功', icon: 'success' })
          fetchCourses()
        } catch (e) {
          uni.showToast({ title: '删除失败', icon: 'error' })
        }
      }
    }
  })
}

const resetForm = () => {
  form.value = {}
  isEdit.value = false
}

const submitForm = () => {
  // Implement the logic to submit the form
  console.log('Form submitted:', form.value)
  dialogVisible.value = false
}

onMounted(fetchCourses)
</script>

<style scoped>
.course-mgmt-mobile{padding:1rem;}
.top-bar{display:flex;gap:.5rem;margin-bottom:1rem;align-items:center;}
.back{font-size:1.8rem;cursor:pointer;}
.top-bar .el-input{flex:1;}
.btn-group{display:flex;gap:.5rem;margin-top:.5rem;}
.loading{padding:1rem;}
.class-list{padding-left:1rem;font-size:1.3rem;color:#555;}
=======
<template>
  <div class="course-mgmt-mobile">
    <div class="top-bar">
      <el-icon class="back" @click="$router.back()"><ArrowLeftBold /></el-icon>
      <el-input v-model="search" placeholder="搜索课程名称/代码" clearable @keyup.enter="fetchCourses" />
      <el-button type="primary" size="small" @click="openDialog()">
        <el-icon><Plus /></el-icon> 添加
      </el-button>
    </div>

    <div v-if="loading" class="loading"><el-skeleton rows="5" animated/></div>
    <el-empty v-else-if="courses.length===0" description="暂无课程" />
    <el-collapse v-else>
      <el-collapse-item v-for="c in courses" :key="c.id" :title="`${c.courseName}(${c.classCode})`">
        <p>学分：{{ c.credit }} | 学时：{{ c.hours }}</p>
        <p>{{ c.description }}</p>
        <div v-if="classMap[c.id] && classMap[c.id].length">
          <h4>教学班</h4>
          <ul class="class-list">
            <li v-for="tc in classMap[c.id]" :key="tc.id">
              {{ tc.classCode }} - 教师: {{ tc.teacher?.realName || '未知' }}
            </li>
          </ul>
        </div>
        <div class="btn-group">
          <el-button size="small" @click="openDialog(c)">编辑</el-button>
          <el-button size="small" type="danger" @click="confirmDelete(c.id)">删除</el-button>
        </div>
      </el-collapse-item>
    </el-collapse>

    <!-- 添加/编辑课程对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑课程' : '添加课程'" width="90%" @close="resetForm">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="form.courseName" />
        </el-form-item>
        <el-form-item label="课程代码" prop="classCode">
          <el-input v-model="form.classCode" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input type="textarea" v-model="form.description" />
        </el-form-item>
        <el-form-item label="学分" prop="credit">
          <el-input-number v-model="form.credit" :step="0.5" :min="0" />
        </el-form-item>
        <el-form-item label="学时" prop="hours">
          <el-input-number v-model="form.hours" :step="8" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ArrowLeftBold, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAllCourses, deleteCourse } from '@/api/admin'
import { getTeachingClassesByCourse } from '@/api/teacher'

const loading = ref(false)
const courses = ref([])
const search = ref('')

const dialogVisible = ref(false)
const currentCourse = ref(null)
const isEdit = ref(false)
const form = ref({})
const rules = ref({})
const formRef = ref(null)

const classMap = ref({})

const fetchCourses = async () => {
  loading.value = true
  try {
    const res = await getAllCourses()
    courses.value = Array.isArray(res.data) ? res.data.filter(c=>{
      if(!search.value) return true
      const q = search.value.toLowerCase()
      return c.courseName.toLowerCase().includes(q) || c.classCode.toLowerCase().includes(q)
    }):[]
    // 获取每门课的教学班
    await Promise.all(courses.value.map(async course=>{
      try{
        const cls=await getTeachingClassesByCourse(course.id)
        classMap.value[course.id]=cls
      }catch(e){ classMap.value[course.id]=[] }
    }))
  } catch(e){ ElMessage.error('加载课程失败') } finally{ loading.value=false }
}

const openDialog = (course=null) => {
  currentCourse.value = course
  isEdit.value = !!course
  form.value = {
    courseName: course?.courseName || '',
    classCode: course?.classCode || '',
    description: course?.description || '',
    credit: course?.credit || 0,
    hours: course?.hours || 0
  }
  dialogVisible.value = true
}

const confirmDelete = (id) => {
  ElMessageBox.confirm('确认删除该课程?','警告',{type:'warning'}).then(async()=>{
    try{ await deleteCourse(id); ElMessage.success('删除成功'); fetchCourses() }catch(e){ElMessage.error('删除失败')} })
}

const resetForm = () => {
  form.value = {}
  isEdit.value = false
}

const submitForm = () => {
  // Implement the logic to submit the form
  console.log('Form submitted:', form.value)
  dialogVisible.value = false
}

onMounted(fetchCourses)
</script>

<style scoped>
.course-mgmt-mobile{padding:1rem;}
.top-bar{display:flex;gap:.5rem;margin-bottom:1rem;align-items:center;}
.back{font-size:1.8rem;cursor:pointer;}
.top-bar .el-input{flex:1;}
.btn-group{display:flex;gap:.5rem;margin-top:.5rem;}
.loading{padding:1rem;}
.class-list{padding-left:1rem;font-size:1.3rem;color:#555;}
>>>>>>> Stashed changes
</style> 