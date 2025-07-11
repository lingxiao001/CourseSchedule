<template>
  <view class="teaching-classes-management">
    <!-- 页面头部 -->
    <u-navbar 
      title="教学班管理" 
      :left-icon="'arrow-left'" 
      @left-click="goBack"
    >
      <template #right>
        <u-tag type="warning" text="教师工作台" />
      </template>
    </u-navbar>

    <u-line />

    <!-- 操作工具栏 -->
    <view class="action-bar">
      <u-button type="primary" @click="showAddDialog = true">
        <u-icon name="plus" />
        添加教学班
      </u-button>
      
      <u-input
        v-model="searchQuery"
        placeholder="搜索教学班代码"
        ::clearable="true"="true"
        @clear="handleSearch"
        @confirm="handleSearch"
      >
        <template #suffix>
          <u-icon name="search" @click="handleSearch" />
        </template>
      </u-input>
    </view>

    <!-- PC 端表格视图 -->
    <view class="table-container" v-if="!isMobile">
      <u-table
        :data="filteredClasses"
        ::border="true"="true"
        ::stripe="true"="true"
        :loading="loading"
      >
        <u-table-column prop="classCode" label="教学班代码" width="150" />
        <u-table-column prop="courseName" label="所属课程" />
        <u-table-column prop="teacherName" label="授课教师" />
        <u-table-column prop="maxStudents" label="最大人数" width="100" align="center" />
        <u-table-column prop="currentStudents" label="当前人数" width="100" align="center" />
        <u-table-column label="操作" width="180" align="center">
          <template #default="{ row }">
            <u-button size="mini" @click="handleEdit(row)">编辑</u-button>
            <u-button size="mini" type="error" @click="handleDelete(row.id)">删除</u-button>
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
            <u-tag size="mini" type="info" :text="cls.courseName" />
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
        :current="currentPage"
        :page-size="pageSize"
        :total="totalClasses"
        @change="fetchClasses"
      />
    </view>

    <!-- 添加/编辑教学班弹窗 -->
    <u-popup
      v-model="showAddDialog"
      mode="center"
      :width="600"
      @close="resetForm"
    >
      <view class="popup-content">
        <view class="popup-header">
          <text>{{ isEditing ? '编辑教学班' : '添加教学班' }}</text>
        </view>
        
        <view class="popup-body">
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
                :filterable="true"
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
                :filterable="true"
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
              <u-number-box
                v-model="classForm.maxStudents"
                :min="1"
                :max="200"
              />
            </u-form-item>
          </u-form>
        </view>
        
        <view class="popup-footer">
          <u-button @click="showAddDialog = false">取消</u-button>
          <u-button type="primary" @click="submitClassForm">
            {{ isEditing ? '更新' : '添加' }}
          </u-button>
        </view>
      </view>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
// 移除 Element Plus 相关导入
// import { Search, Plus, ArrowLeftBold } from '@element-plus/icons-vue'
// import { ElMessage, ElMessageBox } from 'element-plus'
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

// 弹窗和表单相关
const showAddDialog = ref(false)
const isEditing = ref(false)
const classFormRef = ref(null)
const classForm = ref({
  courseId: '',
  teacherId: '',
  classCode: '',
  maxStudents: 50
})

// 表单验证规则
const classRules = {
  courseId: [
    { required: true, message: '请选择所属课程', trigger: 'change' }
  ],
  classCode: [
    { required: true, message: '请输入教学班代码', trigger: 'blur' }
  ],
  maxStudents: [
    { required: true, message: '请输入最大学生数', trigger: 'blur' },
    { type: 'number', min: 1, max: 200, message: '学生数必须在1-200之间', trigger: 'blur' }
  ]
}

// 移动端相关
const isMobile = ref(false)
const expandedId = ref(null)

