<template>
  <div class="home-view retro-theme" @click="initAudioContext">
    <div class="scanlines"></div>

    <section class="page daily-page">
      <div class="page-content">
        <div class="retro-header">
          <span class="blinking-cursor">▶</span> PLAYER_DATE: {{ currentDate }}
        </div>

        <div class="pixel-box main-stat-box">
          <div class="stat-header">
            <span class="label">HP (ENERGY)</span>
            <span class="val">912 / 1,298</span>
          </div>
          <div class="retro-progress-container" @click="triggerLevelUp">
            <div class="retro-progress-bar hp-bar" style="width: 70%"></div>
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
    <Footer @open-radio="showRadio = true"></Footer>

    <Teleport to="body">
      <AiRadioModal :isOpen="showRadio" @close="showRadio = false" />
    </Teleport>

    <div v-if="showModal" class="modal-overlay" @click="closeModal"></div>
    <MealRecordModal v-if="showMealModal" @close="closeMealModal" />
    <WaterRecordModal v-if="showWaterModal" @close="closeWaterModal" />
    <WeightRecordModal v-if="showWeightModal" @close="closeWeightModal" />
  </div>
</template>

<script setup>
import { ref, computed } from "vue";
import dayjs from "dayjs";
import confetti from "canvas-confetti"; // npm install canvas-confetti 필요
import characterImage from "../assets/images/characters/test.gif";
import Footer from "../components/utils/Footer.vue";
import AiRadioModal from "../components/common/AiRadioModal.vue";

// ... import 부분
import WaterRecordModal from "@/components/record/WaterRecordModal.vue"; // 경로 확인
import WeightRecordModal from "@/components/record/WeightRecordModal.vue"; // 경로 확인 필요
import MealRecordModal from "@/components/record/MealRecordModal.vue"; // 경로 확인 필요

//나중에 백 완성되면 화면 마운트 되거나 데이터 변경될 떄 불러오면 될듯 !
// 💡 [추가] 오늘의 식단 데이터 (나중엔 API로 받아올 부분)
// 데이터가 비어있으면([]) 'INSERT COIN' 화면이 뜨고, 있으면 리스트가 뜹니다.
const todayMeals = ref([
  { id: 1, type: "아침", name: "사과 & 계란", cal: 350, icon: "🍎" },
  { id: 2, type: "점심", name: "제육볶음 정식", cal: 700, icon: "🍖" },
  { id: 3, type: "간식", name: "프로틴 쉐이크", cal: 120, icon: "🧪" },
]);
// 💡 [추가] 물 데이터 (0이면 기록 없음 상태)
const waterData = ref({
  current: 1.2, // 현재 마신 양 (L)
  goal: 2.0, // 목표 양 (L)
});

// 💡 [추가] 체중 데이터 (null이면 기록 없음 상태)
const weightData = ref({
  current: 70.5, // 오늘 체중
  change: -0.3, // 어제 대비 변화 (마이너스면 살 빠짐)
});

const showWaterModal = ref(false);
const showWeightModal = ref(false);
const showMealModal = ref(false);
const showRadio = ref(false);
// === 상태 변수 ===
const showModal = ref(false);
const isLevelingUp = ref(false);
const isBouncing = ref(false);
const dialogText = ref('"오늘도 힘내보자구!"');
const currentDate = computed(() => dayjs().format("YY.MM.DD"));

const stats = [
  { label: "⚡STR (탄)", class: "carb", percent: "60%", val: "234g" },
  { label: "🛡️DEF (단)", class: "protein", percent: "34%", val: "89g" },
  { label: "🔮INT (지)", class: "fat", percent: "18%", val: "42g" },
];

// === 🎵 8-bit 사운드 엔진 (Web Audio API) ===
// 외부 파일 없이 브라우저 내장 신디사이저로 소리를 만듭니다.
const audioCtx = ref(null);

const initAudioContext = () => {
  if (!audioCtx.value) {
    audioCtx.value = new (window.AudioContext || window.webkitAudioContext)();
  }
};

