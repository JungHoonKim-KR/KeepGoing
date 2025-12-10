<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { signupApi } from "../api/auth/auth";

const router = useRouter();

// ----------------------------------------------------
// 1. 상태 관리
// ----------------------------------------------------
const currentStep = ref(1);
const totalSteps = 4;
const isLoading = ref(false);

// 에러 상태 관리 (어떤 필드가 에러인지 체크)
const errors = ref({});

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
  { value: "LOW", label: "LOW (앉아있는 시간 많음)", icon: "💤" },
  { value: "MODERATE", label: "NORMAL (규칙적인 활동)", icon: "🚶" },
  { value: "HIGH", label: "HIGH (육체 노동/운동 선수)", icon: "🔥" },
];

const goalOptions = [
  { value: "DIET", label: "WEIGHT LOSS (다이어트)", icon: "📉" },
  { value: "MUSCLE", label: "MUSCLE UP (근력 증가)", icon: "💪" },
  { value: "MAINTAIN", label: "MAINTENANCE (유지)", icon: "⚖️" },
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
  } else if (type === "error") {
    osc.type = "sawtooth";
    osc.frequency.setValueAtTime(150, now);
    gain.gain.setValueAtTime(0.1, now);
    gain.gain.linearRampToValueAtTime(0, now + 0.3);
    osc.start(now);
    osc.stop(now + 0.3);
  }
};

// [신규] 유효성 검사 함수
const validateStep = () => {
  errors.value = {}; // 에러 초기화
  let isValid = true;

  if (currentStep.value === 1) {
    if (!signupData.value.email) {
      errors.value.email = "이메일을 입력하세요.";
      isValid = false;
    }
    if (!signupData.value.password) {
      errors.value.password = "비밀번호를 입력하세요.";
      isValid = false;
    }
    if (signupData.value.password && !signupData.value.confirmPassword) {
      errors.value.confirmPassword = "비밀번호 확인을 입력하세요.";
      isValid = false;
    }
    if (signupData.value.password !== signupData.value.confirmPassword) {
      errors.value.confirmPassword = "비밀번호가 일치하지 않습니다.";
      isValid = false;
    }
    if (!signupData.value.name) {
      errors.value.name = "닉네임을 입력하세요.";
      isValid = false;
    }
  }

  if (currentStep.value === 2) {
    if (!signupData.value.age) {
      errors.value.age = "나이를 입력하세요.";
      isValid = false;
    }
    if (!signupData.value.height) {
      errors.value.height = "키를 입력하세요.";
      isValid = false;
    }
    if (!signupData.value.weight) {
      errors.value.weight = "몸무게를 입력하세요.";
      isValid = false;
    }
    if (!signupData.value.target_weight) {
      errors.value.target_weight = "목표 체중을 입력하세요.";
      isValid = false;
    }
  }

  return isValid;
};

// [신규] 입력 시 에러 해제
const clearError = (field) => {
  if (errors.value[field]) {
    delete errors.value[field];
  }
};

const nextStep = () => {
  // 유효성 검사 실행
  if (!validateStep()) {
    playSound("error");
    // alert 창 제거 (화면 UI로 대체)
    return;
  }

  playSound("next");
  if (currentStep.value < totalSteps) currentStep.value++;
};

const prevStep = () => {
  playSound("blip");
  if (currentStep.value > 1) currentStep.value--;
};

