<template>
  <div class="home-view retro-theme" @click="initAudioContext">
    <div class="scanlines"></div>

    <section class="page daily-page">
      <div class="page-content">
        <div class="retro-header"><span class="blinking-cursor">▶</span> PLAYER_DATE: {{ formattedDate }}</div>

        <div class="pixel-box main-stat-box">
          <div class="stat-header">
            <span class="label">HP (ENERGY)</span>
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
              <div class="dialog-box">
                <p class="typing-effect">{{ dialogText }}</p>
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
            <div class="icon-8bit">🍗</div>
            <h2>LOG ITEM</h2>
            <p class="pixel-text">인벤토리가 비어있습니다.<br />식사를 기록하세요.</p>
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
        <div v-if="waterData.water === 0" class="pixel-card interactive blue-theme" @click="handleWaterClick">
          <h1 class="page-title pixel-font">MANA POTION</h1>
          <div class="empty-state-icon">💧</div>
          <p class="pixel-text-center">마력이 부족합니다.<br />물을 마셔 회복하세요.</p>
          <button class="retro-btn blue-btn">RECHARGE MANA</button>
        </div>

        <div v-else class="pixel-card interactive blue-theme" @click="handleWaterClick">
          <div class="hud-top">
            <span class="hud-label">MANA (H2O)</span>
            <span class="hud-val">{{ Math.round((waterData.water / waterData.goal) * 100) }}%</span>
          </div>

          <div class="water-dashboard">
            <div class="current-water">{{ waterData.water }}<span class="unit">L</span></div>
            <div class="goal-water">MAX: {{ waterData.goal }}L</div>
          </div>

          <div class="mana-bar-container">
            <div
              class="mana-bar-fill"
              :style="{
                width: Math.min((waterData.water / waterData.goal) * 100, 100) + '%',
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
        <div v-if="weightData.weight == 0.0" class="pixel-card interactive purple-theme" @click="handleWeightClick">
          <h1 class="page-title pixel-font">HIGH SCORE</h1>
          <div class="empty-state-icon">⚖️</div>
          <p class="pixel-text-center">오늘의 스코어(체중)를<br />기록하지 않았습니다.</p>
          <button class="retro-btn purple-btn">NEW RECORD</button>
        </div>

        <div v-else class="pixel-card interactive purple-theme" @click="handleWeightClick">
          <div class="hud-top">
            <span class="hud-label">CURRENT RANKING</span>
            <span class="date-badge">TODAY</span>
          </div>

          <div class="weight-dashboard">
            <div class="score-display">
              <span class="score-val">{{ weightData.weight }}</span>
              <span class="score-unit">KG</span>
            </div>

            <div class="score-change" :class="weightData.diff > 0 ? 'bad' : 'good'">
              <span class="change-icon">{{ weightData.diff > 0 ? "▲" : "▼" }}</span>
              {{ Math.abs(weightData.diff) }}kg
              <span class="change-text">{{ weightData.diff > 0 ? "(WARNING)" : "(NICE!)" }}</span>
            </div>
          </div>

          <button class="retro-btn purple-btn sm-btn">UPDATE SCORE</button>
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
            :class="{
              locked: char.isLocked,
              selected: char.id === selectedCharId,
            }"
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
    <MealRecordModal v-if="showMealModal" @close="closeMealModal" :date-to-use="formattedDate" />
    <WaterRecordModal
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
    <Footer></Footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useConfigStore } from "@/stores/configStore"; // Pinia Store 경로를 정확히 확인해주세요.
import { useRoute } from "vue-router";
import Footer from "@/components/utils/Footer.vue";
import dayjs from "dayjs";
import confetti from "canvas-confetti";
// import characterImage from "../assets/images/characters/test.gif"; // [삭제됨] 동적 로딩으로 변경

// 컴포넌트 import (경로가 올바르다고 가정)
import WaterRecordModal from "@/components/record/WaterRecordModal.vue";
import WeightRecordModal from "@/components/record/WeightRecordModal.vue";
import MealRecordModal from "@/components/record/MealRecordModal.vue";

