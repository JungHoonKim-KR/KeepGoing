<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

// ============================================================
// 상태 관리
// ============================================================
const isLoading = ref(true);
const selectedGoal = ref(null);
const recommendationData = ref(null);
const bootLogs = ref([]);

// 사용자 목표 (게임 모드 컨셉)
const goals = [
  {
    id: "weight-loss",
    label: "WEIGHT LOSS",
    sub: "DEFENSE UP",
    icon: "🛡️",
    color: "mode-blue",
  },
  {
    id: "muscle-gain",
    label: "MUSCLE GAIN",
    sub: "ATTACK UP",
    icon: "⚔️",
    color: "mode-red",
  },
  {
    id: "health",
    label: "HEALTH CARE",
    sub: "RECOVERY",
    icon: "❤️",
    color: "mode-green",
  },
  {
    id: "energy",
    label: "ENERGY BOOST",
    sub: "SPEED UP",
    icon: "⚡",
    color: "mode-yellow",
  },
];

// ============================================================
// 사운드 & 유틸리티
// ============================================================
const playSound = (type) => {
  const AudioContext = window.AudioContext || window.webkitAudioContext;
  if (!AudioContext) return;
  const ctx = new AudioContext();
  const osc = ctx.createOscillator();
  const gain = ctx.createGain();
  osc.connect(gain);
  gain.connect(ctx.destination);

  const now = ctx.currentTime;
  if (type === "select") {
    osc.type = "square";
    osc.frequency.setValueAtTime(440, now);
    gain.gain.setValueAtTime(0.05, now);
    gain.gain.exponentialRampToValueAtTime(0.01, now + 0.1);
    osc.start(now);
    osc.stop(now + 0.1);
  } else if (type === "confirm") {
    osc.type = "sawtooth";
    osc.frequency.setValueAtTime(220, now);
    osc.frequency.linearRampToValueAtTime(880, now + 0.2);
    gain.gain.setValueAtTime(0.05, now);
    gain.gain.linearRampToValueAtTime(0, now + 0.2);
    osc.start(now);
    osc.stop(now + 0.2);
  }
};

// ============================================================
// 라이프사이클
// ============================================================
onMounted(() => {
  // 초기 로딩 (시스템 부팅)
  setTimeout(() => {
    isLoading.value = false;
  }, 1000);
});

// ============================================================
// 메서드
// ============================================================
const selectGoal = (goalId) => {
  playSound("confirm");
  selectedGoal.value = goalId;
  isLoading.value = true;
  bootLogs.value = [];

  // 로딩 로그 애니메이션
  const logs = [
    "ANALYZING BIOMETRICS...",
    "CALCULATING MACROS...",
    "GENERATING LOOT TABLE...",
    "FINALIZING STRATEGY...",
  ];
  let i = 0;
  const logInterval = setInterval(() => {
    if (i < logs.length) bootLogs.value.push(logs[i++]);
    else clearInterval(logInterval);
  }, 400);

  setTimeout(() => {
    // 데이터 로드 (기존 데이터 유지)
    const goalData = {
      "weight-loss": {
        goal: "WEIGHT LOSS MISSION",
        dailyCalorie: 1800,
        macros: { protein: 150, carbs: 180, fat: 60 },
        ratio: { protein: 35, carbs: 40, fat: 25 },
        mealPlan: [
          {
            id: 1,
            mealType: "BREAKFAST",
            time: "07:00",
            name: "Scrambled Eggs Set",
            calories: 350,
            description: "Eggs(3) + Spinach + Toast",
            benefits: ["STR +5", "Fullness"],
          },
          {
            id: 2,
            mealType: "LUNCH",
            time: "12:30",
            name: "Grilled Chicken Salad",
            calories: 420,
            description: "Chicken Breast + Veggies",
            benefits: ["AGI +3", "Low Fat"],
          },
          {
            id: 3,
            mealType: "DINNER",
            time: "18:30",
            name: "Salmon Steak",
            calories: 400,
            description: "Salmon + Brown Rice",
            benefits: ["INT +2", "Omega-3"],
          },
        ],
        tips: ["Drink Water (Potion)", "No Late Night Snacks"],
        warnings: ["Avoid Sugar Traps", "Keep Protein High"],
      },
      "muscle-gain": {
        goal: "MUSCLE BUILD MISSION",
        dailyCalorie: 2500,
        macros: { protein: 200, carbs: 300, fat: 80 },
        ratio: { protein: 35, carbs: 45, fat: 20 },
        mealPlan: [
          {
            id: 1,
            mealType: "BREAKFAST",
            time: "07:00",
            name: "Protein Pancakes",
            calories: 500,
            description: "Eggs + Oats + Whey",
            benefits: ["STR +10", "Power"],
          },
          {
            id: 2,
            mealType: "LUNCH",
            time: "12:30",
            name: "Beef Bowl",
            calories: 650,
            description: "Beef + Rice + Potato",
            benefits: ["STR +8", "Energy"],
          },
          {
            id: 3,
            mealType: "DINNER",
            time: "18:30",
            name: "Chicken Curry",
            calories: 600,
            description: "Chicken + Curry + Rice",
            benefits: ["Recov +5", "Taste"],
          },
        ],
        tips: ["Lift Heavy", "Sleep 8 Hours"],
        warnings: ["Don't Skip Leg Day", "Eat More Carbs"],
      },
      health: {
        /* ... 데이터 생략 (위와 동일한 구조로 가정) ... */ goal: "HEALTH CARE",
        dailyCalorie: 2000,
        macros: { protein: 100, carbs: 250, fat: 65 },
        ratio: { protein: 20, carbs: 50, fat: 30 },
        mealPlan: [],
        tips: [],
        warnings: [],
      },
      energy: {
        /* ... 데이터 생략 ... */ goal: "ENERGY BOOST",
        dailyCalorie: 2200,
        macros: { protein: 110, carbs: 280, fat: 70 },
        ratio: { protein: 22, carbs: 52, fat: 26 },
        mealPlan: [],
        tips: [],
        warnings: [],
      },
    };

    // 데이터가 없는 경우를 위한 Fallback (health, energy용)
    if (!goalData[goalId].mealPlan.length) {
      goalData[goalId].mealPlan = [
        {
          id: 1,
          mealType: "BREAKFAST",
          time: "08:00",
          name: "Balanced Meal A",
          calories: 400,
          description: "Healthy ingredients",
          benefits: ["Vit +5"],
        },
        {
          id: 2,
          mealType: "LUNCH",
          time: "12:00",
          name: "Power Salad",
          calories: 500,
          description: "Fresh Greens",
          benefits: ["HP +10"],
        },
      ];
      goalData[goalId].tips = ["Stay Hydrated"];
      goalData[goalId].warnings = ["Rest Well"];
    }

    recommendationData.value = goalData[goalId];
    isLoading.value = false;
  }, 2000);
};

