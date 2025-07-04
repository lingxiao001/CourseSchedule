<template>
  <div class="auto-schedule-page">
    <el-page-header :icon="ArrowLeftBold" title="" @back="$router.go(-1)">
      <template #content>
        <span class="text-large font-600">自动排课</span>
      </template>
    </el-page-header>

    <el-divider />

    <el-form :model="form" label-width="120px" class="schedule-form">
      <el-form-item label="教学班" prop="teachingClassId">
        <el-select v-model="form.teachingClassId" placeholder="选择教学班" filterable style="width: 300px">
          <el-option v-for="cls in teachingClasses" :key="cls.id" :label="cls.classCode + ' - ' + cls.courseName" :value="cls.id" />
        </el-select>
      </el-form-item>

      <el-form-item label="每周节数" prop="lessonsPerWeek">
        <el-input-number v-model="form.lessonsPerWeek" :min="1" :max="10" />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleAutoSchedule" :loading="loading">开始排课</el-button>
        <el-button type="success" @click="goConfig" style="margin-left:10px;">课表配置</el-button>
      </el-form-item>
    </el-form>

    <el-divider content-position="left">排课结果</el-divider>

    <el-table v-if="schedules.length" :data="schedules" border style="width:100%" @row-click="handleRowClick">
      <el-table-column prop="dayOfWeek" label="星期" width="80">
        <template #default="{ row }">{{ dayLabel(row.dayOfWeek) }}</template>
      </el-table-column>
      <el-table-column label="时间" width="160">
        <template #default="{ row }">{{ row.startTime }} - {{ row.endTime }}</template>
      </el-table-column>
      <el-table-column label="教室">
        <template #default="{ row }">{{ row.classroom.building }}-{{ row.classroom.classroomName }}</template>
      </el-table-column>
    </el-table>

    <el-empty v-else description="暂无结果" />

    <el-dialog v-model="conflictDialog" title="冲突详情" width="500px">
      <ul>
        <li v-for="c in conflictList" :key="c.type + c.id">{{ c.message }}</li>
      </ul>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeftBold } from '@element-plus/icons-vue'
import axios from 'axios'
import { getTeachingClasses } from '@/api/teacher'
import { useRouter } from 'vue-router'
import { scheduleApi } from '@/api/schedule'

const teachingClasses = ref([])
const loading = ref(false)
const schedules = ref([])
const router = useRouter()

const conflictDialog = ref(false)
const conflictList = ref([])

const form = ref({
  teachingClassId: null,
  lessonsPerWeek: 2
})

onMounted(async () => {
  await fetchTeachingClasses()
})

const fetchTeachingClasses = async () => {
  try {
    const { data } = await getTeachingClasses({ pageSize: 1000 })
    teachingClasses.value = data
  } catch (err) {
    ElMessage.error('获取教学班列表失败')
  }
}

const handleAutoSchedule = async () => {
  if (!form.value.teachingClassId) {
    ElMessage.warning('请选择教学班')
    return
  }
  loading.value = true
  schedules.value = []
  try {
    const res = await axios.post('http://localhost:8080/api/intelligent-scheduling/rule-based-batch', null, {
      params: {
        teachingClassId: form.value.teachingClassId,
        lessonsPerWeek: form.value.lessonsPerWeek
      }
    })
    schedules.value = res.data
    ElMessage.success('排课完成')
  } catch (err) {
    const msg = err.response?.data?.message || '排课失败'
    ElMessageBox.alert(msg, '错误', { type: 'error' })
  } finally {
    loading.value = false
  }
}

const dayLabel = (d) => ['一','二','三','四','五','六','日'][d-1]

const goConfig = () => {
  router.push('/admin/schedule-config')
}

const handleRowClick = async (row) => {
  try {
    const { data } = await scheduleApi.detectConflicts({
      teachingClassId: form.value.teachingClassId,
      dayOfWeek: row.dayOfWeek,
      startTime: row.startTime,
      endTime: row.endTime,
      classroomId: row.classroom.id
    })
    conflictList.value = data
    conflictDialog.value = true
  } catch (err) {
    ElMessage.error('检测冲突失败')
  }
}
</script>

<style scoped>
.auto-schedule-page{padding:1rem;max-width:800px;margin:0 auto;}
.schedule-form{max-width:600px;}
</style> 