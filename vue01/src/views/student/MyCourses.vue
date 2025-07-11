<template>
  <view class="my-courses-container">
    <view class="header">
      <u-icon @click="goBack"><ArrowLeftBold /></u-icon>
      <text1>已选课程</text1>
      <text></text>
    </view>
    <view v-if="loading" class="loading-container">
      <u-skeleton :rows="5" animated />
    </view>
    <view v-else-if="courses.length === 0" class="empty-state">
      <u-empty description="您还没有选择任何课程"></u-empty>
    </view>
    <view v-else class="course-list">
      <u-card v-for="course in courses" :key="course.selectionId" class="course-card">
        <view class="card-content">
          <view class="course-details">
            <text3 class="course-name">{{ course.courseName }}</text3>
            <text class="teacher-name">授课教师: {{ course.teacherName }}</text>
            <text class="credits">学分: {{ course.credits }}</text>
          </view>
          <view class="course-actions">
            <u-button type="error" plain size="mini" @click="confirmDropCourse(course)">退课</u-button>
          </view>
        </view>
      </u-card>
    </view>
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






import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { getSelectionsByStudentWithTeachers, cancelSelection } from '@/api/student';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();
const courses = ref([]);
const loading = ref(true);
const studentId = authStore.user?.roleId;

const goBack = () => {
  router.back();
};

const fetchSelectedCourses = async () => {
  if (!studentId) {
    window.uni.showToast({ title: '$1', icon: 'error' })('无法获取学生信息，请重新登录');
    loading.value = false;
    return;
  }
  loading.value = true;
  try {
    const response = await getSelectionsByStudentWithTeachers(studentId);
    courses.value = response;
  } catch (error) {
    window.uni.showToast({ title: '$1', icon: 'error' })('获取已选课程失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const confirmDropCourse = (course) => {
  window.uni.showModal({ title: '$1', content: '$2', success: (res) => { if (res.confirm) { $3 } } })(
    `您确定要退选《${course.courseName}》这门课程吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    handleDropCourse(course);
  }).catch(() => {
    // 用户取消操作
  });
};

const handleDropCourse = async (course) => {
    try {
        await cancelSelection(studentId, course.teachingClassId); 
        window.uni.showToast({ title: '$1', icon: 'success' })('退课成功');
        // 重新加载课程列表
        fetchSelectedCourses();
    } catch (error) {
        window.uni.showToast({ title: '$1', icon: 'error' })('退课失败: ' + (error.response?.data || error.message));
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
  :border="true": 1px solid #e0e6ed;
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