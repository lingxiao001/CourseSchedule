<template>
  <view class="teacher-course-management">
    <u-navbar :icon="null" @back="$router.go(-1)">
      <template #content>
        <view class="flex items-center">
          <text class="text-large font-600 mr-3">课程管理</text>
          <u-tag type="warning">教师工作台</u-tag>
        </view>
      </template>
    </u-navbar>

    <u-line />

    <!-- 操作工具栏 -->
    <view class="action-bar">
      <u-button type="primary" @click="showAddDialog = true">
        <u-icon><Plus /></u-icon> 添加课程
      </u-button>
      
      <u-input
        v-model="searchQuery"
        placeholder="搜索课程名称/代码"
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

    <!-- 课程表格 -->
    <u-table
      :data="filteredCourses"
      :border="true"
      :stripe="true"
      :loading="loading"
      style="width: 100%"
    >
      <u-table-column prop="classCode" label="课程代码" width="120" />
      <u-table-column prop="name" label="课程名称" />
      <u-table-column prop="credit" label="学分" width="80" align="center" />
      <u-table-column prop="hours" label="课时" width="80" align="center" />
      <u-table-column label="操作" width="180" align="center">
        <template #default="{ row }">
          <u-button size="mini" @click="handleEdit(scope.row)">编辑</u-button>
          <u-button
            size="mini"
            type="error"
            @click="handleDelete(scope.row.id)"
          >删除</u-button>
        </template>
      </u-table-column>
    </u-table>

    <!-- 分页控件 -->
    <view class="pagination">
      <u-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="totalCourses"
        layout="total, prev, pager, next"
        @current-change="fetchCourses"
      />
    </view>

    <!-- 添加/编辑课程对话框 -->
    <u-popup
      v-model="showAddDialog"
      :title="isEditing ? '编辑课程' : '添加课程'"
      :width="600"
      @close="resetForm"
    >
      <u-form
        ref="courseFormRef"
        :model="courseForm"
        :rules="courseRules"
        label-width="100px"
      >
        <u-form-item label="课程代码" prop="classCode">
          <u-input v-model="courseForm.classCode" />
        </u-form-item>
        
        <u-form-item label="课程名称" prop="name">
          <u-input v-model="courseForm.name" />
        </u-form-item>
        
        <u-form-item label="学分" prop="credit">
          <u-input-number
            v-model="courseForm.credit"
            :min="0"
            :step="0.5"
            controls-position="right"
          />
        </u-form-item>
        
        <u-form-item label="课时" prop="hours">
          <u-input-number
            v-model="courseForm.hours"
            :min="1"
            controls-position="right"
          />
        </u-form-item>
        
        <u-form-item label="课程描述" prop="description">
          <u-input
            v-model="courseForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入课程描述"
          />
        </u-form-item>
      </u-form>
      
      <template #footer>
        <u-button @click="showAddDialog = false">取消</u-button>
        <u-button type="primary" @click="submitCourseForm">
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
    window.uni.showToast({ title: '$1', icon: 'error' })('获取课程列表失败: ' + (error.response?.data?.message || error.message))
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
    await window.uni.showModal({ title: '$1', content: '$2', success: (res) => { if (res.confirm) { $3 } } })('确定删除此课程吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteCourse(id)
    window.uni.showToast({ title: '$1', icon: 'success' })('删除成功')
    fetchCourses()
  } catch (error) {
    if (error !== 'cancel') {
      window.uni.showToast({ title: '$1', icon: 'error' })('删除失败: ' + (error.response?.data?.message || error.message))
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
      window.uni.showToast({ title: '$1', icon: 'success' })('课程更新成功')
    } else {
      await createCourse(formData)
      window.uni.showToast({ title: '$1', icon: 'success' })('课程添加成功')
    }
    
    showAddDialog.value = false
    fetchCourses()
  } catch (error) {
    if (error.name !== 'ValidationError') {
      window.uni.showToast({ title: '$1', icon: 'error' })('操作失败: ' + (error.response?.data?.message || error.message))
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