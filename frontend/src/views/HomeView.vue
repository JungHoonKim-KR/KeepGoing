<template>
  <div class="home-view retro-theme" @click="initAudioContext">
    <div class="scanlines"></div>

    <section class="page daily-page">
      <div class="page-content">
        <div class="retro-header">
          <span class="blinking-cursor">▶</span> PLAYER_DATE: {{ displayDate }}
        </div>

        <div class="pixel-box main-stat-box">
          <div class="stat-header">
            <span class="label">HP (ENERGY)</span>
            <span class="val">{{ currentEnergy }} / {{ maxEnergy }}</span>
          </div>
          <div class="retro-progress-container" @click="triggerLevelUp">
            <div
              class="retro-progress-bar hp-bar"
              :style="{ width: hpPercent + '%' }"
            ></div>
            <div class="click-hint">CLICK BAR TO LEVEL UP!</div>
          </div>
        </div>

        <div class="game-screen-container" @click="playRetroSound('jump')">
          <div class="pixel-border">
            <div class="screen-bg">
              <div
                class="level-badge"
                :class="{ 'level-up-anim': isLevelingUp }"
              >
                {{ isLevelingUp ? "LEVEL UP!" : "Lv.24" }}
              </div>
              <img
                :src="characterImage"
                alt="Character"
                class="character-gif pixelated"
                :class="{ bounce: isBouncing }"
              />
              <div class="dialog-box">
                <p class="typing-effect">{{ dialogText }}</p>
              </div>
            </div>
          </div>
        </div>

        <div class="pixel-box stats-box">
          <h3 class="box-title">PLAYER STATS</h3>
          <div class="stat-row" v-for="(stat, idx) in stats" :key="idx">
            <div class="stat-icon">{{ stat.label }}</div>
            <div class="stat-bar-group">
              <div class="retro-bar-bg">
                <div
                  class="retro-bar-fill"
                  :class="stat.class"
                  :style="{ width: stat.percent }"
                ></div>
              </div>
              <span class="stat-val">{{ stat.val }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="page meal-page">
      <div class="page-content">
        <div
          v-if="todayMeals.length === 0"
          class="pixel-card interactive"
          @click="handleMealClick"
        >
          <div class="card-inner">
            <div class="icon-8bit">🍗</div>
            <h2>LOG ITEM</h2>
            <p class="pixel-text">
              인벤토리가 비어있습니다.<br />식사를 기록하세요.
            </p>
            <button class="retro-btn press-start">INSERT COIN</button>
          </div>
        </div>

        <div v-else class="meal-log-container">
          <div class="retro-header-sm">
            <span>INVENTORY (MEALS)</span>
            <span class="total-xp"
              >TOTAL XP:
              {{ todayMeals.reduce((acc, cur) => acc + cur.cal, 0) }}</span
            >
          </div>

          <div class="meal-list">
            <div
              v-for="meal in todayMeals"
              :key="meal.id"
              class="meal-slot"
              @click="handleMealClick"
            >
              <div class="slot-icon-box">{{ meal.icon }}</div>
              <div class="slot-info">
                <div class="slot-top">
                  <span class="meal-type-badge">{{ meal.type }}</span>
                  <span class="meal-cal">{{ meal.cal }} XP</span>
                </div>
                <div class="meal-name">{{ meal.name }}</div>
              </div>
            </div>

            <div class="meal-slot add-slot" @click="handleMealClick">
              <span class="plus-icon">+</span>
              <span class="add-text">ADD NEW ITEM</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="page water-page">
      <div class="page-content">
        <div
          v-if="waterData.current === 0"
          class="pixel-card interactive blue-theme"
          @click="handleWaterClick"
        >
          <h1 class="page-title pixel-font">MANA POTION</h1>
          <div class="empty-state-icon">💧</div>
          <p class="pixel-text-center">
            마력이 부족합니다.<br />물을 마셔 회복하세요.
          </p>
          <button class="retro-btn blue-btn">RECHARGE MANA</button>
        </div>

        <div
          v-else
          class="pixel-card interactive blue-theme"
          @click="handleWaterClick"
        >
          <div class="hud-top">
            <span class="hud-label">MANA (H2O)</span>
            <span class="hud-val"
              >{{
                Math.round((waterData.current / waterData.goal) * 100)
              }}%</span
            >
          </div>

          <div class="water-dashboard">
            <div class="current-water">
              {{ waterData.current }}<span class="unit">L</span>
            </div>
            <div class="goal-water">MAX: {{ waterData.goal }}L</div>
          </div>

          <div class="mana-bar-container">
            <div
              class="mana-bar-fill"
              :style="{
                width: (waterData.current / waterData.goal) * 100 + '%',
              }"
            >
              <div class="glare"></div>
            </div>
          </div>

          <div class="log-msg">> MP RECOVERING...</div>
          <button class="retro-btn blue-btn sm-btn">DRINK MORE</button>
        </div>
      </div>
    </section>
    <section class="page weight-page">
      <div class="page-content">
        <div
          v-if="!weightData.current"
          class="pixel-card interactive purple-theme"
          @click="handleWeightClick"
        >
          <h1 class="page-title pixel-font">HIGH SCORE</h1>
          <div class="empty-state-icon">⚖️</div>
          <p class="pixel-text-center">
            오늘의 스코어(체중)를<br />기록하지 않았습니다.
          </p>
          <button class="retro-btn purple-btn">NEW RECORD</button>
        </div>

        <div
          v-else
          class="pixel-card interactive purple-theme"
          @click="handleWeightClick"
        >
          <div class="hud-top">
            <span class="hud-label">CURRENT RANKING</span>
            <span class="date-badge">TODAY</span>
          </div>

          <div class="weight-dashboard">
            <div class="score-display">
              <span class="score-val">{{ weightData.current }}</span>
              <span class="score-unit">KG</span>
            </div>

            <div
              class="score-change"
              :class="weightData.change > 0 ? 'bad' : 'good'"
            >
              <span class="change-icon">{{
                weightData.change > 0 ? "▲" : "▼"
              }}</span>
              {{ Math.abs(weightData.change) }}kg
              <span class="change-text">{{
                weightData.change > 0 ? "(WARNING)" : "(NICE!)"
              }}</span>
            </div>
          </div>

          <button class="retro-btn purple-btn sm-btn">UPDATE SCORE</button>
        </div>
      </div>
    </section>
    <div v-if="showModal" class="modal-overlay" @click="closeModal"></div>
    <MealRecordModal v-if="showMealModal" @close="closeMealModal" />
    <WaterRecordModal v-if="showWaterModal" @close="closeWaterModal" />
    <WeightRecordModal v-if="showWeightModal" @close="closeWeightModal" />
    <Footer></Footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, provide } from "vue";
