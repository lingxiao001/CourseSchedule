<template>
  <div class="admin-container">
    <el-container>
      <!-- 侧边栏导航 -->
      <el-aside width="200px">
        <el-menu
          router
          :default-active="$route.path"
          background-color="#545c64"
          text-color="#fff"
          active-text-color="#ffd04b"
        >
          <el-menu-item index="/admin/users">
            <el-icon><user /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="/admin/users/course-schedule">
            <el-icon><calendar /></el-icon>
            <span>课程安排</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { User, Calendar } from '@element-plus/icons-vue'
</script>

<style scoped>
.admin-container {
  height: 100vh;
}
.el-aside {
  background-color: #545c64;
}
.el-menu {
  border-right: none;
}
</style>