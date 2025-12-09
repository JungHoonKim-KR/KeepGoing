<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

// ----------------------------------------------------
// 1. 상태 관리
// ----------------------------------------------------
const currentStep = ref(1);
const totalSteps = 4;
const isLoading = ref(false);

const signupData = ref({
  email: "",
  password: "",
  confirmPassword: "",
  name: "",
  gender: "M",
  age: null,
  height: null,
  weight: null,
  target_weight: null,
  activity: "moderate",
  goal: "diet",
  health_condition: "",
  allergies: "",
  disliked_food: "",
});

// ----------------------------------------------------
// 2. 옵션 데이터
// ----------------------------------------------------
const activityOptions = [
  { value: "low", label: "LOW (앉아있는 시간 많음)", icon: "💤" },
  { value: "moderate", label: "NORMAL (규칙적인 활동)", icon: "🚶" },
  { value: "high", label: "HIGH (육체 노동/운동 선수)", icon: "🔥" },
];

const goalOptions = [
  { value: "diet", label: "WEIGHT LOSS (다이어트)", icon: "📉" },
  { value: "muscle", label: "MUSCLE UP (근력 증가)", icon: "💪" },
  { value: "maintain", label: "MAINTENANCE (유지)", icon: "⚖️" },
];

// ----------------------------------------------------
// 3. 네비게이션 & 액션
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
  if (type === "blip") {
    osc.type = "square";
    osc.frequency.setValueAtTime(600, now);
    gain.gain.setValueAtTime(0.05, now);
    gain.gain.exponentialRampToValueAtTime(0.01, now + 0.05);
    osc.start(now);
    osc.stop(now + 0.05);
  } else if (type === "next") {
    osc.type = "triangle";
    osc.frequency.setValueAtTime(440, now);
    gain.gain.setValueAtTime(0.05, now);
    gain.gain.linearRampToValueAtTime(0, now + 0.1);
    osc.start(now);
    osc.stop(now + 0.1);
  } else if (type === "finish") {
    osc.type = "sawtooth";
    osc.frequency.setValueAtTime(220, now);
    gain.gain.setValueAtTime(0.1, now);
    gain.gain.linearRampToValueAtTime(0, now + 0.5);
    osc.start(now);
    osc.stop(now + 0.5);
  }
};

const nextStep = () => {
  if (
    currentStep.value === 1 &&
    (!signupData.value.email || !signupData.value.password)
  )
    return alert("필수 정보를 입력하세요.");
  if (
    currentStep.value === 1 &&
    signupData.value.password !== signupData.value.confirmPassword
  )
    return alert("비밀번호가 일치하지 않습니다.");

  playSound("next");
  if (currentStep.value < totalSteps) currentStep.value++;
};

const prevStep = () => {
  playSound("blip");
  if (currentStep.value > 1) currentStep.value--;
};

const handleSignup = async () => {
  playSound("finish");
  isLoading.value = true;

  // 실제 API 연동 시 사용할 데이터
  const payload = {
    ...signupData.value,
    health_condition: signupData.value.health_condition || "없음",
    allergies: signupData.value.allergies || "없음",
    disliked_food: signupData.value.disliked_food || "없음",
  };

  console.log("Creating Character...", payload);

  setTimeout(() => {
    isLoading.value = false;
    alert("캐릭터 생성 완료! 로그인해주세요.");
    router.push("/login");
  }, 1500);
};
</script>

