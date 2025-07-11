<template>
  <view class="teaching-classes-mobile">
    <u-navbar :icon="'arrow-left'" title="返回" @back="$router.go(-1)">
      <template #content>
        <view class="flex items-center">
          <text class="text-large font-600 mr-3">教学班管理</text>
          <u-tag type="warning" size="mini">教师工作台</u-tag>
        </view>
      </template>
    </u-navbar>

    <view class="content-area">
      <!-- 操作工具栏 -->
      <view class="action-bar">
        <u-input
          v-model="searchQuery"
          placeholder="搜索教学班"
          :prefix-icon="Search"
          :clearable="true"
          @clear="handleSearch"
          @confirm="handleSearch"
        />
        <u-button type="primary" :icon="'plus'" @click="showAddDialog = true" circle />
      </view>

      <!-- 教学班列表 -->
      <view v-if="loading" class="loading-spinner">
        <u-loading size="large" />
      </view>
      
      <view v-if="!loading && classes.length === 0" class="empty-state">
        <u-empty description="暂无教学班数据" />
      </view>

      <view class="class-list" v-else>
        <u-card v-for="cls in classes" :key="cls.id" class="class-card">
          <view class="card-header">
            <text class="class-code">{{ cls.classCode }}</text>
            <view class="actions">
              <u-button size="mini" type="primary" text @click="handleEdit(cls)">编辑</u-button>
              <u-button size="mini" type="error" text @click="handleDelete(cls.id)">删除</u-button>
            </view>
          </view>
          <view class="card-body">
            <text><strong>课程:</strong> {{ cls.courseName }}</text>
            <text><strong>教师:</strong> {{ cls.teacherName }}</text>
            <text><strong>人数:</strong> {{ cls.currentStudents }} / {{ cls.maxStudents }}</text>
          </view>
        </u-card>

        <view v-if="hasMore" class="load-more">
          <u-button @click="loadMore" :loading="loadingMore">加载更多</u-button>
        </view>
      </view>
    </view>

    <!-- 添加/编辑教学班对话框 -->
    <u-popup
      v-model="showAddDialog"
      :title="isEditing ? '编辑教学班' : '添加教学班'"
      width="95%"
      @close="resetForm"
    >
      <u-form
        ref="classFormRef"
        :model="classForm"
        :rules="classRules"
        label-position="top"
      >
        <u-form-item label="所属课程" prop="courseId" v-if="!isEditing">
          <u-select
            v-model="classForm.courseId"
            placeholder="请选择课程"
            style="width: 100%"
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
            style="width: 100%"
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






import { ref, onMounted, computed } from 'vue'
import { 
  getTeachingClasses, 
  createTeachingClass, 
  updateTeachingClass, 
  deleteTeachingClass 
} from '@/api/teacher'
import { getCourses } from '@/api/teacher'
import { getUsers } from '@/api/admin'
import { useAuthStore } from '@/stores/auth'

// 认证和用户信息
const authStore = useAuthStore()
const currentTeacherId = computed(() => authStore.user?.roleId)

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
    
    // 过滤出当前教师的教学班
    const filteredData = response.data.filter(item => 
      item.teacherId === currentTeacherId.value
    )
    
    const newClasses = mapNamesToClasses(filteredData)
    
    if (isLoadMore) {
      classes.value = [...classes.value, ...newClasses]
    } else {
      classes.value = newClasses
    }
    // 更新总数为过滤后的数量
    totalClasses.value = filteredData.length
  } catch (error) {
    window.uni.showToast({ title: '$1', icon: 'error' })('获取教学班列表失败: ' + (error.response?.data?.message || error.message))
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
    window.uni.showToast({ title: '$1', icon: 'error' })('获取课程列表失败: ' + error.message)
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
    window.uni.showToast({ title: '$1', icon: 'error' })('获取教师列表失败: ' + (error.response?.data?.message || error.message))
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
    await window.uni.showModal({ title: '$1', content: '$2', success: (res) => { if (res.confirm) { $3 } } })('确定删除此教学班吗？此操作不可逆。', '警告', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteTeachingClass(id)
    window.uni.showToast({ title: '$1', icon: 'success' })('删除成功')
    // 重新加载数据
    fetchClasses()
  } catch (error) {
    if (error !== 'cancel') {
      window.uni.showToast({ title: '$1', icon: 'error' })('删除失败: ' + (error.response?.data?.message || error.message))
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
      window.uni.showToast({ title: '$1', icon: 'success' })('更新成功')
    } else {
      await createTeachingClass(classForm.value.courseId, {
        classCode: classForm.value.classCode,
        maxStudents: classForm.value.maxStudents,
        teacherId: classForm.value.teacherId
      })
      window.uni.showToast({ title: '$1', icon: 'success' })('添加成功')
    }
    
    showAddDialog.value = false
    fetchClasses()
  } catch (error) {
    if (error.name !== 'ValidationError') {
       window.uni.showToast({ title: '$1', icon: 'error' })('操作失败: ' + (error.response?.data?.message || error.message))
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
    teacherId: currentTeacherId.value // 默认设置为当前教师
  }
  classFormRef.value?.resetFields()
}

// 初始化加载
onMounted(async () => {
  // 检查当前用户是否为教师
  if (!currentTeacherId.value) {
    window.uni.showToast({ title: '$1', icon: 'error' })('无法获取当前教师信息')
    return
  }
  
  loading.value = true
  try {
    await Promise.all([fetchTeachers(), fetchCourses()])
    await fetchClasses()
  } catch (error) {
    window.uni.showToast({ title: '$1', icon: 'error' })('初始化数据失败: ' + error.message)
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