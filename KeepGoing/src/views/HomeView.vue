<template>
  <div class="home-view">
    <header class="header">
      <h1>건강 관리</h1>
      <p class="date">{{ currentDate }}</p>
    </header>

    <section class="daily-indicators">
      <h2>오늘의 지표</h2>
      <div class="indicator-grid">
        <div class="indicator-card">
          <span class="label">식사</span>
          <span class="value">3/3</span>
        </div>
        <div class="indicator-card">
          <span class="label">물</span>
          <span class="value">1.5L</span>
        </div>
        <div class="indicator-card">
          <span class="label">체중</span>
          <span class="value">70kg</span>
        </div>
      </div>
    </section>

    <section class="quick-actions">
      <h2>기록하기</h2>
      <div class="action-buttons">
        <button @click="goToMealRecord" class="action-btn">
          🍽️ 식사 기록
        </button>
        <button @click="goToWaterRecord" class="action-btn">
          💧 물 기록
        </button>
        <button @click="goToWeightRecord" class="action-btn">
          ⚖️ 체중 기록
        </button>
      </div>
    </section>

    <nav class="bottom-nav">
      <button @click="$router.push('/')" class="nav-btn active">
        🏠 홈
      </button>
      <button @click="$router.push('/calendar')" class="nav-btn">
        📅 캘린더
      </button>
      <button @click="$router.push('/ai-analysis')" class="nav-btn">
        🤖 AI 분석
      </button>
      <button @click="$router.push('/profile')" class="nav-btn">
        👤 마이페이지
      </button>
    </nav>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'

const router = useRouter()

const currentDate = computed(() => {
  return dayjs().format('YYYY년 M월 D일')
})

const goToMealRecord = () => {
  router.push('/record/meal')
}

const goToWaterRecord = () => {
  router.push('/record/water')
}

const goToWeightRecord = () => {
  router.push('/record/weight')
}
</script>

<style scoped>
.home-view {
  min-height: 100vh;
  padding-bottom: 80px;
  background-color: var(--color-background);
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 2rem 1.5rem;
  border-radius: 0 0 1.5rem 1.5rem;
}

.header h1 {
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
}

.date {
  opacity: 0.9;
  font-size: 0.9rem;
}

.daily-indicators, .quick-actions {
  padding: 1.5rem;
}

.daily-indicators h2, .quick-actions h2 {
  font-size: 1.2rem;
  margin-bottom: 1rem;
  color: var(--color-text-primary);
}

.indicator-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
}

.indicator-card {
  background: white;
  padding: 1.5rem 1rem;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-sm);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.indicator-card .label {
  font-size: 0.85rem;
  color: var(--color-text-secondary);
}

.indicator-card .value {
  font-size: 1.5rem;
  font-weight: bold;
  color: var(--color-primary);
}

.action-buttons {
  display: grid;
  gap: 1rem;
}

.action-btn {
  background: white;
  border: none;
  padding: 1.25rem;
  border-radius: var(--radius-lg);
  font-size: 1rem;
  cursor: pointer;
  box-shadow: var(--shadow-sm);
  transition: transform 0.2s, box-shadow 0.2s;
  text-align: left;
}

.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  display: flex;
  justify-content: space-around;
  padding: 1rem;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
}

.nav-btn {
  background: none;
  border: none;
  padding: 0.5rem;
  font-size: 0.75rem;
  cursor: pointer;
  color: var(--color-text-secondary);
  transition: color 0.2s;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
}

.nav-btn.active {
  color: var(--color-primary);
  font-weight: bold;
}
</style>