<template>
  <div class="signup-view retro-theme">
    <div class="scanlines"></div>

    <div class="signup-container">
      <div class="header-section">
        <h1 class="page-title">CHARACTER CREATION</h1>
        <div class="progress-bar-container">
          <div class="progress-label">
            STAGE {{ currentStep }} / {{ totalSteps }}
          </div>
          <div class="progress-track">
            <div
              class="progress-fill"
              :style="{ width: `${(currentStep / totalSteps) * 100}%` }"
            ></div>
          </div>
        </div>
      </div>

      <div class="form-card">
        <div v-if="currentStep === 1" class="step-content fade-in">
          <h2 class="step-title">ID REGISTRATION</h2>
          <div class="input-group">
            <label>EMAIL (LOGIN ID)</label>
            <input
              type="email"
              v-model="signupData.email"
              class="retro-input"
              placeholder="example@game.com"
            />
          </div>
          <div class="input-group">
            <label>PASSWORD</label>
            <input
              type="password"
              v-model="signupData.password"
              class="retro-input"
              placeholder="******"
            />
          </div>
          <div class="input-group">
            <label>CONFIRM PASSWORD</label>
            <input
              type="password"
              v-model="signupData.confirmPassword"
              class="retro-input"
              placeholder="******"
            />
          </div>
          <div class="input-group">
            <label>CHARACTER NAME</label>
            <input
              type="text"
              v-model="signupData.name"
              class="retro-input"
              placeholder="Nickname"
            />
          </div>
        </div>

        <div v-if="currentStep === 2" class="step-content fade-in">
          <h2 class="step-title">BODY STATS</h2>
          <div class="input-group">
            <label>GENDER</label>
            <div class="radio-box">
              <label
                class="radio-btn"
                :class="{ active: signupData.gender === 'M' }"
              >
                <input
                  type="radio"
                  v-model="signupData.gender"
                  value="M"
                  hidden
                />
                MALE
              </label>
              <label
                class="radio-btn"
                :class="{ active: signupData.gender === 'F' }"
              >
                <input
                  type="radio"
                  v-model="signupData.gender"
                  value="F"
                  hidden
                />
                FEMALE
              </label>
            </div>
          </div>
          <div class="row">
            <div class="input-group half">
              <label>AGE (Lv)</label>
              <input
                type="number"
                v-model="signupData.age"
                class="retro-input"
                placeholder="25"
              />
            </div>
            <div class="input-group half">
              <label>HEIGHT (cm)</label>
              <input
                type="number"
                v-model="signupData.height"
                class="retro-input"
                placeholder="175"
              />
            </div>
          </div>
          <div class="row">
            <div class="input-group half">
              <label>CURRENT WEIGHT</label>
              <input
                type="number"
                v-model="signupData.weight"
                class="retro-input"
                placeholder="70 kg"
              />
            </div>
            <div class="input-group half">
              <label class="highlight">TARGET WEIGHT</label>
              <input
                type="number"
                v-model="signupData.target_weight"
                class="retro-input highlight-input"
                placeholder="65 kg"
              />
            </div>
          </div>
        </div>

        <div v-if="currentStep === 3" class="step-content fade-in">
          <h2 class="step-title">PLAY STYLE</h2>
          <div class="input-group">
            <label>ACTIVITY LEVEL</label>
            <div class="select-grid">
              <div
                v-for="opt in activityOptions"
                :key="opt.value"
                class="select-card"
                :class="{ active: signupData.activity === opt.value }"
                @click="
                  signupData.activity = opt.value;
                  playSound('blip');
                "
              >
                <span class="icon">{{ opt.icon }}</span>
                <span class="text">{{ opt.label }}</span>
              </div>
            </div>
          </div>
          <div class="input-group">
            <label>MAIN QUEST (GOAL)</label>
            <div class="select-grid">
              <div
                v-for="opt in goalOptions"
                :key="opt.value"
                class="select-card"
                :class="{ active: signupData.goal === opt.value }"
                @click="
                  signupData.goal = opt.value;
                  playSound('blip');
                "
              >
                <span class="icon">{{ opt.icon }}</span>
                <span class="text">{{ opt.label }}</span>
              </div>
            </div>
          </div>
        </div>

        <div v-if="currentStep === 4" class="step-content fade-in">
          <h2 class="step-title">TRAITS & DEBUFFS</h2>
          <p class="desc">입력하지 않으면 '없음'으로 처리됩니다.</p>

          <div class="input-group">
            <label>HEALTH CONDITION (기저질환)</label>
            <textarea
              v-model="signupData.health_condition"
              class="retro-textarea"
              placeholder="Ex: 당뇨, 고혈압..."
            ></textarea>
          </div>
          <div class="input-group">
            <label>WEAKNESS (알레르기)</label>
            <textarea
              v-model="signupData.allergies"
              class="retro-textarea"
              placeholder="Ex: 견과류, 갑각류..."
            ></textarea>
          </div>
          <div class="input-group">
            <label>BANNED ITEMS (기피 음식)</label>
            <textarea
              v-model="signupData.disliked_food"
              class="retro-textarea"
              placeholder="Ex: 오이, 당근..."
            ></textarea>
          </div>
        </div>
      </div>

      <div class="footer-actions">
        <button v-if="currentStep > 1" @click="prevStep" class="nav-btn prev">
          ◀ PREV
        </button>
        <div class="spacer"></div>
        <button
          v-if="currentStep < totalSteps"
          @click="nextStep"
          class="nav-btn next"
        >
          NEXT STAGE ▶
        </button>
        <button
          v-else
          @click="handleSignup"
          class="nav-btn finish"
          :disabled="isLoading"
        >
          {{ isLoading ? "CREATING..." : "COMPLETE START ▶" }}
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url("https://cdn.jsdelivr.net/gh/neodgm/neodgm-webfont@latest/neodgm/style.css");

