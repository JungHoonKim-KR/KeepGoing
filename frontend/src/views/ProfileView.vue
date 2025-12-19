<script setup>
import { ref } from "vue";
import Footer from "../components/utils/Footer.vue";
import characterImage from "../assets/images/characters/test.gif";

// ----------------------------------------------------
// 1. 상태 관리
// ----------------------------------------------------
const profile = ref({
  name: "Hero_GD",
  age: 30,
  gender: "male",
  height: 175,
  weight: 70,
  activityLevel: "moderate",
  sleepHours: 7,
  weeklyWorkout: 3,
  bodyType: "balanced",
  class: "Adventurer",
  level: 24,
});

const isEditing = ref(false);

const activityOptions = [
  { value: "low", label: "LOW (휴식)", icon: "💤" },
  { value: "moderate", label: "NORMAL (활동)", icon: "🚶" },
  { value: "high", label: "HIGH (운동)", icon: "🔥" },
];

const bodyTypeOptions = [
  { value: "lower", label: "SPEED (하체)", icon: "🦵" },
  { value: "upper", label: "POWER (상체)", icon: "💪" },
  { value: "balanced", label: "BALANCE (균형)", icon: "⚖️" },
];

// ----------------------------------------------------
// 2. 이벤트 핸들러
// ----------------------------------------------------
const playSound = (type) => {
  const AudioContext = window.AudioContext || window.webkitAudioContext;
  if (!AudioContext) return;
  const ctx = new AudioContext();
  const osc = ctx.createOscillator();
  const gain = ctx.createGain();
  osc.connect(gain);
  gain.connect(ctx.destination);

  const now = ctx.currentTime;
  if (type === "click") {
    osc.type = "square";
    osc.frequency.setValueAtTime(600, now);
    gain.gain.setValueAtTime(0.05, now);
    gain.gain.exponentialRampToValueAtTime(0.01, now + 0.1);
    osc.start(now);
    osc.stop(now + 0.1);
  } else if (type === "save") {
    osc.type = "triangle";
    osc.frequency.setValueAtTime(440, now);
    osc.frequency.linearRampToValueAtTime(880, now + 0.2);
    gain.gain.setValueAtTime(0.05, now);
    gain.gain.linearRampToValueAtTime(0, now + 0.3);
    osc.start(now);
    osc.stop(now + 0.3);
  }
};

const startEdit = () => {
  playSound("click");
  isEditing.value = true;
};
const saveProfile = () => {
  playSound("save");
  console.log("Save:", profile.value);
  isEditing.value = false;
};
const cancelEdit = () => {
  playSound("click");
  isEditing.value = false;
};
</script>

