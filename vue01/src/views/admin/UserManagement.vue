<template>
  <view class="user-management">
    <u-card>
      <!-- 搜索和操作栏 -->
<view class="operation-bar">
  <u-input
    v-model="searchQuery"
    placeholder="搜索用户名或姓名"
    style="width: 300px"
    :clearable="true"
    @clear="fetchUsers"
    @confirm="fetchUsers"
  >
    <template #suffix>
      <u-button :icon="'search'" @click="fetchUsers" />
    </template>
  </u-input>
  <u-button type="primary" @click="addUser">
    <u-icon><Plus /></u-icon>添加用户
  </u-button>
</view>


      <!-- 用户表格 -->
      <u-table :data="userList" :loading="loading" :border="true">
        <u-table-column prop="id" label="ID" width="100" />
        <u-table-column prop="username" label="用户名" width="150" />
        <u-table-column prop="realName" label="姓名" width="120" />
        <u-table-column label="角色" width="100">
          <template #default="{ row }">
            <u-tag :type="roleTagType(row.role)">
              {{ roleDisplayName(row.role) }}
            </u-tag>
          </template>
        </u-table-column>
        
        <!-- 学生特定列 -->
        <u-table-column label="年级" width="100" v-if="hasStudents">
          <template #default="{ row }">
            {{ row.grade || '-' }}
          </template>
        </u-table-column>
        
        <u-table-column label="班级" width="120" v-if="hasStudents">
          <template #default="{ row }">
            {{ row.className || '-' }}
          </template>
        </u-table-column>
        
        <!-- 教师特定列 -->
        <u-table-column label="职称" width="120" v-if="hasTeachers">
          <template #default="{ row }">
            {{ row.title || '-' }}
          </template>
        </u-table-column>
        
        <u-table-column label="院系" width="150" v-if="hasTeachers">
          <template #default="{ row }">
            {{ row.department || '-' }}
          </template>
        </u-table-column>
        
        <u-table-column prop="createdAt" label="注册时间" width="180" />
        <u-table-column label="操作" width="180">
          <template #default="{ row }">
            <u-button size="mini" @click="editUser(row)">编辑</u-button>
            <u-button size="mini" type="error" @click="confirmDelete(row.id)">
              删除
            </u-button>
          </template>
        </u-table-column>
      </u-table>

      <!-- 分页 -->
      <u-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="totalUsers"
        layout="total, prev, pager, next"
        @current-change="fetchUsers"
      />
    </u-card>

    <!-- 添加/编辑用户对话框 -->
    <user-dialog
      v-model="showAddDialog"
      :user="currentUser"
      :is-edit="isEdit"
      @submit="handleSubmit"
    />
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getUsers as fetchUserList,
  createUser,
  updateUser,
  deleteUser 
} from '@/api/admin'
import UserDialog from './UserDialog.vue'


const userList = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const totalUsers = ref(0)
const searchQuery = ref('')
const showAddDialog = ref(false)
const currentUser = ref(null)
const isEdit = ref(false)

// 计算属性，判断是否需要显示学生/教师特定列
const hasStudents = computed(() => {
  return userList.value.some(user => user.role === 'student')
})

const hasTeachers = computed(() => {
  return userList.value.some(user => user.role === 'teacher')
})

const roleTagType = (role) => {
  const types = { admin: 'danger', teacher: 'warning', student: 'success' }
  return types[role] || ''
}

const roleDisplayName = (role) => {
  const names = { admin: '管理员', teacher: '教师', student: '学生' }
  return names[role] || role
}

const fetchUsers = async () => {
  try {
    loading.value = true
    const params = { page: currentPage.value - 1, size: pageSize.value }
    if (searchQuery.value) params.search = searchQuery.value
    const response = await fetchUserList(params)
    userList.value = response.data.content.map(user => ({
      ...user,
      realName: user.realName // 确保前端使用驼峰命名
    }))
    totalUsers.value = response.data.totalElements
  } catch (error) {
    uni.showToast({ title: '$1', icon: 'error' })('获取用户列表失败: ' + error.message)
  } finally {
    loading.value = false
  }
}
const addUser = () => {
  currentUser.value = null  // 清空当前用户数据
  isEdit.value = false      // 设置为非编辑模式
  showAddDialog.value = true // 打开对话框
}
const editUser = (user) => {
  currentUser.value = user
  isEdit.value = true
  showAddDialog.value = true
}

const confirmDelete = (userId) => {
  uni.showModal({ title: '$1', content: '$2', success: (res) => { if (res.confirm) { $3 } } })('确定删除此用户吗?', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    handleDelete(userId)
  })
}

const handleDelete = async (userId) => {
  try {
    await deleteUser(userId)
    uni.showToast({ title: '$1', icon: 'success' })('删除成功')
    fetchUsers()
  } catch (error) {
    uni.showToast({ title: '$1', icon: 'error' })('删除失败: ' + error.message)
  }
}

const handleSubmit = async (userData) => {
  try {
    // 确保使用后端期望的字段名
    const requestData = {
      ...userData,
    }

    if (isEdit.value) {
      await updateUser(requestData.id, requestData)
      uni.showToast({ title: '$1', icon: 'success' })('用户更新成功')
    } else {
      await createUser(requestData)
      uni.showToast({ title: '$1', icon: 'success' })('用户添加成功')
    }
    showAddDialog.value = false
    fetchUsers()
  } catch (error) {
    uni.showToast({ title: '$1', icon: 'error' })('操作失败: ' + error.message)
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.operation-bar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
</style>