:root {
  --neon-blue: #00e5ff;
  --neon-green: #00ff00;
  --neon-pink: #ff0055;
  --neon-yellow: #ffd700;
  --bg-color: #101018; /* 매우 어두운 배경 */
  --card-bg: #1a1a24; /* 카드 배경 */
  --input-bg: #000000; /* 입력창 배경 (완전 검정) */
  --text-main: #ffffff; /* 메인 텍스트 (흰색) */
  --text-sub: #aaaaaa; /* 보조 텍스트 (회색) */
}

/* 전체 뷰 */
.signup-view {
  min-height: 100vh;
  background-color: var(--bg-color) !important; /* 강제 적용 */
  font-family: "NeoDunggeunmo", monospace;
  color: var(--text-main);
  padding: 20px;
  box-sizing: border-box;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow-y: auto; /* 내용이 길어지면 스크롤 */
}

/* 스캔라인 (가독성을 위해 매우 옅게) */
.scanlines {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  background: linear-gradient(rgba(18, 16, 16, 0) 50%, rgba(0, 0, 0, 0.05) 50%),
    linear-gradient(
      90deg,
      rgba(255, 0, 0, 0.03),
      rgba(0, 255, 0, 0.01),
      rgba(0, 0, 255, 0.03)
    );
  background-size: 100% 4px, 6px 100%;
  z-index: 1;
}

.signup-container {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 500px;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  padding-bottom: 2rem;
}

/* Header */
.header-section {
  text-align: center;
  margin-bottom: 10px;
}
.page-title {
  font-size: 1.8rem;
  color: var(--neon-yellow);
  text-shadow: 2px 2px 0 var(--neon-pink);
  margin-bottom: 1rem;
  letter-spacing: 1px;
}

.progress-bar-container {
  width: 100%;
}
.progress-label {
  text-align: right;
  font-size: 0.8rem;
  color: var(--neon-blue);
  margin-bottom: 5px;
}
.progress-track {
  width: 100%;
  height: 12px;
  background: #333;
  border: 2px solid #fff;
  padding: 2px;
  box-sizing: border-box;
}
.progress-fill {
  height: 100%;
  background: var(--neon-green);
  transition: width 0.3s ease;
  box-shadow: 0 0 10px var(--neon-green);
}

/* Form Card (배경을 어둡게 하고 테두리 강조) */
.form-card {
  background-color: var(--card-bg);
  border: 2px solid #555;
  padding: 1.5rem;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.7);
  min-height: 400px;
  display: flex;
  flex-direction: column;
  border-radius: 4px;
}

