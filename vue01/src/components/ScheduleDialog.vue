<template>
  <el-dialog
    :title="isEdit ? '编辑课程安排' : '添加课程安排'"
    v-model="visible"
    width="600px"
    :close-on-click-modal="false"
  >
    <el-form 
      :model="formModel" 
      :rules="rules"
      ref="formRef"
      label-width="100px"
      label-position="right"
    >
      <el-form-item label="星期几" prop="dayOfWeek">
        <el-select 
          v-model="formModel.dayOfWeek" 
          placeholder="请选择星期"
          style="width: 100%"
        >
          <el-option
            v-for="(day, index) in weekDays"
            :key="index"
            :label="'星期' + day"
            :value="index"
          />
        </el-select>
      </el-form-item>
      
      <el-form-item label="开始时间" prop="startTime">
        <el-time-select
          v-model="formModel.startTime"
          :picker-options="timePickerOptions"
        />
      </el-form-item>

      <el-form-item label="结束时间" prop="endTime">
        <el-time-select
          v-model="formModel.endTime"
          :picker-options="{
            ...timePickerOptions,
            minTime: formModel.startTime || '08:00'
          }"
        />
      </el-form-item>
      
      <el-form-item label="教学楼" prop="building">
        <el-input v-model="formModel.building" placeholder="请输入教学楼名称" />
      </el-form-item>
      
      <el-form-item label="教室" prop="classroomName">
        <el-input v-model="formModel.classroomName" placeholder="请输入教室号" />
      </el-form-item>
      
      <el-form-item label="教学班ID" prop="teachingClassId">
        <el-input 
          v-model.number="formModel.teachingClassId" 
          placeholder="请输入教学班ID"
          type="number"
        />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button 
          type="primary" 
          @click="handleSubmit"
          :loading="submitting"
        >
          保存
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'

export default {
  name: 'ScheduleDialog',
  
  props: {
    modelValue: {
      type: Boolean,
      default: false
    },
    formData: {
      type: Object,
      default: () => ({})
    },
    isEdit: {
      type: Boolean,
      default: false
    }
  },
  
  emits: ['update:modelValue', 'submit'],
  
  setup(props, { emit }) {
    const formRef = ref(null)
    const submitting = ref(false)
    const weekDays = ['日', '一', '二', '三', '四', '五', '六']
    
    // 预定义时间段配置
    const ALLOWED_TIME_SLOTS = [
      { start: '08:00', end: '09:30' },
      { start: '09:50', end: '11:20' },
      { start: '13:30', end: '15:00' },
      { start: '15:20', end: '16:50' },
      { start: '18:30', end: '20:00' }
    ]

    // 时间选择器配置
    const timePickerOptions = {
      start: '08:00',
      step: '00:30',
      end: '20:00',
      minTime: '08:00',
      maxTime: '20:00',
      format: 'HH:mm',
      disabledHours: () => {
        // 只允许特定小时
        const allowedHours = [8, 9, 13, 15, 18]
        return Array.from({ length: 24 })
          .map((_, i) => i)
          .filter(h => !allowedHours.includes(h))
      },
      disabledMinutes: (selectedHour) => {
        // 根据小时限制分钟选择
        const minuteRules = {
          8: [0],    // 8点只能选00分
          9: [50],   // 9点只能选50分
          13: [30],  // 13点只能选30分
          15: [20],  // 15点只能选20分
          18: [30]   // 18点只能选30分
        }
        
        if (minuteRules[selectedHour]) {
          return Array.from({ length: 60 })
            .map((_, i) => i)
            .filter(m => !minuteRules[selectedHour].includes(m))
        }
        return []
      }
    }

    // 表单模型
    const formModel = ref({
      dayOfWeek: 1,
      startTime: ALLOWED_TIME_SLOTS[0].start,
      endTime: ALLOWED_TIME_SLOTS[0].end,
      building: '',
      classroomName: '',
      teachingClassId: null,
      classroomId: null
    })
    
    // 验证时间段是否合法
    const validateTimeSlot = () => {
      return ALLOWED_TIME_SLOTS.some(
        slot => slot.start === formModel.value.startTime && 
               slot.end === formModel.value.endTime
      )
    }

    // 表单验证规则
    const rules = {
      dayOfWeek: [
        { required: true, message: '请选择星期几', trigger: 'change' }
      ],
      startTime: [
        { required: true, message: '请选择开始时间', trigger: 'change' },
        { 
          validator: (rule, value, callback) => {
            if (!validateTimeSlot()) {
              callback(new Error('请选择有效的时间段'))
            } else {
              callback()
            }
          },
          trigger: 'change'
        }
      ],
      endTime: [
        { required: true, message: '请选择结束时间', trigger: 'change' },
        { 
          validator: (rule, value, callback) => {
            if (formModel.value.startTime && value) {
              if (value <= formModel.value.startTime) {
                callback(new Error('结束时间必须晚于开始时间'))
                return
              }
            }
            if (!validateTimeSlot()) {
              callback(new Error('请选择有效的时间段'))
            } else {
              callback()
            }
          },
          trigger: 'change'
        }
      ],
      building: [
        { required: true, message: '请输入教学楼名称', trigger: 'blur' }
      ],
      classroomName: [
        { required: true, message: '请输入教室号', trigger: 'blur' }
      ],
      teachingClassId: [
        { required: true, message: '请输入教学班ID', trigger: 'blur' },
        { type: 'number', message: '必须为数字值', trigger: 'blur' }
      ]
    }
    
    // 控制对话框显示
    const visible = computed({
      get: () => props.modelValue,
      set: (value) => emit('update:modelValue', value)
    })
    
    // 监听传入的表单数据变化
    watch(
      () => props.formData,
      (newVal) => {
        if (newVal) {
          formModel.value = { ...formModel.value, ...newVal }
        }
      },
      { immediate: true, deep: true }
    )
    
    // 重置表单
    const resetForm = () => {
      formModel.value = {
        dayOfWeek: 1,
        startTime: ALLOWED_TIME_SLOTS[0].start,
        endTime: ALLOWED_TIME_SLOTS[0].end,
        building: '',
        classroomName: '',
        teachingClassId: null,
        classroomId: null
      }
      if (formRef.value) {
        formRef.value.resetFields()
      }
    }
    
    // 取消操作
    const handleCancel = () => {
      resetForm()
      visible.value = false
    }
    
    // 提交表单
    const handleSubmit = () => {
      formRef.value.validate((valid) => {
        if (valid) {
          // 额外验证时间段是否匹配
          if (!validateTimeSlot()) {
            ElMessage.error('请选择有效的时间段')
            return
          }
          
          submitting.value = true
          try {
            emit('submit', { ...formModel.value })
            resetForm()
            visible.value = false
          } finally {
            submitting.value = false
          }
        }
      })
    }
    
    return {
      formRef,
      formModel,
      rules,
      visible,
      weekDays,
      timePickerOptions,
      submitting,
      handleCancel,
      handleSubmit
    }
  }
}
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>