const handleSignup = async () => {
  // 마지막 단계 유효성 검사 (필요 시)

  playSound("finish");
  isLoading.value = true;

  const payload = {
    email: signupData.value.email,
    password: signupData.value.password,
    name: signupData.value.name,
    gender: signupData.value.gender,
    age: signupData.value.age,
    height: signupData.value.height,
    weight: signupData.value.weight,
    targetWeight: signupData.value.target_weight,
    activity: signupData.value.activity,
    goal: signupData.value.goal,
    healthCondition: signupData.value.health_condition || "없음",
    allergies: signupData.value.allergies || "없음",
    dislikedFood: signupData.value.disliked_food || "없음",
  };

  try {
    await signupApi(payload);
    alert("캐릭터 생성 완료! 로그인해주세요.");
    router.push("/login");
  } catch (error) {
    console.error("회원가입 실패:", error);
    playSound("error");
    const msg =
      error.response?.data?.message || "회원가입 중 오류가 발생했습니다.";
    alert("SYSTEM ERROR: " + msg);
  } finally {
    isLoading.value = false;
  }
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
            <label :class="{ 'error-label': errors.email }">
              EMAIL (LOGIN ID)
              <span v-if="errors.email" class="error-badge">!</span>
            </label>
            <input
              type="email"
              v-model="signupData.email"
              class="retro-input"
              :class="{ 'input-error': errors.email }"
              placeholder="example@game.com"
              @input="clearError('email')"
            />
            <span v-if="errors.email" class="error-text"
              >>> ERROR: {{ errors.email }}</span
            >
          </div>

          <div class="input-group">
            <label :class="{ 'error-label': errors.password }">PASSWORD</label>
            <input
              type="password"
              v-model="signupData.password"
              class="retro-input"
              :class="{ 'input-error': errors.password }"
              placeholder="******"
              @input="clearError('password')"
            />
            <span v-if="errors.password" class="error-text"
              >>> ERROR: {{ errors.password }}</span
            >
          </div>

          <div class="input-group">
            <label :class="{ 'error-label': errors.confirmPassword }"
              >CONFIRM PASSWORD</label
            >
            <input
              type="password"
              v-model="signupData.confirmPassword"
              class="retro-input"
              :class="{ 'input-error': errors.confirmPassword }"
              placeholder="******"
              @input="clearError('confirmPassword')"
            />
            <span v-if="errors.confirmPassword" class="error-text"
              >>> ERROR: {{ errors.confirmPassword }}</span
            >
          </div>

          <div class="input-group">
            <label :class="{ 'error-label': errors.name }"
              >CHARACTER NAME</label
            >
            <input
              type="text"
              v-model="signupData.name"
              class="retro-input"
              :class="{ 'input-error': errors.name }"
              placeholder="Nickname"
              @input="clearError('name')"
            />
            <span v-if="errors.name" class="error-text"
              >>> ERROR: {{ errors.name }}</span
            >
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
              <label :class="{ 'error-label': errors.age }">AGE (Lv)</label>
              <input
                type="number"
                v-model="signupData.age"
                class="retro-input"
                :class="{ 'input-error': errors.age }"
                placeholder="25"
                @input="clearError('age')"
              />
            </div>
            <div class="input-group half">
              <label :class="{ 'error-label': errors.height }"
                >HEIGHT (cm)</label
              >
              <input
                type="number"
                v-model="signupData.height"
                class="retro-input"
                :class="{ 'input-error': errors.height }"
                placeholder="175"
                @input="clearError('height')"
              />
            </div>
          </div>
          <div v-if="errors.age || errors.height" class="error-text-row">
            >> ERROR: 필수 정보를 입력하세요.
          </div>

          <div class="row">
            <div class="input-group half">
              <label :class="{ 'error-label': errors.weight }"
                >CURRENT WEIGHT</label
              >
              <input
                type="number"
                v-model="signupData.weight"
                class="retro-input"
                :class="{ 'input-error': errors.weight }"
                placeholder="70 kg"
                @input="clearError('weight')"
              />
            </div>
            <div class="input-group half">
              <label
                :class="{
                  'error-label': errors.target_weight,
                  highlight: !errors.target_weight,
                }"
                >TARGET WEIGHT</label
              >
              <input
                type="number"
                v-model="signupData.target_weight"
                class="retro-input highlight-input"
                :class="{ 'input-error': errors.target_weight }"
                placeholder="65 kg"
                @input="clearError('target_weight')"
              />
            </div>
          </div>
          <div
            v-if="errors.weight || errors.target_weight"
            class="error-text-row"
          >
            >> ERROR: 체중 정보를 입력하세요.
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
  --bg-color: #101018;
  --card-bg: #1a1a24;
  --input-bg: #000000;
  --text-main: #ffffff;
  --text-sub: #aaaaaa;
}

/* 기존 스타일 유지하면서... */

/* ▼▼▼ [신규] 에러 스타일 추가 ▼▼▼ */
.input-error {
  border-color: var(--neon-pink) !important;
  box-shadow: 0 0 10px var(--neon-pink);
  animation: shake 0.4s cubic-bezier(0.36, 0.07, 0.19, 0.97) both;
}

.error-text {
  color: var(--neon-pink);
  font-size: 0.8rem;
  margin-top: 4px;
  display: block;
  animation: blink 1s infinite;
}

.error-text-row {
  color: var(--neon-pink);
  font-size: 0.8rem;
  margin-bottom: 1rem;
  text-align: right;
}

.error-label {
  color: var(--neon-pink) !important;
}

.error-badge {
  display: inline-block;
  background: var(--neon-pink);
  color: white;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  text-align: center;
  line-height: 16px;
  font-size: 12px;
  margin-left: 5px;
}

/* 흔들림 효과 */
@keyframes shake {
  10%,
  90% {
    transform: translate3d(-2px, 0, 0);
  }
  20%,
  80% {
    transform: translate3d(4px, 0, 0);
  }
  30%,
  50%,
  70% {
    transform: translate3d(-6px, 0, 0);
  }
  40%,
  60% {
    transform: translate3d(6px, 0, 0);
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

/* ▲▲▲ [여기까지] ▲▲▲ */

/* --- 기존 스타일 --- */
.signup-view {
  min-height: 100vh;
  background-color: var(--bg-color) !important;
  font-family: "NeoDunggeunmo", monospace;
  color: var(--text-main);
  padding: 20px;
  box-sizing: border-box;
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  overflow-y: auto;
}
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
  background-color: var(--input-bg) !important;
  border: 2px solid #555;
  color: black !important;
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
}
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
}
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