import { useConfigStore } from "@/stores/configStore"; // Pinia Store 경로를 정확히 확인해주세요.
import Footer from "@/components/utils/Footer.vue";
import dayjs from "dayjs";
import confetti from "canvas-confetti";
import characterImage from "../assets/images/characters/test.gif";

// 컴포넌트 import (경로가 올바르다고 가정)
import WaterRecordModal from "@/components/record/WaterRecordModal.vue";
import WeightRecordModal from "@/components/record/WeightRecordModal.vue";
import MealRecordModal from "@/components/record/MealRecordModal.vue";

// =========================
// 🚀 Pinia 스토어 및 상수 설정
// =========================
const config = useConfigStore();
const MEMBER_ID = config.MEMBER_ID;
const API_ENDPOINT = config.API_ENDPOINT;
const displayDate = computed(() => config.currentDate);
const getCurrentDateForAPI = config.getCurrentDateForAPI; // 함수이므로 그대로 사용합니다.

// =========================
// 🍽 식단 데이터
// =========================
const todayMealMap = ref({
  아침: null,
  점심: null,
  저녁: null,
  간식: null,
});

// 화면에 표시할 식단 리스트 (computed)
const todayMeals = computed(() => {
  if (!todayMealMap.value || Object.keys(todayMealMap.value).length === 0) {
    return [];
  }

  const mealIcons = { 아침: "🍳", 점심: "🍖", 저녁: "🍲", 간식: "🍰" };

  return Object.entries(todayMealMap.value)
    .filter(([_, meal]) => meal !== null)
    .map(([type, meal], idx) => ({
      id: meal.id ?? idx,
      type,
      icon: mealIcons[type] || "🍽️",
      cal: Math.round(meal.energy || 0),
      // foods 배열이 유효한지 확인하고 name을 join합니다.
      name:
        meal.foods
          ?.map((f) => f.name)
          .filter((n) => n)
          .join(", ") || "기록된 음식 없음",
    }));
});

