<template>
  <div class="home-view">
    <!-- 페이지 1: 오늘의 지표 -->
    <section class="page daily-page">
      <div class="page-content">
        <div class="date">
          <div>{{ currentDate }}</div>
          </div>
        
        <!-- 칼로리 메인 표시 -->
        <div class="calorie-main">
          <div class="calorie-value">912</div>
          <div class="calorie-goal">/ 1,298 kcal</div>
        </div>

        <!-- 영양소 비율 -->
        <div class="nutrition-ratio">
          <div class="ratio-item">
            <span class="ratio-icon carb">탄</span>
            <span class="ratio-value">60%</span>
          </div>
          <div class="ratio-item">
            <span class="ratio-icon protein">단</span>
            <span class="ratio-value">34%</span>
          </div>
          <div class="ratio-item">
            <span class="ratio-icon fat">지</span>
            <span class="ratio-value">18%</span>
          </div>
        </div>

        <!-- 캐릭터 GIF -->
        <div class="character-container">
          <img :src="characterImage" alt="건강 캐릭터" class="character-gif" />
        </div>

        <!-- 영양소 상세 -->
        <div class="nutrition-detail">

          <div class="nutrition-grid">
            <div class="nutrition-card">
              <div class="nutrition-label">탄수화물</div>
              <div class="nutrition-value">
                <span class="current">234</span>
                <span class="total">/ 350g</span>
              </div>
            </div>
            <div class="nutrition-card">
              <div class="nutrition-label">단백질</div>
              <div class="nutrition-value">
                <span class="current">89</span>
                <span class="total">/ 80g</span>
              </div>
            </div>
            <div class="nutrition-card">
              <div class="nutrition-label">지방</div>
              <div class="nutrition-value">
                <span class="current">42</span>
                <span class="total">/ 90g</span>
              </div>
            </div>
          </div>
        </div>

        <!-- <div class="scroll-hint">
          <span>아래로 스크롤</span>
          <div class="arrow">↓</div>
        </div> -->
      </div>
    </section>

    <!-- 페이지 2: 식사 기록 -->
    <section class="page meal-page">
      <div class="page-content">
        <div class="record-card" @click="goToMealRecord">
          <div class="record-icon">🍽️</div>
          <h2>오늘의 식사를 기록하세요</h2>
          <p>아침, 점심, 저녁, 간식</p>
          <button class="record-btn">기록하기</button>
        </div>
        
        <div class="recent-meals">
          <h3>최근 식사</h3>
          <div class="meal-item">
            <span>🍚 점심 - 12:30</span>
            <span class="calories">650kcal</span>
          </div>
          <div class="meal-item">
            <span>🥐 아침 - 08:00</span>
            <span class="calories">420kcal</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 페이지 3: 물 기록 -->
    <section class="page water-page">
      <div class="page-content">
        <div class="record-card" @click="goToWaterRecord">
          <div class="record-icon">💧</div>
          <div class="water-progress">
            <div class="water-amount">1.5L</div>
            <div class="water-goal">/ 2.0L</div>
          </div>
          <div class="progress-bar">
            <div class="progress-fill" style="width: 75%"></div>
          </div>
          <button class="record-btn">물 마시기 기록</button>
        </div>
        
      
      </div>
    </section>

    <!-- 페이지 4: 체중 기록 -->
    <section class="page weight-page">
      <div class="page-content">
        <h1 class="page-title">체중 관리</h1>
        <div class="record-card weight-record-card" @click="goToWeightRecord">
          <div class="record-icon">⚖️</div>
          <div class="weight-display">
            <span class="weight-value">70.0</span>
            <span class="weight-unit">kg</span>
          </div>
          <div class="weight-change">
            <span class="change-label">어제보다</span>
            <span class="change-value positive">-0.3kg</span>
          </div>
          <button class="record-btn">체중 기록</button>
        </div>
        
        <div class="weight-chart">
          <h3>주간 변화</h3>
          <div class="chart-placeholder">📊</div>
        </div>
      </div>
    </section>

    <!-- 하단 네비게이션 -->
    <nav class="bottom-nav">
      <button @click="$router.push('/')" class="nav-btn active">
        <span class="nav-icon">🏠</span>
      </button>
      <button @click="$router.push('/calendar')" class="nav-btn">
        <span class="nav-icon">📅</span>
      </button>
      <button @click="$router.push('/ai-analysis')" class="nav-btn">
        <span class="nav-icon">🤖</span>
      </button>
      <button @click="$router.push('/profile')" class="nav-btn">
        <span class="nav-icon">👤</span>
      </button>
    </nav>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import characterImage from '../assets/images/characters/test.gif'



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
  height: 100vh;
  height: 100dvh; /* 동적 뷰포트 높이 - 모바일 주소창 고려 */
  overflow-y: scroll;
  scroll-snap-type: y mandatory;
  -webkit-overflow-scrolling: touch;
  overscroll-behavior-y: contain; /* 바운스 효과 제거 */
}

