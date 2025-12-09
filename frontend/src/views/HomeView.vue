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
        <div class="pixel-card interactive" @click="handleMealClick">
          <div class="card-inner">
            <div class="icon-8bit">🍗</div>
            <h2>LOG ITEM</h2>
            <p class="pixel-text">식사를 기록하고 경험치를 획득하세요.</p>
            <button class="retro-btn press-start">INSERT COIN</button>
          </div>
        </div>
      </div>
    </section>

    <section class="page water-page">
      <div class="page-content">
        <div
          class="pixel-card interactive blue-theme"
          @click="handleWaterClick"
        >
          <h1 class="page-title pixel-font">MANA POTION</h1>
          <div class="potion-container">
            <div class="potion-bottle">
              <div class="potion-liquid" style="height: 75%">
                <div class="bubbles">
                  <span></span><span></span><span></span>
                </div>
              </div>
            </div>
          </div>
          <button class="retro-btn blue-btn">DRINK</button>
        </div>
      </div>
    </section>

    <section class="page weight-page">
      <div class="page-content">
        <div
          class="pixel-card interactive purple-theme"
          @click="handleWeightClick"
        >
          <h1 class="page-title pixel-font">HIGH SCORE</h1>
          <div class="score-board"></div>
          <button class="retro-btn purple-btn">UPDATE SCORE</button>
        </div>
      </div>
    </section>

    <Footer></Footer>

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

// ... import 부분
import WaterRecordModal from "@/components/record/WaterRecordModal.vue"; // 경로 확인
import WeightRecordModal from "@/components/record/WeightRecordModal.vue"; // 경로 확인 필요
import MealRecordModal from "@/components/record/MealRecordModal.vue"; // 경로 확인 필요

const showWaterModal = ref(false);
const showWeightModal = ref(false);
const showMealModal = ref(false);
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
</style>
