<script setup>
import { ref, onMounted } from 'vue';
import { Sparkles, TrendingUp, CheckCircle, AlertCircle } from 'lucide-vue-next';

// ============================================================
// 상태 관리
// ============================================================
const isLoading = ref(true);
const selectedGoal = ref(null);
const recommendationData = ref(null);

// 사용자 목표 선택지
const goals = [
  { id: 'weight-loss', label: '체중 감량', icon: '📉', color: 'goal-orange' },
  { id: 'muscle-gain', label: '근육 증가', icon: '💪', color: 'goal-red' },
  { id: 'health', label: '건강 개선', icon: '❤️', color: 'goal-green' },
  { id: 'energy', label: '에너지 부스트', icon: '⚡', color: 'goal-yellow' }
];

// ============================================================
// 라이프사이클
// ============================================================
onMounted(() => {
  // 초기 로딩 완료 (목표 선택 화면 표시)
  const timer = setTimeout(() => {
    isLoading.value = false;
  }, 1500);

  return () => clearTimeout(timer);
});

// ============================================================
// 메서드
// ============================================================
const selectGoal = (goalId) => {
  selectedGoal.value = goalId;
  isLoading.value = true;

  // 목표 선택 후 추천 데이터 로드 시뮬레이션
  const timer = setTimeout(() => {
    const goalData = {
      'weight-loss': {
        goal: '체중 감량',
        dailyCalorie: 1800,
        calorieDeficit: 500,
        macros: {
          protein: 150,
          carbs: 180,
          fat: 60
        },
        ratio: { protein: 35, carbs: 40, fat: 25 },
        mealPlan: [
          {
            id: 1,
            mealType: '아침',
            time: '07:00',
            name: '고단백 계란 스크램블',
            calories: 350,
            description: '계란 3개, 시금치, 토마토, 통곡물 식빵',
            benefits: ['단백질 풍부', '저칼로리', '오래 지속되는 포만감']
          },
          {
            id: 2,
            mealType: '간식',
            time: '10:30',
            name: '그릭 요거트',
            calories: 150,
            description: '무가당 그릭 요거트 150g + 베리류',
            benefits: ['프로바이오틱스', '단백질 보충', '혈당 안정']
          },
          {
            id: 3,
            mealType: '점심',
            time: '12:30',
            name: '구운 닭가슴살 샐러드',
            calories: 420,
            description: '닭가슴살 150g, 혼합 채소, 올리브 오일 드레싱',
            benefits: ['저지방', '풍부한 영양', '근력 유지']
          },
          {
            id: 4,
            mealType: '간식',
            time: '16:00',
            name: '견과류 믹스',
            calories: 180,
            description: '아몬드 30g + 호두 15g',
            benefits: ['건강한 지방', '장시간 에너지', '포만감']
          },
          {
            id: 5,
            mealType: '저녁',
            time: '18:30',
            name: '생선과 채소 구이',
            calories: 400,
            description: '연어 150g, 브로콜리, 당근, 현미밥 150g',
            benefits: ['오메가-3', '완전한 영양', '낮은 칼로리']
          },
          {
            id: 6,
            mealType: '저녁 간식',
            time: '21:00',
            name: '카모마일 차',
            calories: 0,
            description: '무가당 카모마일 차',
            benefits: ['칼로리 없음', '숙면 유도', '소화 촉진']
          }
        ],
        tips: [
          '충분한 물 섭취: 하루 2-3L 이상',
          '야간 간식 피하기: 저녁 8시 이후 섭취 자제',
          '가공식품 줄이기: 자연식 위주로 선택',
          '규칙적인 운동: 주 3-4회, 30분 이상'
        ],
        warnings: [
          '급격한 칼로리 제한은 대사를 낮출 수 있습니다',
          '단백질 충분 섭취로 근손실 방지',
          '영양 불균형 주의'
        ]
      },
      'muscle-gain': {
        goal: '근육 증가',
        dailyCalorie: 2500,
        calorieDeficit: -300,
        macros: {
          protein: 200,
          carbs: 300,
          fat: 80
        },
        ratio: { protein: 35, carbs: 45, fat: 20 },
        mealPlan: [
          {
            id: 1,
            mealType: '아침',
            time: '07:00',
            name: '단백질 팬케이크',
            calories: 500,
            description: '계란 2개, 오트밀 50g, 단백질 파우더, 꿀 1큰술',
            benefits: ['고단백', '충분한 탄수화물', '에너지 부스트']
          },
          {
            id: 2,
            mealType: '사전 운동',
            time: '10:00',
            name: '바나나 + 땅콩버터',
            calories: 300,
            description: '바나나 1개 + 땅콩버터 2큰술',
            benefits: ['빠른 에너지', '근펌프 증진', '혈당 안정']
          },
          {
            id: 3,
            mealType: '점심',
            time: '12:30',
            name: '소고기 스테이크 도시락',
            calories: 650,
            description: '소고기 200g, 흰쌀밥 250g, 브로콜리, 포테이토',
            benefits: ['크레아틴 풍부', '완전한 영양', '근성장 극대화']
          },
          {
            id: 4,
            mealType: '운동 후',
            time: '15:30',
            name: '유청 단백질 쉐이크',
            calories: 250,
            description: '유청 단백질 30g + 바나나 + 우유 200ml',
            benefits: ['빠른 단백질 흡수', '근회복 촉진', '포만감']
          },
          {
            id: 5,
            mealType: '저녁',
            time: '18:30',
            name: '닭다리살 카레',
            calories: 600,
            description: '닭다리살 250g, 카레 소스, 현미밥 200g, 완두콩',
            benefits: ['저지방 단백질', '풍부한 탄수화물', '맛있는 식사']
          },
          {
            id: 6,
            mealType: '야식',
            time: '21:00',
            name: '카제인 쉐이크',
            calories: 200,
            description: '카제인 단백질 25g + 우유 200ml',
            benefits: ['야간 단백질 공급', '천천한 소화', '근성장 지속']
          }
        ],
        tips: [
          '충분한 칼로리 섭취 필수: 유지 칼로리 + 300-500kcal',
          '고단백 식단 유지: 체중 1kg당 2.2g 이상',
          '탄수화물 충분 섭취: 운동 성능 극대화',
          '지속적인 저항 운동 필수'
        ],
        warnings: [
          '과도한 칼로리는 체지방도 증가시킵니다',
          '균형잡힌 영양소 비율 유지',
          '수면 충분: 하루 7-9시간'
        ]
      },
      'health': {
        goal: '건강 개선',
        dailyCalorie: 2000,
        calorieDeficit: 0,
        macros: {
          protein: 100,
          carbs: 250,
          fat: 65
        },
        ratio: { protein: 20, carbs: 50, fat: 30 },
        mealPlan: [
          {
            id: 1,
            mealType: '아침',
            time: '07:00',
            name: '통곡물 시리얼과 베리',
            calories: 350,
            description: '통곡물 시리얼 50g, 우유 200ml, 블루베리 100g, 꿀',
            benefits: ['풍부한 식이섬유', '항산화', '포만감']
          },
          {
            id: 2,
            mealType: '간식',
            time: '10:00',
            name: '딸기와 요거트',
            calories: 150,
            description: '요거트 150g + 딸기 100g',
            benefits: ['프로바이오틱스', '칼슘', '비타민 C']
          },
          {
            id: 3,
            mealType: '점심',
            time: '12:30',
            name: '채소 국수와 두부',
            calories: 400,
            description: '현미국수 150g, 두부 150g, 시즈닝 채소, 멸치육수',
            benefits: ['완전한 단백질', '저칼로리', '소화 용이']
          },
          {
            id: 4,
            mealType: '간식',
            time: '15:30',
            name: '견과류와 과일',
            calories: 200,
            description: '사과 1개 + 아몬드 30g',
            benefits: ['항산화', '건강한 지방', '에너지']
          },
          {
            id: 5,
            mealType: '저녁',
            time: '18:30',
            name: '구운 흰살 생선과 채소',
            calories: 450,
            description: '흰살 생선 150g, 단호박, 시금치, 보리밥 150g',
            benefits: ['오메가-3', '무기질', '낮은 나트륨']
          },
          {
            id: 6,
            mealType: '저녁 차',
            time: '20:30',
            name: '녹차',
            calories: 5,
            description: '무가당 녹차 한 잔',
            benefits: ['항산화', '신진대사 촉진', '정신 건강']
          }
        ],
        tips: [
          '다양한 색깔의 채소 섭취: 무지개 식단',
          '정제 탄수화물 줄이기: 통곡물 위주',
          '염분 섭취 제한: 하루 2,000mg 이하',
          '꾸준한 운동: 주 5회, 30분 이상'
        ],
        warnings: [
          '급격한 식단 변화는 부작용을 유발할 수 있습니다',
          '개인의 건강 상태에 맞는 식단 선택',
          '필요시 영양사 상담'
        ]
      },
      'energy': {
        goal: '에너지 부스트',
        dailyCalorie: 2200,
        calorieDeficit: -100,
        macros: {
          protein: 110,
          carbs: 280,
          fat: 70
        },
        ratio: { protein: 22, carbs: 52, fat: 26 },
        mealPlan: [
          {
            id: 1,
            mealType: '아침',
            time: '07:00',
            name: '에너지 스무디',
            calories: 400,
            description: '우유 200ml, 바나나, 단백질 파우더, 여보, 아몬드버터',
            benefits: ['빠른 에너지', '포만감', '영양 풍부']
          },
          {
            id: 2,
            mealType: '간식',
            time: '10:00',
            name: '에너지 바',
            calories: 250,
            description: '오트밀, 꿀, 견과류 에너지 바 1개',
            benefits: ['휴대 가능', '지속 에너지', '스포츠 최적']
          },
          {
            id: 3,
            mealType: '점심',
            time: '12:30',
            name: '닭고기 카보나라',
            calories: 550,
            description: '닭가슴살 120g, 파스타 200g, 계란, 시금치',
            benefits: ['완전한 에너지', '포만감', '신체 활동 지원']
          },
          {
            id: 4,
            mealType: '사전 운동',
            time: '15:30',
            name: '건포도와 치즈',
            calories: 200,
            description: '건포도 50g + 스트링 치즈 1개',
            benefits: ['빠른 에너지', '단백질', '포만감']
          },
          {
            id: 5,
            mealType: '저녁',
            time: '18:30',
            name: '연어덮밥',
            calories: 500,
            description: '연어 150g, 덮밥용 현미 200g, 계란, 호박 볶음',
            benefits: ['오메가-3', '지속 에너지', '뇌 건강']
          },
          {
            id: 6,
            mealType: '야식',
            time: '20:30',
            name: '호두와 시나몬',
            calories: 200,
            description: '호두 30g + 시나몬 우유 200ml',
            benefits: ['야간 에너지', '숙면 유도', '뇌 건강']
          }
        ],
        tips: [
          '규칙적인 식사 시간 유지: 신체 리듬 안정화',
          '충분한 수분 섭취: 하루 2.5L 이상',
          '카페인 섭취 시간 조절: 오후 3시 이후 자제',
          '적절한 운동: 신진대사 촉진'
        ],
        warnings: [
          '과도한 당분 섭취 주의: 에너지 저하 원인',
          '카페인 과다 섭취는 수면 방해',
          '수분 부족은 피로감 증가'
        ]
      }
    };

    recommendationData.value = goalData[goalId];
    isLoading.value = false;
  }, 2000);

  return () => clearTimeout(timer);
};

