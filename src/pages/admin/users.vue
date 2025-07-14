<template>
  <view class="users-page">
    <!-- 页面标题 -->
    <view class="page-header">
      <text class="page-title">用户管理</text>
    </view>

    <!-- 统计卡片 -->
    <view class="stats-section">
      <view class="stats-grid">
        <view class="stat-card">
          <text class="stat-value">{{ stats.totalStudents }}</text>
          <text class="stat-label">学生用户</text>
        </view>
        <view class="stat-card">
          <text class="stat-value">{{ stats.totalTeachers }}</text>
          <text class="stat-label">教师用户</text>
        </view>
        <view class="stat-card">
          <text class="stat-value">{{ totalAdmins }}</text>
          <text class="stat-label">管理员</text>
        </view>
      </view>
    </view>

    <!-- 搜索和操作栏 -->
    <view class="operation-bar">
      <view class="search-container">
        <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="搜索用户名或姓名..."
          class="search-input"
          @input="onSearchInput"
        />
        <text class="search-icon">🔍</text>
      </view>
      <button class="add-btn" @click="showAddDialog">
        <text class="add-btn-text">+ 添加用户</text>
      </button>
    </view>

    <!-- 用户列表 -->
    <view class="user-list" v-if="!loading">
      <view 
        class="user-card" 
        v-for="user in userList" 
        :key="user.id"
        @click="viewUser(user)"
      >
        <view class="user-header">
          <view class="user-avatar">
            <text class="avatar-text">{{ getUserAvatar(user.real_name) }}</text>
          </view>
          <view class="user-info">
            <text class="user-name">{{ user.real_name }}</text>
            <text class="user-username">@{{ user.username }}</text>
          </view>
          <view class="role-tag" :class="getRoleClass(user.role)">
            <text class="role-text">{{ getRoleDisplayName(user.role) }}</text>
          </view>
        </view>

        <view class="user-details" v-if="user.role === 'student'">
          <view class="detail-item">
            <text class="detail-label">年级：</text>
            <text class="detail-value">{{ user.grade || '未设置' }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">班级：</text>
            <text class="detail-value">{{ user.className || '未设置' }}</text>
          </view>
        </view>

        <view class="user-details" v-else-if="user.role === 'teacher'">
          <view class="detail-item">
            <text class="detail-label">职称：</text>
            <text class="detail-value">{{ user.title || '未设置' }}</text>
          </view>
          <view class="detail-item">
            <text class="detail-label">院系：</text>
            <text class="detail-value">{{ user.department || '未设置' }}</text>
          </view>
        </view>

        <view class="user-actions">
          <button class="action-btn edit-btn" @click.stop="editUser(user)">
            <text>编辑</text>
          </button>
          <button class="action-btn delete-btn" @click.stop="deleteUser(user)">
            <text>删除</text>
          </button>
        </view>
      </view>
    </view>

    <!-- 加载状态 -->
    <view class="loading-container" v-if="loading">
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 空状态 -->
    <view class="empty-state" v-if="!loading && userList.length === 0">
      <text class="empty-icon">👥</text>
      <text class="empty-text">暂无用户数据</text>
    </view>

    <!-- 加载更多 -->
    <view class="load-more" v-if="!loading && hasMore">
      <button class="load-more-btn" @click="loadMore">
        <text>加载更多</text>
      </button>
    </view>

    <!-- 添加/编辑用户弹窗 -->
    <view class="modal-overlay" v-if="showUserDialog" @click="closeUserDialog">
      <view class="modal-content" @click.stop>
        <view class="modal-header">
          <text class="modal-title">{{ isEdit ? '编辑用户' : '添加用户' }}</text>
          <button class="close-btn" @click="closeUserDialog">×</button>
        </view>

        <view class="form-content">
          <view class="form-group">
            <text class="form-label">用户名 *</text>
            <input 
              type="text" 
              v-model="userForm.username" 
              class="form-input"
              :disabled="isEdit"
              placeholder="请输入用户名"
            />
          </view>

          <view class="form-group" v-if="!isEdit">
            <text class="form-label">密码 *</text>
            <input 
              type="password" 
              v-model="userForm.password" 
              class="form-input"
              placeholder="请输入密码"
            />
          </view>

          <view class="form-group" v-if="isEdit">
            <text class="form-label">新密码</text>
            <input 
              type="password" 
              v-model="userForm.newPassword" 
              class="form-input"
              placeholder="留空则不修改密码"
            />
          </view>

          <view class="form-group">
            <text class="form-label">真实姓名 *</text>
            <input 
              type="text" 
              v-model="userForm.realName" 
              class="form-input"
              placeholder="请输入真实姓名"
            />
          </view>

          <view class="form-group">
            <text class="form-label">角色 *</text>
            <view class="role-selector">
              <label class="role-option" v-for="role in roleOptions" :key="role.value">
                <radio 
                  :value="role.value" 
                  v-model="userForm.role"
                  :checked="userForm.role === role.value"
                />
                <text class="role-option-text">{{ role.label }}</text>
              </label>
            </view>
          </view>

          <!-- 学生特定字段 -->
          <template v-if="userForm.role === 'student'">
            <view class="form-group">
              <text class="form-label">学号</text>
              <input 
                type="number" 
                v-model="userForm.studentId" 
                class="form-input"
                placeholder="请输入学号"
              />
            </view>
            <view class="form-group">
              <text class="form-label">年级</text>
              <input 
                type="text" 
                v-model="userForm.grade" 
                class="form-input"
                placeholder="如：2023级"
              />
            </view>
            <view class="form-group">
              <text class="form-label">班级</text>
              <input 
                type="text" 
                v-model="userForm.className" 
                class="form-input"
                placeholder="如：计算机科学与技术1班"
              />
            </view>
          </template>

          <!-- 教师特定字段 -->
          <template v-if="userForm.role === 'teacher'">
            <view class="form-group">
              <text class="form-label">教师编号</text>
              <input 
                type="number" 
                v-model="userForm.teacherId" 
                class="form-input"
                placeholder="请输入教师编号"
              />
            </view>
            <view class="form-group">
              <text class="form-label">职称</text>
              <input 
                type="text" 
                v-model="userForm.title" 
                class="form-input"
                placeholder="如：教授、副教授、讲师"
              />
            </view>
            <view class="form-group">
              <text class="form-label">院系</text>
              <input 
                type="text" 
                v-model="userForm.department" 
                class="form-input"
                placeholder="如：计算机科学与技术学院"
              />
            </view>
          </template>
        </view>

        <view class="modal-footer">
          <button class="cancel-btn" @click="closeUserDialog">取消</button>
          <button class="confirm-btn" @click="submitUser" :disabled="submitting">
            <text>{{ submitting ? '提交中...' : '确定' }}</text>
          </button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { adminApi } from '@/api/admin'

export default {
  name: 'AdminUsers',
  data() {
    return {
      loading: false,
      submitting: false,
      userList: [],
      searchQuery: '',
      searchTimeout: null,
      currentPage: 0,
      pageSize: 10,
      hasMore: true,
      totalUsers: 0,
      
      // 统计数据
      stats: {
        totalStudents: 0,
        totalTeachers: 0
      },

      // 弹窗相关
      showUserDialog: false,
      isEdit: false,
      userForm: {
        id: null,
        username: '',
        password: '',
        newPassword: '',
        realName: '',
        role: 'student',
        studentId: null,
        grade: '',
        className: '',
        teacherId: null,
        title: '',
        department: ''
      },

      roleOptions: [
        { value: 'student', label: '学生' },
        { value: 'teacher', label: '教师' }
      ]
    }
  },

  computed: {
    totalAdmins() {
      return this.userList.filter(user => user.role === 'admin').length
    }
  },

  async onLoad() {
    await this.loadUsers()
    await this.loadStats()
  },

  methods: {
    async loadUsers(reset = true) {
      if (this.loading) return

      try {
        this.loading = true
        
        if (reset) {
          this.currentPage = 0
          this.userList = []
        }

        const params = {
          page: this.currentPage,
          size: this.pageSize
        }

        if (this.searchQuery.trim()) {
          params.search = this.searchQuery.trim()
        }

        const response = await adminApi.getUsers(params)
        const { content, totalElements, totalPages } = response

        if (reset) {
          this.userList = content
        } else {
          this.userList.push(...content)
        }

        this.totalUsers = totalElements
        this.hasMore = this.currentPage < totalPages - 1
        
      } catch (error) {
        console.error('加载用户列表失败:', error)
        uni.showToast({
          title: '加载失败',
          icon: 'error'
        })
      } finally {
        this.loading = false
      }
    },

    async loadStats() {
      try {
        const stats = await adminApi.getStats()
        this.stats = stats
      } catch (error) {
        console.error('加载统计数据失败:', error)
      }
    },

    async loadMore() {
      if (!this.hasMore || this.loading) return
      
      this.currentPage++
      await this.loadUsers(false)
    },

    onSearchInput() {
      if (this.searchTimeout) {
        clearTimeout(this.searchTimeout)
      }
      
      this.searchTimeout = setTimeout(() => {
        this.loadUsers()
      }, 500)
    },

    getUserAvatar(name) {
      return name ? name.charAt(0).toUpperCase() : '?'
    },

    getRoleClass(role) {
      return `role-${role}`
    },

    getRoleDisplayName(role) {
      const names = {
        admin: '管理员',
        teacher: '教师',
        student: '学生'
      }
      return names[role] || role
    },

    viewUser(user) {
      // 可以跳转到用户详情页面
      console.log('查看用户:', user)
    },

    showAddDialog() {
      this.isEdit = false
      this.resetUserForm()
      this.showUserDialog = true
    },

    editUser(user) {
      this.isEdit = true
      this.userForm = {
        id: user.id,
        username: user.username,
        password: '',
        newPassword: '',
        realName: user.real_name,
        role: user.role,
        studentId: user.roleId,
        grade: user.grade || '',
        className: user.className || '',
        teacherId: user.roleId,
        title: user.title || '',
        department: user.department || ''
      }
      this.showUserDialog = true
    },

    async deleteUser(user) {
      const result = await uni.showModal({
        title: '确认删除',
        content: `确定要删除用户 "${user.real_name}" 吗？此操作不可恢复。`,
        confirmText: '删除',
        cancelText: '取消'
      })

      if (!result.confirm) return

      try {
        await adminApi.deleteUser(user.id)
        uni.showToast({
          title: '删除成功',
          icon: 'success'
        })
        await this.loadUsers()
        await this.loadStats()
      } catch (error) {
        console.error('删除用户失败:', error)
        uni.showToast({
          title: '删除失败',
          icon: 'error'
        })
      }
    },

    async submitUser() {
      if (!this.validateForm()) return

      try {
        this.submitting = true

        const formData = {
          username: this.userForm.username,
          realName: this.userForm.realName,
          role: this.userForm.role
        }

        // 添加角色特定字段
        if (this.userForm.role === 'student') {
          if (this.userForm.studentId) {
            formData.studentId = Number(this.userForm.studentId)
          }
          if (this.userForm.grade) {
            formData.grade = this.userForm.grade
          }
          if (this.userForm.className) {
            formData.className = this.userForm.className
          }
        } else if (this.userForm.role === 'teacher') {
          if (this.userForm.teacherId) {
            formData.teacherId = Number(this.userForm.teacherId)
          }
          if (this.userForm.title) {
            formData.title = this.userForm.title
          }
          if (this.userForm.department) {
            formData.department = this.userForm.department
          }
        }

        if (this.isEdit) {
          // 编辑用户
          if (this.userForm.newPassword) {
            formData.newPassword = this.userForm.newPassword
          }
          await adminApi.updateUser(this.userForm.id, formData)
          uni.showToast({
            title: '更新成功',
            icon: 'success'
          })
        } else {
          // 添加用户
          formData.password = this.userForm.password
          await adminApi.createUser(formData)
          uni.showToast({
            title: '添加成功',
            icon: 'success'
          })
        }

        this.closeUserDialog()
        await this.loadUsers()
        await this.loadStats()

      } catch (error) {
        console.error('提交用户信息失败:', error)
        uni.showToast({
          title: '操作失败',
          icon: 'error'
        })
      } finally {
        this.submitting = false
      }
    },

    validateForm() {
      if (!this.userForm.username.trim()) {
        uni.showToast({
          title: '请输入用户名',
          icon: 'error'
        })
        return false
      }

      if (!this.isEdit && !this.userForm.password.trim()) {
        uni.showToast({
          title: '请输入密码',
          icon: 'error'
        })
        return false
      }

      if (!this.userForm.realName.trim()) {
        uni.showToast({
          title: '请输入真实姓名',
          icon: 'error'
        })
        return false
      }

      return true
    },

    resetUserForm() {
      this.userForm = {
        id: null,
        username: '',
        password: '',
        newPassword: '',
        realName: '',
        role: 'student',
        studentId: null,
        grade: '',
        className: '',
        teacherId: null,
        title: '',
        department: ''
      }
    },

    closeUserDialog() {
      this.showUserDialog = false
      this.resetUserForm()
    }
  }
}
</script>

<style scoped>
.users-page {
  padding: 20rpx;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 30rpx;
}

.page-title {
  font-size: 48rpx;
  font-weight: bold;
  color: #2c3e50;
}

/* 统计卡片 */
.stats-section {
  margin-bottom: 30rpx;
}

.stats-grid {
  display: flex;
  gap: 20rpx;
}

.stat-card {
  flex: 1;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16rpx;
  padding: 30rpx 20rpx;
  text-align: center;
  color: white;
}

.stat-value {
  display: block;
  font-size: 48rpx;
  font-weight: bold;
  margin-bottom: 10rpx;
}

.stat-label {
  font-size: 24rpx;
  opacity: 0.9;
}

/* 操作栏 */
.operation-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 30rpx;
  gap: 20rpx;
}

