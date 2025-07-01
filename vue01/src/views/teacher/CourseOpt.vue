<template>
  <div class="teacher-course-management">
    <el-page-header :icon="null" @back="$router.go(-1)">
      <template #content>
        <div class="flex items-center">
          <span class="text-large font-600 mr-3">课程管理</span>
          <el-tag type="warning">教师工作台</el-tag>
        </div>
      </template>
    </el-page-header>

    <el-divider />

    <!-- 操作工具栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon> 添加课程
      </el-button>
      
      <el-input
        v-model="searchQuery"
        placeholder="搜索课程名称/代码"
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

    <!-- 课程表格 -->
    <el-table
      :data="filteredCourses"
      border
      stripe
      v-loading="loading"
      style="width: 100%"
    >
      <el-table-column prop="classCode" label="课程代码" width="120" />
      <el-table-column prop="name" label="课程名称" />
      <el-table-column prop="credit" label="学分" width="80" align="center" />
      <el-table-column prop="hours" label="课时" width="80" align="center" />
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

    <!-- 分页控件 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="totalCourses"
        layout="total, prev, pager, next"
        @current-change="fetchCourses"
      />
    </div>

    <!-- 添加/编辑课程对话框 -->
    <el-dialog
      v-model="showAddDialog"
      :title="isEditing ? '编辑课程' : '添加课程'"
      width="600px"
      @closed="resetForm"
    >
      <el-form
        ref="courseFormRef"
        :model="courseForm"
        :rules="courseRules"
        label-width="100px"
      >
        <el-form-item label="课程代码" prop="classCode">
          <el-input v-model="courseForm.classCode" />
        </el-form-item>
        
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="courseForm.name" />
        </el-form-item>
        
        <el-form-item label="学分" prop="credit">
          <el-input-number
            v-model="courseForm.credit"
            :min="0"
            :step="0.5"
            controls-position="right"
          />
        </el-form-item>
        
        <el-form-item label="课时" prop="hours">
          <el-input-number
            v-model="courseForm.hours"
            :min="1"
            controls-position="right"
          />
        </el-form-item>
        
        <el-form-item label="课程描述" prop="description">
          <el-input
            v-model="courseForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入课程描述"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="submitCourseForm">
          {{ isEditing ? '更新' : '添加' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createCourse, updateCourse, deleteCourse, getCourses } from '@/api/teacher'

// 课程数据
const courses = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalCourses = ref(0)
const searchQuery = ref('')

// 表单相关
const showAddDialog = ref(false)
const isEditing = ref(false)
const courseForm = ref({
  id: null,
  classCode: '',
  name: '',
  credit: 1,
  hours: 32,
  description: ''
})
const courseFormRef = ref()
const courseRules = {
  classCode: [{ required: true, message: '请输入课程代码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  credit: [{ required: true, message: '请设置学分', trigger: 'blur' }],
  hours: [{ required: true, message: '请设置课时', trigger: 'blur' }],
  description: [{ required: true, message: '请输入课程描述', trigger: 'blur' }]
}

// 获取课程列表
const fetchCourses = async () => {
  try {
    loading.value = true
    const response = await getCourses({
      page: currentPage.value,
      size: pageSize.value,
      query: searchQuery.value
    })
    courses.value = response.data
    totalCourses.value = response.total
  } catch (error) {
    ElMessage.error('获取课程列表失败: ' + (error.response?.data?.message || error.message))
    console.error('完整错误:', error)
  } finally {
    loading.value = false
  }
}

// 搜索课程
const handleSearch = () => {
  currentPage.value = 1
  fetchCourses()
}

// 过滤课程列表
const filteredCourses = computed(() => {
  return courses.value.filter(course => {
    const query = searchQuery.value.toLowerCase()
    return (
      course.name.toLowerCase().includes(query) ||
      course.classCode.toLowerCase().includes(query)
    )
  })
})

// 编辑课程
const handleEdit = (course) => {
  isEditing.value = true
  courseForm.value = {
    id: course.id,
    classCode: course.classCode,
    name: course.name,
    credit: course.credit,
    hours: course.hours,
    description: course.description || '' // 确保description有值
  }
  showAddDialog.value = true
}

// 删除课程
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除此课程吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteCourse(id)
    ElMessage.success('删除成功')
    fetchCourses()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + (error.response?.data?.message || error.message))
    }
  }
}

// 提交表单
const submitCourseForm = async () => {
  try {
    // 验证表单
    await courseFormRef.value.validate()
    
    const formData = {
      classCode: courseForm.value.classCode,
      name: courseForm.value.name,
      credit: courseForm.value.credit,
      hours: courseForm.value.hours,
      description: courseForm.value.description
    }
    
    if (isEditing.value) {
      await updateCourse(courseForm.value.id, formData)
      ElMessage.success('课程更新成功')
    } else {
      await createCourse(formData)
      ElMessage.success('课程添加成功')
    }
    
    showAddDialog.value = false
    fetchCourses()
  } catch (error) {
    if (error.name !== 'ValidationError') {
      ElMessage.error('操作失败: ' + (error.response?.data?.message || error.message))
    }
  }
}

// 重置表单
const resetForm = () => {
  courseForm.value = {
    id: null,
    classCode: '',
    name: '',
    credit: 1,
    hours: 32,
    description: ''
  }
  isEditing.value = false
  courseFormRef.value?.resetFields()
}

// 初始化加载
onMounted(() => {
  fetchCourses()
})
</script>

<style scoped>
.teacher-course-management {
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
</style>