// =========================
// 🚀 Pinia 스토어 및 상수 설정
// =========================
const config = useConfigStore();
const route = useRoute();
const MEMBER_ID = config.MEMBER_ID;
const API_ENDPOINT = config.API_ENDPOINT;
const formattedDate = computed(() => {
  const routeDate = route.query.date;
  if (routeDate) {
    return dayjs(routeDate).format("YYYY-MM-DD");
  } else {
    return dayjs().format("YYYY-MM-DD");
  }
});

// =========================
// 🎮 캐릭터 및 레벨 시스템 (신규 추가)
// =========================
const currentLevel = ref(10); // 테스트용 현재 레벨 (API에서 받아온 값으로 교체 필요)
const selectedCharId = ref(1); // 현재 선택된 캐릭터 ID (기본 1번)
const showCharModal = ref(false); // 캐릭터 변경 모달 표시 여부

// [수정됨] 이미지 경로: characters
const getCharImage = (id) => {
  return new URL(`../assets/images/characters/${id}.png`, import.meta.url).href;
};

// 메인 화면에 표시될 현재 캐릭터 이미지
const currentCharacterImage = computed(() => getCharImage(selectedCharId.value));

// 캐릭터 리스트 (1~16번) 생성 및 잠금 상태 계산
const characterList = computed(() => {
  return Array.from({ length: 16 }, (_, i) => {
    const id = i + 1;
    return {
      id,
      src: getCharImage(id),
      isLocked: id > currentLevel.value, // 현재 레벨보다 높으면 잠금
    };
  });
});

// 캐릭터 선택 핸들러
const selectCharacter = (char) => {
  if (char.isLocked) {
    console.log("Locked Character");
    return;
  }
  selectedCharId.value = char.id;
  playRetroSound("coin");
  showCharModal.value = false;
};

// [수정됨] 스크린 클릭 핸들러 (점프 제거, 모달 오픈)
const handleScreenClick = () => {
  console.log("캐릭터 선택 모달 오픈");
  showCharModal.value = true;
};

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
  water: 1.2,
  goal: 2.0,
});