const goBack = () => {
  playSound("select");
  selectedGoal.value = null;
  recommendationData.value = null;
};
</script>

<template>
  <div class="diet-plan-view retro-theme">
    <div class="scanlines"></div>

    <div class="content">
      <div class="header">
        <button v-if="selectedGoal" @click="goBack" class="back-btn">
          ◀ BACK
        </button>
        <div class="header-title">
          <span class="icon">📜</span>
          {{ selectedGoal ? "STRATEGY GUIDE" : "SELECT MODE" }}
        </div>
      </div>

      <div v-if="!selectedGoal && !isLoading" class="mode-select-container">
        <div class="pixel-text-center">CHOOSE YOUR MISSION</div>
        <div class="goals-grid">
          <button
            v-for="goal in goals"
            :key="goal.id"
            @click="selectGoal(goal.id)"
            :class="['goal-card', goal.color]"
          >
            <div class="goal-icon">{{ goal.icon }}</div>
            <div class="goal-info">
              <div class="goal-label">{{ goal.label }}</div>
              <div class="goal-sub">{{ goal.sub }}</div>
            </div>
            <div class="select-cursor">◀</div>
          </button>
        </div>
      </div>

      <div v-if="isLoading" class="loading-terminal">
        <div class="terminal-text">
          <div v-for="(log, idx) in bootLogs" :key="idx" class="log-line">
            > {{ log }}
          </div>
          <div class="log-line blink">> _</div>
        </div>
        <div class="loading-bar-frame">
          <div class="loading-bar-fill"></div>
        </div>
      </div>

      <div
        v-if="selectedGoal && !isLoading && recommendationData"
        class="result-container"
      >
        <div class="hud-panel">
          <div class="hud-header">=== PLAYER STATUS ===</div>
          <div class="main-stat">
            <span class="stat-label">TOTAL ENERGY</span>
            <span class="stat-val"
              >{{ recommendationData.dailyCalorie }} <small>KCAL</small></span
            >
          </div>

          <div class="macro-bars">
            <div class="bar-group">
              <div class="bar-label">
                PRO ({{ recommendationData.macros.protein }}g)
              </div>
              <div class="pixel-bar-bg">
                <div
                  class="pixel-bar-fill hp"
                  :style="{ width: recommendationData.ratio.protein + '%' }"
                ></div>
              </div>
            </div>
            <div class="bar-group">
              <div class="bar-label">
                CAR ({{ recommendationData.macros.carbs }}g)
              </div>
              <div class="pixel-bar-bg">
                <div
                  class="pixel-bar-fill mp"
                  :style="{ width: recommendationData.ratio.carbs + '%' }"
                ></div>
              </div>
            </div>
            <div class="bar-group">
              <div class="bar-label">
                FAT ({{ recommendationData.macros.fat }}g)
              </div>
              <div class="pixel-bar-bg">
                <div
                  class="pixel-bar-fill sp"
                  :style="{ width: recommendationData.ratio.fat + '%' }"
                ></div>
              </div>
            </div>
          </div>
        </div>

        <div class="inventory-panel">
          <div class="hud-header">=== SUPPLY CRATE (MEALS) ===</div>
          <div class="meal-list">
            <div
              v-for="(meal, idx) in recommendationData.mealPlan"
              :key="meal.id"
              class="item-slot"
              :style="{ animationDelay: `${idx * 0.1}s` }"
            >
              <div class="item-icon-box">
                <span class="item-type">{{
                  meal.mealType.substring(0, 1)
                }}</span>
              </div>
              <div class="item-details">
                <div class="item-header">
                  <span class="item-name">{{ meal.name }}</span>
                  <span class="item-cal">{{ meal.calories }}kcal</span>
                </div>
                <div class="item-desc">{{ meal.description }}</div>
                <div class="item-buffs">
                  <span
                    v-for="buff in meal.benefits"
                    :key="buff"
                    class="buff-tag"
                    >{{ buff }}</span
                  >
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="log-panel">
          <div class="hud-header">=== SYSTEM LOGS ===</div>
          <div class="log-content">
            <div class="log-section">
              <span class="log-title">[ TIPS ]</span>
              <ul>
                <li v-for="tip in recommendationData.tips" :key="tip">
                  {{ tip }}
                </li>
              </ul>
            </div>
            <div class="log-section warning">
              <span class="log-title">[ WARNING ]</span>
              <ul>
                <li v-for="warn in recommendationData.warnings" :key="warn">
                  {{ warn }}
                </li>
              </ul>
            </div>
          </div>
        </div>

        <div class="action-row">
          <button class="retro-btn save">SAVE PLAN</button>
          <button class="retro-btn reset" @click="goBack">RESET</button>
        </div>
      </div>
    </div>

    <div style="height: 100px"></div>
  </div>