.search-container {
  flex: 1;
  position: relative;
}

.search-input {
  width: 100%;
  height: 80rpx;
  background: white;
  border-radius: 40rpx;
  padding: 0 50rpx 0 20rpx;
  border: 2rpx solid #e1e8ed;
  font-size: 28rpx;
}

.search-input:focus {
  border-color: #409eff;
}

.search-icon {
  position: absolute;
  right: 20rpx;
  top: 50%;
  transform: translateY(-50%);
  font-size: 32rpx;
  color: #999;
}

.add-btn {
  height: 80rpx;
  padding: 0 30rpx;
  background: linear-gradient(135deg, #42a5f5, #1e88e5);
  color: white;
  border: none;
  border-radius: 40rpx;
  font-size: 28rpx;
}

.add-btn-text {
  color: white;
}

/* 用户列表 */
.user-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.user-card {
  background: white;
  border-radius: 16rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
}

.user-header {
  display: flex;
  align-items: center;
  margin-bottom: 20rpx;
}

.user-avatar {
  width: 80rpx;
  height: 80rpx;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20rpx;
}

.avatar-text {
  color: white;
  font-size: 32rpx;
  font-weight: bold;
}

.user-info {
  flex: 1;
}

.user-name {
  display: block;
  font-size: 32rpx;
  font-weight: bold;
  color: #2c3e50;
  margin-bottom: 8rpx;
}

.user-username {
  font-size: 24rpx;
  color: #7f8c8d;
}

.role-tag {
  padding: 8rpx 16rpx;
  border-radius: 20rpx;
  font-size: 22rpx;
}

.role-tag.role-admin {
  background: #fee2e2;
  color: #dc2626;
}

.role-tag.role-teacher {
  background: #fef3c7;
  color: #d97706;
}

.role-tag.role-student {
  background: #dcfce7;
  color: #16a34a;
}

.role-text {
  font-size: 22rpx;
}

.user-details {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
  margin-bottom: 20rpx;
}

.detail-item {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 200rpx;
}

.detail-label {
  font-size: 24rpx;
  color: #7f8c8d;
  margin-right: 10rpx;
}

.detail-value {
  font-size: 24rpx;
  color: #2c3e50;
}

.user-actions {
  display: flex;
  gap: 20rpx;
  justify-content: flex-end;
}

.action-btn {
  padding: 12rpx 24rpx;
  border-radius: 20rpx;
  font-size: 24rpx;
  border: none;
}

.edit-btn {
  background: #e3f2fd;
  color: #1976d2;
}

.delete-btn {
  background: #ffebee;
  color: #d32f2f;
}

/* 加载状态 */
.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200rpx;
}

