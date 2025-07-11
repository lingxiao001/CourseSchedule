<template>
  <view class="course-management-container">
    <view class="header">
      <u-button type="default" @click="$router.back()" :icon="'arrow-left'" circle></u-button>
      <text1 class="title">课程管理</text>
      <u-button type="primary" @click="openCourseDialog()" :icon="'plus'">添加课程</u-button>
    </view>

    <u-card class="table-card" shadow="never">
      <u-table :data="courses" :loading="loading" :stripe="true" height="calc(100vh - 200px)">
        <u-table-column prop="id" label="ID" width="80" align="center"></u-table-column>
        <u-table-column prop="courseName" label="课程名称" width="200"></u-table-column>
        <u-table-column prop="classCode" label="课程代码" width="120"></u-table-column>
        <u-table-column prop="description" label="课程描述" show-overflow-tooltip></u-table-column>
        <u-table-column prop="credit" label="学分" width="100" align="center"></u-table-column>
        <u-table-column prop="hours" label="学时" width="100" align="center"></u-table-column>
        <u-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <u-button size="mini" type="primary" plain @click="openCourseDialog(scope.row)" :icon="'edit-pen'">编辑</u-button>
            <u-button size="mini" type="error" plain @click="confirmDelete(scope.row.id)" :icon="'trash'">删除</u-button>
          </template>
        </u-table-column>
      </u-table>
    </u-card>

    <!-- 添加/编辑课程对话框 -->
    <u-popup v-model="dialogVisible" :title="dialogTitle" width="500px" @close="resetForm">
      <u-form :model="courseForm" :rules="rules" ref="courseFormRef" label-width="100px">
        <u-form-item label="课程名称" prop="courseName">
          <u-input v-model="courseForm.courseName" placeholder="请输入课程名称"></u-input>
        </u-form-item>
        <u-form-item label="课程代码" prop="classCode">
          <u-input v-model="courseForm.classCode" placeholder="例如：CS101"></u-input>
        </u-form-item>
        <u-form-item label="课程描述" prop="description">
          <u-input v-model="courseForm.description" type="textarea" placeholder="请输入课程描述"></u-input>
        </u-form-item>
        <u-form-item label="学分" prop="credit">
          <u-number-box v-model="courseForm.credit" :precision="1" :step="0.5" :min="0"></u-number-box>
        </u-form-item>
        <u-form-item label="学时" prop="hours">
          <u-number-box v-model="courseForm.hours" :step="8" :min="0"></u-number-box>
        </u-form-item>
      </u-form>
      <template #footer>
        <u-button @click="dialogVisible = false">取消</u-button>
        <u-button type="primary" @click="submitForm">确定</u-button>
      </template>
    </u-popup>
  </view>
</template>

<script setup>

// #ifdef H5
const uni = window.uni || {
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
// #endif

// #ifndef H5
const uni = {
  showToast: (options) => {
    console.log(options.title);
  },
  showModal: (options) => {
    const result = confirm(options.content || options.title);
    if (options.success) {
      options.success({ confirm: result });
    }
  },
  navigateTo: (options) => {
    console.log('Navigate to:', options.url);
  },
  navigateBack: () => {
    console.log('Navigate back');
  },
  redirectTo: (options) => {
    console.log('Redirect to:', options.url);
  },
  reLaunch: (options) => {
    console.log('ReLaunch to:', options.url);
  }
};
// #endif

import { ref, reactive, onMounted, computed } from 'vue';
import { getAllCourses, createCourse, updateCourse, deleteCourse } from '@/api/admin';

const courses = ref([]);
const loading = ref(false);
const dialogVisible = ref(false);
const courseFormRef = ref(null);
const isEdit = ref(false);

const initialFormState = {
  id: null,
  courseName: '',
  classCode: '',
  description: '',
  credit: 1.0,
  hours: 16,
};
const courseForm = reactive({ ...initialFormState });

const rules = {
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  classCode: [{ required: true, message: '请输入课程代码', trigger: 'blur' }],
  credit: [{ required: true, message: '请输入学分', trigger: 'blur' }],
  hours: [{ required: true, message: '请输入学时', trigger: 'blur' }],
};

const dialogTitle = computed(() => (isEdit.value ? '编辑课程' : '添加课程'));

const fetchCourses = async () => {
  loading.value = true;
  try {
    const response = await getAllCourses();
    courses.value = response.data;
  } catch (error) {
    window.uni.showToast({ title: '$1', icon: 'error' })('获取课程列表失败');
  } finally {
    loading.value = false;
  }
};

const openCourseDialog = (course = null) => {
  if (course) {
    isEdit.value = true;
    Object.assign(courseForm, course);
  } else {
    isEdit.value = false;
  }
  dialogVisible.value = true;
};

const resetForm = () => {
  Object.assign(courseForm, initialFormState);
  if (courseFormRef.value) {
    courseFormRef.value.clearValidate();
  }
};

const submitForm = async () => {
  if (!courseFormRef.value) return;
  await courseFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (isEdit.value) {
          await updateCourse(courseForm.id, courseForm);
          window.uni.showToast({ title: '$1', icon: 'success' })('更新成功');
        } else {
          await createCourse(courseForm);
          window.uni.showToast({ title: '$1', icon: 'success' })('添加成功');
        }
        dialogVisible.value = false;
        fetchCourses(); // 重新加载数据
      } catch (error) {
        window.uni.showToast({ title: '$1', icon: 'error' })(`操作失败: ${error.message}`);
      }
    }
  });
};

const confirmDelete = (id) => {
  window.uni.showModal({ title: '$1', content: '$2', success: (res) => { if (res.confirm) { $3 } } })('确定要删除这门课程吗？此操作不可恢复。', '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        await deleteCourse(id);
        window.uni.showToast({ title: '$1', icon: 'success' })('删除成功');
        fetchCourses(); // 重新加载数据
      } catch (error) {
        window.uni.showToast({ title: '$1', icon: 'error' })(`删除失败: ${error.message}`);
      }
    })
    .catch(() => { /* 用户取消 */ });
};

onMounted(fetchCourses);
</script>

<style scoped>
.course-management-container {
  padding: 1.5rem;
  background-color: #f7f8fa;
  height: 100vh;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}
.title {
  font-size: 1.8rem;
  font-weight: 600;
  color: #333;
  margin: 0;
}
.table-card {
  :border="true": none;
  border-radius: 1rem;
}
</style> 