<template>
  <div class="profile-view retro-theme">
    <div class="scanlines"></div>

    <div class="content">
      <header class="header">
        <!-- <div class="player-info">
          <div class="name-group">
            <span class="player-badge">ID</span>
            <span class="player-name">{{ profile.name }}</span>
          </div>
          <span class="level-badge">Lv.{{ profile.level }}</span>
        </div> -->
        <button v-if="!isEditing" @click="startEdit" class="retro-btn sm-btn">
          EDIT
        </button>
        <button
          v-if="isEditing"
          @click="cancelEdit"
          class="retro-btn sm-btn cancel"
        >
          CANCEL
        </button>
      </header>

      <div class="character-showcase">
        <div class="holo-pad"></div>
        <img :src="characterImage" alt="Avatar" class="character-sprite" />
        <div class="class-label">{{ profile.class }}</div>
      </div>

      <div class="stats-container">
        <template v-if="!isEditing">
          <div class="section-title pixel-font">PLAYER STATS</div>

          <div class="stats-grid">
            <div class="stat-box">
              <span class="label">AGE</span>
              <span class="val">{{ profile.age }}</span>
            </div>
            <div class="stat-box">
              <span class="label">SEX</span>
              <span class="val">{{
                profile.gender === "male" ? "M" : "F"
              }}</span>
            </div>
            <div class="stat-box">
              <span class="label">HGT</span>
              <span class="val">{{ profile.height }}<small>cm</small></span>
            </div>
            <div class="stat-box">
              <span class="label">WGT</span>
              <span class="val">{{ profile.weight }}<small>kg</small></span>
            </div>
          </div>

          <div class="section-title pixel-font">TRAITS</div>

          <div class="traits-list">
            <div class="trait-row">
              <div class="trait-icon">⚡</div>
              <div class="trait-info">
                <div class="trait-label">ACTIVITY LEVEL</div>
                <div class="trait-val">
                  {{
                    activityOptions.find(
                      (o) => o.value === profile.activityLevel
                    )?.label
                  }}
                </div>
              </div>
            </div>
            <div class="trait-row">
              <div class="trait-icon">💤</div>
              <div class="trait-info">
                <div class="trait-label">SLEEP RECOVERY</div>
                <div class="trait-val">
                  {{ profile.sleepHours }} HOURS / DAY
                </div>
              </div>
            </div>
            <div class="trait-row">
              <div class="trait-icon">⚔️</div>
              <div class="trait-info">
                <div class="trait-label">TRAINING FREQ</div>
                <div class="trait-val">
                  {{ profile.weeklyWorkout }} TIMES / WEEK
                </div>
              </div>
            </div>
            <div class="trait-row">
              <div class="trait-icon">🧬</div>
              <div class="trait-info">
                <div class="trait-label">BODY TYPE</div>
                <div class="trait-val">
                  {{
                    bodyTypeOptions.find((o) => o.value === profile.bodyType)
                      ?.label
                  }}
                </div>
              </div>
            </div>
          </div>
        </template>

        <template v-else>
          <div class="section-title pixel-font blink">MODIFY DATA...</div>

          <div class="edit-form">
            <div class="form-row">
              <div class="form-group">
                <label>AGE</label>
                <input
                  type="number"
                  v-model.number="profile.age"
                  class="retro-input"
                />
              </div>
              <div class="form-group">
                <label>SEX</label>
                <div class="radio-pill-group">
                  <label
                    class="pill"
                    :class="{ active: profile.gender === 'male' }"
                  >
                    <input
                      type="radio"
                      value="male"
                      v-model="profile.gender"
                      hidden
                    />
                    M
                  </label>
                  <label
                    class="pill"
                    :class="{ active: profile.gender === 'female' }"
                  >
                    <input
                      type="radio"
                      value="female"
                      v-model="profile.gender"
                      hidden
                    />
                    F
                  </label>
                </div>
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>HEIGHT (cm)</label>
                <input
                  type="number"
                  v-model.number="profile.height"
                  class="retro-input"
                />
              </div>
              <div class="form-group">
                <label>WEIGHT (kg)</label>
                <input
                  type="number"
                  v-model.number="profile.weight"
                  class="retro-input"
                />
              </div>
            </div>

            <div class="form-group">
              <label>ACTIVITY LEVEL</label>
              <select v-model="profile.activityLevel" class="retro-select">
                <option
                  v-for="opt in activityOptions"
                  :key="opt.value"
                  :value="opt.value"
                >
                  {{ opt.icon }} {{ opt.label }}
                </option>
              </select>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>SLEEP (H)</label>
                <input
                  type="number"
                  v-model.number="profile.sleepHours"
                  class="retro-input"
                />
              </div>
              <div class="form-group">
                <label>WORKOUT/WK</label>
                <input
                  type="number"
                  v-model.number="profile.weeklyWorkout"
                  class="retro-input"
                />
              </div>
            </div>

            <div class="form-group">
              <label>BODY TYPE</label>
              <div class="body-type-grid">
                <label
                  v-for="opt in bodyTypeOptions"
                  :key="opt.value"
                  class="type-card"
                  :class="{ active: profile.bodyType === opt.value }"
                >
                  <input
                    type="radio"
                    :value="opt.value"
                    v-model="profile.bodyType"
                    hidden
                  />
                  <div class="type-icon">{{ opt.icon }}</div>
                  <div class="type-name">{{ opt.label }}</div>
                </label>
              </div>
            </div>

            <button @click="saveProfile" class="retro-btn full-btn">
              SAVE CHANGES
            </button>
          </div>
        </template>
      </div>
    </div>

    <Footer></Footer>
  </div>
