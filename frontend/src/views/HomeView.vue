<template>
  <div class="home-view retro-theme" @click="initAudioContext">

    <section class="page daily-page">
      <div class="page-content">
        
        <div class="retro-header"><span class="blinking-cursor">▶</span> PLAYER_DATE: {{ formattedDate }}</div>

        <div class="pixel-box main-stat-box">
          <div class="stat-header">
            <span class="label">(Kal)</span>
            <span class="val">{{ currentEnergy }} / {{ maxEnergy }}</span>
          </div>
          <div class="retro-progress-container" @click="triggerLevelUp">
            <div class="retro-progress-bar hp-bar" :style="{ width: hpPercent + '%' }"></div>
            <div class="click-hint">CLICK BAR TO LEVEL UP!</div>
          </div>
        </div>

        <div class="game-screen-container" @click="handleScreenClick">
          <div class="pixel-border">
            <div class="screen-bg">
              <div class="level-badge" :class="{ 'level-up-anim': isLevelingUp }">
                {{ isLevelingUp ? "LEVEL UP!" : `Lv.${currentLevel}` }}
              </div>

              <img
                :src="currentCharacterImage"
                alt="Character"
                class="character-gif pixelated"
                :class="{ bounce: isBouncing }"
              />

              <div class="screen-xp-area">
                <div class="screen-xp-label">EXP</div>
                <div class="screen-xp-bar">
                  <div class="screen-xp-fill" :style="{ width: currentLevelExpPercent + '%' }"></div>
                </div>
              </div>

              <div class="edit-hint">Click screen to change character</div>
            </div>
          </div>
        </div>

        <div class="pixel-box stats-box">
          <h3 class="box-title">PLAYER STATS</h3>
          <div class="stat-row" v-for="(stat, idx) in stats" :key="idx">
            <div class="stat-icon">{{ stat.label }}</div>
            <div class="stat-bar-group">
              <div class="retro-bar-bg">
                <div class="retro-bar-fill" :class="stat.class" :style="{ width: stat.percent }"></div>
              </div>
              <span class="stat-val">{{ stat.val }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="page meal-page">
      <div class="page-content">

        <div v-if="todayMeals.length === 0" class="pixel-card interactive" @click="handleMealClick">
          <div class="card-inner">
            <h2>식단 기록</h2>
            <div class="icon-8bit">🍗</div>
            <p class="pixel-text">오늘의 식사를<br />기록하지 않았습니다.</p>
            <button class="retro-btn press-start">INSERT COIN</button>
          </div>
        </div>

        <div v-else class="meal-log-container">
          <div class="retro-header-sm">
            <span>INVENTORY (MEALS)</span>
            <span class="total-xp">TOTAL XP: {{ todayMeals.reduce((acc, cur) => acc + cur.cal, 0) }}</span>
          </div>

          <div class="meal-list">
            <div v-for="meal in todayMeals" :key="meal.id" class="meal-slot" @click="handleMealClick">
              <div class="slot-icon-box">{{ meal.icon }}</div>
              <div class="slot-info">
                <div class="slot-top">
                  <span class="meal-type-badge">{{ meal.type }}</span>
                  <span class="meal-cal">{{ meal.cal }} XP</span>
                </div>
                <div class="meal-name">{{ meal.name }}</div>
              </div>
            </div>

            <div v-if="!isAllMealsRecorded" class="meal-slot add-slot" @click="handleMealClick">
              <span class="plus-icon">+</span>
              <span class="add-text">ADD NEW ITEM</span>
            </div>
          </div>
        </div>

        <div class="ai-btn-container">
  <button 
    class="ai-analyze-btn" 
    :class="{ 'active': isAllMealsRecorded }"
    :disabled="!isAllMealsRecorded"
    @click="startAIAnalysis"
  >
    <div class="btn-shimmer" v-if="isAllMealsRecorded"></div>
    
    <div class="btn-content">
      <span class="btn-icon">
        <template v-if="isAllMealsRecorded">⚡</template>
        <template v-else>🔒</template>
      </span>
      <span class="btn-text">
        {{ isAllMealsRecorded ? 'AI STRATEGY ANALYSIS' : `LOCKED (${recordedCount}/4)` }}
      </span>
    </div>
    
    <div class="corner-line top-left"></div>
    <div class="corner-line bottom-right"></div>
  </button>
</div>


      </div>
    </section>

    <section class="page water-page">
      <div class="page-content split-layout">
        
        <div 
          class="pixel-card interactive blue-theme half-card" 
          @click="handleWaterClick"
        >
          <template v-if="waterData.water === 0">
            <h1 class="page-title pixel-font">수분 섭취</h1>
            <div class="empty-state-icon">💧</div>
            <p class="pixel-text-center">기록이 없습니다.</p>
            <button class="retro-btn blue-btn sm-btn">START</button>
          </template>

          <template v-else>
            <h1 class="page-title pixel-font">수분 섭취</h1>
            

            <div class="mana-bar-container">
              <div
                class="mana-bar-fill"
                :style="{ width: Math.min((waterData.water / waterData.goal) * 100, 100) + '%' }"
              >
                <div class="glare-effect"></div>
              </div>
              <div class="mana-text-overlay">
                {{ waterData.water }}L <span class="divider">/</span> {{ waterData.goal }}L
              </div>
            </div>
            <button class="retro-btn blue-btn sm-btn" style="margin-top:auto;">DRINK</button>
          </template>
        </div>

       <div 
          class="pixel-card interactive green-theme half-card" 
          @click="handleWeightClick"
        >
          <template v-if="weightData.weight == 0.0">
            <h1 class="page-title pixel-font">체중 기록</h1>
            <div class="empty-state-icon">⚖️</div>
            <p class="pixel-text-center">기록이 없습니다.</p>
            <button class="retro-btn green-btn sm-btn">RECORD</button>
          </template>

          <template v-else>
            <div class="weight-dashboard">
              <h1 class="page-title pixel-font">체중 기록</h1>
              <div class="score-display">
                <span class="score-val">{{ weightData.weight }}</span>
                <span class="score-unit">KG</span>
                <div class="score-change" :class="weightData.diff > 0 ? 'bad' : 'good'">
                  <span class="change-icon">{{ weightData.diff > 0 ? "▲" : "▼" }}</span>
                  {{ Math.abs(weightData.diff) }}kg
                  <span class="change-text">{{ weightData.diff > 0 ? "(WARN)" : "(GOOD)" }}</span>
                </div>
              </div>
            </div>
            <button class="retro-btn green-btn sm-btn" style="margin-top:auto;">UPDATE</button>
          </template>
        </div>

      </div>
    </section>

    

    <div v-if="showCharModal" class="modal-overlay" @click.self="showCharModal = false">
      <div class="pixel-card char-select-modal" @click.stop>
        <div class="retro-header-sm">SELECT CHARACTER</div>
        <div class="char-grid">
          <div
            v-for="char in characterList"
            :key="char.id"
            class="char-slot"
            :class="{ locked: char.isLocked, selected: char.id === selectedCharId }"
            @click="selectCharacter(char)"
          >
            <div v-if="char.isLocked" class="lock-overlay">🔒</div>
            <img :src="char.src" class="grid-char-img" />
            <span class="char-num">NO.{{ char.id }}</span>
          </div>
        </div>
        <button class="retro-btn" @click="showCharModal = false">CLOSE</button>
      </div>
    </div>

    <div v-if="showModal" class="modal-overlay" @click="closeModal"></div>
<MealRecordModal
      v-if="showMealModal"
      @close="closeMealModal"
      :date-to-use="formattedDate"
      :initial-meal-data="todayMealMap"
    />    <WaterRecordModal
      v-if="showWaterModal"
      @close="closeWaterModal"
      @update-water="handleWaterUpdate"
      :initial-amount="waterData.water"
      :initial-goal="waterData.goal"
      :date-to-use="formattedDate"
    />
    <WeightRecordModal
      v-if="showWeightModal"
      @close="closeWeightModal"
      @update-weight="handleWeightUpdate"
      :date-to-use="formattedDate"
    />
    <Transition name="fade">
  <div v-if="isAiLoading" class="loading-overlay">
    <div class="loading-content">
      <div class="loading-icon">🤖</div>
      <div class="loading-status-bar">
        <div class="status-fill"></div>
      </div>
      <p class="loading-text">{{ loadingText }}</p>
    </div>
  </div>
</Transition>
<template>
  <Teleport to="body">
  <Transition name="retro-modal">
    <div v-if="isResultModalOpen" class="ai-result-overlay" @click.self="closeResultModal">
      <div class="ai-result-modal retro-pixel-border">
        <div class="scanline"></div>
        
        <div class="modal-header-section">
          <div class="pixel-tag">SYSTEM_REPORT</div>
          <h2 class="modal-main-title">
            <span class="glitch" :data-text="analysisResult?.dailyTitle">{{ analysisResult?.dailyTitle }}</span>
          </h2>
        </div>

        <div class="modal-body-scroll">
          <div class="score-card-retro premium-border">
  <div class="rank-aura-container">
    <div class="rank-aura" :class="'aura-' + analysisResult?.rank"></div>
    <div class="rank-container">
      <div class="rank-label">CLASS</div>
      <div class="rank-visual-mega" :class="'rank-' + analysisResult?.rank">
        {{ analysisResult?.rank }}
      </div>
    </div>
  </div>
  
  <div class="score-container-mega">
    <div class="score-label-neon">TOTAL GAINED XP</div>
    <div class="score-number-glitch" :data-text="analysisResult?.score">
      {{ analysisResult?.score }}
    </div>
    <div class="xp-bar-mini">
      <div class="xp-bar-fill"></div>
    </div>
  </div>
</div>

          <div class="insights-section">
            <h3 class="section-title-retro">▶ ANALYSIS_LOG</h3>
            <div class="insight-list-retro">
              <div v-for="(item, idx) in analysisResult?.insights" :key="idx" class="insight-card-retro" :class="item.type">
                <div class="insight-header">
                  <span class="status-dot"></span>
                  <h4 class="insight-title">[{{ item.title }}]</h4>
                </div>
                <p class="insight-description">{{ item.description }}</p>
              </div>
            </div>
          </div>

          <div class="summary-box-retro">
            <div class="npc-thumb">🤖</div>
            <p class="summary-text">{{ analysisResult?.oneLineSummary }}</p>
          </div>
        </div>

        <button class="retro-confirm-btn" @click="closeResultModal">
          RETURN TO MENU (A)
        </button>
      </div>
    </div>
  </Transition>
</Teleport>

</template>
    <Footer></Footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useConfigStore } from "@/stores/configStore";
