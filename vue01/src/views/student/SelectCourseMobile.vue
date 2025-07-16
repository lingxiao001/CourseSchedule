<<<<<<< Updated upstream
<template>
  <view class="select-course-mobile">
    <header class="header">
      <u-icon @click="$router.back()" class="back"><ArrowLeftBold /></u-icon>
      <text2>选课中心</text2>
      <u-button type="primary" link @click="refreshCourses">
        <u-icon><Refresh /></u-icon>
      </u-button>
    </header>

    <view v-if="loading" class="loading"><u-skeleton :rows="5" animated/></view>
    <view v-else>
      <u-empty v-if="courses.length===0" description="暂无可选课程" />
      <u-card v-else v-for="course in courses" :key="course.id" class="course-card">
        <view class="title">{{ course.name }}</view>
        <text class="desc">{{ course.description }}</text>
        <view class="footer">
          <u-tag type="success">{{ course.credit }} 学分</u-tag>
          <u-button size="mini" type="primary" plain @click="openDialog(course.id)">选课</u-button>
        </view>
      </u-card>
    </view>

    <!-- 选课弹窗 -->
    <u-popup v-model="dialogVisible" title="输入教学班ID" width="85%">
      <u-select v-model="form.teachingClassId" placeholder="选择教学班" filterable style="width:100%">
        <u-option v-for="cls in teachingClasses" :key="cls.id" :label="cls.classCode || cls.id" :value="cls.id" />
      </u-select>
      <template #footer>
        <u-button @click="dialogVisible=false">取消</u-button>
        <u-button type="primary" @click="handleSelect">确定</u-button>
      </template>
    </u-popup>
  </view>
</template>

<script setup>

// 全局 uni 对象定义 - 已移除，使用原生方法替代






import { ref, onMounted } from 'vue'
import { getCourses, getTeachingClassesByCourse } from '@/api/teacher'
import { selectCourse } from '@/api/student'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const studentId = authStore.user?.roleId

const courses = ref([])
const loading = ref(false)

const dialogVisible = ref(false)
const form = ref({ teachingClassId: '', courseId: null })
const teachingClasses = ref([])

const fetchCourses = async () => {
  loading.value = true
  try {
    const res = await getCourses()
    courses.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    window.uni.showToast({ title: '$1', icon: 'error' })('加载课程失败')
  } finally {
    loading.value = false
  }
}

const refreshCourses = fetchCourses

const openDialog = async (courseId) => {
  form.value.courseId = courseId
  form.value.teachingClassId = ''
  dialogVisible.value = true
  try {
    teachingClasses.value = await getTeachingClassesByCourse(courseId)
  } catch (e) {
    window.uni.showToast({ title: '$1', icon: 'error' })('获取教学班失败')
    teachingClasses.value = []
  }
}

const handleSelect = async () => {
  if (!form.value.teachingClassId) {
    window.uni.showToast({ title: '$1', icon: 'none' })('请输入教学班ID')
    return
  }
  try {
    await selectCourse(studentId, form.value.teachingClassId)
    window.uni.showToast({ title: '$1', icon: 'success' })('选课成功')
    dialogVisible.value = false
    fetchCourses()
  } catch (e) {
    window.uni.showToast({ title: '$1', icon: 'error' })('选课失败')
  }
}

onMounted(fetchCourses)
</script>

<style scoped>
.select-course-mobile {
  padding: 1.5rem;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}
.back {
  font-size: 1.8rem;
  cursor: pointer;
}
.course-card {
  margin-bottom: 1rem;
}
.course-card .title {
  font-weight: 600;
  font-size: 1.6rem;
}
.course-card .desc {
  font-size: 1.2rem;
  color: #666;
  margin: 0.5rem 0 1rem;
}
.footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.loading {
  padding: 2rem;
}
=======
<template>
  <div class="select-course-mobile">
    <header class="header">
      <el-icon @click="$router.back()" class="back"><ArrowLeftBold /></el-icon>
      <h2>选课中心</h2>
      <el-button type="primary" link @click="refreshCourses">
        <el-icon><Refresh /></el-icon>
      </el-button>
    </header>

    <div v-if="loading" class="loading"><el-skeleton :rows="5" animated/></div>
    <div v-else>
      <el-empty v-if="courses.length===0" description="暂无可选课程" />
      <el-card v-else v-for="course in courses" :key="course.id" class="course-card">
        <div class="title">{{ course.name }}</div>
        <p class="desc">{{ course.description }}</p>
        <div class="footer">
          <el-tag type="success">{{ course.credit }} 学分</el-tag>
          <el-button size="small" type="primary" plain @click="openDialog(course.id)">选课</el-button>
        </div>
      </el-card>
    </div>

    <!-- 选课弹窗 -->
    <el-dialog v-model="dialogVisible" title="输入教学班ID" width="85%">
      <el-select v-model="form.teachingClassId" placeholder="选择教学班" filterable style="width:100%">
        <el-option v-for="cls in teachingClasses" :key="cls.id" :label="cls.classCode || cls.id" :value="cls.id" />
      </el-select>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="handleSelect">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ArrowLeftBold, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getCourses, getTeachingClassesByCourse } from '@/api/teacher'
import { selectCourse } from '@/api/student'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const studentId = authStore.user?.roleId

const courses = ref([])
const loading = ref(false)

const dialogVisible = ref(false)
const form = ref({ teachingClassId: '', courseId: null })
const teachingClasses = ref([])

const fetchCourses = async () => {
  loading.value = true
  try {
    const res = await getCourses()
    courses.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    ElMessage.error('加载课程失败')
  } finally {
    loading.value = false
  }
}

const refreshCourses = fetchCourses

const openDialog = async (courseId) => {
  form.value.courseId = courseId
  form.value.teachingClassId = ''
  dialogVisible.value = true
  try {
    teachingClasses.value = await getTeachingClassesByCourse(courseId)
  } catch (e) {
    ElMessage.error('获取教学班失败')
    teachingClasses.value = []
  }
}

const handleSelect = async () => {
  if (!form.value.teachingClassId) {
    ElMessage.warning('请输入教学班ID')
    return
  }
  try {
    await selectCourse(studentId, form.value.teachingClassId)
    ElMessage.success('选课成功')
    dialogVisible.value = false
    fetchCourses()
  } catch (e) {
    ElMessage.error('选课失败')
  }
}

onMounted(fetchCourses)
</script>

<style scoped>
.select-course-mobile {
  padding: 1.5rem;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}
.back {
  font-size: 1.8rem;
  cursor: pointer;
}
.course-card {
  margin-bottom: 1rem;
}
.course-card .title {
  font-weight: 600;
  font-size: 1.6rem;
}
.course-card .desc {
  font-size: 1.2rem;
  color: #666;
  margin: 0.5rem 0 1rem;
}
.footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.loading {
  padding: 2rem;
}
>>>>>>> Stashed changes
</style> 