const goBack = () => {
  selectedGoal.value = null;
  recommendationData.value = null;
};

const getGoalColor = (goalId) => {
  const colorMap = {
    'weight-loss': '#f97316',
    'muscle-gain': '#dc2626',
    'health': '#22c55e',
    'energy': '#eab308'
  };
  return colorMap[goalId] || '#9333ea';
};

const getMealTypeColor = (mealType) => {
  const colorMap = {
    '아침': 'meal-breakfast',
    '간식': 'meal-snack',
    '점심': 'meal-lunch',
    '사전 운동': 'meal-pre-workout',
    '운동 후': 'meal-post-workout',
    '저녁': 'meal-dinner',
    '저녁 간식': 'meal-evening-snack',
    '야식': 'meal-late-night',
    '야간 간식': 'meal-late-snack',
    '저녁 차': 'meal-evening-tea'
  };
  return colorMap[mealType] || 'meal-default';
};
</script>

<template>
  <div class="container">
    <div class="wrapper">
      <!-- 헤더 -->
      <div class="header-section">
        <div v-if="selectedGoal" class="back-button-wrapper">
          <button @click="goBack" class="back-button">
            ← 돌아가기
          </button>
        </div>
        <div class="title-row">
          <Sparkles class="icon-sparkle" />
          <h1>{{ selectedGoal ? '식단 추천' : 'AI 식단 추천' }}</h1>
        </div>
        <p class="subtitle">
          {{ selectedGoal ? '당신의 목표에 맞춰진 맞춤 식단입니다' : '당신의 목표를 선택해주세요' }}
        </p>
      </div>

      <!-- 목표 선택 화면 -->
      <div v-if="!selectedGoal && !isLoading" class="goals-container">
        <div class="goals-grid">
          <button
            v-for="goal in goals"
            :key="goal.id"
            @click="selectGoal(goal.id)"
            :class="['goal-card', goal.color]"
          >
            <div class="goal-icon">{{ goal.icon }}</div>
            <div class="goal-label">{{ goal.label }}</div>
          </button>
        </div>
      </div>

      <!-- 로딩 화면 -->
      <div v-if="isLoading" class="loading-container">
        <div class="loading-box">
          <div class="pulse-effect">
            <svg viewBox="0 0 200 220" class="agent-svg">
              <defs>
                <radialGradient id="agentGradient" cx="50%" cy="40%">
                  <stop offset="0%" stop-color="#60a5fa" stop-opacity="0.6" />
                  <stop offset="100%" stop-color="#3b82f6" stop-opacity="0.3" />
                </radialGradient>
                <filter id="glow">
                  <feGaussianBlur stdDeviation="3" result="coloredBlur"/>
                  <feMerge>
                    <feMergeNode in="coloredBlur"/>
                    <feMergeNode in="SourceGraphic"/>
                  </feMerge>
                </filter>
              </defs>
              <circle cx="100" cy="90" r="75" fill="url(#agentGradient)" filter="url(#glow)" />
              <circle cx="100" cy="70" r="35" fill="#f3f4f6" />
              <circle cx="88" cy="65" r="4" fill="#1f2937" />
              <circle cx="112" cy="65" r="4" fill="#1f2937" />
              <circle cx="89" cy="63" r="1.5" fill="#fff" />
              <circle cx="113" cy="63" r="1.5" fill="#fff" />
              <circle cx="100" cy="75" r="3" fill="#e5e7eb" />
              <path d="M 95 82 Q 100 85 105 82" stroke="#d1d5db" stroke-width="2" fill="none" stroke-linecap="round" />
              <ellipse cx="100" cy="125" rx="28" ry="35" fill="#e0e7ff" />
              <ellipse cx="72" cy="120" rx="12" ry="28" fill="#f3f4f6" transform="rotate(-20 72 120)" />
              <ellipse cx="128" cy="120" rx="12" ry="28" fill="#f3f4f6" transform="rotate(20 128 120)" />
              <circle cx="62" cy="135" r="8" fill="#fcd34d" />
              <circle cx="138" cy="135" r="8" fill="#fcd34d" />
              <g opacity="0.7">
                <circle cx="45" cy="60" r="3" fill="#a78bfa" />
                <line x1="45" y1="50" x2="45" y2="70" stroke="#a78bfa" stroke-width="1.5" />
                <line x1="35" y1="60" x2="55" y2="60" stroke="#a78bfa" stroke-width="1.5" />
              </g>
              <g opacity="0.7">
                <circle cx="155" cy="50" r="2.5" fill="#c084fc" />
                <line x1="155" y1="42" x2="155" y2="58" stroke="#c084fc" stroke-width="1.5" />
                <line x1="147" y1="50" x2="163" y2="50" stroke="#c084fc" stroke-width="1.5" />
              </g>
            </svg>
          </div>
        </div>
        <div class="loading-dots">
          <div class="dot" v-for="i in 3" :key="i" :style="{ animationDelay: `${i * 0.15}s` }"></div>
        </div>
      </div>

      <!-- 추천 식단 화면 -->
      <div v-if="selectedGoal && !isLoading && recommendationData" class="recommendation-container">
        <!-- 목표 요약 카드 -->
        <div class="summary-card">
          <div class="summary-content">
            <h2>{{ recommendationData.goal }}</h2>
            <div class="nutrition-summary">
              <div class="nutrition-item">
                <span class="label">일일 칼로리</span>
                <span class="value">{{ recommendationData.dailyCalorie }}kcal</span>
              </div>
              <div class="nutrition-item">
                <span class="label">단백질</span>
                <span class="value">{{ recommendationData.macros.protein }}g</span>
              </div>
              <div class="nutrition-item">
                <span class="label">탄수화물</span>
                <span class="value">{{ recommendationData.macros.carbs }}g</span>
              </div>
              <div class="nutrition-item">
                <span class="label">지방</span>
                <span class="value">{{ recommendationData.macros.fat }}g</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 매크로 비율 표시 -->
        <div class="macro-visualization">
          <div class="macro-bar">
            <div class="macro-segment protein" :style="{ width: `${recommendationData.ratio.protein}%` }"></div>
            <div class="macro-segment carbs" :style="{ width: `${recommendationData.ratio.carbs}%` }"></div>
            <div class="macro-segment fat" :style="{ width: `${recommendationData.ratio.fat}%` }"></div>
          </div>
          <div class="macro-legend">
            <div class="legend-item">
              <div class="legend-color protein"></div>
              <span>단백질 {{ recommendationData.ratio.protein }}%</span>
            </div>
            <div class="legend-item">
              <div class="legend-color carbs"></div>
              <span>탄수화물 {{ recommendationData.ratio.carbs }}%</span>
            </div>
            <div class="legend-item">
              <div class="legend-color fat"></div>
              <span>지방 {{ recommendationData.ratio.fat }}%</span>
            </div>
          </div>
        </div>

        <!-- 식단 계획 -->
        <div class="meal-plan-section">
          <h2 class="section-title">
            <Sparkles class="icon-sm" />
            하루 식단 계획
          </h2>
          <div class="meal-cards">
            <div
              v-for="(meal, idx) in recommendationData.mealPlan"
              :key="meal.id"
              :class="['meal-card', getMealTypeColor(meal.mealType)]"
              :style="{ animationDelay: `${idx * 100}ms` }"
            >
              <div class="meal-header">
                <div class="meal-meta">
                  <span class="meal-type">{{ meal.mealType }}</span>
                  <span class="meal-time">{{ meal.time }}</span>
                </div>
                <div class="meal-calorie">{{ meal.calories }}kcal</div>
              </div>
              <h3 class="meal-name">{{ meal.name }}</h3>
              <p class="meal-description">{{ meal.description }}</p>
              <div class="meal-benefits">
                <div v-for="benefit in meal.benefits" :key="benefit" class="benefit-tag">
                  ✓ {{ benefit }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 팁과 경고 -->
        <div class="tips-section">
          <div class="tips-card">
            <h3 class="tips-title">💡 식단 팁</h3>
            <ul class="tips-list">
              <li v-for="(tip, idx) in recommendationData.tips" :key="idx">
                {{ tip }}
              </li>
            </ul>
          </div>

          <div class="warnings-card">
            <h3 class="warnings-title">⚠️ 주의사항</h3>
            <ul class="warnings-list">
              <li v-for="(warning, idx) in recommendationData.warnings" :key="idx">
                {{ warning }}
              </li>
            </ul>
          </div>
        </div>

        <!-- 액션 버튼 -->
        <div class="button-group">
          <button class="btn btn-primary">구매 목록 생성</button>
          <button class="btn btn-secondary" @click="goBack">다시 선택하기</button>
        </div>
      </div>
    </div>

    <div class="footer-spacer"></div>
  </div>
</template>

<style scoped>
/* =====================================================
   기본 레이아웃 및 배경
   ===================================================== */
.container {
  min-height: 100vh;
  background-color: #000000;
  color: #ffffff;
  padding: 1rem;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

.wrapper {
  max-width: 42rem;
  margin: 0 auto;
}

.footer-spacer {
  height: 100px;
  width: 100%;
}

/* =====================================================
   헤더
   ===================================================== */
.header-section {
  margin-bottom: 1.5rem;
}

.back-button-wrapper {
  margin-bottom: 1rem;
}

.back-button {
  background: none;
  border: none;
  color: #9ca3af;
  font-size: 0.875rem;
  cursor: pointer;
  padding: 0.5rem 0;
  transition: color 0.2s;
}

.back-button:hover {
  color: #f3f4f6;
}

.title-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.title-row h1 {
  font-size: 1.5rem;
  font-weight: 700;
}

.icon-sparkle {
  width: 1.25rem;
  height: 1.25rem;
  color: #c084fc;
}

.subtitle {
  color: #9ca3af;
  font-size: 0.875rem;
}

/* =====================================================
   목표 선택 카드
   ===================================================== */
.goals-container {
  margin-bottom: 2rem;
}

.goals-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}

.goal-card {
  background: linear-gradient(135deg, rgba(31, 41, 55, 0.5), rgba(17, 24, 39, 0.5));
  border: 2px solid transparent;
  border-radius: 1rem;
  padding: 1.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.875rem;
  font-weight: 600;
}

.goal-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
}

