<<<<<<< Updated upstream
<template>
  <view class="teaching-classes-management">
    <u-navbar :icon="'arrow-left'" title="" @back="$router.go(-1)">
      <template #content>
        <view class="flex items-center">
          <text class="text-large font-600 mr-3">教学班管理</text>
          <u-tag type="warning">教师工作台</u-tag>
        </view>
      </template>
    </u-navbar>

    <u-line />

    <!-- 操作工具栏 -->
    <view class="action-bar">
      <u-button type="primary" @click="showAddDialog = true">
        <u-icon><Plus /></u-icon> 添加教学班
      </u-button>
      
      <u-input
        v-model="searchQuery"
        placeholder="搜索教学班代码"
        style="width: 300px"
        :clearable="true"
        @clear="handleSearch"
        @confirm="handleSearch"
      >
        <template #suffix>
          <u-button :icon="'search'" @click="handleSearch" />
        </template>
      </u-input>
    </view>

    <!-- PC 端表格视图 -->
    <view class="table-container" v-if="!isMobile">
      <u-table
        :data="filteredClasses"
        :border="true"
        :stripe="true"
        :loading="loading"
        style="width: 100%"
      >
        <u-table-column prop="classCode" label="教学班代码" width="150" />
        <u-table-column prop="courseName" label="所属课程" />
        <u-table-column prop="teacherName" label="授课教师" />
        <u-table-column prop="maxStudents" label="最大人数" width="100" align="center" />
        <u-table-column prop="currentStudents" label="当前人数" width="100" align="center" />
        <u-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <u-button size="mini" @click="handleEdit(row)">编辑</u-button>
            <u-button
              size="mini"
              type="error"
              @click="handleDelete(row.id)"
            >删除</u-button>
          </template>
        </u-table-column>
      </u-table>
    </view>

    <!-- 移动端卡片视图 -->
    <view v-else>
      <u-card
        v-for="cls in filteredClasses"
        :key="cls.id"
        class="class-card"
        @click="toggleDetails(cls.id)"
      >
        <template #header>
          <view class="card-header">
            <text>{{ cls.classCode }}</text>
            <u-tag size="mini" type="info">{{ cls.courseName }}</u-tag>
          </view>
        </template>
        <view v-if="expandedId === cls.id" class="card-details">
          <text>授课教师：{{ cls.teacherName }}</text>
          <text>最大人数：{{ cls.maxStudents }}</text>
          <text>当前人数：{{ cls.currentStudents }}</text>
          <view class="card-actions">
            <u-button size="mini" @click.stop="handleEdit(cls)">编辑</u-button>
            <u-button size="mini" type="error" @click.stop="handleDelete(cls.id)">删除</u-button>
          </view>
        </view>
      </u-card>
    </view>

    <!-- 分页控件 -->
    <view class="pagination">
      <u-pagination
        v-model="currentPage"
        :page-size="pageSize"
        :total="totalClasses"
        layout="total, prev, pager, next"
        @current-change="fetchClasses"
      />
    </view>

    <!-- 添加/编辑教学班对话框 -->
    <u-popup
      v-model="showAddDialog"
      :title="isEditing ? '编辑教学班' : '添加教学班'"
      :width="600"
      @close="resetForm"
    >
      <u-form
        ref="classFormRef"
        :model="classForm"
        :rules="classRules"
        label-width="120px"
      >
        <u-form-item label="所属课程" prop="courseId" v-if="!isEditing">
          <u-select
            v-model="classForm.courseId"
            placeholder="请选择课程"
            style="width: 100%"
            filterable
            :disabled="isEditing"
          >
            <u-option
              v-for="course in courses"
              :key="course.id"
              :label="`${course.name} (${course.classCode})`"
              :value="course.id"
            />
          </u-select>
        </u-form-item>
        
        <u-form-item label="授课教师" prop="teacherId" v-if="false">
          <!-- 隐藏教师选择器，因为只能是当前教师 -->
          <u-select
            v-model="classForm.teacherId"
            placeholder="请选择授课教师"
            style="width: 100%"
            filterable
          >
            <u-option
              v-for="teacher in teachers"
              :key="teacher.id"
              :label="teacher.name"
              :value="teacher.id"
            />
          </u-select>
        </u-form-item>
        
        <u-form-item label="教学班代码" prop="classCode" v-if="!isEditing">
          <u-input 
            v-model="classForm.classCode" 
            placeholder="例如：CS101-01"
          />
        </u-form-item>
        
        <u-form-item label="最大学生数" prop="maxStudents">
          <u-input-number
            v-model="classForm.maxStudents"
            :min="1"
            :max="200"
            controls-position="right"
          />
        </u-form-item>
      </u-form>
      
      <template #footer>
        <u-button @click="showAddDialog = false">取消</u-button>
        <u-button type="primary" @click="submitClassForm">
          {{ isEditing ? '更新' : '添加' }}
        </u-button>
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