import { useAuthStore } from "@/stores/authStore";
import { useRoute } from "vue-router";
import { analyzeDiet } from "@/api/diet/dietApi";
import Footer from "@/components/utils/Footer.vue";
import dayjs from "dayjs";
import confetti from "canvas-confetti";

import WaterRecordModal from "@/components/record/WaterRecordModal.vue";
import WeightRecordModal from "@/components/record/WeightRecordModal.vue";
import MealRecordModal from "@/components/record/MealRecordModal.vue";

/* --- 기존 import 아래에 추가 --- */
const isAiLoading = ref(false); // 로딩 상태
const loadingText = ref("AI 분석 서버 연결 중..."); // 로딩 멘트

// [수정] 4가지 식사가 모두 기록되었는지 확인
const isAllMealsRecorded = computed(() => {
  // 'todayDiet'가 아니라 'todayMeals'를 사용해야 합니다.
  if (!todayMeals.value) return false;

  // 프로젝트에서 사용하는 한글 키값 기준
  const requiredTypes = ['아침', '점심', '저녁', '간식'];
  
  // 현재 기록된 식사 타입들 추출 (meal.mealType이 아니라 meal.type)
  const recordedTypes = todayMeals.value.map(d => d.type);
  
  // 4가지가 모두 있는지 확인
  return requiredTypes.every(type => recordedTypes.includes(type));
});

// [수정] 기록된 식사 개수 (버튼 표시용)
const recordedCount = computed(() => {
  return todayMeals.value ? todayMeals.value.length : 0;
});

/* --- 기존 ref 선언부에 추가 --- */
const isResultModalOpen = ref(false);
const analysisResult = ref(null);

const closeResultModal = () => {
  isResultModalOpen.value = false;
};


const authStore = useAuthStore();
const config = useConfigStore();
const route = useRoute();
const MEMBER_ID = authStore.memberId;
const TODAY_DATE = new Date().toISOString().split("T")[0];
const API_ENDPOINT = config.API_ENDPOINT;

const formattedDate = computed(() => {
  const routeDate = route.query.date;
  return routeDate ? dayjs(routeDate).format("YYYY-MM-DD") : dayjs().format("YYYY-MM-DD");
});

const currentLevel = computed(() => authStore.level || 1);
const currentLevelExpPercent = computed(() => authStore.exp || 0);
const selectedCharId = ref(1);
const showCharModal = ref(false);

const getCharImage = (id) => {
  return new URL(`../assets/images/characters/${id}.png`, import.meta.url).href;
};

const currentCharacterImage = computed(() => getCharImage(selectedCharId.value));

const characterList = computed(() => {
  return Array.from({ length: 16 }, (_, i) => {
    const id = i + 1;
    return {
      id,
      src: getCharImage(id),
      isLocked: id > currentLevel.value,
    };
  });
});