.goal-orange {
  border-color: rgba(249, 115, 22, 0.5);
  background: linear-gradient(135deg, rgba(249, 115, 22, 0.15), rgba(249, 115, 22, 0.05));
}

.goal-orange:hover {
  background: linear-gradient(135deg, rgba(249, 115, 22, 0.25), rgba(249, 115, 22, 0.15));
}

.goal-red {
  border-color: rgba(220, 38, 38, 0.5);
  background: linear-gradient(135deg, rgba(220, 38, 38, 0.15), rgba(220, 38, 38, 0.05));
}

.goal-red:hover {
  background: linear-gradient(135deg, rgba(220, 38, 38, 0.25), rgba(220, 38, 38, 0.15));
}

.goal-green {
  border-color: rgba(34, 197, 94, 0.5);
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.15), rgba(34, 197, 94, 0.05));
}

.goal-green:hover {
  background: linear-gradient(135deg, rgba(34, 197, 94, 0.25), rgba(34, 197, 94, 0.15));
}

.goal-yellow {
  border-color: rgba(234, 179, 8, 0.5);
  background: linear-gradient(135deg, rgba(234, 179, 8, 0.15), rgba(234, 179, 8, 0.05));
}

.goal-yellow:hover {
  background: linear-gradient(135deg, rgba(234, 179, 8, 0.25), rgba(234, 179, 8, 0.15));
}

