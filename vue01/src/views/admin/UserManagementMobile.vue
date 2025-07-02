<template>
  <div class="mobile-user-mgmt">
    <!-- 顶部搜索与新增 -->
    <div class="top-bar">
      <el-icon class="back" @click="$router.back()"><ArrowLeftBold /></el-icon>
      <el-input v-model="searchQuery" placeholder="搜索用户名/姓名" clearable @keyup.enter="fetchUsers">
        <template #append>
          <el-button :icon="Search" @click="fetchUsers" />
        </template>
      </el-input>
      <el-button type="primary" size="small" @click="openDialog()">
        <el-icon><Plus /></el-icon>
        添加
      </el-button>
    </div>

    <!-- 列表区域 -->
    <div v-if="loading" class="loading"><el-skeleton rows="5" animated /></div>
    <el-empty v-else-if="users.length===0" description="暂无用户" />
    <el-collapse v-else class="user-collapse">
      <el-collapse-item v-for="user in users" :key="user.id" :title="`${user.username} / ${user.realName}`" >
        <p>ID：{{ user.id }} | 角色ID：{{ user.roleId }}</p>
        <p>角色：{{ roleDisplayName(user.role) }}</p>
        <p v-if="user.role==='student'">年级：{{ user.grade || '-' }} | 班级：{{ user.className || '-' }}</p>
        <p v-if="user.role==='teacher'">职称：{{ user.title || '-' }} | 院系：{{ user.department || '-' }}</p>
        <p>注册时间：{{ user.createdAt }}</p>
        <div class="btn-group">
          <el-button size="small" @click="openDialog(user)">编辑</el-button>
          <el-button size="small" type="danger" @click="confirmDelete(user.id)">删除</el-button>
        </div>
      </el-collapse-item>
    </el-collapse>

    <!-- 分页 -->
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      layout="total, prev, pager, next"
      small
      @current-change="fetchUsers"
    />

    <!-- 用户对话框复用 -->
    <user-dialog v-model="dialogVisible" :user="currentUser" :is-edit="isEdit" @submit="handleSubmit" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Search, Plus, ArrowLeftBold } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUsers as fetchUserList, createUser, updateUser, deleteUser } from '@/api/admin'
import UserDialog from './UserDialog.vue'

const users = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchQuery = ref('')

const dialogVisible = ref(false)
const currentUser = ref(null)
const isEdit = ref(false)

const roleDisplayName = (role) => ({ admin:'管理员', teacher:'教师', student:'学生' })[role] || role

const fetchUsers = async () => {
  loading.value = true
  try {
    const params = { page: currentPage.value-1, size: pageSize.value }
    if (searchQuery.value) params.search = searchQuery.value
    const res = await fetchUserList(params)
    users.value = res.data.content
    total.value = res.data.totalElements
  } catch (e) {
    ElMessage.error('获取用户失败')
  } finally {
    loading.value = false
  }
}

const openDialog = (user = null) => {
  currentUser.value = user
  isEdit.value = !!user
  dialogVisible.value = true
}

const handleSubmit = async (data) => {
  try {
    if (isEdit.value) {
      await updateUser(data.id, data)
      ElMessage.success('更新成功')
    } else {
      await createUser(data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchUsers()
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

const confirmDelete = (id) => {
  ElMessageBox.confirm('确定删除该用户？', '警告', { type:'warning' }).then(async ()=>{
    try { await deleteUser(id); ElMessage.success('删除成功'); fetchUsers() } catch(e){ ElMessage.error('删除失败') }
  })
}

onMounted(fetchUsers)
</script>

<style scoped>
.mobile-user-mgmt {
  padding: 1rem;
}
.top-bar {
  display: flex;
  gap: .5rem;
  margin-bottom: 1rem;
}
.back {
  font-size: 1.8rem;
  cursor: pointer;
}
.top-bar .el-input {
  flex: 1;
}
.loading { padding: 1rem; }
.user-collapse .btn-group { margin-top: .5rem; display:flex; gap:.5rem; }
</style> 