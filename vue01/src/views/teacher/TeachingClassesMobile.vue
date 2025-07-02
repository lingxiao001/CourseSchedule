<template>
  <div class="teaching-classes-mobile">
    <el-page-header :icon="ArrowLeft" title="返回" @back="$router.go(-1)">
      <template #content>
        <div class="flex items-center">
          <span class="text-large font-600 mr-3">教学班管理</span>
          <el-tag type="warning" size="small">教师工作台</el-tag>
        </div>
      </template>
    </el-page-header>

    <div class="content-area">
      <!-- 操作工具栏 -->
      <div class="action-bar">
        <el-input
          v-model="searchQuery"
          placeholder="搜索教学班"
          :prefix-icon="Search"
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" :icon="Plus" @click="showAddDialog = true" circle />
      </div>

      <!-- 教学班列表 -->
      <div v-if="loading" class="loading-spinner">
        <el-spinner size="large" />
      </div>
      
      <div v-if="!loading && classes.length === 0" class="empty-state">
        <el-empty description="暂无教学班数据" />
      </div>

      <div class="class-list" v-else>
        <el-card v-for="cls in classes" :key="cls.id" class="class-card">
          <div class="card-header">
            <span class="class-code">{{ cls.classCode }}</span>
            <div class="actions">
              <el-button size="small" type="primary" text @click="handleEdit(cls)">编辑</el-button>
              <el-button size="small" type="danger" text @click="handleDelete(cls.id)">删除</el-button>
            </div>
          </div>
          <div class="card-body">
            <p><strong>课程:</strong> {{ cls.courseName }}</p>
            <p><strong>教师:</strong> {{ cls.teacherName }}</p>
            <p><strong>人数:</strong> {{ cls.currentStudents }} / {{ cls.maxStudents }}</p>
          </div>
        </el-card>

        <div v-if="hasMore" class="load-more">
          <el-button @click="loadMore" :loading="loadingMore">加载更多</el-button>
        </div>
      </div>
    </div>

    <!-- 添加/编辑教学班对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="isEditing ? '编辑教学班' : '添加教学班'"
      width="95%"
      @closed="resetForm"
    >
      <el-form
        ref="classFormRef"
        :model="classForm"
        :rules="classRules"
        label-position="top"
      >
        <el-form-item label="所属课程" prop="courseId" v-if="!isEditing">
          <el-select
            v-model="classForm.courseId"
            placeholder="请选择课程"
            style="width: 100%"
            :disabled="isEditing"
          >
            <el-option
              v-for="course in courses"
              :key="course.id"
              :label="`${course.name} (${course.classCode})`"
              :value="course.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="授课教师" prop="teacherId">
          <el-select
            v-model="classForm.teacherId"
            placeholder="请选择授课教师"
            style="width: 100%"
          >
            <el-option
              v-for="teacher in teachers"
              :key="teacher.id"
              :label="teacher.name"
              :value="teacher.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="教学班代码" prop="classCode" v-if="!isEditing">
          <el-input 
            v-model="classForm.classCode" 
            placeholder="例如：CS101-01"
          />
        </el-form-item>
        
        <el-form-item label="最大学生数" prop="maxStudents">
          <el-input-number
            v-model="classForm.maxStudents"
            :min="1"
            :max="200"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="submitClassForm">
          {{ isEditing ? '更新' : '添加' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Search, Plus, ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getTeachingClasses, 
  createTeachingClass, 
  updateTeachingClass, 
  deleteTeachingClass 
} from '@/api/teacher'
import { getCourses } from '@/api/teacher'
import { getUsers } from '@/api/admin'

// 数据
const classes = ref([])
const courses = ref([])
const teachers = ref([])
const loading = ref(false)
const loadingMore = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalClasses = ref(0)
const searchQuery = ref('')

const hasMore = computed(() => classes.value.length < totalClasses.value)

// 表单相关
const showAddDialog = ref(false)
const isEditing = ref(false)
const classForm = ref({
  id: null,
  classCode: '',
  maxStudents: 50,
  courseId: null,
  teacherId: null
})
const classFormRef = ref()
const classRules = {
  courseId: [{ required: true, message: '请选择所属课程', trigger: 'change' }],
  teacherId: [{ required: true, message: '请选择授课教师', trigger: 'change' }],
  classCode: [{ required: true, message: '请输入教学班代码', trigger: 'blur' }],
  maxStudents: [
    { required: true, message: '请设置最大学生数', trigger: 'blur' },
    { type: 'number', min: 1, max: 200, message: '人数应在1-200之间' }
  ]
}

// 映射课程和教师名称
const mapNamesToClasses = (classData) => {
  return classData.map(item => ({
    ...item,
    courseName: courses.value.find(c => c.id === item.courseId)?.name || '未知课程',
    teacherName: teachers.value.find(t => t.id === item.teacherId)?.name || '未知教师'
  }))
}

// 获取教学班列表
const fetchClasses = async (isLoadMore = false) => {
  if (isLoadMore) {
    loadingMore.value = true
  } else {
    loading.value = true
    currentPage.value = 1 // 重置页面
  }
  
  try {
    const response = await getTeachingClasses({
      page: currentPage.value,
      size: pageSize.value,
      query: searchQuery.value
    })
    
    const newClasses = mapNamesToClasses(response.data)
    
    if (isLoadMore) {
      classes.value = [...classes.value, ...newClasses]
    } else {
      classes.value = newClasses
    }
    totalClasses.value = response.total
  } catch (error) {
    ElMessage.error('获取教学班列表失败: ' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const loadMore = () => {
  if (hasMore.value) {
    currentPage.value++
    fetchClasses(true)
  }
}

// 获取课程列表
const fetchCourses = async () => {
  try {
    const response = await getCourses({ page: 1, size: 1000 })
    courses.value = response.data
  } catch (error) {
    ElMessage.error('获取课程列表失败: ' + error.message)
  }
}

// 获取教师列表
const fetchTeachers = async () => {
  try {
    const response = await getUsers({ page: 1, size: 1000, role: 'teacher' })
    teachers.value = response.data.content.map(user => ({
      id: user.roleId,
      name: user.realName
    }))
  } catch (error) {
    ElMessage.error('获取教师列表失败: ' + (error.response?.data?.message || error.message))
  }
}

// 搜索
const handleSearch = () => {
  fetchClasses()
}

// 编辑
const handleEdit = (cls) => {
  isEditing.value = true
  classForm.value = { ...cls }
  showAddDialog.value = true
}

// 删除
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除此教学班吗？此操作不可逆。', '警告', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteTeachingClass(id)
    ElMessage.success('删除成功')
    // 重新加载数据
    fetchClasses()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + (error.response?.data?.message || error.message))
    }
  }
}