// =========================
// 💧 물 / ⚖️ 체중
// =========================
const waterData = ref({
  current: 1.2,
  goal: 2.0,
});

const weightData = ref({
  current: 70.5,
  change: -0.3,
});

// =========================
// 📦 모달 상태
// =========================
const showModal = ref(false);
const showWaterModal = ref(false);
const showWeightModal = ref(false);
const showMealModal = ref(false);
const showRadio = ref(false);

// =========================
// 🧠 캐릭터 상태
// =========================
const isLevelingUp = ref(false);
const isBouncing = ref(false);
const dialogText = ref('"오늘도 힘내보자구!"');

// =========================
// 📊 스탯 (에너지 및 매크로 계산)
// =========================
const maxEnergy = 3000; // 일일 권장 칼로리 (임의 설정)
const currentEnergy = computed(() => {
  return Object.values(todayMealMap.value)
    .reduce((acc, meal) => acc + (meal ? meal.energy : 0), 0)
    .toFixed(0);
});
const hpPercent = computed(() =>
  Math.min((currentEnergy.value / maxEnergy) * 100, 100).toFixed(0)
);

const stats = computed(() => {
  const totalProtein = Object.values(todayMealMap.value).reduce(
    (acc, meal) => acc + (meal ? meal.protein : 0),
    0
  );
  const totalCarb = Object.values(todayMealMap.value).reduce(
    (acc, meal) => acc + (meal ? meal.carbohydrate : 0),
    0
  );
  const totalFat = Object.values(todayMealMap.value).reduce(
    (acc, meal) => acc + (meal ? meal.fat : 0),
    0
  );
  const totalMacro = totalProtein + totalCarb + totalFat;

  const getPercent = (value) =>
    totalMacro > 0 ? ((value / totalMacro) * 100).toFixed(0) : 0;

  return [
    {
      label: "⚡STR (탄)",
      class: "carb",
      percent: `${getPercent(totalCarb)}%`,
      val: `${totalCarb.toFixed(1)}g`,
    },
    {
      label: "🛡️DEF (단)",
      class: "protein",
      percent: `${getPercent(totalProtein)}%`,
      val: `${totalProtein.toFixed(1)}g`,
    },
    {
      label: "🔮INT (지)",
      class: "fat",
      percent: `${getPercent(totalFat)}%`,
      val: `${totalFat.toFixed(1)}g`,
    },
  ];
});

// =========================
// 🔊 사운드 및 효과
// =========================
const audioCtx = ref(null);

const initAudioContext = () => {
  if (!audioCtx.value) {
    console.log("Audio Context Initialized (Dummy)");
  }
};

const playRetroSound = (type) => {
  console.log(`Playing sound: ${type}`);
  isBouncing.value = type === "jump";
  setTimeout(() => (isBouncing.value = false), 500);
};

const triggerLevelUp = () => {
  if (isLevelingUp.value) return;

  isLevelingUp.value = true;
  dialogText.value = "LEVEL UP! 능력이 상승했다!";

  playRetroSound("levelup");

  confetti({
    particleCount: 120,
    spread: 70,
    origin: { y: 0.6 },
  });

  setTimeout(() => {
    isLevelingUp.value = false;
    dialogText.value = '"다음 레벨로 가보자!"';
  }, 3000);
};