.loading-text {
  font-size: 28rpx;
  color: #7f8c8d;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400rpx;
}

.empty-icon {
  font-size: 120rpx;
  margin-bottom: 20rpx;
}

.empty-text {
  font-size: 28rpx;
  color: #7f8c8d;
}

/* 加载更多 */
.load-more {
  display: flex;
  justify-content: center;
  margin-top: 30rpx;
}

.load-more-btn {
  padding: 20rpx 40rpx;
  background: #f8f9fa;
  color: #6c757d;
  border: 2rpx solid #e9ecef;
  border-radius: 40rpx;
  font-size: 28rpx;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 40rpx;
}

.modal-content {
  background: white;
  border-radius: 20rpx;
  width: 100%;
  max-width: 600rpx;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 30rpx;
  border-bottom: 2rpx solid #f5f7fa;
}

.modal-title {
  font-size: 36rpx;
  font-weight: bold;
  color: #2c3e50;
}

.close-btn {
  width: 60rpx;
  height: 60rpx;
  background: #f5f7fa;
  color: #7f8c8d;
  border: none;
  border-radius: 50%;
  font-size: 40rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.form-content {
  padding: 30rpx;
}

.form-group {
  margin-bottom: 30rpx;
}

.form-label {
  display: block;
  font-size: 28rpx;
  color: #2c3e50;
  margin-bottom: 12rpx;
}

.form-input {
  width: 100%;
  height: 80rpx;
  background: #f8f9fa;
  border: 2rpx solid #e9ecef;
  border-radius: 12rpx;
  padding: 0 20rpx;
  font-size: 28rpx;
}

.form-input:focus {
  border-color: #409eff;
  background: white;
}

.form-input:disabled {
  background: #e9ecef;
  color: #6c757d;
}

.role-selector {
  display: flex;
  gap: 30rpx;
}

.role-option {
  display: flex;
  align-items: center;
  gap: 12rpx;
}

.role-option-text {
  font-size: 28rpx;
  color: #2c3e50;
}

.modal-footer {
  display: flex;
  gap: 20rpx;
  padding: 30rpx;
  border-top: 2rpx solid #f5f7fa;
}

.cancel-btn,
.confirm-btn {
  flex: 1;
  height: 80rpx;
  border-radius: 12rpx;
  font-size: 28rpx;
  border: none;
}

.cancel-btn {
  background: #f8f9fa;
  color: #6c757d;
}

.confirm-btn {
  background: linear-gradient(135deg, #42a5f5, #1e88e5);
  color: white;
}

.confirm-btn:disabled {
  background: #e9ecef;
  color: #6c757d;
}
</style>