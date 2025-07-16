<<<<<<< Updated upstream
<template>
  <view class="dashboard select-course-dashboard">
    <text2 class="dashboard-title">选课中心</text2>

    <u-card shadow="hover" class="course-card">
      <template #header>
        <view class="card-header">
          <text class="card-title">可选课程列表</text>
          <view>
            <u-button type="default" @click="$router.back()">
              <u-icon><ArrowLeft /></u-icon> 返回
            </u-button>
            <u-button type="primary" @click="refreshCourses">
              <u-icon><Refresh /></u-icon> 刷新
            </u-button>
          </view>
        </view>
      </template>

      <u-table 
        :data="courses" 
        :loading="loading" 
        height="500"
        :stripe="true"
        :border="true"
        style="width: 100%"
        highlight-current-row
      >
        <u-table-column prop="name" label="课程名称" width="180" />
        <u-table-column prop="description" label="课程描述" show-overflow-tooltip />
        <u-table-column prop="credit" label="学分" width="80" align="center" />
        <u-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <u-button
              type="primary"
              size="mini"
              plain
              @click="openDialog(row.id)"
            >
              <u-icon><CirclePlus /></u-icon> 选课
            </u-button>
          </template>
        </u-table-column>
      </u-table>
    </u-card>

    <!-- 选课对话框 -->
    <u-popup 
      v-model="dialogVisible" 
      title="选课确认" 
      width="450px"
      center
    >
      <u-form label-width="120px">
        <u-form-item label="教学班ID" required>
          <u-input 
            v-model="form.teachingClassId" 
            placeholder="请输入教学班 ID"
            :clearable="true"
          />
        </u-form-item>
        <u-form-item label="课程信息">
          <u-text type="info">
            请确认教学班ID是否正确，选课后不可更改
          </u-text>
        </u-form-item>
      </u-form>

      <template #footer>
        <u-button @click="dialogVisible = false">取消</u-button>
        <u-button type="primary" @click="handleSelect">
          <u-icon><Select /></u-icon> 确认选课
        </u-button>
      </template>
    </u-popup>
  </view>
</template>

<script setup>

// 全局 uni 对象定义 - 已移除，使用原生方法替代






import { ref, onMounted } from 'vue'
import { getCourses } from '@/api/teacher'
import {  selectCourse } from '@/api/student'
import { useAuthStore } from '@/stores/auth'
const authStore = useAuthStore()
const studentId = authStore.user?.roleId
// 选课数据
const courses = ref([])
const loading = ref(false)



// 对话框控制
const dialogVisible = ref(false)
const form = ref({
  teachingClassId: '',
  courseId: null,
})

// 打开对话框
const openDialog = (courseId) => {
  form.value.courseId = courseId
  form.value.teachingClassId = ''
  dialogVisible.value = true
}

// 选课请求
const handleSelect = async () => {
  try {
    await selectCourse(studentId, form.value.teachingClassId)
    window.uni.showToast({ title: '$1', icon: 'success' })('选课成功')
    dialogVisible.value = false
    fetchCourses()
  } catch (error) {
    window.uni.showToast({ title: '$1', icon: 'error' })('选课失败：' + (error.response?.data || error.message))
  }
}

const fetchCourses = async () => {
  loading.value = true;
  try {
    const response = await getCourses();
    // 提取 response.data 作为表格数据
    courses.value = Array.isArray(response.data) ? response.data : [];
    // 如果需要分页，可以保留 total
    // total.value = response.total;
  } catch (error) {
    window.uni.showToast({ title: '$1', icon: 'error' })('加载课程失败');
    courses.value = []; // 保证始终是数组
  } finally {
    loading.value = false;
  }
};

onMounted(fetchCourses)
</script>

<style scoped>
.select-course-dashboard {
  padding: 20px;
  background-color: #f5f7fa;
}

.dashboard-title {
  color: #303133;
  margin-bottom: 20px;
  font-size: 24px;
  font-weight: 500;
  display: flex;
  align-items: center;
}

