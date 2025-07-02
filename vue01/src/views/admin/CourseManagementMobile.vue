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
</style> 