// 提交表单
const submitClassForm = async () => {
  if (!classFormRef.value) return
  try {
    await classFormRef.value.validate()
    
    if (isEditing.value) {
      await updateTeachingClass(classForm.value.id, {
        maxStudents: classForm.value.maxStudents,
        teacherId: classForm.value.teacherId
      })
      ElMessage.success('更新成功')
    } else {
      await createTeachingClass(classForm.value.courseId, {
        classCode: classForm.value.classCode,
        maxStudents: classForm.value.maxStudents,
        teacherId: classForm.value.teacherId
      })
      ElMessage.success('添加成功')
    }
    
    showAddDialog.value = false
    fetchClasses()
  } catch (error) {
    if (error.name !== 'ValidationError') {
       ElMessage.error('操作失败: ' + (error.response?.data?.message || error.message))
    }
  }
}

// 重置表单
const resetForm = () => {
  isEditing.value = false
  classForm.value = {
    id: null,
    classCode: '',
    maxStudents: 50,
    courseId: null,
    teacherId: null
  }
  classFormRef.value?.resetFields()
}

// 初始化加载
onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([fetchTeachers(), fetchCourses()])
    await fetchClasses()
  } catch (error) {
    ElMessage.error('初始化数据失败: ' + error.message)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.teaching-classes-mobile {
  padding: 10px;
  background-color: #f4f5f7;
  min-height: 100vh;
}

.content-area {
  padding-top: 10px;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 5px;
}

.action-bar .el-input {
  margin-right: 10px;
}

.loading-spinner, .empty-state {
  margin-top: 50px;
  display: flex;
  justify-content: center;
}

.class-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.class-card {
  border-radius: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 8px;
}

.class-code {
  font-weight: bold;
  color: #303133;
}

.actions .el-button {
  padding: 4px;
}

.card-body p {
  margin: 4px 0;
  font-size: 14px;
  color: #606266;
}

.card-body p strong {
  color: #303133;
}

.load-more {
  margin-top: 16px;
  text-align: center;
}
</style> 