.page {
  height: 100vh;
  height: 100dvh;
  scroll-snap-align: start;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: max(env(safe-area-inset-top), 1rem) 1rem calc(env(safe-area-inset-bottom) + 80px) 1rem;
  position: relative;
}

.page-content {
  width: 100%;
  max-width: 420px; /* 모바일 최적화 */
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.page-title {
  font-size: 1.75rem;
  font-weight: 700;
  text-align: center;
  margin: 0;
  letter-spacing: -0.02em;
}

.date {
  text-align: center;
  color: rgba(255, 255, 255, 0.85);
  margin-top: -1rem;
  font-size: 0.9rem;
}

/* 페이지별 배경색 */
.daily-page {
  background: rgba(16, 16, 16, 1);
  color: white;
}

.meal-page {
  background: linear-gradient(135deg, #98D8C8 0%, #6FAFAA 100%);
  color: white;
}

.water-page {
  background: linear-gradient(135deg, #89CFF0 0%, #5DADE2 100%);
  color: white;
}

.weight-page {
  background: linear-gradient(135deg, #B39CD0 0%, #8E7CC3 100%);
  color: white;
}

/* 오늘의 지표 - 새로운 디자인 */
.calorie-main {
  display: flex;
  justify-content: center;
  text-align: center;
  margin: 1rem 0 0.5rem;
}

.calorie-value {
  font-size: 4rem;
  font-weight: 700;
  color: white;
  line-height: 1;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.calorie-goal {
  font-size: 1.1rem;
  color: rgba(255, 255, 255, 0.9);
  margin-top: 0.25rem;
}

/* 영양소 비율 */
.nutrition-ratio {
  display: flex;
  justify-content: center;
  gap: 1.5rem;
  margin: 1.5rem 0;
}

.ratio-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.ratio-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.85rem;
  font-weight: 600;
  color: white;
}

.ratio-icon.carb {
  background: linear-gradient(135deg, #FF6B6B 0%, #FF8E53 100%);
}

.ratio-icon.protein {
  background: linear-gradient(135deg, #4ECDC4 0%, #44A08D 100%);
}

.ratio-icon.fat {
  background: linear-gradient(135deg, #4A90E2 0%, #357ABD 100%);
}

.ratio-value {
  font-size: 1rem;
  font-weight: 600;
  color: white;
}

/* 캐릭터 */
.character-container {
  display: flex;
  justify-content: center;
  margin: 1rem 0;
  min-height: 200px;
  align-items: center;
}

.character-gif {
  width: 180px;
  height: 180px;
  object-fit: contain;
  filter: drop-shadow(0 4px 15px rgba(0, 0, 0, 0.15));
}

/* 영양소 상세 */
.nutrition-detail {
  background: rgba(255, 255, 255, 0.15);
  padding: 1.25rem;
  border-radius: 1.5rem;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.nutrition-status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
  font-size: 0.9rem;
  flex-wrap: wrap;
}

.status-icon {
  font-size: 1.1rem;
}

.status-text {
  color: white;
  font-weight: 500;
}

.status-divider {
  color: rgba(255, 255, 255, 0.5);
}

.status-good {
  color: white;
  font-weight: 500;
}

.nutrition-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.75rem;
}

.nutrition-card {
  background: rgba(255, 255, 255, 0.2);
  padding: 0.875rem 0.5rem;
  border-radius: 1rem;
  text-align: center;
}

.nutrition-label {
  font-size: 0.8rem;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 0.5rem;
  font-weight: 500;
}

.nutrition-value {
  display: flex;
  flex-direction: column;
  gap: 0.1rem;
}

.nutrition-value .current {
  font-size: 1.5rem;
  font-weight: 700;
  color: white;
  line-height: 1;
}

.nutrition-value .total {
  font-size: 0.75rem;
  color: rgba(255, 255, 255, 0.8);
}

/* 스크롤 힌트 */
.scroll-hint {
  text-align: center;
  margin-top: 2rem;
  animation: bounce 2s infinite;
}

.scroll-hint span {
  display: block;
  font-size: 0.9rem;
  opacity: 0.8;
  margin-bottom: 0.5rem;
}

.arrow {
  font-size: 1.5rem;
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-10px);
  }
  60% {
    transform: translateY(-5px);
  }
}

/* 기록 카드 */
.record-card {
  background: rgba(255, 255, 255, 0.2);
  padding: 2rem 1.5rem;
  border-radius: 1.75rem;
  text-align: center;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  cursor: pointer;
  transition: transform 0.15s ease-out;
  -webkit-tap-highlight-color: transparent;
  touch-action: manipulation;
  user-select: none;
}

.record-card:active {
  transform: scale(0.97);
}

.record-icon {
  font-size: 3.5rem;
  margin-bottom: 1rem;
}

.record-card h2 {
  color: var(--color-text-primary);
  font-size: 1.2rem;
  margin-bottom: 0.5rem;
  font-weight: 600;
}

.record-card p {
  color: var(--color-text-secondary);
  margin-bottom: 1.5rem;
  font-size: 0.9rem;
}

.record-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 1rem 2rem;
  border-radius: 3rem;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  width: 100%;
  transition: transform 0.15s ease-out;
  -webkit-tap-highlight-color: transparent;
  touch-action: manipulation;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.record-btn:active {
  transform: scale(0.97);
}

/* 최근 식사 */
.recent-meals {
  background: rgba(255, 255, 255, 0.2);
  padding: 1.25rem;
  border-radius: 1.25rem;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.recent-meals h3 {
  margin-bottom: 1rem;
  font-size: 1rem;
  font-weight: 600;
}

.meal-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.875rem 1rem;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 0.875rem;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
  -webkit-tap-highlight-color: transparent;
}

.meal-item:last-child {
  margin-bottom: 0;
}

.calories {
  font-weight: 600;
}

/* 물 섭취 */
.water-progress {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 0.5rem;
  margin: 1rem 0;
}

.water-amount {
  font-size: 3rem;
  font-weight: bold;
  color: var(--color-primary);
}

.water-goal {
  font-size: 1.5rem;
  color: var(--color-text-secondary);
}

.progress-bar {
  width: 100%;
  height: 12px;
  background: #E5E7EB;
  border-radius: 1rem;
  overflow: hidden;
  margin: 1.5rem 0;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #3B82F6 0%, #60A5FA 100%);
  transition: width 0.3s;
}

.water-tips {
  background: rgba(255, 255, 255, 0.2);
  padding: 1.5rem;
  border-radius: 1.5rem;
  backdrop-filter: blur(10px);
}

.water-tips h3 {
  margin-bottom: 0.5rem;
}

/* 체중 */
.weight-record-card {
  padding: 1.5rem 1.25rem;
}


.weight-display {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 0.5rem;
  margin: 0.75rem 0;
}

.weight-value {
  font-size: 3rem;
  font-weight: bold;
  color: var(--color-primary);
}

.weight-unit {
  font-size: 1.25rem;
  color: var(--color-text-secondary);
}

.weight-change {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
  font-size: 0.9rem;
}

.change-label {
  color: var(--color-text-secondary);
}

.change-value {
  font-weight: 600;
  font-size: 1rem;
}

.change-value.positive {
  color: #10B981;
}

.change-value.negative {
  color: #EF4444;
}

.weight-chart {
  background: rgba(255, 255, 255, 0.2);
  padding: 1rem;
  border-radius: 1.25rem;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.weight-chart h3 {
  margin-bottom: 0.75rem;
  font-size: 0.95rem;
  font-weight: 600;
}

.chart-placeholder {
  background: rgba(255, 255, 255, 0.3);
  padding: 2rem 1rem;
  border-radius: 0.875rem;
  text-align: center;
  font-size: 2rem;
}

/* 하단 네비게이션 */
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  display: flex;
  justify-content: space-around;
  box-shadow: 0 -2px 20px rgba(0, 0, 0, 0.08);
  z-index: 1000;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
    padding: 0.75rem 1rem;

}

.nav-btn {
  background: none;
  border: none;
  padding: 0.4rem 0.5rem;
  cursor: pointer;
  color: var(--color-text-secondary);
  transition: all 0.2s ease-out;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.2rem;
  flex: 1;
  max-width: 70px;
  -webkit-tap-highlight-color: transparent;
  touch-action: manipulation;
  user-select: none;
}

.nav-icon {
  font-size: 1.35rem;
  transition: transform 0.2s ease-out;
  display: block;
}

.nav-label {
  font-size: 0.625rem;
  font-weight: 500;
  letter-spacing: -0.01em;
}

.nav-btn.active {
  color: var(--color-primary);
}

.nav-btn.active .nav-icon {
  transform: scale(1.1);
}

.nav-btn:active {
  transform: scale(0.92);
  opacity: 0.7;
}

/* 스크롤바 숨기기 */
.home-view::-webkit-scrollbar {
  display: none;
}

.home-view {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

/* 모바일 환경 최적화 */
@media (max-width: 390px) {
  .page-title {
    font-size: 1.5rem;
  }
  
  .page-content {
    gap: 1rem;
  }
  
  .calorie-value {
    font-size: 3.5rem;
  }
  
  .calorie-goal {
    font-size: 1rem;
  }
  
  .nutrition-ratio {
    gap: 1rem;
  }
  
  .ratio-icon {
    width: 28px;
    height: 28px;
    font-size: 0.75rem;
  }
  
  .ratio-value {
    font-size: 0.9rem;
  }
  
  .character-gif {
    width: 150px;
    height: 150px;
  }
  
  .character-container {
    min-height: 170px;
  }
  
  .nutrition-detail {
    padding: 1rem;
  }
  
  .nutrition-status {
    font-size: 0.85rem;
  }
  
  .nutrition-value .current {
    font-size: 1.3rem;
  }
  
  .record-icon {
    font-size: 3rem;
  }
  
  .record-card h2 {
    font-size: 1.1rem;
  }
  
  .weight-record-card {
    padding: 1.25rem 1rem;
  }
  
  .weight-value {
    font-size: 2.5rem;
  }
  
  .weight-chart {
    padding: 0.875rem;
  }
  
  .chart-placeholder {
    padding: 1.5rem 1rem;
  }
}

/* 아이폰 노치 대응 */
@supports (padding: max(0px)) {
  .page {
    padding-top: max(env(safe-area-inset-top), 1rem);
    padding-bottom: calc(env(safe-area-inset-bottom) + 70px);
  }
}
</style>