</template>

<style scoped>
/* 레트로 폰트 */
@import url("https://cdn.jsdelivr.net/gh/neodgm/neodgm-webfont@latest/neodgm/style.css");

:root {
  --neon-blue: #00e5ff;
  --neon-pink: #ff0055;
  --neon-green: #00ff00;
  --bg-dark: #101018;
  --card-bg: #1a1a24;
}

/* === 전체 컨테이너 === */
/* 배경색을 강제로 지정하여 흰색 배경 방지 */
.profile-view.retro-theme {
  min-height: 100vh;
  background-color: #101018 !important; /* 강제 적용 */
  font-family: "NeoDunggeunmo", monospace;
  color: #e0e0e0;
  padding-bottom: 120px; /* 푸터 공간 확보 */
  position: relative;
  overflow-x: hidden;
  box-sizing: border-box;
}

/* 스캔라인 효과 (가독성을 위해 투명도 낮춤) */
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
  padding: 1rem;
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 500px; /* 태블릿/데스크탑 대응 */
  margin: 0 auto;
  box-sizing: border-box;
}

/* === 헤더 === */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  /* margin-bottom: 2rem; */
  border-bottom: 2px solid #333;
  padding-bottom: 1rem;
}
.player-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.name-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.player-badge {
  background: var(--neon-blue);
  color: #000;
  padding: 2px 6px;
  font-weight: bold;
  font-size: 0.7rem;
}
.player-name {
  font-size: 1.1rem;
  color: #fff;
  text-shadow: 0 0 5px var(--neon-blue);
}
.level-badge {
  color: var(--neon-green);
  font-size: 0.8rem;
  border: 1px solid var(--neon-green);
  padding: 2px 4px;
  border-radius: 4px;
  display: inline-block;
  width: fit-content;
}

/* 버튼 스타일 */
.retro-btn {
  background: transparent;
  border: 2px solid #fff;
  color: #fff;
  padding: 8px 12px;
  font-family: inherit;
  cursor: pointer;
  box-shadow: 2px 2px 0 #000;
  transition: all 0.1s;
  font-size: 0.9rem;
}
.retro-btn:active {
  transform: translate(2px, 2px);
  box-shadow: none;
}
.sm-btn {
  font-size: 0.8rem;
  padding: 6px 10px;
}
.sm-btn.cancel {
  border-color: var(--neon-pink);
  color: var(--neon-pink);
}
.full-btn {
  width: 100%;
  background: var(--neon-green);
  color: #000;
  font-weight: bold;
  border: none;
  padding: 15px;
  font-size: 1.1rem;
  margin-top: 1rem;
}

/* === 캐릭터 아바타 === */
.character-showcase {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  /* margin-bottom: 2rem; */
  position: relative;
  height: 160px;
}
.character-sprite {
  width: 100px;
  height: 100px;
  object-fit: contain;
  image-rendering: pixelated;
  z-index: 2;
  animation: float 3s infinite ease-in-out;
}
@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.holo-pad {
  position: absolute;
  bottom: 20px;
  width: 120px;
  height: 30px;
  border: 2px solid var(--neon-blue);
  border-radius: 50%;
  background: rgba(0, 229, 255, 0.1);
  transform: rotateX(60deg);
  box-shadow: 0 0 20px var(--neon-blue);
  z-index: 1;
  animation: pulsePad 2s infinite;
}
@keyframes pulsePad {
  0%,
  100% {
    opacity: 0.5;
  }
  50% {
    opacity: 1;
  }
}

.class-label {
  margin-top: 10px;
  font-size: 0.9rem;
  color: var(--neon-pink);
  letter-spacing: 2px;
  border-bottom: 1px solid var(--neon-pink);
  padding-bottom: 2px;
}