.goal-icon {
  font-size: 2rem;
}

.goal-label {
  text-align: center;
}

/* =====================================================
   로딩 상태
   ===================================================== */
.loading-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  align-items: center;
}

.loading-box {
  background: linear-gradient(to right, #1f2937, #111827);
  border-radius: 1.5rem;
  padding: 3rem;
  border: 1px solid rgba(55, 65, 81, 0.5);
  display: flex;
  justify-content: center;
}

.pulse-effect {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.agent-svg {
  width: 8rem;
  height: 8rem;
}

.loading-dots {
  display: flex;
  justify-content: center;
  gap: 0.75rem;
}

.dot {
  width: 0.5rem;
  height: 0.5rem;
  background-color: #4b5563;
  border-radius: 9999px;
  animation: bounce 1s infinite;
}

/* =====================================================
   추천 화면 - 요약 카드
   ===================================================== */
.recommendation-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.summary-card {
  background: linear-gradient(to bottom right, #111827, #1f2937, #000000);
  border-radius: 1.5rem;
  padding: 1.5rem;
  border: 1px solid rgba(55, 65, 81, 0.5);
}

.summary-content h2 {
  font-size: 1.5rem;
  margin-bottom: 1rem;
}

.nutrition-summary {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}

.nutrition-item {
  background: rgba(55, 65, 81, 0.3);
  border-radius: 0.75rem;
  padding: 1rem;
  text-align: center;
}

.nutrition-item .label {
  display: block;
  color: #9ca3af;
  font-size: 0.75rem;
  margin-bottom: 0.5rem;
}

.nutrition-item .value {
  display: block;
  font-size: 1.5rem;
  font-weight: 700;
  color: #c084fc;
}

/* =====================================================
   매크로 비율 시각화
   ===================================================== */
.macro-visualization {
  background: linear-gradient(to bottom right, #111827, #1f2937, #000000);
  border-radius: 1.5rem;
  padding: 1.5rem;
  border: 1px solid rgba(55, 65, 81, 0.5);
}

.macro-bar {
  display: flex;
  border-radius: 0.5rem;
  overflow: hidden;
  margin-bottom: 1rem;
  height: 2rem;
}

.macro-segment {
  height: 100%;
  transition: width 0.6s ease;
}

.macro-segment.protein {
  background: linear-gradient(to right, #ec4899, #f472b6);
}

.macro-segment.carbs {
  background: linear-gradient(to right, #a855f7, #d8b4fe);
}

.macro-segment.fat {
  background: linear-gradient(to right, #fbbf24, #fcd34d);
}

.macro-legend {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.875rem;
}

.legend-color {
  width: 1rem;
  height: 1rem;
  border-radius: 0.25rem;
}

.legend-color.protein {
  background: linear-gradient(to right, #ec4899, #f472b6);
}

.legend-color.carbs {
  background: linear-gradient(to right, #a855f7, #d8b4fe);
}

.legend-color.fat {
  background: linear-gradient(to right, #fbbf24, #fcd34d);
}

/* =====================================================
   식단 계획
   ===================================================== */
.meal-plan-section {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.section-title {
  font-size: 0.875rem;
  font-weight: 600;
  color: #d1d5db;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.icon-sm {
  width: 1rem;
  height: 1rem;
  color: #c084fc;
}

.meal-cards {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.meal-card {
  background: linear-gradient(to right, rgba(31, 41, 55, 0.5), rgba(17, 24, 39, 0.5));
  border: 1px solid transparent;
  border-radius: 0.75rem;
  padding: 1rem;
  animation: slideUp 0.5s ease-out forwards;
  opacity: 0;
}

.meal-breakfast { border-color: rgba(249, 115, 22, 0.3); }
.meal-snack { border-color: rgba(34, 197, 94, 0.3); }
.meal-lunch { border-color: rgba(59, 130, 246, 0.3); }
.meal-pre-workout { border-color: rgba(234, 179, 8, 0.3); }
.meal-post-workout { border-color: rgba(220, 38, 38, 0.3); }
.meal-dinner { border-color: rgba(168, 85, 247, 0.3); }
.meal-evening-snack { border-color: rgba(236, 72, 153, 0.3); }
.meal-late-night { border-color: rgba(99, 102, 241, 0.3); }
.meal-late-snack { border-color: rgba(107, 114, 128, 0.3); }
.meal-evening-tea { border-color: rgba(16, 185, 129, 0.3); }

.meal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}

.meal-meta {
  display: flex;
  gap: 0.5rem;
  font-size: 0.75rem;
}

.meal-type {
  background: rgba(168, 85, 247, 0.2);
  border: 1px solid rgba(168, 85, 247, 0.3);
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  color: #c084fc;
}

.meal-time {
  color: #9ca3af;
  font-style: italic;
}

.meal-calorie {
  font-size: 0.875rem;
  font-weight: 600;
  color: #fbbf24;
}

.meal-name {
  font-size: 0.95rem;
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.meal-description {
  font-size: 0.75rem;
  color: #d1d5db;
  margin-bottom: 0.5rem;
  line-height: 1.4;
}

.meal-benefits {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.benefit-tag {
  font-size: 0.7rem;
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.2);
  padding: 0.25rem 0.5rem;
  border-radius: 0.25rem;
  color: #10b981;
}

/* =====================================================
   팁과 경고
   ===================================================== */
.tips-section {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1rem;
}

.tips-card,
.warnings-card {
  background: linear-gradient(to bottom right, #111827, #1f2937, #000000);
  border-radius: 1rem;
  padding: 1.5rem;
  border: 1px solid rgba(55, 65, 81, 0.5);
}

.tips-title,
.warnings-title {
  font-size: 0.95rem;
  font-weight: 600;
  margin-bottom: 1rem;
}

.tips-list,
.warnings-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.tips-list li,
.warnings-list li {
  font-size: 0.875rem;
  color: #d1d5db;
  padding-left: 1.5rem;
  position: relative;
}

.tips-list li::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #10b981;
  font-weight: bold;
}

.warnings-list li::before {
  content: '!';
  position: absolute;
  left: 0;
  color: #f97316;
  font-weight: bold;
}

/* =====================================================
   버튼
   ===================================================== */
.button-group {
  display: flex;
  gap: 0.75rem;
  margin-top: 1rem;
  margin-bottom: 2rem;
}

.btn {
  flex: 1;
  padding: 0.875rem;
  border-radius: 0.75rem;
  font-weight: 600;
  font-size: 0.875rem;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}

.btn-primary {
  background: linear-gradient(to right, #9333ea, #db2777);
  color: white;
}

.btn-primary:hover {
  opacity: 0.9;
  transform: translateY(-2px);
}

.btn-secondary {
  background-color: #1f2937;
  color: #e5e7eb;
  border: 1px solid #4b5563;
}

.btn-secondary:hover {
  background-color: #374151;
}

/* =====================================================
   애니메이션
   ===================================================== */
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

@keyframes bounce {
  0%, 100% { transform: translateY(-25%); animation-timing-function: cubic-bezier(0.8,0,1,1); }
  50% { transform: none; animation-timing-function: cubic-bezier(0,0,0.2,1); }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>