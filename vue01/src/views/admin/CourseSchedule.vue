<template>
  <view class="schedule-management">
    <h2>课程安排管理</h2>
    
    <!-- 搜索和添加按钮 -->
    <view class="toolbar">
      <u-input
        v-model="search"
        placeholder="搜索课程..."
        :clearable="true"
        style="width: 300px; margin-right: 10px;"
      />
      <u-button type="primary" @click="openAddDialog">
        <i class="el-icon-plus"></i> 添加课程安排
      </u-button>
    </view>

    <!-- 数据表格 -->
    <u-table
      :data="filteredSchedules"
      :border="true"
      :stripe="true"
      :loading="isLoading"
      style="width: 100%; margin-top: 20px;"
    >
      <u-table-column prop="id" label="ID" width="120">
        <template #default="{ row }">
          {{ row.id || '无ID' }}
        </template>
      </u-table-column>

      <u-table-column prop="dayOfWeek" label="星期几" width="100">
        <template #default="{ row }">
          {{ ['日', '一', '二', '三', '四', '五', '六'][row.dayOfWeek] }}
        </template>
      </u-table-column>
      <u-table-column prop="startTime" label="开始时间" width="120" />
      <u-table-column prop="endTime" label="结束时间" width="120" />
      <u-table-column prop="classroomName" label="教室" />
      <u-table-column prop="building" label="教学楼" />
      <u-table-column prop="teachingClassId" label="教学班ID" width="120" />
      <u-table-column label="操作" width="150">
        <template #default="{ row }">
          <u-button size="mini" @click="editItem(row)" type="text">
            <i class="el-icon-edit"></i> 编辑
          </u-button>
          <u-button size="mini" @click="deleteItem(row)" type="text" style="color: #f56c6c;">
            <i class="el-icon-delete"></i> 删除
          </u-button>
        </template>
      </u-table-column>
    </u-table>

    <!-- 弹窗组件 -->
    <ScheduleDialog
      v-model="dialogVisible"
      :form-data="currentItem"
      :is-edit="isEditMode"
      @submit="handleSubmit"
    />
  </view>
</template>

<script>

import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useScheduleStore } from '@/stores/schedule'
import ScheduleDialog from '@/components/ScheduleDialog.vue'

export default {
  components: { ScheduleDialog },
  
  setup() {
    const scheduleStore = useScheduleStore()
    const search = ref('')
    const dialogVisible = ref(false)
    const currentItem = ref({})
    const isEditMode = ref(false)

    const schedules = computed(() => scheduleStore.schedules || [])
    const isLoading = computed(() => scheduleStore.isLoading)

    const filteredSchedules = computed(() => {
      return schedules.value.filter(item => 
        Object.values(item).some(val => 
          String(val).toLowerCase().includes(search.value.toLowerCase())
        )
      )
    })

    const initFormData = () => ({
      id: null, // 确保初始化时包含id字段
      dayOfWeek: 1,
      startTime: '08:00',
      endTime: '09:30',
      classroomName: '',
      building: '',
      teachingClassId: null,
      classroomId: null
    })

    const openAddDialog = () => {
      isEditMode.value = false
      currentItem.value = initFormData()
      dialogVisible.value = true
    }

    const editItem = (item) => {
      isEditMode.value = true
      currentItem.value = { ...item }
      dialogVisible.value = true
    }

    const deleteItem = async (item) => {
      try {
        await uni.showModal({ title: '$1', content: '$2', success: (res) => { if (res.confirm) { $3 } } })(
          `确定要删除 "${item.id}" 的课程安排吗?`,
          '警告',
          { type: 'warning' }
        )
        await scheduleStore.deleteSchedule(item.id)
        uni.showToast({ title: '$1', icon: 'success' })('删除成功')
      } catch (error) {
        if (error !== 'cancel') {
          uni.showToast({ title: '$1', icon: 'error' })(error.message || '删除失败')
        }
      }
    }

    const handleSubmit = async (formData) => {
      try {
        if (isEditMode.value) {
          // 确保formData包含id
          if (!formData.id) {
            throw new Error('缺少课程安排ID')
          }
          await scheduleStore.updateSchedule(formData.id, formData)
          uni.showToast({ title: '$1', icon: 'success' })('更新成功')
        } else {
          if (!formData.teachingClassId) {
            throw new Error('教学班ID不能为空')
          }
          await scheduleStore.addSchedule(formData.teachingClassId, formData)
          uni.showToast({ title: '$1', icon: 'success' })('添加成功')
        }
        dialogVisible.value = false
      } catch (error) {
        uni.showToast({ title: '$1', icon: 'error' })(error.message || '操作失败')
        console.error('操作失败:', error)
      }
    }

onMounted(() => {
  // 修复 ResizeObserver 问题
  const originalError = console.error
  console.error = (...args) => {
    if (args[0] && args[0].includes('ResizeObserver')) {
      return
    }
    originalError.apply(console, args)
  }
  
  // 你的原有 mounted 逻辑
  scheduleStore.fetchAllSchedules().catch(error => {
    uni.showToast({ title: '$1', icon: 'error' })('加载数据失败: ' + error.message)
  })
})

    return {
      search,
      dialogVisible,
      currentItem,
      isEditMode,
      schedules,
      filteredSchedules,
      isLoading,
      openAddDialog,
      editItem,
      deleteItem,
      handleSubmit
    }
  }
}
</script>

<style scoped>
.schedule-management {
  padding: 20px;
}

.toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}
</style>