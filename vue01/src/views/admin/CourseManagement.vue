<template>
  <div class="course-management-container">
    <div class="header">
      <el-button type="default" @click="$router.back()" :icon="ArrowLeftBold" circle></el-button>
      <h1 class="title">课程管理</h1>
      <el-button type="primary" @click="openCourseDialog()" :icon="Plus">添加课程</el-button>
    </div>

    <el-card class="table-card" shadow="never">
      <el-table :data="courses" v-loading="loading" stripe height="calc(100vh - 200px)">
        <el-table-column prop="id" label="ID" width="80" align="center"></el-table-column>
        <el-table-column prop="courseName" label="课程名称" width="200"></el-table-column>
        <el-table-column prop="classCode" label="课程代码" width="120"></el-table-column>
        <el-table-column prop="description" label="课程描述" show-overflow-tooltip></el-table-column>
        <el-table-column prop="credit" label="学分" width="100" align="center"></el-table-column>
        <el-table-column prop="hours" label="学时" width="100" align="center"></el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="scope">
            <el-button size="small" type="primary" plain @click="openCourseDialog(scope.row)" :icon="Edit">编辑</el-button>
            <el-button size="small" type="danger" plain @click="confirmDelete(scope.row.id)" :icon="Delete">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑课程对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" @close="resetForm">
      <el-form :model="courseForm" :rules="rules" ref="courseFormRef" label-width="100px">
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="courseForm.courseName" placeholder="请输入课程名称"></el-input>
        </el-form-item>
        <el-form-item label="课程代码" prop="classCode">
          <el-input v-model="courseForm.classCode" placeholder="例如：CS101"></el-input>
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input v-model="courseForm.description" type="textarea" placeholder="请输入课程描述"></el-input>
        </el-form-item>
        <el-form-item label="学分" prop="credit">
          <el-input-number v-model="courseForm.credit" :precision="1" :step="0.5" :min="0"></el-input-number>
        </el-form-item>
        <el-form-item label="学时" prop="hours">
          <el-input-number v-model="courseForm.hours" :step="8" :min="0"></el-input-number>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowLeftBold, Plus, Edit, Delete } from '@element-plus/icons-vue';
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
    ElMessage.error('获取课程列表失败');
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
          ElMessage.success('更新成功');
        } else {
          await createCourse(courseForm);
          ElMessage.success('添加成功');
        }
        dialogVisible.value = false;
        fetchCourses(); // 重新加载数据
      } catch (error) {
        ElMessage.error(`操作失败: ${error.message}`);
      }
    }
  });
};

const confirmDelete = (id) => {
  ElMessageBox.confirm('确定要删除这门课程吗？此操作不可恢复。', '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(async () => {
      try {
        await deleteCourse(id);
        ElMessage.success('删除成功');
        fetchCourses(); // 重新加载数据
      } catch (error) {
        ElMessage.error(`删除失败: ${error.message}`);
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
  border: none;
  border-radius: 1rem;
}
</style> 