/* === STATS 컨테이너 === */
.stats-container {
  background: #1a1a24; /* 명시적 배경색 */
  border: 2px solid #333;
  padding: 1.25rem;
  position: relative;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
  border-radius: 4px;
}
.section-title {
  font-size: 0.9rem;
  color: var(--neon-green);
  margin-bottom: 1rem;
  border-left: 4px solid var(--neon-green);
  padding-left: 10px;
}
.blink {
  animation: blink 1s infinite;
}
@keyframes blink {
  50% {
    opacity: 0;
  }
}

/* 그리드 (읽기 모드) */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  margin-bottom: 2rem;
}
.stat-box {
  background: #222;
  padding: 12px;
  border: 1px solid #444;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.stat-box .label {
  color: #888;
  font-size: 0.7rem;
}
.stat-box .val {
  color: #fff;
  font-size: 1rem;
  font-weight: bold;
}
.stat-box .val small {
  font-size: 0.7rem;
  color: #aaa;
  margin-left: 2px;
  font-weight: normal;
}

.traits-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.trait-row {
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px dashed #333;
  padding-bottom: 8px;
}
.trait-icon {
  font-size: 1.4rem;
  width: 30px;
  text-align: center;
}
.trait-info {
  flex: 1;
}
.trait-label {
  font-size: 0.65rem;
  color: #888;
  margin-bottom: 3px;
  display: block;
}
.trait-val {
  font-size: 0.9rem;
  color: var(--neon-blue);
  line-height: 1.2;
}

/* === 수정 모드 (Edit Form) === */
.edit-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

/* 모바일 대응: 기본적으로 flex-wrap을 허용하여 좁을 땐 떨어지게 함 */
.form-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
  min-width: 140px; /* 너무 좁아지지 않게 */
}
.form-group label {
  font-size: 0.75rem;
  color: #aaa;
}

/* 입력창 스타일 강화 (흰색 방지) */
.retro-input,
.retro-select {
  background-color: #000 !important; /* 배경 검정 강제 */
  border: 2px solid #555;
  color: #fff !important; /* 글씨 흰색 강제 */
  padding: 12px;
  font-family: inherit;
  font-size: 1rem;
  outline: none;
  border-radius: 0;
  width: 100%;
  box-sizing: border-box;
  -webkit-appearance: none; /* 아이폰 기본 스타일 제거 */
}
.retro-input:focus,
.retro-select:focus {
  border-color: var(--neon-blue);
}

/* 라디오 버튼 (알약 형태) */
.radio-pill-group {
  display: flex;
  gap: 8px;
  height: 48px;
}
.pill {
  flex: 1;
  border: 2px solid #555;
  color: #888;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-weight: bold;
  background: #222;
  transition: all 0.2s;
}
.pill.active {
  border-color: var(--neon-blue);
  background: rgba(0, 229, 255, 0.2);
  color: #fff;
}

/* 체형 그리드 (모바일 1단 -> 큰화면 3단) */
.body-type-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}
.type-card {
  background: #222;
  border: 2px solid #444;
  padding: 12px 5px;
  text-align: center;
  cursor: pointer;
  opacity: 0.6;
  transition: all 0.2s;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.type-card.active {
  border-color: var(--neon-green);
  opacity: 1;
  transform: scale(1.02);
  background: #000;
}
.type-icon {
  font-size: 1.5rem;
  margin-bottom: 5px;
}
.type-name {
  font-size: 0.7rem;
  color: #fff;
}

/* 모바일 미디어 쿼리 (매우 작은 화면용) */
@media (max-width: 380px) {
  .stats-grid {
    grid-template-columns: 1fr;
  } /* 스탯 한 줄로 */
  .body-type-grid {
    grid-template-columns: 1fr;
  } /* 체형 선택 한 줄로 */
  .form-row {
    flex-direction: column;
    gap: 15px;
  } /* 입력창 세로 배치 */
}
</style>