const weightData = ref({
  weight: 70.5,
  diff: -0.3,
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
const hpPercent = computed(() => Math.min((currentEnergy.value / maxEnergy) * 100, 100).toFixed(0));

const stats = computed(() => {
  const totalProtein = Object.values(todayMealMap.value).reduce((acc, meal) => acc + (meal ? meal.protein : 0), 0);
  const totalCarb = Object.values(todayMealMap.value).reduce((acc, meal) => acc + (meal ? meal.carbohydrate : 0), 0);
  const totalFat = Object.values(todayMealMap.value).reduce((acc, meal) => acc + (meal ? meal.fat : 0), 0);
  const totalMacro = totalProtein + totalCarb + totalFat;

  const getPercent = (value) => (totalMacro > 0 ? ((value / totalMacro) * 100).toFixed(0) : 0);

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
const handleWaterUpdate = async (newAmount) => {
  waterData.value.water = newAmount;
};
const handleWeightUpdate = async (newWeight) => {
  if (newWeight) {
    weightData.value.weight = newWeight;
  }
  await fetchWeightData();
};
const closeModal = () => (showModal.value = false);

// =========================
// 📡 API 로딩 (서비스 통합)
// =========================

async function fetchDailyDiet() {
  const url = `${API_ENDPOINT}/diets/meal-daily?memberId=${MEMBER_ID}&date=${formattedDate.value}`;

  try {
    const response = await fetch(url);

    if (!response.ok) {
      throw new Error(`HTTP error! Status: ${response.status}`);
    }

    const data = await response.json();

    todayMealMap.value = data;
    console.log("API 데이터 로드 성공:", data);
  } catch (error) {
    console.error("일일 식단 데이터를 불러오는 데 실패했습니다. Mock 데이터를 사용합니다.", error);
    // default
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

async function fetchHydrationData() {
  // 물 데이터 API 호출 로직 (임의 구현)
  const baseURL = `${API_ENDPOINT}/diets/hydration`;
  const params = new URLSearchParams({
    memberId: MEMBER_ID,
    date: formattedDate.value,
  });
  const url = `${baseURL}?${params.toString()}`;
  try {
    const response = await fetch(url);
    if (!response.ok) {
      throw new Error(`HTTP error! Status: ${response.status}`);
    }
    const data = await response.json();
    waterData.value.water = data;
  } catch (error) {
    console.error("물 데이터를 불러오는 데 실패했습니다. Mock 데이터를 사용합니다.", error);
    waterData.value = { water: 1.2, goal: 2.0 };
  }
}
async function fetchWeightData() {
  const baseURL = `${API_ENDPOINT}/api/member/weight`;
  const params = new URLSearchParams({
    memberId: MEMBER_ID,
    date: formattedDate.value,
  });
  const url = `${baseURL}?${params.toString()}`;
  try {
    const response = await fetch(url);

    if (!response.ok) {
      throw new Error(`HTTP error! Status: ${response.status}`);
    }
    const data = await response.json();
    weightData.value.weight = data.weight;
    weightData.value.diff = data.diff;
  } catch (error) {
    console.error("일일 식단 데이터를 불러오는 데 실패했습니다. Mock 데이터를 사용합니다.", error);
  }
}

onMounted(async () => {
  await fetchDailyDiet();
  await fetchHydrationData();
  await fetchWeightData();
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
    linear-gradient(90deg, rgba(255, 0, 0, 0.06), rgba(0, 255, 0, 0.02), rgba(0, 0, 255, 0.06));
  background-size: 100% 4px, 6px 100%;
  pointer-events: none;
  z-index: 999;
}

/* === 레이아웃 수정: 상단 정렬 및 여백 축소 === */
.page {
  min-height: 100vh;
  scroll-snap-align: start;
  display: flex;
  flex-direction: column;
  align-items: center; /* 가로 중앙 정렬 */
  justify-content: flex-start; /* 세로 상단 정렬 (기존 center에서 변경) */
  padding: 4rem 1rem 6rem 1rem; /* 상단 여백 확보, 하단은 푸터 공간 확보 */
  box-sizing: border-box;
  padding-top: 10rem;
}

.page-content {
  width: 100%;
  max-width: 600px;
  display: flex;
  flex-direction: column;
  gap: 0.8rem; /* 기존 1.5rem에서 축소하여 더 촘촘하게 */
}

/* === 공통 박스 스타일 수정: 패딩 축소 === */
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
  padding: 0.8rem; /* 기존 1rem에서 축소 */
}

.pixel-card {
  padding: 1rem; /* 기존 1.5rem에서 축소 */
  text-align: center;
  background: #e6dac3;
  color: #3e2723;
  border-color: #5d4037;
}

.interactive:active {
  transform: translate(2px, 2px);
  box-shadow: 2px 2px 0 rgba(0, 0, 0, 0.5);
}

/* === 페이지별 배경색 === */
.page.daily-page {
  background: #222034;
  padding: 1rem 1rem 6rem 1rem;
}
.meal-page {
  background: #4b692f;
  padding: 1rem 1rem 6rem 1rem;
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
  margin-bottom: 0.2rem; /* 마진 축소 */
  font-size: 1.1rem;
}

.blinking-cursor {
  animation: blink 1s step-end infinite;
}

/* 프로그레스 바 */
.retro-progress-container {
  height: 20px; /* 높이 약간 축소 */
  background: #333;
  border: 2px solid #fff;
  padding: 2px;
  position: relative;
  margin-top: 5px;
}
.retro-progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #ff0055, #ff5500);
  transition: width 0.5s steps(10);
}
.click-hint {
  position: absolute;
  top: -18px;
  right: 0;
  font-size: 0.6rem;
  color: var(--accent-color);
  animation: blink 0.5s infinite alternate;
}

/* === 캐릭터 화면 수정: 높이 축소 === */
.game-screen-container .pixel-border {
  border: 6px solid #444; /* 테두리 두께 약간 축소 */
  background: #8fb8ca;
  padding: 0; /* 내부 패딩 제거하여 공간 확보 */
  border-radius: 6px;
  overflow: hidden;
}

.screen-bg {
  background: url("https://i.pinimg.com/originals/10/78/3f/10783f947938361b02390a382c44843b.png") repeat-x bottom;
  background-size: cover; /* contain -> cover로 변경하여 꽉 차게 */
  width: 100%;
  height: 150px; /* 기존 200px -> 150px로 축소 (핵심) */
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  position: relative;
  cursor: pointer;
}

/* [추가] 힌트 메시지 스타일 */
.edit-hint {
  position: absolute;
  top: 5px;
  right: 5px;
  font-size: 0.6rem;
  color: rgba(255, 255, 255, 0.6);
  text-shadow: 1px 1px 0 #000;
  animation: blink 2s infinite;
  pointer-events: none;
}

.character-gif {
  width: 80px; /* 캐릭터 크기 약간 축소 */
  image-rendering: pixelated;
  margin-bottom: 5px;
}
.bounce {
  animation: bounce 0.5s infinite alternate;
}
@keyframes bounce {
  from {
    transform: translateY(0);
  }
  to {
    transform: translateY(-10px);
  }
}

.level-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
  padding: 2px 6px;
  font-size: 0.7rem;
  border: 2px solid #fff;
}
.level-up-anim {
  color: var(--accent-color);
  border-color: var(--accent-color);
  animation: blink 0.2s infinite;
}

.dialog-box {
  width: 95%;
  background: rgba(0, 40, 150, 0.9);
  border: 2px solid #fff;
  padding: 4px;
  margin-bottom: 5px;
  text-align: center;
  font-size: 0.75rem;
  line-height: 1.2;
}

/* 스탯 */
.box-title {
  margin: 0 0 0.5rem 0;
  font-size: 0.9rem;
  color: #aaa;
}
.stat-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 0.3rem; /* 간격 축소 */
}
.stat-icon {
  width: 70px;
  font-size: 0.75rem;
}
.stat-bar-group {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
}
.retro-bar-bg {
  flex: 1;
  height: 10px; /* 두께 축소 */
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
.stat-val {
  font-size: 0.75rem;
  min-width: 35px;
  text-align: right;
}

/* 버튼 및 기타 */
.retro-btn {
  margin-top: 0.8rem;
  background: #ff0055;
  color: #fff;
  border: 2px solid #fff;
  padding: 8px 16px;
  font-family: inherit;
  cursor: pointer;
  box-shadow: 3px 3px 0 #000;
  font-size: 0.9rem;
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
  gap: 0.5rem; /* 간격 축소 */
}

.retro-header-sm {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 2px dashed #fff;
  padding-bottom: 5px;
  font-size: 0.85rem;
  color: #ffd700;
  text-shadow: 1px 1px 0 #000;
  margin-bottom: 0.5rem;
}

.meal-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 55vh; /* 스크롤 영역 확보 */
  overflow-y: auto;
  padding-right: 5px;
}
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
  padding: 8px; /* 패딩 축소 */
  gap: 10px;
  cursor: pointer;
  transition: transform 0.1s, background 0.1s;
  box-shadow: 3px 3px 0 rgba(0, 0, 0, 0.3);
}
.meal-slot:active {
  transform: translate(2px, 2px);
  box-shadow: 1px 1px 0 rgba(0, 0, 0, 0.3);
  background: rgba(255, 255, 255, 0.1);
}

.slot-icon-box {
  width: 32px;
  height: 32px;
  background: #2d2d3a;
  border: 2px solid #555;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
}

.slot-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.slot-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.meal-type-badge {
  font-size: 0.65rem;
  background: #ff0055;
  color: #fff;
  padding: 1px 5px;
  border: 1px solid #fff;
}
.meal-cal {
  font-size: 0.75rem;
  color: #00e5ff;
}
.meal-name {
  font-size: 0.9rem;
  color: #fff;
  font-weight: bold;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}

.meal-slot.add-slot {
  border: 2px dashed #aaa;
  background: transparent;
  justify-content: center;
  color: #aaa;
  box-shadow: none;
  padding: 6px;
}
.meal-slot.add-slot:hover {
  border-color: #ffd700;
  color: #ffd700;
  background: rgba(255, 215, 0, 0.1);
}
.plus-icon {
  font-size: 1rem;
  font-weight: bold;
  margin-right: 5px;
}
.add-text {
  font-size: 0.8rem;
}

/* === 공통 유틸 및 기타 페이지 === */
.pixel-text-center {
  text-align: center;
  color: rgba(255, 255, 255, 0.7);
  font-size: 0.85rem;
  margin-bottom: 0.8rem;
  line-height: 1.4;
}
.empty-state-icon {
  font-size: 2.5rem; /* 아이콘 크기 축소 */
  text-align: center;
  margin-bottom: 8px;
  opacity: 0.8;
  animation: float 3s infinite ease-in-out;
}
.page-title {
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
}

/* === 물 (Mana) === */
.hud-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  border-bottom: 2px dashed rgba(255, 255, 255, 0.3);
  padding-bottom: 4px;
}
.current-water {
  font-size: 3rem; /* 폰트 축소 */
  font-weight: bold;
  color: #00e5ff;
  text-shadow: 0 0 10px #00e5ff;
  line-height: 1;
}
.current-water .unit {
  font-size: 1.2rem;
  margin-left: 5px;
}
.goal-water {
  font-size: 0.8rem;
  margin-top: 4px;
}
.mana-bar-container {
  height: 16px;
  margin-top: 5px;
}

/* === 체중 (Score) === */
.score-val {
  font-size: 3rem; /* 폰트 축소 */
  font-weight: bold;
  color: #d500f9;
  text-shadow: 0 0 10px #d500f9;
}
.score-unit {
  font-size: 1.2rem;
}
.score-change {
  font-size: 0.9rem;
  margin-top: 8px;
  padding: 4px 8px;
}
.sm-btn {
  padding: 6px;
  font-size: 0.85rem;
  margin-top: 10px;
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

/* ========================================================= */
/* [추가] 캐릭터 선택 모달 스타일 (기존 CSS 아래에 안전하게 추가) */
/* ========================================================= */
.char-select-modal {
  width: 90%;
  max-width: 400px;
  background: #2d2d3a;
  border: 4px solid #ffd700;
  color: #fff;
  z-index: 10000; /* 최상위 우선순위 */
  position: relative;
}

.char-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr); /* 4열 */
  gap: 10px;
  margin: 1rem 0;
  max-height: 50vh;
  overflow-y: auto;
  padding: 5px;
}
.char-grid::-webkit-scrollbar {
  width: 5px;
}
.char-grid::-webkit-scrollbar-thumb {
  background: #ffd700;
}

.char-slot {
  position: relative;
  background: rgba(0, 0, 0, 0.3);
  border: 2px solid #555;
  border-radius: 4px;
  padding: 5px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: all 0.1s;
}

.grid-char-img {
  width: 100%;
  height: auto;
  image-rendering: pixelated;
}

.char-num {
  font-size: 0.6rem;
  margin-top: 4px;
  color: #aaa;
}

/* 선택된 캐릭터 */
.char-slot.selected {
  border-color: #00e5ff;
  background: rgba(0, 229, 255, 0.2);
  box-shadow: 0 0 5px #00e5ff;
}
.char-slot.selected .char-num {
  color: #00e5ff;
}

/* 잠긴 캐릭터 */
.char-slot.locked {
  border-color: #333;
  opacity: 0.7;
  cursor: not-allowed;
}
.char-slot.locked .grid-char-img {
  filter: grayscale(100%) brightness(0.3);
}

.lock-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 1.5rem;
  z-index: 10;
  text-shadow: 2px 2px 0 #000;
}

/* 모달 오버레이 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.85);
  z-index: 9999; /* 스캔라인(999)보다 위 */
  display: flex;
  justify-content: center;
  align-items: center;
  animation: fadeIn 0.2s;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}
</style>