</template>

<style scoped>
@import url("https://cdn.jsdelivr.net/gh/neodgm/neodgm-webfont@latest/neodgm/style.css");

:root {
  --bg-dark: #101018;
  --neon-blue: #00e5ff;
  --neon-green: #00ff00;
  --neon-red: #ff0055;
  --neon-yellow: #ffd700;
  --card-bg: #1a1a24;
}

.diet-plan-view {
  min-height: 100vh;
  background-color: var(--bg-dark);
  font-family: "NeoDunggeunmo", monospace;
  color: #fff;
  padding: 1rem;
  position: relative;
  overflow-x: hidden;
}

/* Scanlines */
.scanlines {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  background: linear-gradient(rgba(18, 16, 16, 0) 50%, rgba(0, 0, 0, 0.1) 50%),
    linear-gradient(
      90deg,
      rgba(255, 0, 0, 0.03),
      rgba(0, 255, 0, 0.01),
      rgba(0, 0, 255, 0.03)
    );
  background-size: 100% 4px, 6px 100%;
  z-index: 1;
}

.content {
  position: relative;
  z-index: 10;
  max-width: 500px;
  margin: 0 auto;
}

/* Header */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 2rem;
  border-bottom: 2px solid #333;
  padding-bottom: 10px;
}
.back-btn {
  background: none;
  border: none;
  color: #888;
  font-family: inherit;
  cursor: pointer;
  font-size: 0.9rem;
}
.header-title {
  font-size: 1.2rem;
  color: var(--neon-yellow);
  text-shadow: 2px 2px 0 #000;
}

/* 1. Mode Select */
.mode-select-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.pixel-text-center {
  text-align: center;
  color: var(--neon-blue);
  margin-bottom: 10px;
  font-size: 0.9rem;
}
.goals-grid {
  display: grid;
  gap: 12px;
}

.goal-card {
  background: #222;
  border: 2px solid #444;
  padding: 15px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 15px;
  position: relative;
  transition: all 0.2s;
  text-align: left;
}
.goal-card:hover,
.goal-card:active {
  transform: translateX(5px);
  background: #2a2a2a;
  border-color: #fff;
}
.goal-icon {
  font-size: 1.5rem;
}
.goal-label {
  font-size: 1rem;
  font-weight: bold;
  color: #fff;
}
.goal-sub {
  font-size: 0.7rem;
  color: #888;
  margin-top: 2px;
}
.select-cursor {
  position: absolute;
  right: 10px;
  color: var(--neon-green);
  opacity: 0;
  transition: opacity 0.2s;
}
.goal-card:hover .select-cursor {
  opacity: 1;
  animation: blink 1s infinite;
}