// =========================
// 🧭 이벤트 및 모달 컨트롤
// =========================
const handleMealClick = () => {
  playRetroSound("coin");
  showMealModal.value = true;
};
const closeMealModal = async () => {
  showMealModal.value = false;
  await fetchDailyDiet();
};
const closeWaterModal = () => (showWaterModal.value = false);
const closeWeightModal = () => (showWeightModal.value = false);

const handleWaterClick = () => {
  playRetroSound("potion");
  showWaterModal.value = true;
};
const handleWeightClick = () => {
  playRetroSound("jump");
  showWeightModal.value = true;
};
const closeModal = () => (showModal.value = false);

// =========================
// 📡 API 로딩 (서비스 통합)
// =========================

async function fetchDailyDiet() {
  const url = `${API_ENDPOINT}/diets/meal-daily?memberId=${MEMBER_ID}&date=${displayDate.value}`;

  try {
    const response = await fetch(url);

    if (!response.ok) {
      throw new Error(`HTTP error! Status: ${response.status}`);
    }

    const data = await response.json();

    todayMealMap.value = data;
    console.log("API 데이터 로드 성공:", data);
  } catch (error) {
    console.error(
      "일일 식단 데이터를 불러오는 데 실패했습니다. Mock 데이터를 사용합니다.",
      error
    );

    todayMealMap.value = {
      아침: null,
      점심: null,
      저녁: {
        id: 3,
        memberId: 1,
        date: "2025-12-09",
        foods: [
          {
            code: "D103-150010000-0001",
            name: "만두_고기만두",
            energy: 159.0,
            protein: 12.38,
            fat: 4.45,
            carbohydrate: 17.4,
          },
          {
            code: "D105-205000000-0001",
            name: "김치국",
            energy: 23.0,
            protein: 1.34,
            fat: 0.76,
            carbohydrate: 2.63,
          },
        ],
        energy: 441.8,
        water: 520.16,
        protein: 32.6,
        fat: 0.0,
        carbohydrate: 48.8,
        mealTime: "저녁",
      },
      간식: null,
    };
  }
}

onMounted(async () => {
  await fetchDailyDiet();
});
</script>

<style scoped>
/* 폰트: 둥근모꼴 */
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

/* 스캔라인 효과 */
.scanlines {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(rgba(18, 16, 16, 0) 50%, rgba(0, 0, 0, 0.25) 50%),
    linear-gradient(
      90deg,
      rgba(255, 0, 0, 0.06),
      rgba(0, 255, 0, 0.02),
      rgba(0, 0, 255, 0.06)
    );
  background-size: 100% 4px, 6px 100%;
  pointer-events: none;
  z-index: 999;
}

.page {
  height: 100vh;
  scroll-snap-align: start;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
}

.page-content {
  width: 100%;
  max-width: 400px;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* === 공통 박스 스타일 === */
.pixel-box,
.pixel-card {
  border: 4px solid #fff;
  box-shadow: 4px 4px 0px 0px rgba(0, 0, 0, 0.5);
  position: relative;
  transition: transform 0.1s;
  cursor: pointer;
}

.pixel-box {
  background: #2d2d3a;
  padding: 1rem;
}
.pixel-card {
  padding: 1.5rem;
  text-align: center;
  background: #e6dac3;
  color: #3e2723;
  border-color: #5d4037;
}

.interactive:active {
  transform: translate(2px, 2px);
  box-shadow: 2px 2px 0 rgba(0, 0, 0, 0.5);
}

/* === 페이지별 디테일 === */
.daily-page {
  background: #222034;
}
.meal-page {
  background: #4b692f;
}
.water-page {
  background: #000022;
}
.weight-page {
  background: #2a0a29;
}

.retro-header {
  text-align: center;
  color: var(--secondary-color);
  margin-bottom: 0.5rem;
}
.blinking-cursor {
  animation: blink 1s step-end infinite;
}
@keyframes blink {
  50% {
    opacity: 0;
  }
}

/* 프로그레스 바 */
.retro-progress-container {
  height: 24px;
  background: #333;
  border: 2px solid #fff;
  padding: 2px;
  position: relative;
}
.retro-progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #ff0055, #ff5500);
  transition: width 0.5s steps(10);
}
.click-hint {
  position: absolute;
  top: -20px;
  right: 0;
  font-size: 0.7rem;
  color: var(--accent-color);
  animation: blink 0.5s infinite alternate;
}

