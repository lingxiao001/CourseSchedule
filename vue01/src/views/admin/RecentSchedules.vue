<<<<<<< Updated upstream
<template>
    <v-container>
      <v-row>
        <v-col cols="12">
          <v-card>
            <v-card-title>
              <text class="text-h5">近期课程安排</text>
              <v-spacer></v-spacer>
              <v-btn icon @click="refresh">
                <v-icon>mdi-refresh</v-icon>
              </v-btn>
            </v-card-title>
            
            <v-card-text>
              <v-timeline>
                <v-timeline-item
                  v-for="(schedule, index) in recentSchedules"
                  :key="index"
                  :color="getColor(schedule.dayOfWeek)"
                  small
                >
                  <template v-slot:opposite>
                    <text>{{ getDayName(schedule.dayOfWeek) }}</text>
                  </template>
                  <v-card class="elevation-2">
                    <v-card-title class="text-h6">
                      {{ schedule.startTime }} - {{ schedule.endTime }}
                    </v-card-title>
                    <v-card-text>
                      <view>教室: {{ schedule.classroom }}</view>
                      <view>教学楼: {{ schedule.building }}</view>
                      <view>教学班ID: {{ schedule.teachingClassId }}</view>
                    </v-card-text>
                  </v-card>
                </v-timeline-item>
              </v-timeline>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </template>
  
  <script>
  import {onMounted } from 'vue';
  import { useScheduleStore } from '@/stores/schedule';
  
  export default {
    setup() {
      const scheduleStore = useScheduleStore();
      
      const getDayName = (dayOfWeek) => {
        const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
        return days[dayOfWeek % 7];
      };
      
      const getColor = (dayOfWeek) => {
        const colors = ['red', 'pink', 'purple', 'deep-purple', 'indigo', 'blue', 'cyan'];
        return colors[dayOfWeek % 7];
      };
      
      const refresh = async () => {
        try {
          await scheduleStore.fetchRecentSchedules();
        } catch (error) {
          console.error(error);
        }
      };
      
      onMounted(() => {
        refresh();
      });
      
      return {
      recentSchedules: scheduleStore.recentSchedules,
      isLoading: scheduleStore.isLoading,
      getDayName,
      getColor,
      refresh
      };
    }
  };
=======
<template>
    <v-container>
      <v-row>
        <v-col cols="12">
          <v-card>
            <v-card-title>
              <span class="text-h5">近期课程安排</span>
              <v-spacer></v-spacer>
              <v-btn icon @click="refresh">
                <v-icon>mdi-refresh</v-icon>
              </v-btn>
            </v-card-title>
            
            <v-card-text>
              <v-timeline>
                <v-timeline-item
                  v-for="(schedule, index) in recentSchedules"
                  :key="index"
                  :color="getColor(schedule.dayOfWeek)"
                  small
                >
                  <template v-slot:opposite>
                    <span>{{ getDayName(schedule.dayOfWeek) }}</span>
                  </template>
                  <v-card class="elevation-2">
                    <v-card-title class="text-h6">
                      {{ schedule.startTime }} - {{ schedule.endTime }}
                    </v-card-title>
                    <v-card-text>
                      <div>教室: {{ schedule.classroom }}</div>
                      <div>教学楼: {{ schedule.building }}</div>
                      <div>教学班ID: {{ schedule.teachingClassId }}</div>
                    </v-card-text>
                  </v-card>
                </v-timeline-item>
              </v-timeline>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </template>
  
  <script>
  import {onMounted } from 'vue';
  import { useScheduleStore } from '@/stores/schedule';
  
  export default {
    setup() {
      const scheduleStore = useScheduleStore();
      
      const getDayName = (dayOfWeek) => {
        const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
        return days[dayOfWeek % 7];
      };
      
      const getColor = (dayOfWeek) => {
        const colors = ['red', 'pink', 'purple', 'deep-purple', 'indigo', 'blue', 'cyan'];
        return colors[dayOfWeek % 7];
      };
      
      const refresh = async () => {
        try {
          await scheduleStore.fetchRecentSchedules();
        } catch (error) {
          console.error(error);
        }
      };
      
      onMounted(() => {
        refresh();
      });
      
      return {
      recentSchedules: scheduleStore.recentSchedules,
      isLoading: scheduleStore.isLoading,
      getDayName,
      getColor,
      refresh
      };
    }
  };
>>>>>>> Stashed changes
  </script>