.mode-blue {
  border-left: 4px solid var(--neon-blue);
}
.mode-red {
  border-left: 4px solid var(--neon-red);
}
.mode-green {
  border-left: 4px solid var(--neon-green);
}
.mode-yellow {
  border-left: 4px solid var(--neon-yellow);
}

/* 2. Loading Terminal */
.loading-terminal {
  background: #000;
  border: 2px solid var(--neon-green);
  padding: 15px;
  height: 200px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.terminal-text {
  font-size: 0.8rem;
  color: var(--neon-green);
}
.log-line {
  margin-bottom: 5px;
}
.loading-bar-frame {
  height: 15px;
  border: 2px solid var(--neon-green);
  padding: 2px;
}
.loading-bar-fill {
  height: 100%;
  background: var(--neon-green);
  width: 0%;
  animation: load 1.8s forwards;
}
@keyframes load {
  to {
    width: 100%;
  }
}

/* 3. Result HUD */
.result-container {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}
.hud-panel {
  background: var(--card-bg);
  border: 2px solid #333;
  padding: 15px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5);
}
.hud-header {
  font-size: 0.8rem;
  color: #888;
  border-bottom: 1px dashed #555;
  padding-bottom: 5px;
  margin-bottom: 10px;
  text-align: center;
}

.main-stat {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 15px;
}
.stat-label {
  color: #aaa;
  font-size: 0.9rem;
}
.stat-val {
  font-size: 1.5rem;
  color: var(--neon-yellow);
  font-weight: bold;
}

.macro-bars {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.bar-group {
  display: flex;
  align-items: center;
  gap: 10px;
}
.bar-label {
  width: 80px;
  font-size: 0.7rem;
  color: #fff;
  text-align: right;
}
.pixel-bar-bg {
  flex: 1;
  height: 12px;
  background: #000;
  border: 1px solid #555;
  padding: 1px;
}
.pixel-bar-fill {
  height: 100%;
}
.pixel-bar-fill.hp {
  background: var(--neon-red);
  box-shadow: 0 0 5px var(--neon-red);
}
.pixel-bar-fill.mp {
  background: var(--neon-blue);
  box-shadow: 0 0 5px var(--neon-blue);
}
.pixel-bar-fill.sp {
  background: var(--neon-yellow);
  box-shadow: 0 0 5px var(--neon-yellow);
}

/* Inventory Style Meal List */
.inventory-panel {
  background: #222;
  border: 2px solid #444;
  padding: 10px;
}
.meal-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.item-slot {
  display: flex;
  gap: 10px;
  background: #111;
  border: 1px solid #333;
  padding: 8px;
  animation: slideIn 0.3s ease-out backwards;
}
@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.item-icon-box {
  width: 40px;
  height: 40px;
  background: #333;
  border: 1px solid #555;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  color: #fff;
  font-weight: bold;
}
.item-details {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.item-name {
  font-size: 0.9rem;
  color: #fff;
  font-weight: bold;
}
.item-cal {
  font-size: 0.8rem;
  color: var(--neon-yellow);
}
.item-desc {
  font-size: 0.7rem;
  color: #888;
}
.item-buffs {
  display: flex;
  gap: 5px;
  margin-top: 3px;
}
.buff-tag {
  font-size: 0.6rem;
  background: rgba(0, 255, 0, 0.1);
  color: var(--neon-green);
  padding: 1px 4px;
  border: 1px solid var(--neon-green);
}

/* Log Panel */
.log-panel {
  background: #000;
  border: 2px solid #fff;
  padding: 10px;
  font-size: 0.8rem;
}
.log-section {
  margin-bottom: 10px;
}
.log-title {
  display: block;
  color: var(--neon-blue);
  margin-bottom: 3px;
  font-weight: bold;
}
.log-section.warning .log-title {
  color: var(--neon-red);
}
.log-panel ul {
  padding-left: 15px;
  margin: 0;
  list-style: square;
  color: #ccc;
}

/* Buttons */
.action-row {
  display: flex;
  gap: 10px;
}
.retro-btn {
  flex: 1;
  padding: 12px;
  font-family: inherit;
  font-weight: bold;
  cursor: pointer;
  font-size: 1rem;
  border: 2px solid #fff;
  box-shadow: 4px 4px 0 #000;
  transition: transform 0.1s;
}
.retro-btn:active {
  transform: translate(4px, 4px);
  box-shadow: none;
}
.retro-btn.save {
  background: var(--neon-blue);
  color: #000;
}
.retro-btn.reset {
  background: #333;
  color: #fff;
}

@keyframes blink {
  50% {
    opacity: 0;
  }
}
</style>