/* 캐릭터 애니메이션 */
.game-screen-container .pixel-border {
  border: 8px solid #444;
  background: #8fb8ca;
  padding: 4px;
  border-radius: 8px;
}
.screen-bg {
  background: url("https://i.pinimg.com/originals/10/78/3f/10783f947938361b02390a382c44843b.png")
    repeat-x bottom;
  background-size: contain;
  width: 280px;
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
}
.character-gif {
  width: 100px;
  image-rendering: pixelated;
  margin-bottom: 10px;
}
.bounce {
  animation: bounce 0.5s infinite alternate;
}
@keyframes bounce {
  from {
    transform: translateY(0);
  }
  to {
    transform: translateY(-20px);
  }
}

.level-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
  padding: 4px 8px;
  border: 2px solid #fff;
}
.level-up-anim {
  color: var(--accent-color);
  border-color: var(--accent-color);
  animation: blink 0.2s infinite;
}

.dialog-box {
  width: 90%;
  background: rgba(0, 40, 150, 0.9);
  border: 2px solid #fff;
  padding: 8px;
  margin-bottom: 10px;
  text-align: center;
  font-size: 0.8rem;
}

/* 스탯 */
.stat-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 0.5rem;
}
.stat-icon {
  width: 80px;
  font-size: 0.8rem;
}
.stat-bar-group {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 10px;
}
.retro-bar-bg {
  flex: 1;
  height: 12px;
  background: #111;
  border: 1px solid #555;
}
.retro-bar-fill {
  height: 100%;
}
.retro-bar-fill.carb {
  background: #ffd700;
}
.retro-bar-fill.protein {
  background: #ff0055;
}
.retro-bar-fill.fat {
  background: #00e5ff;
}

/* 버튼 및 기타 */
.retro-btn {
  margin-top: 1rem;
  background: #ff0055;
  color: #fff;
  border: 2px solid #fff;
  padding: 10px 20px;
  font-family: inherit;
  cursor: pointer;
  box-shadow: 4px 4px 0 #000;
}
.blue-theme {
  border-color: #00e5ff;
  color: #00e5ff;
  background: #000;
}
.blue-btn {
  background: #00e5ff;
  color: #000;
}
.purple-theme {
  border-color: #d500f9;
  color: #d500f9;
  background: #000;
}
.purple-btn {
  background: #d500f9;
  color: #fff;
}

.pixelated {
  image-rendering: pixelated;
}

/* === 식단 리스트 스타일 (Inventory Style) === */
.meal-log-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.retro-header-sm {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 2px dashed #fff;
  padding-bottom: 5px;
  font-size: 0.9rem;
  color: #ffd700; /* Gold */
  text-shadow: 1px 1px 0 #000;
}

.meal-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 60vh; /* 너무 길어지면 스크롤 */
  overflow-y: auto;
  padding-right: 5px; /* 스크롤바 공간 */
}

/* 스크롤바 커스텀 */
.meal-list::-webkit-scrollbar {
  width: 4px;
}
.meal-list::-webkit-scrollbar-thumb {
  background: #ffd700;
  border-radius: 2px;
}

/* 개별 슬롯 (아이템 창) */
.meal-slot {
  display: flex;
  align-items: center;
  background: rgba(0, 0, 0, 0.6);
  border: 2px solid #fff;
  padding: 10px;
  gap: 12px;
  cursor: pointer;
  transition: transform 0.1s, background 0.1s;
  box-shadow: 4px 4px 0 rgba(0, 0, 0, 0.3);
}

.meal-slot:active {
  transform: translate(2px, 2px);
  box-shadow: 2px 2px 0 rgba(0, 0, 0, 0.3);
  background: rgba(255, 255, 255, 0.1);
}