const selectCharacter = async (char) => {
  if (char.isLocked) return;

  try {
    // 1. API 호출 (캐릭터 변경 반영)
    // URL은 프로젝트 설정에 따라 /api/member/character 등으로 수정될 수 있습니다.
    const response = await fetch(`${API_ENDPOINT}/api/member/character`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      // Java 컨트롤러에서 @RequestBody Integer로 받으므로 char.id만 보냄
      body: JSON.stringify({
        memberId: MEMBER_ID,
        characterNumber: char.id,
      }), 
    });

    if (response.ok) {
      selectedCharId.value = char.id;
      

      playRetroSound("coin");
      showCharModal.value = false;
      
      console.log(`Character changed to NO.${char.id}`);
    } else {
      console.error("캐릭터 변경 실패:", response.status);
      alert("캐릭터를 변경할 수 없습니다.");
    }
  } catch (error) {
    console.error("API 호출 중 에러 발생:", error);
    alert("서버 통신 중 오류가 발생했습니다.");
  }
};

const handleScreenClick = () => {
  showCharModal.value = true;
};

const todayMealMap = ref({ 아침: null, 점심: null, 저녁: null, 간식: null });
const todayMeals = computed(() => {
  if (!todayMealMap.value) return [];
  const mealIcons = { 아침: "🍳", 점심: "🍖", 저녁: "🍲", 간식: "🍰" };
  return Object.entries(todayMealMap.value)
    .filter(([_, meal]) => meal !== null)
    .map(([type, meal], idx) => ({
      id: meal.id ?? idx,
      type,
      icon: mealIcons[type] || "🍽️",
      cal: Math.round(meal.energy || 0),
      name: meal.foods?.map((f) => f.name).join(", ") || "기록된 음식 없음",
    }));
});
const startAIAnalysis = async () => {
  if (!isAllMealsRecorded.value) {
    alert("오늘의 4가지 식단(아침, 점심, 저녁, 간식)을 모두 기록해야 분석이 가능합니다!");
    return;
  }
  if (isAiLoading.value) return;

  isAiLoading.value = true;
  
  const messages = ["🎯 데이터 스캔 중...", "🥩 영양 분석 중...", "🤖 AI 전략 수립 중...", "✨ 결과 정리 중..."];
  let msgIndex = 0;
  loadingText.value = messages[0];
  const msgInterval = setInterval(() => {
    msgIndex = (msgIndex + 1) % messages.length;
    loadingText.value = messages[msgIndex];
  }, 800);

  try {
    const data = await analyzeDiet(MEMBER_ID, TODAY_DATE);

    analysisResult.value = {
      score: data.score,
      rank: data.rank,
      dailyTitle: data.dailyTitle,
      oneLineSummary: data.oneLineSummary,
      insights: data.insights
    };

    await new Promise(resolve => setTimeout(resolve, 2500));
    isResultModalOpen.value = true;
    
  } catch (error) {
    console.error("AI 분석 호출 실패:", error);
    
    // 💡 백엔드 연결 실패 시 Mock 데이터 표시
    analysisResult.value = {
      score: 85,
      rank: "A",
      dailyTitle: "균형잡힌 하루였습니다!",
      oneLineSummary: "단백질 섭취가 우수하고, 전체적인 영양 밸런스가 좋습니다. 내일도 화이팅!",
      insights: [
        {
          type: "good",
          iconType: "muscle",
          title: "단백질 섭취 우수",
          description: "목표 대비 120% 달성으로 근육 성장에 도움이 됩니다."
        },
        {
          type: "warning",
          iconType: "warning",
          title: "탄수화물 다소 높음",
          description: "권장량보다 15% 높습니다. 저녁 식사량을 조절해보세요."
        },
        {
          type: "good",
          iconType: "check",
          title: "수분 섭취 적정",
          description: "하루 2L 목표를 달성했습니다."
        }
      ]
    };
    
    await new Promise(resolve => setTimeout(resolve, 1000));
    isResultModalOpen.value = true;
  } finally {
    clearInterval(msgInterval);
    isAiLoading.value = false;
  }
};

const waterData = ref({ water: 0, goal: 2.0 });
const weightData = ref({ weight: 0.0, diff: 0.0 });

const showModal = ref(false);
const showWaterModal = ref(false);
const showWeightModal = ref(false);
const showMealModal = ref(false);

const isLevelingUp = ref(false);
const isBouncing = ref(false);
const dialogText = ref('"오늘도 힘내보자구!"');

const maxEnergy = 3000;
const currentEnergy = computed(() => {
  return Object.values(todayMealMap.value)
    .reduce((acc, meal) => acc + (meal ? meal.energy : 0), 0)
    .toFixed(0);
});
const hpPercent = computed(() => Math.min((currentEnergy.value / maxEnergy) * 100, 100).toFixed(0));

const stats = computed(() => {
  const tProtein = Object.values(todayMealMap.value).reduce((acc, meal) => acc + (meal ? meal.protein : 0), 0);
  const tCarb = Object.values(todayMealMap.value).reduce((acc, meal) => acc + (meal ? meal.carbohydrate : 0), 0);
  const tFat = Object.values(todayMealMap.value).reduce((acc, meal) => acc + (meal ? meal.fat : 0), 0);
  const total = tProtein + tCarb + tFat;
  const getP = (v) => (total > 0 ? ((v / total) * 100).toFixed(0) : 0);
  return [
    { label: "⚡탄수화물", class: "carb", percent: `${getP(tCarb)}%`, val: `${tCarb.toFixed(1)}g` },
    { label: "🛡️단백질", class: "protein", percent: `${getP(tProtein)}%`, val: `${tProtein.toFixed(1)}g` },
    { label: "🔮지방", class: "fat", percent: `${getP(tFat)}%`, val: `${tFat.toFixed(1)}g` },
  ];
});

const initAudioContext = () => {};
const playRetroSound = (type) => {
  if (type === "coin" || type === "jump") {
    isBouncing.value = true;
    setTimeout(() => (isBouncing.value = false), 500);
  }
};
const triggerLevelUp = () => {
  if (isLevelingUp.value) return;
  isLevelingUp.value = true;
  dialogText.value = "LEVEL UP! 능력이 상승했다!";
  confetti({ particleCount: 120, spread: 70, origin: { y: 0.6 } });
  setTimeout(() => {
    isLevelingUp.value = false;
    dialogText.value = '"다음 레벨로 가보자!"';
  }, 3000);
};

const handleMealClick = () => (showMealModal.value = true);
const closeMealModal = async () => {
  showMealModal.value = false;
  await fetchDailyDiet();
};
const closeWaterModal = () => (showWaterModal.value = false);
const closeWeightModal = () => (showWeightModal.value = false);
const handleWaterClick = () => (showWaterModal.value = true);
const handleWeightClick = () => (showWeightModal.value = true);
const handleWaterUpdate = async (newAmount) => (waterData.value.water = newAmount);
const handleWeightUpdate = async (newWeight) => {
  if (newWeight) weightData.value.weight = newWeight;
  await fetchWeightData();
};
const closeModal = () => (showModal.value = false);

async function fetchDailyDiet() {
  const url = `${API_ENDPOINT}/diets/meal-daily?memberId=${MEMBER_ID}&date=${formattedDate.value}`;
  try {
    const res = await fetch(url);
    if (res.ok) todayMealMap.value = await res.json();
  } catch (e) { console.error(e); }
}

