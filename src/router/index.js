import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView
    },
    {
      path: '/calendar',
      name: 'calendar',
      component: () => import('../views/CalendarView.vue')
    },
    {
      path: '/ai-analysis',
      name: 'ai-analysis',
      component: () => import('../views/AIAnalysisView.vue')
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfileView.vue')
    },
    {
      path: '/record/meal',
      name: 'meal-record',
      component: () => import('../views/record/MealRecordView.vue')
    },
    {
      path: '/record/water',
      name: 'water-record',
      component: () => import('../views/record/WaterRecordView.vue')
    },
    {
      path: '/record/weight',
      name: 'weight-record',
      component: () => import('../views/record/WeightRecordView.vue')
    }
  ]
})

export default router