/* 아이콘 박스 */
.slot-icon-box {
  width: 40px;
  height: 40px;
  background: #2d2d3a;
  border: 2px solid #555;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
}

/* 텍스트 정보 */
.slot-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.slot-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.meal-type-badge {
  font-size: 0.7rem;
  background: #ff0055;
  color: #fff;
  padding: 2px 6px;
  border: 1px solid #fff;
}

.meal-cal {
  font-size: 0.8rem;
  color: #00e5ff; /* Cyan */
}

.meal-name {
  font-size: 1rem;
  color: #fff;
  font-weight: bold;
}

/* 추가 버튼 (빈 슬롯 스타일) */
.meal-slot.add-slot {
  border: 2px dashed #aaa;
  background: transparent;
  justify-content: center;
  color: #aaa;
  box-shadow: none;
}

.meal-slot.add-slot:hover {
  border-color: #ffd700;
  color: #ffd700;
  background: rgba(255, 215, 0, 0.1);
}

.plus-icon {
  font-size: 1.2rem;
  font-weight: bold;
}
.add-text {
  font-size: 0.9rem;
}
/* === 공통 유틸 === */
.pixel-text-center {
  text-align: center;
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.9rem;
  margin-bottom: 1rem;
}
.empty-state-icon {
  font-size: 3rem;
  text-align: center;
  margin-bottom: 10px;
  opacity: 0.8;
  animation: float 3s infinite ease-in-out;
}
.hud-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  border-bottom: 2px dashed rgba(255, 255, 255, 0.3);
  padding-bottom: 5px;
}
.hud-label {
  font-size: 0.8rem;
  font-weight: bold;
}
.sm-btn {
  padding: 8px;
  font-size: 0.9rem;
  margin-top: 15px;
  width: 100%;
}

/* === 물 (Mana) 스타일 === */
.water-dashboard {
  text-align: center;
  margin-bottom: 10px;
}
.current-water {
  font-size: 3.5rem;
  font-weight: bold;
  color: #00e5ff;
  text-shadow: 0 0 10px #00e5ff;
  line-height: 1;
}
.current-water .unit {
  font-size: 1.5rem;
  color: #fff;
  margin-left: 5px;
}
.goal-water {
  color: #888;
  font-size: 0.9rem;
  margin-top: 5px;
}

.mana-bar-container {
  width: 100%;
  height: 20px;
  background: #111;
  border: 2px solid #fff;
  padding: 2px;
  position: relative;
}
.mana-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #00e5ff, #0077ff);
  width: 0%;
  transition: width 1s ease-out;
  position: relative;
  overflow: hidden;
}
.glare {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: rgba(255, 255, 255, 0.5);
}
.log-msg {
  font-size: 0.7rem;
  color: #00e5ff;
  margin-top: 5px;
  animation: blink 2s infinite;
}

/* === 체중 (Score) 스타일 === */
.weight-dashboard {
  text-align: center;
  padding: 10px 0;
}
.score-display {
  display: flex;
  justify-content: center;
  align-items: baseline;
  gap: 5px;
}
.score-val {
  font-size: 3.5rem;
  font-weight: bold;
  color: #d500f9;
  text-shadow: 0 0 10px #d500f9;
}
.score-unit {
  font-size: 1.5rem;
  color: #fff;
}

.score-change {
  font-size: 1rem;
  margin-top: 10px;
  display: inline-block;
  padding: 5px 10px;
  border-radius: 4px;
  background: rgba(0, 0, 0, 0.3);
  border: 1px solid;
}
.score-change.good {
  color: #00ff00;
  border-color: #00ff00;
} /* 살 빠짐 */
.score-change.bad {
  color: #ff0055;
  border-color: #ff0055;
} /* 살 찜 */
.change-text {
  font-size: 0.7rem;
  margin-left: 5px;
}

.date-badge {
  font-size: 0.7rem;
  background: #d500f9;
  color: #fff;
  padding: 2px 5px;
}

@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
}
@keyframes blink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}
</style>
