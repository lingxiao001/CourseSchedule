<template>
  <div class="teaching-classes-management">
    <el-page-header :icon="null" @back="$router.go(-1)">
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

    <!-- 教学班表格 -->
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

    <!-- 分页控件 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
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
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getTeachingClasses, 
  createTeachingClass, 
  updateTeachingClass, 
  deleteTeachingClass 
} from '@/api/teacher'
import { getCourses} from '@/api/teacher'
import { getUsers } from '@/api/admin'
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
  teacherId: [{ required: true, message: '请选择授课教师', trigger: 'change' }],
  classCode: [{ required: true, message: '请输入教学班代码', trigger: 'blur' }],
  maxStudents: [
    { required: true, message: '请设置最大学生数', trigger: 'blur' },
    { type: 'number', min: 1, max: 200, message: '人数应在1-200之间', trigger: 'blur' }
  ]
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
    classes.value = response.data.map(item => ({
      ...item,
      courseName: courses.value.find(c => c.id === item.courseId)?.name || '未知课程',
      teacherName: teachers.value.find(t => t.id === item.teacherId)?.name || '未知教师'
    }))
    totalClasses.value = response.total
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
    const response = await getUsers({ page: 1, size: 1000 })
console.log('教师列表:', teachers.value)
console.log('教学班数据:', response.data)
    teachers.value = response.data.content
      .filter(user => user.role === 'teacher')
      .map(user => ({
        id: user.roleId,
        name: user.realName
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
    teacherId: null
  }
  isEditing.value = false
  classFormRef.value?.resetFields()
}

// 初始化加载
onMounted(async () => {
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
</style>