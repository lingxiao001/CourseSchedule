<script setup lang="ts">
import { onLaunch, onShow, onHide } from "@dcloudio/uni-app";
onLaunch(() => {
  console.log("App Launch");
});
onShow(() => {
  console.log("App Show");
});
onHide(() => {
  console.log("App Hide");
});
</script>

<template>
	<!-- App.vue是应用的主组件，所有页面都是在App.vue下进行切换的 -->
</template>

<style lang="scss">
	/* 全局样式 */
	page {
		background-color: #f5f5f5;
	}
</style>