async function fetchHydrationData() {
  const url = `${API_ENDPOINT}/diets/hydration?memberId=${MEMBER_ID}&date=${formattedDate.value}`;
  try {
    const res = await fetch(url);
    if (res.ok) waterData.value.water = await res.json();
  } catch (e) { console.error(e); }
}

async function fetchWeightData() {
  const url = `${API_ENDPOINT}/api/member/weight?memberId=${MEMBER_ID}&date=${formattedDate.value}`;
  try {
    const res = await fetch(url);
    if (res.ok) {
      const d = await res.json();
      weightData.value.weight = d.weight;
      weightData.value.diff = d.diff;
    }
  } catch (e) { console.error(e); }
}

onMounted(async () => {
  await fetchDailyDiet();
  await fetchHydrationData();
  await fetchWeightData();
});
</script>

<style scoped>
@import url("https://cdn.jsdelivr.net/gh/neodgm/neodgm-webfont@latest/neodgm/style.css");

.retro-theme {
  --bg-color: #202028;
  --text-color: #e0e0e0;
  --primary-color: #ff0055;
  --secondary-color: #00e5ff;
  --accent-color: #ffcc00;
  font-family: "NeoDunggeunmo", monospace;
  background-color: var(--bg-color);
  color: var(--text-color);
  height: 100vh;
  overflow-y: scroll;
  scroll-snap-type: y mandatory;
}

.section-title-tag {
  display: inline-block;
  background-color: #000;
  color: #fff;
  border: 2px solid #fff;
  padding: 4px 12px;
  font-size: 0.8rem;
  margin: 0 auto 0.1rem;
  width: fit-content;
  align-self: center;
  box-shadow: 4px 4px 0px rgba(0,0,0,0.5);
  letter-spacing: 1px;
}