.step-title {
  color: var(--neon-blue);
  border-bottom: 2px dashed #555;
  padding-bottom: 10px;
  margin-bottom: 1.5rem;
  font-size: 1.3rem;
  text-shadow: 0 0 5px var(--neon-blue);
}
.desc {
  font-size: 0.85rem;
  color: var(--text-sub);
  margin-bottom: 1rem;
  margin-top: -10px;
}

/* Input Styles (가독성 핵심) */
.input-group {
  margin-bottom: 1.2rem;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.input-group label {
  font-size: 0.85rem;
  color: var(--text-sub);
  font-weight: bold;
}
.input-group label.highlight {
  color: var(--neon-pink);
}

.retro-input,
.retro-textarea {
  background-color: var(--input-bg) !important; /* 배경 검정 강제 */
  border: 2px solid #555;
  color: #ffffff !important; /* 글씨 흰색 강제 */
  padding: 12px;
  font-family: inherit;
  font-size: 1rem;
  outline: none;
  width: 100%;
  box-sizing: border-box;
  border-radius: 4px;
  transition: all 0.2s;
}
.retro-input:focus,
.retro-textarea:focus {
  border-color: var(--neon-blue);
  box-shadow: 0 0 8px rgba(0, 229, 255, 0.3);
}
.retro-input::placeholder {
  color: #555;
} /* 플레이스홀더 잘 보이게 */
.highlight-input:focus {
  border-color: var(--neon-pink);
}

.row {
  display: flex;
  gap: 10px;
}
.half {
  flex: 1;
}

.retro-textarea {
  height: 80px;
  resize: none;
}

/* Radio Box (Gender) */
.radio-box {
  display: flex;
  gap: 10px;
}
.radio-btn {
  flex: 1;
  background: #222;
  border: 2px solid #555;
  color: #888;
  padding: 12px;
  text-align: center;
  cursor: pointer;
  font-weight: bold;
  transition: all 0.2s;
}
.radio-btn.active {
  background: rgba(0, 229, 255, 0.2);
  border-color: var(--neon-blue);
  color: #fff;
  box-shadow: 0 0 10px rgba(0, 229, 255, 0.2);
}

/* Select Grid (Activity, Goal) */
.select-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.select-card {
  background: #222;
  border: 1px solid #555;
  padding: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.2s;
}
.select-card:hover {
  background: #2a2a2a;
}
.select-card.active {
  border-color: var(--neon-green);
  background: #000;
  color: #fff;
  transform: translateX(5px);
  box-shadow: inset 0 0 10px rgba(0, 255, 0, 0.2);
}
.select-card .icon {
  font-size: 1.4rem;
}
.select-card .text {
  font-size: 0.95rem;
  font-weight: bold;
}

/* Footer Actions */
.footer-actions {
  display: flex;
  margin-top: auto;
  padding-top: 1.5rem;
  gap: 10px;
}
.nav-btn {
  padding: 14px 20px;
  font-family: inherit;
  font-weight: bold;
  font-size: 1rem;
  cursor: pointer;
  border: 2px solid #fff;
  transition: all 0.1s;
  flex: 1;
}
.nav-btn:active {
  transform: translate(2px, 2px);
  box-shadow: none;
}

.prev {
  background: #333;
  color: #fff;
  border-color: #888;
  flex: 0.4;
}
.next {
  background: var(--neon-blue);
  color: #000;
  border-color: #fff;
  box-shadow: 4px 4px 0 #000;
}
.finish {
  background: var(--neon-pink);
  color: #fff;
  border-color: #fff;
  box-shadow: 4px 4px 0 #000;
}
.spacer {
  display: none;
} /* flex 1로 버튼 크기 조절 */

/* Animations */
.fade-in {
  animation: fadeIn 0.3s ease-out;
}
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Mobile */
@media (max-width: 400px) {
  .page-title {
    font-size: 1.5rem;
  }
  .row {
    flex-direction: column;
    gap: 0;
  }
  .form-card {
    padding: 1rem;
    min-height: 350px;
  }
  .footer-actions {
    flex-direction: row;
  }
}
</style>