import { ref, computed, onMounted } from 'vue'
import { 
  getTeachingClasses, 
  createTeachingClass, 
  updateTeachingClass, 
  deleteTeachingClass 
} from '@/api/teacher'
import { getCourses} from '@/api/teacher'
import { getUsers } from '@/api/admin'
import { useAuthStore } from '@/stores/auth'

// 认证和用户信息
const authStore = useAuthStore()
const userRole = computed(() => (authStore.user?.role || '').toLowerCase())
const currentTeacherId = computed(() => authStore.user?.roleId)

// 教学班数据
const classes = ref([])
const courses = ref([])
const teachers = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalClasses = ref(0)
const searchQuery = ref('')

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
  classCode: [{ required: true, message: '请输入教学班代码', trigger: 'blur' }],
  maxStudents: [
    { required: true, message: '请设置最大学生数', trigger: 'blur' },
    { type: 'number', min: 1, max: 200, message: '人数应在1-200之间', trigger: 'blur' }
  ]
}

// 响应式判断是否为移动端
const isMobile = ref(window.innerWidth < 768)
window.addEventListener('resize', () => {
  isMobile.value = window.innerWidth < 768
})

const expandedId = ref(null)
const toggleDetails = (id) => {
  expandedId.value = expandedId.value === id ? null : id
}

// 获取教学班列表
const fetchClasses = async () => {
  try {
    loading.value = true
    const response = await getTeachingClasses({
      page: currentPage.value,
      size: pageSize.value,
      query: searchQuery.value
    })
    let data = response.data
    // 如果当前用户是教师，只展示自己的教学班；管理员则查看全部
    if (userRole.value === 'teacher') {
      data = data.filter(item => item.teacherId === currentTeacherId.value)
    }

    classes.value = data.map(item => ({
      ...item,
      courseName: courses.value.find(c => Number(c.id) === Number(item.courseId))?.name || '未知课程',
      teacherName: item.teacherName || teachers.value.find(t => Number(t.id) === Number(item.teacherId))?.name || '未知教师'
    }))

    totalClasses.value = data.length
  } catch (error) {
    uni.showToast({ title: '获取教学班列表失败: ' + (error.response?.data?.message || error.message), icon: 'error' })
  } finally {
    loading.value = false
  }
}

// 获取课程列表
const fetchCourses = async () => {
  try {
    const response = await getCourses({ page: 1, size: 1000 })
    courses.value = response.data
  } catch (error) {
    uni.showToast({ title: '获取课程列表失败: ' + error.message, icon: 'error' })
  }
}

// 获取教师列表
const fetchTeachers = async () => {
  try {
    const response = await getUsers({ page: 0, size: 1000 })
    teachers.value = response.data.content
      .filter(user => user.role === 'teacher')
      .map(user => ({
        id: user.roleId,
        name: user.realName || user.real_name || user.username
      }))
  } catch (error) {
    uni.showToast({ title: '获取教师列表失败: ' + error.message, icon: 'error' })
  }
}

// 搜索教学班
const handleSearch = () => {
  currentPage.value = 1
  fetchClasses()
}

// 过滤教学班列表
const filteredClasses = computed(() => {
  return classes.value.filter(cls => {
    const query = searchQuery.value.toLowerCase()
    return (
      cls.classCode.toLowerCase().includes(query) ||
      cls.courseName.toLowerCase().includes(query) ||
      cls.teacherName.toLowerCase().includes(query)
    )
  })
})