const playRetroSound = (type) => {
  if (!audioCtx.value) initAudioContext();
  const ctx = audioCtx.value;
  const osc = ctx.createOscillator();
  const gainNode = ctx.createGain();

  osc.connect(gainNode);
  gainNode.connect(ctx.destination);

  const now = ctx.currentTime;

  if (type === "coin") {
    // 띠-링! (Coin Sound)
    osc.type = "square";
    osc.frequency.setValueAtTime(987.77, now); // B5
    osc.frequency.setValueAtTime(1318.51, now + 0.1); // E6
    gainNode.gain.setValueAtTime(0.1, now);
    gainNode.gain.exponentialRampToValueAtTime(0.01, now + 0.3);
    osc.start(now);
    osc.stop(now + 0.3);
  } else if (type === "jump") {
    // 뿅! (Jump/Select)
    osc.type = "square";
    osc.frequency.setValueAtTime(150, now);
    osc.frequency.linearRampToValueAtTime(600, now + 0.1);
    gainNode.gain.setValueAtTime(0.1, now);
    gainNode.gain.exponentialRampToValueAtTime(0.01, now + 0.1);
    osc.start(now);
    osc.stop(now + 0.1);
  } else if (type === "potion") {
    // 꼴깍 (Drinking)
    osc.type = "triangle";
    osc.frequency.setValueAtTime(600, now);
    osc.frequency.linearRampToValueAtTime(300, now + 0.3);
    gainNode.gain.setValueAtTime(0.1, now);
    gainNode.gain.linearRampToValueAtTime(0.01, now + 0.3);
    osc.start(now);
    osc.stop(now + 0.3);
  } else if (type === "levelup") {
    // 따다단딴! (Fanfare)
    osc.type = "square";
    const melody = [523.25, 659.25, 783.99, 1046.5]; // C E G C
    const duration = 0.1;

    // 아르페지오 효과를 위해 여러 개의 오실레이터 생성 필요하지만
    // 간단히 주파수 변경으로 구현
    osc.frequency.setValueAtTime(523.25, now);
    osc.frequency.setValueAtTime(659.25, now + 0.1);
    osc.frequency.setValueAtTime(783.99, now + 0.2);
    osc.frequency.setValueAtTime(1046.5, now + 0.3);

    gainNode.gain.setValueAtTime(0.1, now);
    gainNode.gain.setValueAtTime(0.1, now + 0.3);
    gainNode.gain.exponentialRampToValueAtTime(0.01, now + 0.6);

    osc.start(now);
    osc.stop(now + 0.6);
  }
};

// === 🎉 폭죽 효과 (Confetti) ===
const fireConfetti = () => {
  // 중앙에서 터지는 효과
  confetti({
    particleCount: 100,
    spread: 70,
    origin: { y: 0.6 },
    colors: ["#ff0055", "#00e5ff", "#ffcc00"], // 테마 컬러
  });

  // 양옆에서 쏘는 효과 (좀 더 화려하게)
  setTimeout(() => {
    confetti({
      particleCount: 50,
      angle: 60,
      spread: 55,
      origin: { x: 0 },
      colors: ["#ff0055", "#00e5ff"],
    });
    confetti({
      particleCount: 50,
      angle: 120,
      spread: 55,
      origin: { x: 1 },
      colors: ["#ff0055", "#00e5ff"],
    });
  }, 200);
};

// === 이벤트 핸들러 ===
const handleMealClick = () => {
  playRetroSound("coin");
  // 모달 열기 로직...
  showMealModal.value = true;
};

const handleWaterClick = () => {
  playRetroSound("potion");
  // 물 추가 로직...
  showWaterModal.value = true;
};
const handleWeightClick = () => {
  playRetroSound("jump");
  showWeightModal.value = true;
};
const closeWaterModal = () => {
  showWaterModal.value = false;
};
const closeWeightModal = () => {
  showWeightModal.value = false;
};
const closeMealModal = () => {
  showMealModal.value = false;
};

const triggerLevelUp = () => {
  if (isLevelingUp.value) return;

  isLevelingUp.value = true;
  dialogText.value = "LEVEL UP! 능력이 상승했다!";

  playRetroSound("levelup");
  fireConfetti();

  // 캐릭터 점프 애니메이션
  isBouncing.value = true;
  setTimeout(() => (isBouncing.value = false), 1000);

  setTimeout(() => {
    isLevelingUp.value = false;
    dialogText.value = '"다음 레벨로 가보자!"';
  }, 3000);
};

const closeModal = () => (showModal.value = false);
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