.dashboard-title::before {
  content: "";
  display: inline-block;
  width: 4px;
  height: 20px;
  background-color: #67c23a;
  margin-right: 10px;
  border-radius: 2px;
}

.course-card {
  border-radius: 8px;
}

.course-card >>> .el-card__header {
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.el-table >>> .el-table__header th {
  background-color: #f5f7fa;
  font-weight: 600;
}

.el-table >>> .el-button {
  padding: 5px 10px;
}
</style>
=======
<template>
  <div class="dashboard select-course-dashboard">
    <h2 class="dashboard-title">选课中心</h2>

    <el-card shadow="hover" class="course-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">可选课程列表</span>
          <div>
            <el-button type="default" @click="$router.back()">
              <el-icon><ArrowLeft /></el-icon> 返回
            </el-button>
            <el-button type="primary" @click="refreshCourses">
              <el-icon><Refresh /></el-icon> 刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-table 
        :data="courses" 
        v-loading="loading" 
        height="500"
        stripe
        border
        style="width: 100%"
        highlight-current-row
      >
        <el-table-column prop="name" label="课程名称" width="180" />
        <el-table-column prop="description" label="课程描述" show-overflow-tooltip />
        <el-table-column prop="credit" label="学分" width="80" align="center" />
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              plain
              @click="openDialog(scope.row.id)"
            >
              <el-icon><CirclePlus /></el-icon> 选课
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 选课对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      title="选课确认" 
      width="450px"
      center
    >
      <el-form label-width="120px">
        <el-form-item label="教学班ID" required>
          <el-input 
            v-model="form.teachingClassId" 
            placeholder="请输入教学班 ID"
            clearable
          />
        </el-form-item>
        <el-form-item label="课程信息">
          <el-text type="info">
            请确认教学班ID是否正确，选课后不可更改
          </el-text>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSelect">
          <el-icon><Select /></el-icon> 确认选课
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCourses } from '@/api/teacher'
import {  selectCourse } from '@/api/student'
import { useAuthStore } from '@/stores/auth'
const authStore = useAuthStore()
const studentId = authStore.user?.roleId
// 选课数据
const courses = ref([])
const loading = ref(false)



// 对话框控制
const dialogVisible = ref(false)
const form = ref({
  teachingClassId: '',
  courseId: null,
})

// 打开对话框
const openDialog = (courseId) => {
  form.value.courseId = courseId
  form.value.teachingClassId = ''
  dialogVisible.value = true
}

// 选课请求
const handleSelect = async () => {
  try {
    await selectCourse(studentId, form.value.teachingClassId)
    ElMessage.success('选课成功')
    dialogVisible.value = false
    fetchCourses()
  } catch (error) {
    ElMessage.error('选课失败：' + (error.response?.data || error.message))
  }
}

const fetchCourses = async () => {
  loading.value = true;
  try {
    const response = await getCourses();
    // 提取 response.data 作为表格数据
    courses.value = Array.isArray(response.data) ? response.data : [];
    // 如果需要分页，可以保留 total
    // total.value = response.total;
  } catch (error) {
    ElMessage.error('加载课程失败');
    courses.value = []; // 保证始终是数组
  } finally {
    loading.value = false;
  }
};

onMounted(fetchCourses)
</script>

<style scoped>
.select-course-dashboard {
  padding: 20px;
  background-color: #f5f7fa;
}

.dashboard-title {
  color: #303133;
  margin-bottom: 20px;
  font-size: 24px;
  font-weight: 500;
  display: flex;
  align-items: center;
}

.dashboard-title::before {
  content: "";
  display: inline-block;
  width: 4px;
  height: 20px;
  background-color: #67c23a;
  margin-right: 10px;
  border-radius: 2px;
}

.course-card {
  border-radius: 8px;
}

.course-card >>> .el-card__header {
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.el-table >>> .el-table__header th {
  background-color: #f5f7fa;
  font-weight: 600;
}

.el-table >>> .el-button {
  padding: 5px 10px;
}
</style>
>>>>>>> Stashed changes
