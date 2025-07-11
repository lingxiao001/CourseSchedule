<template>
  <view class="manual-schedule">
    <!-- 顶部筛选 -->
    <view class="top-bar">
      <u-icon class="back" @click="$router.back()"><ArrowLeftBold /></u-icon>
      <u-select v-model="selectedTeachingClass" placeholder="选择教学班" filterable @change="loadSchedules">
        <u-option v-for="tc in teachingClasses" :key="tc.id" :label="tc.classCode" :value="tc.id" />
      </u-select>
      <u-select v-model="week" placeholder="周次" style="width:90px" @change="loadSchedules">
        <u-option v-for="w in 20" :key="w" :label="`第${w}周`" :value="w" />
      </u-select>
      <u-icon class="config-btn" @click="goConfig"><Setting /></u-icon>
    </view>

    <!-- 课表表格 -->
    <u-table :data="timeSlots" :border="true" style="width:100%" class="table">
      <u-table-column prop="slot" label="时间/周" width="80" />
      <u-table-column v-for="d in 7" :key="d" :label="dayLabel(d)">
        <template #default="{ row }">
          <view class="cell" @click="cellClick(d,row.index)">
            <text v-if="grid[row.index][d]">
              {{ grid[row.index][d].building }}-{{ grid[row.index][d].classroomName }}\n{{ grid[row.index][d].startTime }}
            </text>
            <text v-else class="plus">+</text>
          </view>
        </template>
      </u-table-column>
    </u-table>

    <!-- 对话框 -->
    <u-popup v-model="dialogVisible" :title="dialogTitle" width="90%" @close="resetDialog">
      <u-form :model="form" ref="formRef" label-width="90px" :rules="rules">
        <u-form-item label="教室" prop="classroomId">
          <u-select v-model="form.classroomId" placeholder="选择教室" filterable>
            <u-option v-for="c in classrooms" :key="c.id" :label="`${c.building}-${c.classroomName}`" :value="c.id" />
          </u-select>
        </u-form-item>
      </u-form>
      <template #footer>
        <u-button v-if="editingSchedule" type="error" @click="deleteSchedule">删除</u-button>
        <u-button @click="dialogVisible=false">取消</u-button>
        <u-button type="primary" @click="submitSchedule">确定</u-button>
      </template>
    </u-popup>
  </view>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ArrowLeftBold, Setting } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { scheduleApi } from '@/api/schedule'
import { getTeachingClasses } from '@/api/teacher'
import { getClassrooms } from '@/api/admin'
import { useRouter } from 'vue-router'

const teachingClasses = ref([])
const selectedTeachingClass = ref(null)
const week = ref(1)

// 节次及对应时间，可在此调整
const slotTimes = ref([
  { start:'08:00', end:'08:45' }, // 第1节
  { start:'08:55', end:'09:40' }, // 第2节
  { start:'10:00', end:'10:45' }, // 第3节
  { start:'14:00', end:'14:45' }, // 第4节
  { start:'15:00', end:'15:45' }  // 第5节
])

// 表格行数据
const timeSlots = computed(()=> slotTimes.value.map((t,i)=>({slot:`第${i+1}节`, index:i, time:`${t.start}-${t.end}`})))

// grid[row][day]
const grid = reactive(Array.from({length:5},()=>({1:null,2:null,3:null,4:null,5:null,6:null,7:null})))

const dayLabel = (d)=>['一','二','三','四','五','六','日'][d-1]

const loadSchedules = async ()=>{
  if(!selectedTeachingClass.value) return
  const data = await scheduleApi.getSchedulesByTeachingClass(selectedTeachingClass.value)
  // reset grid
  for(let r of grid) for(let k in r) r[k]=null
  data.forEach(s=>{ grid[slotIndex(s.startTime)][s.dayOfWeek]=s })
}
const slotIndex = (time)=>{
  const idx = slotTimes.value.findIndex(t=>t.start===time)
  return idx>=0?idx:0
}

// dialog state
const dialogVisible=ref(false)
const editingSchedule=ref(null)
const form=reactive({dayOfWeek:1,rowIndex:0,classroomId:null})
const formRef=ref(null)
const rules={classroomId:[{required:true}]}

const classrooms=ref([])

const cellClick=(day,rowIndex)=>{
  editingSchedule.value = grid[rowIndex][day]
  if(editingSchedule.value){
    const cls=classrooms.value.find(c=>c.building===editingSchedule.value.building && c.classroomName===editingSchedule.value.classroomName)
    Object.assign(form, {
      dayOfWeek: editingSchedule.value.dayOfWeek,
      rowIndex: slotIndex(editingSchedule.value.startTime),
      classroomId: cls?.id || null
    })
  }else{
    Object.assign(form,{dayOfWeek:day,rowIndex:rowIndex,classroomId:null})
  }
  dialogVisible.value=true
}

const submitSchedule=async()=>{
  if(!selectedTeachingClass.value) return
  if(editingSchedule.value){
    const payload=composePayload(form.rowIndex)
    await scheduleApi.updateSchedule(editingSchedule.value.id, payload)
    uni.showToast({ title: '$1', icon: 'success' })('已更新')
  }else{
    const payload=composePayload(form.rowIndex)
    await scheduleApi.addSchedule(selectedTeachingClass.value, payload)
    uni.showToast({ title: '$1', icon: 'success' })('已添加')
  }
  dialogVisible.value=false
  loadSchedules()
}
const deleteSchedule=async()=>{
  await scheduleApi.deleteSchedule(editingSchedule.value.id)
  uni.showToast({ title: '$1', icon: 'success' })('已删除')
  dialogVisible.value=false
  loadSchedules()
}
const resetDialog=()=>{editingSchedule.value=null}

const composePayload=(rowIdx)=>{
  const cls=classrooms.value.find(c=>c.id===form.classroomId)
  return {
    dayOfWeek: form.dayOfWeek,
    startTime: slotTimes.value[rowIdx].start,
    endTime: slotTimes.value[rowIdx].end,
    classroomId: form.classroomId,
    building: cls?.building,
    classroomName: cls?.classroomName
  }
}

const goConfig = () =>{
  router.push('/admin/schedule-config')
}

const router = useRouter()

onMounted(async()=>{
  const res = await getTeachingClasses()
  teachingClasses.value = res.data
  const cr = await getClassrooms(); classrooms.value=cr.data
})
</script>

<style scoped>
.manual-schedule{padding:1rem;}
.top-bar{display:flex;gap:.5rem;margin-bottom:1rem;align-items:center;}
.back{font-size:1.8rem;cursor:pointer;}
.top-bar .el-input, .top-bar .el-select{flex:1;}
.cell{height:60px;display:flex;align-items:center;justify-content:center;white-space:pre-line;cursor:pointer;}
.plus{color:#ccc;font-size:1.6rem;}
.table >>> .el-table__body td{padding:0;}
.config-btn{font-size:1.8rem;cursor:pointer;}
</style> 