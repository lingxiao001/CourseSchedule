<<<<<<< Updated upstream
<template>
  <view class="schedule-config">
    <u-navbar :icon="'arrow-left'" title="" @back="$router.go(-1)">
      <template #content>
        <text class="text-large font-600">课表配置</text>
      </template>
    </u-navbar>

    <u-line/>

    <!-- 学期起始日期 -->
    <u-form label-width="120px" class="config-form">
      <u-form-item label="第一周开始">
        <u-datetime-picker
          v-model="termStart"
          type="date"
          placeholder="选择日期"
          format="YYYY/MM/DD"
        />
      </u-form-item>

      <!-- 节次数量 -->
      <u-form-item label="每天节数">
        <u-input-number v-model="periodCount" :min="1" :max="12" />
      </u-form-item>

      <!-- 统一课时设置 -->
      <u-form-item label="课时(分钟)">
        <u-input-number v-model="globalDuration" :min="10" :max="240" />
      </u-form-item>

      <!-- 节次时间配置 -->
      <view class="period-table-wrapper">
        <u-table :data="periods" :border="true" style="width:100%" class="period-table" table-layout="fixed">
          <u-table-column prop="index" label="节次" width="60" />
          <u-table-column label="开始时间" width="130">
            <template #default="{ row }">
              <u-time-picker v-model="row.start" :editable="false" format="HH:mm" value-format="HH:mm" placeholder="开始" @change="updateEnd(row)" />
            </template>
          </u-table-column>
          <u-table-column label="结束时间" width="130">
            <template #default="{ row }">
              <u-time-picker v-model="row.end" :editable="false" format="HH:mm" value-format="HH:mm" placeholder="结束" disabled />
            </template>
          </u-table-column>
        </u-table>
      </view>

      <u-button type="primary" class="save-btn" @click="saveConfig">保存配置</u-button>
    </u-form>

    <!-- 预览 -->
    <u-line content-position="left">课表预览（当前周）</u-line>
    <view class="preview-wrapper">
      <table class="preview-table">
        <thead>
          <tr>
            <th>时间/周</th>
            <th v-for="d in 7" :key="d">{{ dayLabel(d) }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in periods" :key="p.index">
            <td class="time-col">
              <view>{{ p.index }}</view>
              <view class="time-range">{{ p.start }} - {{ p.end }}</view>
            </td>
            <td v-for="d in 7" :key="d" class="dash-cell"></td>
          </tr>
        </tbody>
      </table>
    </view>
  </view>
</template>

<script setup>

// 全局 uni 对象定义 - 已移除，使用原生方法替代






import { ref, watch } from 'vue'
// 初始数据（可从本地缓存或接口加载）
const stored = JSON.parse(localStorage.getItem('scheduleConfig') || '{}')

const termStart = ref(stored.termStart ? new Date(stored.termStart) : new Date())
const periodCount = ref(stored.periodCount || 10)
const periods = ref([])
const globalDuration = ref(stored.globalDuration || 45)

// 初始化 periods
const initPeriods = () => {
  periods.value = Array.from({ length: periodCount.value }).map((_, i) => {
    const saved = stored.periods?.[i] || {}
    const start = saved.start || '08:00'
    return {
      index: i + 1,
      start,
      end: addMinutes(start, globalDuration.value)
    }
  })
}

initPeriods()

// 当改变节次数量时重新生成
watch(periodCount, initPeriods)

// 工具: 根据开始时间与时长得到结束时间 (使用函数声明可提升)
function addMinutes(timeStr, mins) {
  const [h, m] = timeStr.split(':').map(Number)
  const date = new Date()
  date.setHours(h, m + mins)
  const hh = String(date.getHours()).padStart(2, '0')
  const mm = String(date.getMinutes()).padStart(2, '0')
  return `${hh}:${mm}`
}

const updateEnd = (row) => {
  if(row.start){
    row.end = addMinutes(row.start, globalDuration.value)
  }
}

// 深度监听 start 的变动自动更新结束时间
watch(periods, (list)=>{
  list.forEach(r=>updateEnd(r))
}, {deep:true})

// 监听全局课时改变
watch(globalDuration, (val)=>{
  periods.value.forEach(r=>{ r.end = addMinutes(r.start, val) })
})

const dayLabel = d => ['一','二','三','四','五','六','日'][d-1]

const saveConfig = () => {
  const cfg = {
    termStart: termStart.value,
    periodCount: periodCount.value,
    periods: periods.value,
    globalDuration: globalDuration.value
  }
  localStorage.setItem('scheduleConfig', JSON.stringify(cfg))
  window.uni.showToast({ title: '$1', icon: 'success' })('配置已保存')
}
</script>

<style scoped>
.schedule-config{padding:1rem;}
.config-form{max-width:500px;}
.period-table-wrapper{overflow-x:auto;}
.period-table :deep(.el-input__inner){text-align:center;}
.period-table :deep(.el-time-picker){width:100%;}
.save-btn{margin-top:1rem;}
.preview-wrapper{overflow-x:auto;margin-top:1rem;}
.preview-table{width:100%;border-collapse:collapse;font-size:13px;}
.preview-table th,.preview-table td{border:1px dashed #c0c4cc;text-align:center;padding:6px;}
.time-col{min-width:90px;}
.time-range{font-size:11px;color:#888;}
.dash-cell{height:40px;}
=======
<template>
  <div class="schedule-config">
    <el-page-header :icon="ArrowLeftBold" title="" @back="$router.go(-1)">
      <template #content>
        <span class="text-large font-600">课表配置</span>
      </template>
    </el-page-header>

    <el-divider/>

    <!-- 学期起始日期 -->
    <el-form label-width="120px" class="config-form">
      <el-form-item label="第一周开始">
        <el-date-picker
          v-model="termStart"
          type="date"
          placeholder="选择日期"
          format="YYYY/MM/DD"
        />
      </el-form-item>

      <!-- 节次数量 -->
      <el-form-item label="每天节数">
        <el-input-number v-model="periodCount" :min="1" :max="12" />
      </el-form-item>

      <!-- 统一课时设置 -->
      <el-form-item label="课时(分钟)">
        <el-input-number v-model="globalDuration" :min="10" :max="240" />
      </el-form-item>

      <!-- 节次时间配置 -->
      <div class="period-table-wrapper">
        <el-table :data="periods" border style="width:100%" class="period-table" table-layout="fixed">
          <el-table-column prop="index" label="节次" width="60" />
          <el-table-column label="开始时间" width="130">
            <template #default="{ row }">
              <el-time-picker v-model="row.start" :editable="false" format="HH:mm" value-format="HH:mm" placeholder="开始" @change="updateEnd(row)" />
            </template>
          </el-table-column>
          <el-table-column label="结束时间" width="130">
            <template #default="{ row }">
              <el-time-picker v-model="row.end" :editable="false" format="HH:mm" value-format="HH:mm" placeholder="结束" disabled />
            </template>
          </el-table-column>
        </el-table>
      </div>

      <el-button type="primary" class="save-btn" @click="saveConfig">保存配置</el-button>
    </el-form>

    <!-- 预览 -->
    <el-divider content-position="left">课表预览（当前周）</el-divider>
    <div class="preview-wrapper">
      <table class="preview-table">
        <thead>
          <tr>
            <th>时间/周</th>
            <th v-for="d in 7" :key="d">{{ dayLabel(d) }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in periods" :key="p.index">
            <td class="time-col">
              <div>{{ p.index }}</div>
              <div class="time-range">{{ p.start }} - {{ p.end }}</div>
            </td>
            <td v-for="d in 7" :key="d" class="dash-cell"></td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ArrowLeftBold } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

// 初始数据（可从本地缓存或接口加载）
const stored = JSON.parse(localStorage.getItem('scheduleConfig') || '{}')

const termStart = ref(stored.termStart ? new Date(stored.termStart) : new Date())
const periodCount = ref(stored.periodCount || 10)
const periods = ref([])
const globalDuration = ref(stored.globalDuration || 45)

// 初始化 periods
const initPeriods = () => {
  periods.value = Array.from({ length: periodCount.value }).map((_, i) => {
    const saved = stored.periods?.[i] || {}
    const start = saved.start || '08:00'
    return {
      index: i + 1,
      start,
      end: addMinutes(start, globalDuration.value)
    }
  })
}

initPeriods()

// 当改变节次数量时重新生成
watch(periodCount, initPeriods)

// 工具: 根据开始时间与时长得到结束时间 (使用函数声明可提升)
function addMinutes(timeStr, mins) {
  const [h, m] = timeStr.split(':').map(Number)
  const date = new Date()
  date.setHours(h, m + mins)
  const hh = String(date.getHours()).padStart(2, '0')
  const mm = String(date.getMinutes()).padStart(2, '0')
  return `${hh}:${mm}`
}

const updateEnd = (row) => {
  if(row.start){
    row.end = addMinutes(row.start, globalDuration.value)
  }
}

// 深度监听 start 的变动自动更新结束时间
watch(periods, (list)=>{
  list.forEach(r=>updateEnd(r))
}, {deep:true})

// 监听全局课时改变
watch(globalDuration, (val)=>{
  periods.value.forEach(r=>{ r.end = addMinutes(r.start, val) })
})

const dayLabel = d => ['一','二','三','四','五','六','日'][d-1]

const saveConfig = () => {
  const cfg = {
    termStart: termStart.value,
    periodCount: periodCount.value,
    periods: periods.value,
    globalDuration: globalDuration.value
  }
  localStorage.setItem('scheduleConfig', JSON.stringify(cfg))
  ElMessage.success('配置已保存')
}
</script>

<style scoped>
.schedule-config{padding:1rem;}
.config-form{max-width:500px;}
.period-table-wrapper{overflow-x:auto;}
.period-table :deep(.el-input__inner){text-align:center;}
.period-table :deep(.el-time-picker){width:100%;}
.save-btn{margin-top:1rem;}
.preview-wrapper{overflow-x:auto;margin-top:1rem;}
.preview-table{width:100%;border-collapse:collapse;font-size:13px;}
.preview-table th,.preview-table td{border:1px dashed #c0c4cc;text-align:center;padding:6px;}
.time-col{min-width:90px;}
.time-range{font-size:11px;color:#888;}
.dash-cell{height:40px;}
>>>>>>> Stashed changes
</style> 