<template>
  <div class="my-courses-container">
    <div class="header">
      <el-icon @click="goBack"><ArrowLeftBold /></el-icon>
      <h1>已选课程</h1>
      <span></span>
    </div>
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>
    <div v-else-if="courses.length === 0" class="empty-state">
      <el-empty description="您还没有选择任何课程"></el-empty>
    </div>
    <div v-else class="course-list">
      <el-card v-for="course in courses" :key="course.selectionId" class="course-card">
        <div class="card-content">
          <div class="course-details">
            <h3 class="course-name">{{ course.courseName }}</h3>
            <p class="teacher-name">授课教师: {{ course.teacherName }}</p>
            <p class="credits">学分: {{ course.credits }}</p>
          </div>
          <div class="course-actions">
            <el-button type="danger" plain size="small" @click="confirmDropCourse(course.selectionId)">退课</el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowLeftBold } from '@element-plus/icons-vue';
// import { getSelectedCourses, dropCourse } from '@/api/student';

const router = useRouter();
const courses = ref([]);
const loading = ref(true);

const goBack = () => {
  router.back();
};

const fetchSelectedCourses = async () => {
  loading.value = true;
  try {
    // const response = await getSelectedCourses();
    // courses.value = response.data;
    
    // 使用占位数据以便预览UI
    courses.value = [
      { selectionId: 1, courseName: '计算机体系结构', teacherName: '张三', credits: 3 },
      { selectionId: 2, courseName: '操作系统', teacherName: '李四', credits: 4 },
      { selectionId: 3, courseName: '数据结构', teacherName: '王五', credits: 3.5 },
    ];

  } catch (error) {
    ElMessage.error('获取已选课程失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const confirmDropCourse = (selectionId) => {
  ElMessageBox.confirm(
    '您确定要退选这门课程吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    handleDropCourse(selectionId);
  }).catch(() => {
    // 用户取消操作
  });
};

const handleDropCourse = async (selectionId) => {
    try {
        // await dropCourse(selectionId); 
        ElMessage.success('退课成功 (模拟)');
        // 从列表中移除课程以提供即时反馈
        courses.value = courses.value.filter(c => c.selectionId !== selectionId);
    } catch (error) {
        ElMessage.error('退课失败');
        console.error(error);
    }
};


onMounted(() => {
  fetchSelectedCourses();
});
</script>

<style scoped>
.my-courses-container {
  padding: 1.5rem;
  background-color: #f4f7fa;
  min-height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  color: #333;
}

.header .el-icon {
  font-size: 2rem;
  cursor: pointer;
}

.header h1 {
  font-size: 1.8rem;
  font-weight: 600;
  margin: 0;
}

/* 这个span用于辅助h1标题居中 */
.header span {
  width: 2rem; 
}

.loading-container {
  padding: 2rem;
}

.empty-state {
  margin-top: 5rem;
}

.course-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.course-card {
  border-radius: 1rem;
  border: 1px solid #e0e6ed;
}

.course-card .card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.course-details .course-name {
  font-size: 1.6rem;
  font-weight: 600;
  margin: 0 0 0.8rem 0;
  color: #333;
}

.course-details p {
  margin: 0.4rem 0;
  font-size: 1.3rem;
  color: #666;
}

.course-actions .el-button {
  font-size: 1.3rem;
}
</style> 