// 编辑教学班
const handleEdit = (cls) => {
  isEditing.value = true
  classForm.value = {
    id: cls.id,
    classCode: cls.classCode,
    maxStudents: cls.maxStudents,
    courseId: cls.courseId,
    teacherId: cls.teacherId
  }
  showAddDialog.value = true
}

// 删除教学班
const handleDelete = (id) => {
  uni.showModal({
    title: '警告',
    content: '确定删除此教学班吗？',
    success: async (res) => {
      if (res.confirm) {
        try {
          await deleteTeachingClass(id)
          uni.showToast({ title: '删除成功', icon: 'success' })
          fetchClasses()
        } catch (error) {
          uni.showToast({ title: '删除失败: ' + (error.response?.data?.message || error.message), icon: 'error' })
        }
      }
    }
  })
}

// 提交表单
const submitClassForm = async () => {
  try {
    await classFormRef.value.validate()
    
    if (isEditing.value) {
      // 更新教学班
      await updateTeachingClass(classForm.value.id, {
        maxStudents: classForm.value.maxStudents,
        teacherId: classForm.value.teacherId
      })
      uni.showToast({ title: '教学班更新成功', icon: 'success' })
    } else {
      console.log('提交的教师ID:', classForm.value.teacherId)
      console.log('所有教师:', teachers.value)
      // 创建教学班
      const course = courses.value.find(c => c.id === classForm.value.courseId)
      if (!course) {
        throw new Error('选择的课程不存在')
      }
      
      await createTeachingClass(classForm.value.courseId, {
        classCode: classForm.value.classCode,
        maxStudents: classForm.value.maxStudents,
        teacherId: classForm.value.teacherId
      })
      uni.showToast({ title: '教学班添加成功', icon: 'success' })
    }
    
    showAddDialog.value = false
    fetchClasses()
  } catch (error) {
    if (error.name !== 'ValidationError') {
      uni.showToast({ title: '操作失败: ' + (error.response?.data?.message || error.message), icon: 'error' })
    }
  }
}

// 重置表单
const resetForm = () => {
  classForm.value = {
    id: null,
    classCode: '',
    maxStudents: 50,
    courseId: null,
    teacherId: userRole.value === 'teacher' ? currentTeacherId.value : null // 仅教师默认自身
  }
  isEditing.value = false
  classFormRef.value?.resetFields()
}