// 计算属性
const filteredClasses = computed(() => {
  if (!searchQuery.value) return classes.value
  return classes.value.filter(cls => 
    cls.classCode.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

// 方法
const goBack = () => {
  uni.navigateBack()
}

// 替换消息提示
const showSuccess = (message) => {
  uni.showToast({
    title: message,
    icon: 'success'
  })
}

const showError = (message) => {
  uni.showToast({
    title: message,
    icon: 'error'
  })
}

const showConfirm = (title, content) => {
  return new Promise((resolve) => {
    uni.showModal({
      title,
      content,
      success: (res) => {
        resolve(res.confirm)
      }
    })
  })
}

// 获取教学班列表
const fetchClasses = async (page = 1) => {
  try {
    loading.value = true
    const response = await getTeachingClasses({
      page,
      size: pageSize.value,
      teacherId: currentTeacherId.value
    })
    classes.value = response.data.content
    totalClasses.value = response.data.totalElements
    currentPage.value = page
  } catch (error) {
    showError('获取教学班列表失败')
  } finally {
    loading.value = false
  }
}

// 获取课程列表
const fetchCourses = async () => {
  try {
    const response = await getCourses()
    courses.value = response.data
  } catch (error) {
    showError('获取课程列表失败')
  }
}

// 获取教师列表
const fetchTeachers = async () => {
  try {
    const response = await getUsers({ role: 'TEACHER' })
    teachers.value = response.data
  } catch (error) {
    showError('获取教师列表失败')
  }
}

// 搜索处理
const handleSearch = () => {
  // 搜索逻辑已在计算属性中处理
}

// 编辑教学班
const handleEdit = (cls) => {
  isEditing.value = true
  classForm.value = {
    courseId: cls.courseId,
    teacherId: cls.teacherId,
    classCode: cls.classCode,
    maxStudents: cls.maxStudents
  }
  showAddDialog.value = true
}

// 删除教学班
const handleDelete = async (classId) => {
  const confirmed = await showConfirm('确认删除', '确定要删除这个教学班吗？')
  if (!confirmed) return

  try {
    await deleteTeachingClass(classId)
    showSuccess('删除成功')
    fetchClasses(currentPage.value)
  } catch (error) {
    showError('删除失败')
  }
}

// 提交表单
const submitClassForm = async () => {
  try {
    const valid = await classFormRef.value.validate()
    if (!valid) return

    if (isEditing.value) {
      await updateTeachingClass(classForm.value.id, classForm.value)
      showSuccess('更新成功')
    } else {
      await createTeachingClass(classForm.value)
      showSuccess('添加成功')
    }
    
    showAddDialog.value = false
    fetchClasses(currentPage.value)
  } catch (error) {
    showError(isEditing.value ? '更新失败' : '添加失败')
  }
}

// 重置表单
const resetForm = () => {
  isEditing.value = false
  classForm.value = {
    courseId: '',
    teacherId: '',
    classCode: '',
    maxStudents: 50
  }
  classFormRef.value?.resetFields()
}

// 切换详情展开
const toggleDetails = (id) => {
  expandedId.value = expandedId.value === id ? null : id
}

// 检测移动端
const checkMobile = () => {
  // #ifdef H5
  isMobile.value = window.innerWidth <= 768
  // #endif
  // #ifndef H5
  isMobile.value = true
  // #endif
}

// 生命周期
onMounted(() => {
  checkMobile()
  fetchClasses()
  fetchCourses()
  fetchTeachers()
})
</script>

<style scoped>
.teaching-classes-management {
  padding: 20rpx;
}

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20rpx;
  gap: 20rpx;
}

.table-container {
  margin-bottom: 20rpx;
}

.class-card {
  margin-bottom: 20rpx;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-details {
  padding: 20rpx 0;
}

.card-details text {
  display: block;
  margin-bottom: 10rpx;
}

.card-actions {
  margin-top: 20rpx;
  display: flex;
  gap: 20rpx;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20rpx;
}

.popup-content {
  background: #fff;
  :border="true"-radius: 20rpx;
  overflow: hidden;
}

.popup-header {
  padding: 40rpx;
  text-align: center;
  font-size: 32rpx;
  font-weight: bold;
  :border="true"-bottom: 1rpx solid #eee;
}

.popup-body {
  padding: 40rpx;
}

.popup-footer {
  padding: 40rpx;
  display: flex;
  justify-content: flex-end;
  gap: 20rpx;
  :border="true"-top: 1rpx solid #eee;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .action-bar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .action-bar .u-input {
    width: 100%;
  }
}
</style> 