<template>
  <div class="dashboard student-dashboard">
    <h2 class="dashboard-title">学生个人中心</h2>

    <!-- 选课信息 -->
    <el-row :gutter="20" class="dashboard-row">
      <el-col :span="12">
        <el-card class="selected-courses" shadow="hover">
          <template #header>
            <div class="card-header">
              <span class="card-title">已选课程</span>
              <el-button type="primary" size="small" @click="$router.push('/student/SelectCourse')">
                <el-icon><Plus /></el-icon> 前往选课
              </el-button>
            </div>
          </template>
                  <el-table 
          :data="selectedCourses" 
          height="300" 
          v-loading="loading"
          stripe
          border
          style="width: 100%"
        >
          <el-table-column prop="courseName" label="课程名称" width="180" align="center" />
          <el-table-column prop="teachingClassId" label="教学班ID" width="120" align="center" />
          <el-table-column prop="teacherName" label="授课教师" width="120" align="center" />
          <el-table-column prop="selectionTime" label="选课时间" width="180" align="center" />
          <el-table-column label="操作" width="100" align="center">
            <template #default="scope">
              <el-button size="small" type="danger" plain @click="handleCancel(scope.row)">
                <el-icon><Delete /></el-icon> 退选
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        </el-card>
      </el-col>

      <!-- 课表日历 -->
 <el-col :span="12">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="card-title">本周课表</span>
          <el-tag type="info">{{ currentWeek }}</el-tag>
        </div>
      </template>
      <div class="timetable-container">
        <div class="timetable">
          <div class="timetable-header">
            <div class="time-col">时间</div>
            <div 
              v-for="day in ['周一', '周二', '周三', '周四', '周五', '周六', '周日']" 
              :key="day" 
              class="day-col"
            >
              {{ day }}
            </div>
          </div>
          <div class="timetable-body">
            <div 
              v-for="(timeSlot, index) in timeSlots" 
              :key="index" 
              class="time-row"
            >
              <div class="time-col">{{ timeSlot }}</div>
              <div 
                v-for="day in 7" 
                :key="day" 
                class="day-col"
                :class="{
                  'has-course': getCourseAt(day, index + 1),
                }"
                @click="showCourseDetail(day, index + 1)"
              >
                <div v-if="getCourseAt(day, index + 1)" class="course-cell">
                  <div class="course-name">{{ getCourseAt(day, index + 1).courseName }}</div>
                  <div class="course-info">
                    <el-icon><Location /></el-icon>
                    {{ getCourseAt(day, index + 1).building }}{{ getCourseAt(day, index + 1).classroomName }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 课程详情对话框 -->
    <el-dialog 
      v-model="detailDialogVisible" 
      title="课程详情" 
      width="400px"
      center
    >
      <div v-if="currentCourse" class="course-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="教学班ID">
            <el-tag>{{ currentCourse.teachingClassId }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="授课教师">
            <el-tag type="success">{{ currentCourse.teacherName || '未知教师' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="上课时间">
            {{ formatClassTime(currentCourse) }}
          </el-descriptions-item>
          <el-descriptions-item label="上课地点">
            <el-tag type="info">
              {{ currentCourse.building }}{{ currentCourse.classroomName }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button type="primary" @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>


<script setup>
import { onMounted, ref } from 'vue'
import { 
  getSelectionsByStudentWithTeachers, 
  cancelSelection, 
  getStudentSchedules 
} from '@/api/student'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { getCourseNameById } from '@/api/teacher'
import { getCourseIdByTeachingClassId } from '@/api/teacher'
const authStore = useAuthStore()
const studentId = authStore.user?.roleId

const loading = ref(false)
const selectedCourses = ref([])
const courseSchedules = ref([])
const detailDialogVisible = ref(false)
const currentCourse = ref(null)

// 定义时间段的显示文本
const timeSlots = ref([
  "08:00-09:30", 
  "09:50-11:20",
  "13:30-15:00", 
  "15:20-16:50",
  "18:30-20:00"
])

// 课表数据，按天、节次组织
const timetable = ref(Array(7).fill().map(() => Array(5).fill(null)))

// 格式化上课时间显示
const formatClassTime = (course) => {
  if (!course) return ''
  const dayMap = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  return `${dayMap[course.dayOfWeek - 1]} ${course.startTime}-${course.endTime}`
}

// 获取指定位置课程
const getCourseAt = (day, timeSlot) => {
  return timetable.value[day - 1]?.[timeSlot - 1] || null
}

// 显示课程详情
const showCourseDetail = (day, timeSlot) => {
  const course = getCourseAt(day, timeSlot)
  if (course) {
    currentCourse.value = course
    detailDialogVisible.value = true
  }
}

const loadSelectedCourses = async () => {
  loading.value = true;
  try {
    // 1. 获取选课记录
    const selections = await getSelectionsByStudentWithTeachers(studentId);
    
    // 2. 处理选课记录
    selectedCourses.value = await Promise.all(
      selections.map(async (selection) => {
        try {
          const teachingClassId = selection.teachingClassId;
          if (!teachingClassId) {
            console.error('缺少teachingClassId:', selection);
            return { ...selection, courseName: '未知课程(无教学班ID)' };
          }
          
          // 关键修改：通过教学班ID获取课程ID
          const courseId = await getCourseIdByTeachingClassId(teachingClassId);
          const courseName = await getCourseNameById(courseId);
          
          return {
            ...selection,
            courseName,
            courseId // 保留课程ID
          };
        } catch (error) {
          console.error('处理选课记录失败:', error);
          return { ...selection, courseName: `未知课程(${error.message})` };
        }
      })
    );
    
    // 3. 处理课表数据（同样逻辑）
    const schedules = await getStudentSchedules(studentId);
    courseSchedules.value = await Promise.all(
      schedules.map(async (schedule) => {
        try {
          const courseId = await getCourseIdByTeachingClassId(schedule.teachingClassId);
          const courseName = await getCourseNameById(courseId);
          return { ...schedule, courseName };
        } catch (error) {
          console.error('处理课表记录失败:', error);
          return { ...schedule, courseName: `未知课程(${error.message})` };
        }
      })
    );
    
    mapSchedulesToTimetable();
  } catch (error) {
    ElMessage.error('加载数据失败: ' + error.message);
  } finally {
    loading.value = false;
  }
};

// 将课程安排映射到时间表
const mapSchedulesToTimetable = () => {
  timetable.value = Array(7).fill().map(() => Array(5).fill(null));
  
  courseSchedules.value.forEach(schedule => {
    // 确保只处理学生课程
    if (!schedule.isStudentCourse) return;
    
    const timeSlotIndex = timeSlots.value.findIndex(
      slot => slot === `${schedule.startTime}-${schedule.endTime}`
    );
    
    if (timeSlotIndex >= 0 && schedule.dayOfWeek >= 1 && schedule.dayOfWeek <= 7) {
      timetable.value[schedule.dayOfWeek - 1][timeSlotIndex] = schedule;
    }
  });
};


// 退选课程
const handleCancel = async (row) => {
  try {
    await cancelSelection(studentId, row.teachingClassId)
    ElMessage.success('退课成功')
    await loadSelectedCourses()
  } catch (error) {
    ElMessage.error('退课失败：' + (error.response?.data || error.message))
  }
}

onMounted(() => {
  loadSelectedCourses()
})
</script>
<style scoped>
.student-dashboard {
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
  background-color: #409eff;
  margin-right: 10px;
  border-radius: 2px;
}

.dashboard-row {
  margin-bottom: 20px;
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

.selected-courses {
  height: 100%;
  border-radius: 8px;
}

.selected-courses >>> .el-card__header {
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
}

.timetable-container {
  overflow-x: auto;
  padding: 5px;
}

.timetable {
  display: flex;
  flex-direction: column;
  min-width: 800px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}

.timetable-header {
  display: flex;
  background-color: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  font-weight: 500;
}

.timetable-body {
  display: flex;
  flex-direction: column;
}

.time-row {
  display: flex;
  min-height: 90px;
  border-bottom: 1px solid #ebeef5;
}

.time-row:last-child {
  border-bottom: none;
}

.time-col {
  width: 100px;
  padding: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  border-right: 1px solid #ebeef5;
  font-size: 13px;
  color: #606266;
  font-weight: 500;
}

.day-col {
  flex: 1;
  padding: 4px;
  border-right: 1px solid #ebeef5;
  min-height: 90px;
  position: relative;
  transition: all 0.3s;
}

.day-col:last-child {
  border-right: none;
}

.day-col.has-course {
  background-color: #f0f9ff;
  cursor: pointer;
}

.day-col.has-course:hover {
  background-color: #e6f7ff;
}

.day-col.current-course {
  background-color: #e1f3d8;
}

.day-col.current-course:hover {
  background-color: #d1e9c6;
}

.course-cell {
  padding: 8px;
  border-radius: 4px;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #303133;
}

.course-cell {
  padding: 8px;
  border-radius: 4px;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #303133;
  background-color: #f0f9ff;
}

.course-name {
  font-weight: 600;
  font-size: 14px;
  margin-bottom: 6px;
  color: #409eff;
  text-align: center;
}

.course-info {
  font-size: 12px;
  color: #606266;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 2px;
}

.course-info i {
  margin-right: 4px;
  font-size: 14px;
}

.course-detail {
  padding: 10px;
}

.course-detail p {
  margin-bottom: 15px;
  line-height: 1.6;
  display: flex;
  align-items: center;
}

.course-detail strong {
  width: 80px;
  display: inline-block;
  color: #909399;
}
</style>