.daily-page .section-title-tag { border-color: var(--secondary-color); color: var(--secondary-color); }
.meal-page .section-title-tag { border-color: #ffd700; color: #ffd700; }
.water-page .section-title-tag { border-color: #00e5ff; color: #00e5ff; }
.weight-page .section-title-tag { border-color: #d500f9; color: #d500f9; }

.meal-type-badge {
  font-size: 0.65rem;
  background: #ff0055;
  color: #fff;
  padding: 2px 6px;
  border: 1px solid #fff;
  display: inline-block;
  line-height: 1;
  text-shadow: 1px 1px 0 #000;
}

.slot-top {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 8px;
  margin-bottom: 4px;
}

.meal-cal {
  font-size: 0.75rem;
  color: #00e5ff;
}

/* .scanlines {
  position: fixed;
  top: 0; left: 0; width: 100%; height: 100%;
  pointer-events: none;
  background: linear-gradient(rgba(18, 16, 16, 0) 50%, rgba(0, 0, 0, 0.25) 50%),
    linear-gradient(90deg, rgba(255, 0, 0, 0.06), rgba(0, 255, 0, 0.02), rgba(0, 0, 255, 0.06));
  background-size: 100% 4px, 6px 100%;
  z-index: 999;
} */

.page {
  min-height: 100vh;
  scroll-snap-align: start;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start; /* 세로 중앙 정렬 */
  
  /* 헤더와 푸터 높이만큼 패딩을 넉넉하게 줍니다 */
  /* padding-top: 5rem;    상단 헤더 공간 확보 */
  padding-bottom: 6rem; /* 하단 푸터 공간 확보 */
  padding-left: 1rem;
  padding-right: 1rem;
  box-sizing: border-box;
}
.page-content {
  width: 100%;
  max-width: 600px;
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}
.page-content.split-layout {
  height: 100%;
  max-height: 70vh; /* 너무 길어지지 않게 제한 */
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 1.5rem; /* 두 카드 사이의 간격 */
}

/* 반반 카드 스타일 (Flex로 공간 균등 분배) */
.half-card {
  flex: 1; /* 남은 공간을 1:1로 차지 */
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 0; /* Flexbox 내부 스크롤 방지 */
}

/* 내부 요소들이 너무 붙지 않게 여백 조정 */
.half-card .page-title {
  margin-bottom: 0.5rem;
  font-size: 1.1rem;
}

.empty-state-icon {
  font-size: 2.5rem;
  margin: 0.5rem 0;
}

/* 체중 대시보드 스타일 미세 조정 */
.weight-dashboard {
  font-size: 2rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  width: 100%;
}

.score-display {
  background: rgba(0,0,0,0.2);
  padding: 5px 15px;
  border-radius: 4px;
  margin: 5px 0;
}
.score-val {
  font-size: 2.8rem;
  color: #fff;
  font-weight: bold;
  line-height: 1;
  text-shadow: 2px 2px 0 #000; /* 글자 입체감 */
  letter-spacing: 2px;
}

/* 단위 (KG) */
.score-unit {
  font-size: 1rem;
  color: #888;
  margin-top: 2px;
  margin-bottom: 10px;
  font-family: monospace;
}

/* 증감 표시 박스 (가장 아래 배치) */
.score-change {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 0.85rem;
  background: rgba(0, 0, 0, 0.4);
  border: 1px solid transparent;
  width: 100%; /* 박스 꽉 채우기 */
  justify-content: center;
  box-sizing: border-box;
}

/* 체중 감소 (좋음 - 파란색/민트색 계열) */
.score-change.good {
  color: #ffd700;
  border-color: #ffd700;
  background: rgba(0, 229, 255, 0.1);
}

/* 체중 증가 (나쁨 - 붉은색 계열) */
.score-change.bad {
  color: #ff0055;
  border-color: #ff0055;
  background: rgba(255, 0, 85, 0.1);
}

.change-icon {
  font-size: 0.7rem;
}

.change-text {
  font-size: 0.7rem;
  opacity: 0.8;
  margin-left: auto; /* 텍스트를 오른쪽 끝으로 밀어줌 (선택사항) */
}

/* 반응형: 화면이 너무 작으면 갭을 줄임 */
@media (max-height: 700px) {
  .page-content.split-layout {
    gap: 0.8rem;
  }
  .page {
    padding-top: 2rem;
    padding-bottom: 5rem;
  }
  .pixel-box, .pixel-card {
    padding: 0.6rem;
  }
}
.pixel-box, .pixel-card {
  border: 4px solid #fff;
  box-shadow: 4px 4px 0px 0px rgba(0, 0, 0, 0.5);
  position: relative;
  transition: transform 0.1s;
  cursor: pointer;
}

.pixel-box { background: #2d2d3a; padding: 0.8rem; }
.pixel-card { padding: 1rem; text-align: center; background: #e6dac3; color: #3e2723; border-color: #5d4037; }
.interactive:active { transform: translate(2px, 2px); box-shadow: 2px 2px 0 rgba(0, 0, 0, 0.5); }

.page.daily-page { background: #222034; }
.meal-page { background: #4b692f; }
.water-page { background: #1a1a24; }
.weight-page { background: #2a0a29; }

.retro-header { text-align: center; color: var(--secondary-color); margin-bottom: 0.2rem; font-size: 1.1rem; }
.blinking-cursor { animation: blink 1s step-end infinite; }
@keyframes blink { 50% { opacity: 0; } }

.retro-progress-container { height: 20px; background: #333; border: 2px solid #fff; padding: 2px; position: relative; margin-top: 5px; }
.retro-progress-bar { height: 100%; background: linear-gradient(90deg, #ff0055, #ff5500); transition: width 0.5s steps(10); }
.click-hint { position: absolute; top: -18px; right: 0; font-size: 0.6rem; color: var(--accent-color); animation: blink 0.5s infinite alternate; }

.game-screen-container .pixel-border { border: 6px solid #444; background: #8fb8ca; border-radius: 6px; overflow: hidden; }
.screen-bg {
  background: url("https://i.pinimg.com/originals/10/78/3f/10783f947938361b02390a382c44843b.png") repeat-x bottom;
  background-size: cover; width: 100%; height: 150px;
  display: flex; flex-direction: column; align-items: center; justify-content: center; position: relative;
}

.edit-hint { position: absolute; top: 5px; right: 5px; font-size: 0.6rem; color: rgba(255, 255, 255, 0.6); animation: blink 2s infinite; }
.character-gif {
  width: auto;       /* 너비를 강제하지 않고 원본 비율에 따름 */
  max-width: 90%;    /* 혹시 너무 넓은 이미지가 와도 화면을 넘지 않게 제한 */
  height: 200px;     /* 높이를 고정하여 게임 화면 내에서 일정한 크기 유지 (max-height 대신 height 권장) */
  image-rendering: pixelated;
  margin-bottom: 5px;
  object-fit: contain; /* 비율을 유지하며 영역 안에 쏙 들어가게 함 (안전장치) */
}
.bounce { animation: bounce 0.5s infinite alternate; }
@keyframes bounce { from { transform: translateY(0); } to { transform: translateY(-10px); } }

.level-badge { position: absolute; top: 8px; left: 8px; background: rgba(0, 0, 0, 0.7); color: #fff; padding: 2px 6px; font-size: 0.7rem; border: 2px solid #fff; z-index: 10; }
.screen-xp-area { position: absolute; bottom: 0; left: 0; width: 100%; display: flex; align-items: center; background: rgba(0, 0, 0, 0.6); padding: 3px 8px; gap: 8px; box-sizing: border-box; }
.screen-xp-label { font-size: 0.6rem; color: #00e5ff; font-weight: bold; }
.screen-xp-bar { flex: 1; height: 6px; background: #222; border: 1px solid #777; overflow: hidden; }
.screen-xp-fill { height: 100%; background: #00e5ff; transition: width 0.5s; box-shadow: 0 0 5px #00e5ff; }

.box-title { margin: 0 0 0.5rem 0; font-size: 0.9rem; color: #aaa; }
.stat-row { display: flex; align-items: center; gap: 8px; margin-bottom: 0.3rem; }
.stat-icon { width: 70px; font-size: 0.75rem; }
.stat-bar-group { flex: 1; display: flex; align-items: center; gap: 8px; }
.retro-bar-bg { flex: 1; height: 10px; background: #111; border: 1px solid #555; }
.retro-bar-fill { height: 100%; }
.retro-bar-fill.carb { background: #ffd700; }
.retro-bar-fill.protein { background: #ff0055; }
.retro-bar-fill.fat { background: #00e5ff; }
.stat-val { font-size: 0.75rem; min-width: 35px; text-align: right; }

.retro-btn { margin-top: 0.8rem; background: #ff0055; color: #fff; border: 2px solid #fff; padding: 8px 16px; cursor: pointer; box-shadow: 3px 3px 0 #000; font-size: 0.9rem; }

.blue-theme { 
  border-color: #00ffff; 
  color: #00ffff; 
  background: rgba(0, 20, 40, 0.6); /* 투명도 살짝 줌 */
}
.blue-btn { 
  background: #00ffff; 
  color: #000; 
  box-shadow: 3px 3px 0 rgba(0, 255, 255, 0.4);
}
.purple-theme { border-color: #d500f9; color: #d500f9; background: #000; }
.purple-btn { background: #d500f9; color: #fff; }
.green-theme { 
  border-color: #39ff14; /* 네온 라임 */
  color: #39ff14; 
  background: rgba(10, 30, 10, 0.6); 
}

.green-btn { 
  background: #39ff14; 
  color: #000;
  box-shadow: 3px 3px 0 rgba(57, 255, 20, 0.4);
}
.green-theme .score-display {
  border-color: #39ff14; /* 박스 테두리도 초록색으로 통일 */
  box-shadow: inset 0 0 10px rgba(57, 255, 20, 0.1);
}

.green-theme .score-val {
  color: #fff;
  text-shadow: 0 0 5px rgba(57, 255, 20, 0.5); /* 초록색 글로우 효과 */
}

.green-theme .score-unit {
  color: #8fbc8f; /* 약간 채도 낮춘 초록색 */
}
.meal-log-container { width: 100%; display: flex; flex-direction: column; gap: 0.5rem; }
.retro-header-sm { display: flex; justify-content: space-between; border-bottom: 2px dashed #fff; padding-bottom: 5px; font-size: 0.85rem; color: #ffd700; }
.meal-list { display: flex; flex-direction: column; gap: 8px; max-height: 55vh; overflow-y: auto; }
.meal-slot { display: flex; align-items: center; background: rgba(0, 0, 0, 0.6); border: 2px solid #fff; padding: 8px; gap: 10px; box-shadow: 3px 3px 0 rgba(0, 0, 0, 0.3); }
.meal-name { font-size: 0.9rem; color: #fff; font-weight: bold; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 200px; }

.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0, 0, 0, 0.85); z-index: 9999; display: flex; justify-content: center; align-items: center; }

/* --- [수정 핵심] 캐릭터 모달 최적화 스타일 --- */
.char-select-modal {
  width: 90%;
  max-width: 400px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  background: #2d2d3a !important;
  color: #fff !important;
  border: 4px solid var(--secondary-color) !important;
  padding: 1rem;
  box-sizing: border-box;
}

.char-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr); /* 4열 배치 */
  gap: 10px;
  margin: 15px 0;
  overflow-y: auto;
  padding-right: 5px;
  flex: 1;
}

.char-grid::-webkit-scrollbar { width: 6px; }
.char-grid::-webkit-scrollbar-track { background: #1a1a1a; }
.char-grid::-webkit-scrollbar-thumb { background: var(--secondary-color); }

.char-slot {
  position: relative;
  aspect-ratio: 1 / 1;
  border: 2px solid #555;
  background: #1a1a1a;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  overflow: hidden;
}

.char-slot.selected {
  border-color: var(--secondary-color);
  background: rgba(0, 229, 255, 0.1);
  box-shadow: inset 0 0 8px var(--secondary-color);
}

.char-slot.locked { filter: grayscale(1); cursor: not-allowed; opacity: 0.5; }

.grid-char-img { width: 85%; height: 85%; object-fit: contain; image-rendering: pixelated; }

.lock-overlay {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex; align-items: center; justify-content: center;
  font-size: 1rem; z-index: 2;
}

.char-num {
  position: absolute;
  bottom: 1px; right: 2px;
  font-size: 0.5rem; color: #777;
}
/* --- 수분(마나) 바 스타일 --- */
.mana-bar-container {
  position: relative;
  width: 100%;
  height: 35px; /* 바 두께를 키워서 잘 보이게 함 */
  background-color: #001133; /* 빈 공간은 어두운 남색 */
  border: 4px solid #fff; /* 픽셀 테두리 */
  margin: 15px 0;
  box-shadow: inset 2px 2px 4px rgba(0, 0, 0, 0.8); /* 안쪽 그림자로 깊이감 */
  overflow: hidden;
}

.mana-bar-fill {
  height: 100%;
  background: linear-gradient(180deg, #40e0d0 0%, #00e5ff 40%, #0077be 100%); /* 입체적인 파란색 */
  transition: width 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94); /* 부드럽게 차오르는 애니메이션 */
  position: relative;
  box-shadow: 0 0 10px #00e5ff; /* 빛나는 효과 */
}

/* 유리 질감 효과 */
.glare-effect {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 40%;
  background: rgba(255, 255, 255, 0.3);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

/* 바 중앙에 위치하는 텍스트 */
.mana-text-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%); /* 정중앙 정렬 */
  color: #fff;
  font-size: 1rem;
  font-weight: bold;
  text-shadow: 2px 2px 0 #000, -1px -1px 0 #003366; /* 글자 가독성을 위한 테두리 */
  z-index: 5;
  letter-spacing: 1px;
}

.mana-text-overlay .divider {
  color: #aaddff;
  margin: 0 4px;
}

.blink-text {
  animation: pulse-opacity 2s infinite;
}

.pixel-desc {
  font-size: 0.8rem;
  color: #88ccff;
  margin-top: -5px;
  margin-bottom: 10px;
}

@keyframes pulse-opacity {
  0% { opacity: 1; }
  50% { opacity: 0.7; }
  100% { opacity: 1; }
}
/* --- 세련된 사이버 펑크 AI 버튼 --- */
.ai-btn-container {
  width: 100%;
  padding: 30px 20px;
  display: flex;
  justify-content: center;
  background: transparent;
}

.ai-analyze-btn {
  position: relative;
  width: 100%;
  max-width: 360px;
  height: 60px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 4px; /* 너무 둥글지 않게 하여 날카로운 느낌 강조 */
  overflow: hidden;
  cursor: not-allowed;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.btn-content {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: black;
  font-family: 'Orbitron', sans-serif; /* 게이밍 폰트가 없다면 기본 고딕 */
  letter-spacing: 2px;
  font-size: 0.9rem;
  font-weight: 800;
}

/* 활성화 상태 (Active) */
.ai-analyze-btn.active {
  cursor: pointer;
  background: rgba(110, 69, 226, 0.1);
  border: 1px solid rgba(0, 229, 255, 0.5);
  box-shadow: 0 0 20px rgba(0, 229, 255, 0.2);
}

.ai-analyze-btn.active .btn-content {
  color: #00e5ff;
  text-shadow: 0 0 8px rgba(0, 229, 255, 0.8);
}

/* 내부 빛 흐름 효과 (Shimmer) */
.btn-shimmer {
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(
    120deg,
    transparent,
    rgba(0, 229, 255, 0.2),
    transparent
  );
  transition: all 0.6s;
  animation: shimmer 3s infinite;
}

@keyframes shimmer {
  0% { left: -100%; }
  30% { left: 100%; }
  100% { left: 100%; }
}

/* 코너 장식 라인 */
.corner-line {
  position: absolute;
  width: 10px;
  height: 10px;
  border: 2px solid transparent;
  transition: all 0.3s;
}
.top-left { top: 0; left: 0; border-top-color: #444; border-left-color: #444; }
.bottom-right { bottom: 0; right: 0; border-bottom-color: #444; border-right-color: #444; }

.ai-analyze-btn.active .top-left { border-top-color: #00e5ff; border-left-color: #00e5ff; }
.ai-analyze-btn.active .bottom-right { border-bottom-color: #00e5ff; border-right-color: #00e5ff; }

/* 호버 시 반응 */
.ai-analyze-btn.active:hover {
  transform: translateY(-2px);
  background: rgba(0, 229, 255, 0.15);
  box-shadow: 0 0 30px rgba(0, 229, 255, 0.4);
}

.ai-analyze-btn.active:active {
  transform: scale(0.97);
}
/* --- 로딩 오버레이 스타일 (추가) --- */
.loading-overlay {
  position: fixed;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0, 0, 20, 0.95);
  z-index: 15000; /* 모달보다 높게 설정 */
  display: flex;
  justify-content: center;
  align-items: center;
  backdrop-filter: blur(5px);
}

.loading-content {
  text-align: center;
}

.loading-icon {
  font-size: 3rem;
  margin-bottom: 20px;
  animation: pulse 1s infinite;
}

.loading-status-bar {
  width: 200px;
  height: 4px;
  background: #333;
  margin: 15px auto;
  position: relative;
  overflow: hidden;
}

.status-fill {
  width: 100%;
  height: 100%;
  background: #00e5ff;
  animation: loading-scan 1.5s infinite;
}

@keyframes loading-scan {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

/* --- 모달 오버레이 스타일 --- */
.result-modal-overlay {
  position: fixed; /* 화면에 고정 */
  top: 0;
  left: 0;
  width: 100vw;    /* 너비 전체 */
  height: 100vh;   /* 높이 전체 */
  background: rgba(0, 0, 10, 0.9); /* 배경 어둡게 */
  backdrop-filter: blur(10px);     /* 배경 흐림 효과 */
  z-index: 99999;  /* 다른 어떤 요소보다 위에 뜨도록 아주 높은 값 설정 */
  display: flex;
  justify-content: center;
  align-items: center; /* 수직/수평 중앙 정렬 */
  padding: 20px;
  box-sizing: border-box;
  overflow-y: auto; /* 내용이 너무 길면 모달 내부에서 스크롤 가능하게 함 */
}

/* --- 모달 컨텐츠 스타일 --- */
.result-modal-content {
  position: relative;
  width: 100%;
  max-width: 380px;
  background: #1a1a24;
  border: 1px solid #00e5ff;
  padding: 25px;
  color: #fff;
  box-shadow: 0 0 30px rgba(0, 229, 255, 0.2);
  margin-top: auto;   /* 화면보다 길어질 경우 대비 */
  margin-bottom: auto;
}

/* 크롬/사파리용 스크롤바 디자인 (선택) */
.result-modal-content::-webkit-scrollbar {
  width: 4px;
}
.result-modal-content::-webkit-scrollbar-thumb {
  background: #00e5ff;
}

@keyframes pulse {
  0% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.1); opacity: 0.7; }
  100% { transform: scale(1); opacity: 1; }
}

/* 페이드 애니메이션 */
.fade-enter-active, .fade-leave-active { transition: opacity 0.5s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

.report-title {
  font-size: 0.75rem;
  color: #00e5ff;
  letter-spacing: 2px;
  font-weight: bold;
}

.header-line {
  height: 2px;
  background: linear-gradient(90deg, #00e5ff, transparent);
  margin-top: 5px;
  margin-bottom: 20px;
}

.result-main {
  display: flex;
  align-items: center;
  justify-content: space-around;
  margin-bottom: 30px;
  background: rgba(255, 255, 255, 0.03);
  padding: 20px;
  border-radius: 4px;
}

.rank-badge {
  font-size: 4.5rem;
  font-weight: 900;
  text-shadow: 0 0 20px currentColor;
}
.rank-S { color: #ffcc00; }
.rank-A { color: #00e5ff; }
.rank-B { color: #00ff99; }

.score-label { font-size: 0.6rem; color: #888; margin-bottom: 5px; }
.score-value { font-size: 2.5rem; font-weight: 800; color: #fff; }
.small-pt { font-size: 1rem; margin-left: 4px; color: #00e5ff; }

.result-stats { margin-bottom: 25px; }
.stat-item { margin-bottom: 12px; }
.stat-info { display: flex; justify-content: space-between; font-size: 0.75rem; margin-bottom: 6px; color: #aaa; }
.stat-bar-bg { width: 100%; height: 4px; background: #333; }
.stat-bar-fill { height: 100%; box-shadow: 0 0 10px currentColor; transition: width 1.5s ease-out; }

.result-comment {
  background: rgba(0, 229, 255, 0.05);
  border-left: 3px solid #00e5ff;
  padding: 15px;
  margin-bottom: 30px;
  text-align: left;
}
.comment-label { color: #00e5ff; font-size: 0.65rem; font-weight: bold; margin-bottom: 10px; }
.comment-text { font-size: 0.9rem; line-height: 1.5; margin-bottom: 10px; color: #eee; }
.advice-text { font-size: 0.85rem; color: #00ff99; font-style: italic; opacity: 0.9; }

.result-close-btn {
  width: 100%;
  padding: 16px;
  background: transparent;
  border: 1px solid #00e5ff;
  color: #00e5ff;
  font-family: "NeoDunggeunmo";
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
}
.result-close-btn:hover { background: #00e5ff; color: #1a1a24; box-shadow: 0 0 20px rgba(0, 229, 255, 0.4); }

/* 장식용 코너 */
.modal-corner { position: absolute; width: 12px; height: 12px; border: 2px solid #00e5ff; }
.tl { top: -2px; left: -2px; border-right: none; border-bottom: none; }
.tr { top: -2px; right: -2px; border-left: none; border-bottom: none; }
.bl { bottom: -2px; left: -2px; border-right: none; border-top: none; }
.br { bottom: -2px; right: -2px; border-left: none; border-top: none; }

/* 모달 애니메이션 */
.modal-bounce-enter-active { animation: modal-bounce-in 0.6s cubic-bezier(0.68, -0.55, 0.265, 1.55); }
.modal-bounce-leave-active { animation: modal-bounce-in 0.3s reverse ease-in; }
@keyframes modal-bounce-in {
  0% { transform: scale(0.5); opacity: 0; }
  100% { transform: scale(1); opacity: 1; }
}

/* --- 모바일 최적화 수정 버전 --- */

.ai-result-overlay {
  position: fixed;
  top: 0; left: 0;
  width: 100%;
  /* dvh는 모바일 브라우저 UI를 제외한 실제 가시 영역 높이를 잡습니다 */
  height: 100dvh; 
  background: rgba(0, 0, 0, 0.85);
  backdrop-filter: blur(5px);
  z-index: 99999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px; /* 모바일 여백 */
}

.ai-result-modal {
  background: #1a1a1a;
  border: 4px solid #fff;
  width: 100%;
  max-width: 420px;
  /* 화면 높이에 맞춰 최대 높이 설정 */
  max-height: 90dvh; 
  position: relative;
  display: flex;
  flex-direction: column; /* 세로 배치 */
  overflow: hidden;
  box-shadow: 8px 8px 0px 0px rgba(0, 0, 0, 0.5);
}

/* 내부 스크롤 영역 */
.modal-body-scroll {
  flex: 1; /* 남은 공간 모두 차지 */
  overflow-y: auto; /* 내용이 많으면 스크롤 생성 */
  padding: 10px 15px;
  
  /* 레트로 스타일 스크롤바 */
  scrollbar-width: thin;
  scrollbar-color: #ff0055 #222;
}

/* 크롬, 사파리용 스크롤바 디자인 */
.modal-body-scroll::-webkit-scrollbar {
  width: 6px;
}
.modal-body-scroll::-webkit-scrollbar-track {
  background: #222;
}
.modal-body-scroll::-webkit-scrollbar-thumb {
  background: #ff0055;
  border: 1px solid #fff;
}

/* 헤더 & 버튼 고정 스타일링 */
.modal-header-section {
  padding: 15px 15px 10px;
  border-bottom: 2px dashed #444;
  flex-shrink: 0; /* 높이 고정 */
}

.retro-confirm-btn {
  margin: 10px 15px 15px; /* 하단 고정 버튼 여백 */
  flex-shrink: 0; /* 높이 고정 */
  padding: 12px;
  font-size: 0.9rem;
}

/* 모바일 텍스트 크기 미세 조정 */
@media (max-height: 700px) {
  .rank-visual {
    font-size: 2.5rem;
  }
  .score-number-retro {
    font-size: 1.8rem;
  }
  .modal-main-title {
    font-size: 1rem;
  }
}

/* 픽셀 느낌을 위한 테두리 효과 추가 */
.retro-pixel-border {
  image-rendering: pixelated;
  position: relative;
}

/* CRT 스캔라인 효과 */
.scanline {
  position: absolute;
  top: 0; left: 0;
  width: 100%; height: 100%;
  background: linear-gradient(
    rgba(18, 16, 16, 0) 50%, 
    rgba(0, 0, 0, 0.1) 50%
  ), linear-gradient(90deg, rgba(255, 0, 0, 0.03), rgba(0, 255, 0, 0.01), rgba(0, 0, 255, 0.03));
  background-size: 100% 3px, 3px 100%;
  pointer-events: none;
  z-index: 10;
}

/* 헤더 섹션 */
.modal-header-section {
  text-align: center;
  margin-bottom: 20px;
  border-bottom: 2px dashed #444;
  padding-bottom: 15px;
}

.pixel-tag {
  display: inline-block;
  background: #ff0055;
  color: #fff;
  font-size: 0.7rem;
  padding: 2px 8px;
  margin-bottom: 10px;
}

.modal-main-title {
  font-size: 1.2rem;
  color: #00e5ff;
  text-shadow: 2px 2px 0 #000;
}

/* 랭크 & 점수 카드 */
.score-card-retro {
  display: flex;
  justify-content: space-around;
  align-items: center;
  background: #000;
  border: 2px solid #333;
  padding: 15px;
  margin-bottom: 20px;
}

.rank-visual {
  font-size: 3.5rem;
  font-weight: 900;
  line-height: 1;
  margin-top: 5px;
  animation: rankPop 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.rank-S { color: #ffcc00; text-shadow: 0 0 15px #ffcc00; }
.rank-A { color: #ff0055; text-shadow: 0 0 15px #ff0055; }
.rank-B { color: #00e5ff; text-shadow: 0 0 15px #00e5ff; }

.score-number-retro {
  font-size: 2.5rem;
  color: #fff;
  text-shadow: 2px 2px 0 #444;
}

/* 인사이트 로그 스타일 */
.section-title-retro {
  font-size: 0.8rem;
  color: #888;
  margin-bottom: 10px;
}

.insight-list-retro {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 20px;
}

.insight-card-retro {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid #333;
  padding: 10px;
  position: relative;
}

.insight-card-retro.good { border-left: 4px solid #00ff88; }
.insight-card-retro.warning { border-left: 4px solid #ffaa00; }

.status-dot {
  display: inline-block;
  width: 6px; height: 6px;
  background: currentColor;
  margin-right: 8px;
  vertical-align: middle;
}

.insight-title {
  font-size: 0.9rem;
  display: inline-block;
  color: #eee;
}

.insight-description {
  font-size: 0.8rem;
  color: #aaa;
  margin-top: 5px;
  line-height: 1.4;
}

/* 요약 박스 (NPC 대화 스타일) */
.summary-box-retro {
  background: #222;
  border: 2px solid #444;
  padding: 12px;
  display: flex;
  gap: 12px;
  align-items: center;
}

.npc-thumb {
  font-size: 1.5rem;
  background: #333;
  padding: 5px;
  border: 1px solid #555;
}

.summary-text {
  font-size: 0.85rem;
  color: #00ff88;
  line-height: 1.4;
}

/* 확인 버튼 */
.retro-confirm-btn {
  margin-top: 20px;
  background: #ff0055;
  color: #fff;
  border: none;
  padding: 15px;
  font-family: "NeoDunggeunmo";
  font-size: 1rem;
  cursor: pointer;
  box-shadow: 0 4px 0 #990033;
  transition: all 0.1s;
}

.retro-confirm-btn:active {
  transform: translateY(2px);
  box-shadow: 0 2px 0 #990033;
}

/* 애니메이션 정의 */
@keyframes rankPop {
  0% { transform: scale(0); opacity: 0; }
  80% { transform: scale(1.2); }
  100% { transform: scale(1); opacity: 1; }
}

/* 모달 등장 애니메이션 */
.retro-modal-enter-active {
  animation: modalSlideIn 0.3s steps(5);
}
.retro-modal-leave-active {
  animation: modalSlideIn 0.2s steps(5) reverse;
}

@keyframes modalSlideIn {
  from { transform: translateY(30px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

/* 모바일 대응 */
@media (max-width: 400px) {
  .ai-result-modal {
    padding: 15px;
  }
  .rank-visual { font-size: 2.8rem; }
  .score-number-retro { font-size: 2rem; }
}
/* --- 점수 & 랭크 강조 스타일 --- */

.score-card-retro.premium-border {
  display: flex;
  justify-content: space-around;
  align-items: center;
  background: linear-gradient(135deg, #000 0%, #1a1a1a 100%);
  border: 3px solid #ffd700; /* 황금색 테두리 */
  padding: 25px 15px;
  margin-bottom: 25px;
  position: relative;
  overflow: hidden;
  box-shadow: inset 0 0 15px rgba(255, 215, 0, 0.2);
}

/* 랭크 뒤에서 회전하는 후광(Aura) 효과 */
.rank-aura-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1;
}

.rank-aura {
  position: absolute;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  filter: blur(20px);
  opacity: 0.6;
  animation: rotateAura 4s linear infinite;
}

/* 등급별 아우라 색상 */
.aura-S { background: conic-gradient(#ff0055, #ffd700, #ff0055); }
.aura-A { background: conic-gradient(#00e5ff, #764ba2, #00e5ff); }
.aura-B { background: conic-gradient(#00ff88, #0085ff, #00ff88); }

@keyframes rotateAura {
  from { transform: rotate(0deg) scale(1); }
  50% { transform: rotate(180deg) scale(1.2); }
  to { transform: rotate(360deg) scale(1); }
}

/* 랭크 글자 효과: 금속 느낌 그라데이션 + 강한 글로우 */
.rank-visual-mega {
  font-size: 4.5rem;
  font-weight: 900;
  line-height: 1;
  position: relative;
  z-index: 2;
  font-family: 'Arial Black', sans-serif; /* 더 두꺼운 폰트 추천 */
  animation: rankPulse 1.5s ease-in-out infinite;
}

.rank-S {
  background: linear-gradient(to bottom, #fff 20%, #ffd700 50%, #b8860b 80%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  filter: drop-shadow(0 0 15px rgba(255, 215, 0, 0.8));
}

.rank-A {
  background: linear-gradient(to bottom, #fff 20%, #ff0055 50%, #8b0000 80%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  filter: drop-shadow(0 0 15px rgba(255, 0, 85, 0.8));
}

/* 점수 숫자 효과: 네온 사인 느낌 */
.score-container-mega {
  text-align: right;
  z-index: 2;
}

.score-label-neon {
  font-size: 0.7rem;
  color: #ffd700;
  letter-spacing: 2px;
  margin-bottom: 5px;
  text-shadow: 0 0 5px rgba(255, 215, 0, 0.5);
}

.score-number-glitch {
  font-size: 3rem;
  color: #fff;
  font-weight: bold;
  text-shadow: 
    3px 3px 0px #ff0055,
    -3px -3px 0px #00e5ff;
  animation: scoreFloat 3s ease-in-out infinite;
}

/* XP 바 애니메이션 (게이미피케이션 요소) */
.xp-bar-mini {
  width: 100%;
  height: 4px;
  background: #333;
  margin-top: 10px;
  border-radius: 2px;
  overflow: hidden;
}

.xp-bar-fill {
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, #ffd700, #fff, #ffd700);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite linear;
}

/* 신규 애니메이션들 */
@keyframes rankPulse {
  0%, 100% { transform: scale(1); filter: brightness(1) drop-shadow(0 0 15px currentColor); }
  50% { transform: scale(1.1); filter: brightness(1.3) drop-shadow(0 0 25px currentColor); }
}

@keyframes scoreFloat {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-5px); }
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

/* 모바일 텍스트 크기 최적화 */
@media (max-width: 400px) {
  .rank-visual-mega { font-size: 3.5rem; }
  .score-number-glitch { font-size: 2.2rem; }
  .rank-aura { width: 90px; height: 90px; }
}
</style>