// 初始化加载
onMounted(async () => {
  // 对角色进行判断：只有教师角色才必须有 teacherId
  if (userRole.value === 'teacher' && !currentTeacherId.value) {
    uni.showToast({ title: '无法获取当前教师信息', icon: 'error' })
    return
  }

  try {
    loading.value = true
    // 并行加载教师和课程数据
    await Promise.all([fetchTeachers(), fetchCourses()])
    // 然后再加载教学班数据
    await fetchClasses()
  } catch (error) {
    uni.showToast({ title: '初始化数据失败: ' + error.message, icon: 'error' })
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.teaching-classes-management {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.action-bar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
.table-container {
  overflow-x: auto;
}
@media (max-width: 768px) {
  .action-bar {
    flex-direction: column;
    gap: 12px;
  }
  .action-bar :deep(.el-input) {
    width: 100% !important;
  }
  .action-bar :deep(.el-button) {
    width: 100%;
  }
}

/* 追加移动端卡片样式 */
.class-card {
  margin-bottom: 12px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-details p {
  margin: 4px 0;
  font-size: 14px;
  color: #555;
}
.card-actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}
=======
<template>
  <div class="teaching-classes-management">
    <el-page-header :icon="ArrowLeftBold" title="" @back="$router.go(-1)">
      <template #content>
        <div class="flex items-center">
          <span class="text-large font-600 mr-3">教学班管理</span>
          <el-tag type="warning">教师工作台</el-tag>
        </div>
      </template>
    </el-page-header>

    <el-divider />

    <!-- 操作工具栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon> 添加教学班
      </el-button>
      
      <el-input
        v-model="searchQuery"
        placeholder="搜索教学班代码"
        style="width: 300px"
        clearable
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button :icon="Search" @click="handleSearch" />
        </template>
      </el-input>
    </div>

    <!-- PC 端表格视图 -->
    <div class="table-container" v-if="!isMobile">
      <el-table
        :data="filteredClasses"
        border
        stripe
        v-loading="loading"
        style="width: 100%"
      >
        <el-table-column prop="classCode" label="教学班代码" width="150" />
        <el-table-column prop="courseName" label="所属课程" />
        <el-table-column prop="teacherName" label="授课教师" />
        <el-table-column prop="maxStudents" label="最大人数" width="100" align="center" />
        <el-table-column prop="currentStudents" label="当前人数" width="100" align="center" />
        <el-table-column label="操作" width="180" align="center">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDelete(scope.row.id)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 移动端卡片视图 -->
    <div v-else>
      <el-card
        v-for="cls in filteredClasses"
        :key="cls.id"
        class="class-card"
        @click="toggleDetails(cls.id)"
      >
        <template #header>
          <div class="card-header">
            <span>{{ cls.classCode }}</span>
            <el-tag size="small" type="info">{{ cls.courseName }}</el-tag>
          </div>
        </template>
        <div v-if="expandedId === cls.id" class="card-details">
          <p>授课教师：{{ cls.teacherName }}</p>
          <p>最大人数：{{ cls.maxStudents }}</p>
          <p>当前人数：{{ cls.currentStudents }}</p>
          <div class="card-actions">
            <el-button size="small" @click.stop="handleEdit(cls)">编辑</el-button>
            <el-button size="small" type="danger" @click.stop="handleDelete(cls.id)">删除</el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 分页控件 -->
    <div class="pagination">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :total="totalClasses"
        layout="total, prev, pager, next"
        @current-change="fetchClasses"
      />
    </div>

    <!-- 添加/编辑教学班对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="isEditing ? '编辑教学班' : '添加教学班'"
      width="600px"
      @closed="resetForm"
    >
      <el-form
        ref="classFormRef"
        :model="classForm"
        :rules="classRules"
        label-width="120px"
      >
        <el-form-item label="所属课程" prop="courseId" v-if="!isEditing">
          <el-select
            v-model="classForm.courseId"
            placeholder="请选择课程"
            style="width: 100%"
            filterable
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
        
        <el-form-item label="授课教师" prop="teacherId" v-if="false">
          <!-- 隐藏教师选择器，因为只能是当前教师 -->
          <el-select
            v-model="classForm.teacherId"
            placeholder="请选择授课教师"
            style="width: 100%"
            filterable
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
import { ref, computed, onMounted } from 'vue'
import { Search, Plus, ArrowLeftBold } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getTeachingClasses, 
  createTeachingClass, 
  updateTeachingClass, 
  deleteTeachingClass 
} from '@/api/teacher'
import { getCourses} from '@/api/teacher'
import { getUsers } from '@/api/admin'
import { useAuthStore } from '@/stores/auth'

// 认证和用户信息
const authStore = useAuthStore()
const userRole = computed(() => (authStore.user?.role || '').toLowerCase())
const currentTeacherId = computed(() => authStore.user?.roleId)

// 教学班数据
const classes = ref([])
const courses = ref([])
const teachers = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalClasses = ref(0)
const searchQuery = ref('')

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
  classCode: [{ required: true, message: '请输入教学班代码', trigger: 'blur' }],
  maxStudents: [
    { required: true, message: '请设置最大学生数', trigger: 'blur' },
    { type: 'number', min: 1, max: 200, message: '人数应在1-200之间', trigger: 'blur' }
  ]
}

// 响应式判断是否为移动端
const isMobile = ref(window.innerWidth < 768)
window.addEventListener('resize', () => {
  isMobile.value = window.innerWidth < 768
})

const expandedId = ref(null)
const toggleDetails = (id) => {
  expandedId.value = expandedId.value === id ? null : id
}

// 获取教学班列表
const fetchClasses = async () => {
  try {
    loading.value = true
    const response = await getTeachingClasses({
      page: currentPage.value,
      size: pageSize.value,
      query: searchQuery.value
    })
    let data = response.data
    // 如果当前用户是教师，只展示自己的教学班；管理员则查看全部
    if (userRole.value === 'teacher') {
      data = data.filter(item => item.teacherId === currentTeacherId.value)
    }

    classes.value = data.map(item => ({
      ...item,
      courseName: courses.value.find(c => Number(c.id) === Number(item.courseId))?.name || '未知课程',
      teacherName: item.teacherName || teachers.value.find(t => Number(t.id) === Number(item.teacherId))?.name || '未知教师'
    }))

    totalClasses.value = data.length
  } catch (error) {
    ElMessage.error('获取教学班列表失败: ' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
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
    const response = await getUsers({ page: 0, size: 1000 })
    teachers.value = response.data.content
      .filter(user => user.role === 'teacher')
      .map(user => ({
        id: user.roleId,
        name: user.realName || user.real_name || user.username
      }))
  } catch (error) {
    ElMessage.error('获取教师列表失败: ' + error.message)
  }
}

// 搜索教学班
const handleSearch = () => {
  currentPage.value = 1
  fetchClasses()
}

// 过滤教学班列表
const filteredClasses = computed(() => {
  return classes.value.filter(cls => {
    const query = searchQuery.value.toLowerCase()
    return (
      cls.classCode.toLowerCase().includes(query) ||
      cls.courseName.toLowerCase().includes(query) ||
      cls.teacherName.toLowerCase().includes(query)
    )
  })
})

// 编辑教学班
const handleEdit = (cls) => {
  isEditing.value = true
  classForm.value = {
    id: cls.id,
    classCode: cls.classCode,
    maxStudents: cls.maxStudents,
    courseId: cls.courseId,
    teacherId: cls.teacherId
  }
  showAddDialog.value = true
}

// 删除教学班
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除此教学班吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteTeachingClass(id)
    ElMessage.success('删除成功')
    fetchClasses()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + (error.response?.data?.message || error.message))
    }
  }
}

