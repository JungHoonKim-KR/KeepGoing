<template>
  <div class="home-view retro-theme" @click="initAudioContext">
    <div class="scanlines"></div>

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

            <div class="meal-slot add-slot" @click="handleMealClick">
              <span class="plus-icon">+</span>
              <span class="add-text">ADD NEW ITEM</span>
            </div>
          </div>
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
            <div class="hud-top">
              <span class="hud-label">MP (WATER)</span>
              <span class="hud-val blink-text">{{ Math.round((waterData.water / waterData.goal) * 100) }}%</span>
            </div>

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
          class="pixel-card interactive purple-theme half-card" 
          @click="handleWeightClick"
        >
          <template v-if="weightData.weight == 0.0">
            <h1 class="page-title pixel-font">체중 기록</h1>
            <div class="empty-state-icon">⚖️</div>
            <p class="pixel-text-center">기록이 없습니다.</p>
            <button class="retro-btn purple-btn sm-btn">RECORD</button>
          </template>

          <template v-else>
            <div class="weight-dashboard">
              <h1 class="page-title pixel-font">체중 기록</h1>
              <div class="score-display">
                <span class="score-val">{{ weightData.weight }}</span>
                <span class="score-unit">KG</span>
              </div>
              <div class="score-change" :class="weightData.diff > 0 ? 'bad' : 'good'">
                <span class="change-icon">{{ weightData.diff > 0 ? "▲" : "▼" }}</span>
                {{ Math.abs(weightData.diff) }}kg
                <span class="change-text">{{ weightData.diff > 0 ? "(WARN)" : "(GOOD)" }}</span>
              </div>
            </div>
            <button class="retro-btn purple-btn sm-btn" style="margin-top:auto;">UPDATE</button>
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
import { useConfigStore } from "@/stores/configStore";
import { useAuthStore } from "@/stores/authStore";
import { useRoute } from "vue-router";
import Footer from "@/components/utils/Footer.vue";
import dayjs from "dayjs";
import confetti from "canvas-confetti";

import WaterRecordModal from "@/components/record/WaterRecordModal.vue";
import WeightRecordModal from "@/components/record/WeightRecordModal.vue";
import MealRecordModal from "@/components/record/MealRecordModal.vue";

const authStore = useAuthStore();
const config = useConfigStore();
const route = useRoute();
const MEMBER_ID = authStore.memberId;
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

.scanlines {
  position: fixed;
  top: 0; left: 0; width: 100%; height: 100%;
  pointer-events: none;
  background: linear-gradient(rgba(18, 16, 16, 0) 50%, rgba(0, 0, 0, 0.25) 50%),
    linear-gradient(90deg, rgba(255, 0, 0, 0.06), rgba(0, 255, 0, 0.02), rgba(0, 0, 255, 0.06));
  background-size: 100% 4px, 6px 100%;
  z-index: 999;
}

.page {
  min-height: 100vh;
  scroll-snap-align: start;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center; /* 세로 중앙 정렬 */
  
  /* 헤더와 푸터 높이만큼 패딩을 넉넉하게 줍니다 */
  padding-top: 5rem;    /* 상단 헤더 공간 확보 */
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

/* 반응형: 화면이 너무 작으면 갭을 줄임 */
@media (max-height: 700px) {
  .page-content.split-layout {
    gap: 0.8rem;
  }
  .page {
    padding-top: 4rem;
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
.water-page { background: #000022; }
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
  height: 100px;     /* 높이를 고정하여 게임 화면 내에서 일정한 크기 유지 (max-height 대신 height 권장) */
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

.blue-theme { border-color: #00e5ff; color: #00e5ff; background: #000; }
.blue-btn { background: #00e5ff; color: #000; }
.purple-theme { border-color: #d500f9; color: #d500f9; background: #000; }
.purple-btn { background: #d500f9; color: #fff; }

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
</style>