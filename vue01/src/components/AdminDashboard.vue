<template>
  <view class="admin-container">
    <u-container>
      <!-- 侧边栏导航 -->
      <u-aside width="200px">
        <u-menu
          router
          :default-active="$route.path"
          background-color="#545c64"
          text-color="#fff"
          active-text-color="#ffd04b"
        >
          <u-menu-item index="/admin/users">
            <u-icon><user /></u-icon>
            <text>用户管理</text>
          </u-menu-item>
          <u-menu-item index="/admin/users/course-schedule">
            <u-icon><calendar /></u-icon>
            <text>课程安排</text>
          </u-menu-item>
        </u-menu>
      </u-aside>

      <!-- 主内容区 -->
      <u-main>
        <router-view />
      </u-main>
    </u-container>
  </view>
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
  :border="true"-right: none;
}
</style>