// 提交表单
const submitClassForm = async () => {
  try {
    await classFormRef.value.validate()
    
    if (isEditing.value) {
      // 更新教学班
      await updateTeachingClass(classForm.value.id, {
        maxStudents: classForm.value.maxStudents,
        teacherId: classForm.value.teacherId
      })
      ElMessage.success('教学班更新成功')
    } else {
      console.log('提交的教师ID:', classForm.value.teacherId)
      console.log('所有教师:', teachers.value)
      // 创建教学班
      const course = courses.value.find(c => c.id === classForm.value.courseId)
      if (!course) {
        throw new Error('选择的课程不存在')
      }
      
      await createTeachingClass(classForm.value.courseId, {
        classCode: classForm.value.classCode,
        maxStudents: classForm.value.maxStudents,
        teacherId: classForm.value.teacherId
      })
      ElMessage.success('教学班添加成功')
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
  classForm.value = {
    id: null,
    classCode: '',
    maxStudents: 50,
    courseId: null,
    teacherId: userRole.value === 'teacher' ? currentTeacherId.value : null // 仅教师默认自身
  }
  isEditing.value = false
  classFormRef.value?.resetFields()
}

// 初始化加载
onMounted(async () => {
  // 对角色进行判断：只有教师角色才必须有 teacherId
  if (userRole.value === 'teacher' && !currentTeacherId.value) {
    ElMessage.error('无法获取当前教师信息')
    return
  }

  try {
    loading.value = true
    // 并行加载教师和课程数据
    await Promise.all([fetchTeachers(), fetchCourses()])
    // 然后再加载教学班数据
    await fetchClasses()
  } catch (error) {
    ElMessage.error('初始化数据失败: ' + error.message)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.teaching-classes-management {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
.action-bar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}
.table-container {
  overflow-x: auto;
}
@media (max-width: 768px) {
  .action-bar {
    flex-direction: column;
    gap: 12px;
  }
  .action-bar :deep(.el-input) {
    width: 100% !important;
  }
  .action-bar :deep(.el-button) {
    width: 100%;
  }
}

/* 追加移动端卡片样式 */
.class-card {
  margin-bottom: 12px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-details p {
  margin: 4px 0;
  font-size: 14px;
  color: #555;
}
.card-actions {
  margin-top: 8px;
  display: flex;
  gap: 8px;
}
>>>>>>> Stashed changes
</style>