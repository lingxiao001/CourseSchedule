<template>
  <div class="manual-schedule">
    <!-- 顶部筛选 -->
    <div class="top-bar">
      <el-icon class="back" @click="$router.back()"><ArrowLeftBold /></el-icon>
      <el-select v-model="selectedTeachingClass" placeholder="选择教学班" filterable @change="loadSchedules">
        <el-option v-for="tc in teachingClasses" :key="tc.id" :label="tc.classCode" :value="tc.id" />
      </el-select>
      <el-select v-model="week" placeholder="周次" style="width:90px" @change="loadSchedules">
        <el-option v-for="w in 20" :key="w" :label="`第${w}周`" :value="w" />
      </el-select>
    </div>

    <!-- 课表表格 -->
    <el-table :data="timeSlots" border style="width:100%" class="table">
      <el-table-column prop="slot" label="时间/周" width="80" />
      <el-table-column v-for="d in 7" :key="d" :label="dayLabel(d)">
        <template #default="{ row }">
          <div class="cell" @click="cellClick(d,row.index)">
            <span v-if="grid[row.index][d]">
              {{ grid[row.index][d].building }}-{{ grid[row.index][d].classroomName }}\n{{ grid[row.index][d].startTime }}
            </span>
            <span v-else class="plus">+</span>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <!-- 对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="90%" @close="resetDialog">
      <el-form :model="form" ref="formRef" label-width="90px" :rules="rules">
        <el-form-item label="教室" prop="classroomId">
          <el-select v-model="form.classroomId" placeholder="选择教室" filterable>
            <el-option v-for="c in classrooms" :key="c.id" :label="`${c.building}-${c.classroomName}`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始" prop="startTime"><el-input v-model="form.startTime" placeholder="08:00"/></el-form-item>
        <el-form-item label="结束" prop="endTime"><el-input v-model="form.endTime" placeholder="09:30"/></el-form-item>
      </el-form>
      <template #footer>
        <el-button v-if="editingSchedule" type="danger" @click="deleteSchedule">删除</el-button>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="submitSchedule">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ArrowLeftBold } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { scheduleApi } from '@/api/schedule'
import { getTeachingClasses } from '@/api/teacher'
import { getClassrooms } from '@/api/admin'

const teachingClasses = ref([])
const selectedTeachingClass = ref(null)
const week = ref(1)

const timeSlots = ref([
  { slot:'1', index:0 },
  { slot:'2', index:1 },
  { slot:'3', index:2 },
  { slot:'4', index:3 },
  { slot:'5', index:4 }
])
// grid[row][day] -> schedule object
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
  const map={'08:00':0,'09:50':1,'13:30':2,'15:20':3,'18:30':4}
  return map[time]??0
}

// dialog state
const dialogVisible=ref(false)
const editingSchedule=ref(null)
const form=reactive({dayOfWeek:1,startTime:'',endTime:'',classroomId:null})
const formRef=ref(null)
const rules={classroomId:[{required:true}],startTime:[{required:true}],endTime:[{required:true}]}

const classrooms=ref([])

const cellClick=(day,rowIndex)=>{
  editingSchedule.value = grid[rowIndex][day]
  if(editingSchedule.value){
    const cls=classrooms.value.find(c=>c.building===editingSchedule.value.building && c.classroomName===editingSchedule.value.classroomName)
    Object.assign(form, {
      dayOfWeek: editingSchedule.value.dayOfWeek,
      startTime: editingSchedule.value.startTime,
      endTime: editingSchedule.value.endTime,
      classroomId: cls?.id || null
    })
  }else{
    Object.assign(form,{dayOfWeek:day,startTime:'',endTime:'',classroomId:null})
  }
  dialogVisible.value=true
}

const submitSchedule=async()=>{
  if(!selectedTeachingClass.value) return
  if(editingSchedule.value){
    const payload=composePayload()
    await scheduleApi.updateSchedule(editingSchedule.value.id, payload)
    ElMessage.success('已更新')
  }else{
    const payload=composePayload()
    await scheduleApi.addSchedule(selectedTeachingClass.value, payload)
    ElMessage.success('已添加')
  }
  dialogVisible.value=false
  loadSchedules()
}
const deleteSchedule=async()=>{
  await scheduleApi.deleteSchedule(editingSchedule.value.id)
  ElMessage.success('已删除')
  dialogVisible.value=false
  loadSchedules()
}
const resetDialog=()=>{editingSchedule.value=null}

const composePayload=()=>{
  const cls=classrooms.value.find(c=>c.id===form.classroomId)
  return {
    dayOfWeek: form.dayOfWeek,
    startTime: form.startTime,
    endTime: form.endTime,
    classroomId: form.classroomId,
    building: cls?.building,
    classroomName: cls?.classroomName
  }
}

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
</style> 