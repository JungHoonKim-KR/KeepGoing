<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
// [수정] applyDietPlanApi 추가
import { analyzeDiet, generateDietPlanApi, scanBodyApi, scanFoodImageApi, applyDietPlanApi } from "../api/diet/dietApi";
import Footer from "../components/utils/Footer.vue";
import { useConfigStore } from "@/stores/configStore";
import { useAuthStore } from "@/stores/authStore";

const router = useRouter();
const config = useConfigStore();
const authStore = useAuthStore();

// ------------------------------------------------------------------
// 1. 상태 관리 변수들
// ------------------------------------------------------------------
const isLoading = ref(false);
const isAnalyzing = ref(false);
const analysisData = ref(null);
const bootLogs = ref([]);

const showDietPlanModal = ref(false);
const showFoodScanModal = ref(false);
const showBodyScanModal = ref(false);

const dietPlanStep = ref("survey");
const surveyStep = ref(0);
const surveyAnswers = ref({});
const generatedPlan = ref([]);
const selectedDuration = ref(3);

const scanStep = ref("upload");
const scannedImage = ref(null);
const currentFile = ref(null);
const scanResult = ref(null);

const isScanningBody = ref(false);
const bodyScanResult = ref(null);

const MEMBER_ID = authStore.memberId;
const TODAY_DATE = new Date().toISOString().split("T")[0];

// ------------------------------------------------------------------
// 2. 설문 데이터
// ------------------------------------------------------------------
const surveyQuestions = [
  {
    id: "duration",
    question: "얼마나 오래 식단을 따를 예정인가요?",
    emoji: "📅",
    options: [
      { value: 3, label: "3일", desc: "짧고굵게!" },
      { value: 7, label: "1주일", desc: "딱 일주일만" },
      { value: 14, label: "2주일", desc: "진지하게 도전" },
    ],
  },
  {
    id: "exercise",
    question: "평소 운동량은 어느 정도인가요?",
    emoji: "💪",
    options: [
      { value: "none", label: "안 함", desc: "숨쉬기 운동만.." },
      { value: "light", label: "가볍게", desc: "산책/홈트" },
      { value: "hard", label: "열심히", desc: "헬스/크로스핏" },
    ],
  },
  {
    id: "sleep",
    question: "하루 수면 시간은?",
    emoji: "😴",
    options: [
      { value: "short", label: "6시간 미만", desc: "피곤해요" },
      { value: "normal", label: "6-8시간", desc: "적당해요" },
      { value: "long", label: "8시간+", desc: "개운해요" },
    ],
  },
  {
    id: "water",
    question: "물은 하루에 얼마나 마시나요?",
    emoji: "💧",
    options: [
      { value: "low", label: "1L 미만", desc: "잘 안 마심" },
      { value: "medium", label: "1-2L", desc: "노력 중" },
      { value: "high", label: "2L+", desc: "하마 수준" },
    ],
  },
  {
    id: "meals",
    question: "하루 식사 횟수는?",
    emoji: "🍽️",
    options: [
      { value: 2, label: "2끼", desc: "아침 패스" },
      { value: 3, label: "3끼", desc: "규칙적으로" },
      { value: 5, label: "5끼", desc: "조금씩 자주" },
    ],
  },
  {
    id: "favorite",
    question: "식단에 포함하고 싶은 특식은?",
    emoji: "❤️",
    options: [
      { value: "none", label: "없음", desc: "클린하게" },
      { value: "chicken", label: "치킨", desc: "단백질..이죠?" },
      { value: "pizza", label: "피자", desc: "치즈 못 참아" },
    ],
  },
];

const getRankColor = (score) => {
  if (score >= 90) return "#ffd700";
  if (score >= 80) return "#00e5ff";
  if (score >= 70) return "#00ff00";
  if (score >= 50) return "#ffaa00";
  return "#ff0055";
};

// ------------------------------------------------------------------
// API 로직들
// ------------------------------------------------------------------
const fetchAnalysis = async () => {
  if (isAnalyzing.value) return;
  isAnalyzing.value = true;
  isLoading.value = true;
  bootLogs.value = [];
  runBootSequence();

  try {
    const data = await analyzeDiet(MEMBER_ID, TODAY_DATE);
    analysisData.value = {
      overallScore: data.score,
      rank: data.rank,
      title: data.dailyTitle,
      insights: data.insights || [],
      recommendation: data.oneLineSummary,
      questItems: data.recommendations || [],
    };
  } catch (error) {
    console.error("분석 실패:", error);
    alert("오늘 기록된 식단이 없거나 서버 연결에 실패했습니다.");
  } finally {
    setTimeout(() => {
      isLoading.value = false;
      isAnalyzing.value = false;
    }, 1500);
  }
};

const openDietPlanModal = () => {
  showDietPlanModal.value = true;
  dietPlanStep.value = "survey";
  surveyStep.value = 0;
  surveyAnswers.value = {};
};

const selectAnswer = (questionId, value) => {
  surveyAnswers.value[questionId] = value;
  if (questionId === "duration") selectedDuration.value = value;
  if (surveyStep.value < surveyQuestions.length - 1) {
    setTimeout(() => surveyStep.value++, 250);
  } else {
    setTimeout(() => generateDietPlan(), 250);
  }
};

const generateDietPlan = async () => {
  dietPlanStep.value = "loading";
  try {
    const payload = {
      survey: {
        duration: selectedDuration.value,
        sleep: surveyAnswers.value.sleep || "normal",
        water: surveyAnswers.value.water || "medium",
        meals: surveyAnswers.value.meals || 3,
        favorite: surveyAnswers.value.favorite || "none",
      },
      userProfile: {
        activity: surveyAnswers.value.exercise || "normal",
        height: 170,
        weight: 65,
        age: 25,
        gender: "M",
      },
    };
    const result = await generateDietPlanApi(payload);
    generatedPlan.value = result;
    console.log(generatedPlan.value);
    dietPlanStep.value = "result";
  } catch (error) {
    alert("식단을 생성하지 못했습니다. 잠시 후 다시 시도해주세요.");
    showDietPlanModal.value = false;
  }
};

const openFoodScanModal = () => {
  showFoodScanModal.value = true;
  scanStep.value = "upload";
  scannedImage.value = null;
  currentFile.value = null;
  scanResult.value = null;
};

const handleImageUpload = (event) => {
  const file = event.target.files[0];
  if (!file) return;
  currentFile.value = file;
  const reader = new FileReader();
  reader.onload = (e) => {
    scannedImage.value = e.target.result;
    analyzeFoodImage();
  };
  reader.readAsDataURL(file);
};

const analyzeFoodImage = async () => {
  if (!currentFile.value) return;
  scanStep.value = "analyzing";
  try {
    const result = await scanFoodImageApi(currentFile.value);
    // 분석 효과를 위해 2초 딜레이
    setTimeout(() => {
      scanResult.value = result;
      scanStep.value = "result";
    }, 2000);
  } catch (error) {
    alert("이미지를 분석할 수 없습니다.");
    scanStep.value = "upload";
  }
};

const analyzeBodyStats = async () => {
  isScanningBody.value = true;
  try {
    const result = await scanBodyApi(MEMBER_ID);
    bodyScanResult.value = result;
    showBodyScanModal.value = true;
  } catch (error) {
    alert("신체 정보를 불러올 수 없습니다.");
  } finally {
    isScanningBody.value = false;
  }
};

const runBootSequence = () => {
  const logs = ["INITIALIZING SYSTEM...", "CONNECTING NEURAL NET...", "LOADING BIOMETRICS...", "ACCESS GRANTED."];
  let logIndex = 0;
  const interval = setInterval(() => {
    if (logIndex < logs.length) {
      bootLogs.value.push(logs[logIndex++]);
    } else {
      clearInterval(interval);
    }
  }, 300);
};

// [추가] 시스템 적용 (DB 저장) 함수
const confirmDietPlan = async () => {
  // 1. 데이터 검증
  if (!generatedPlan.value || generatedPlan.value.length === 0) {
    alert("SYSTEM ERROR: 저장할 식단 데이터가 없습니다.");
    return;
  }

  // 2. 사용자 확인
  const isConfirmed = confirm(
    `[SYSTEM NOTICE]\n생성된 ${selectedDuration.value}일치 식단을 스케쥴 데이터베이스에 동기화하시겠습니까?`
  );

  if (!isConfirmed) return;

  // 3. 로딩 상태 전환
  dietPlanStep.value = "loading";

  try {
    // 4. API 호출
    await applyDietPlanApi(MEMBER_ID, generatedPlan.value);

    // 5. 성공 시
    alert("SYNC COMPLETE: 식단이 스케쥴에 정상적으로 등록되었습니다.");
    showDietPlanModal.value = false; // 모달 닫기
  } catch (error) {
    // 6. 실패 시
    console.error(error);
    alert("SYNC FAILED: 서버 통신 중 오류가 발생했습니다.");
    dietPlanStep.value = "result"; // 다시 결과 화면으로 복귀
  }
};
</script>

<template>
  <div class="ai-view">
    <div class="crt-overlay"></div>

    <div class="content-wrapper">
      <div class="retro-header">
        <div class="system-status"><span class="status-light blink"></span> SYSTEM ONLINE</div>
        <h1 class="page-title glitch" data-text="AI HEALTH LAB">AI HEALTH LAB</h1>
      </div>

      <div v-if="isLoading" class="loading-terminal">
        <div class="terminal-screen">
          <div v-for="(log, index) in bootLogs" :key="index" class="log-line">> {{ log }}</div>
          <div class="cursor-line">> <span class="blink-cursor">_</span></div>
        </div>
        <div class="loading-bar-container"><div class="loading-bar"></div></div>
      </div>

      <div v-else class="dashboard-container">
        <div class="ai-avatar-container" @click="fetchAnalysis">
          <div class="cyber-eye-wrapper" :class="{ analyzing: isAnalyzing }">
            <div class="ring outer"></div>
            <div class="ring inner"></div>
            <div class="core-eye">
              <div class="pupil"></div>
              <div class="glare"></div>
            </div>
          </div>
          <div class="ai-message">
            <p v-if="!analysisData" class="typing-text">"시스템 준비 완료. 터치하여 분석 시작."</p>
            <p v-else class="result-text">
              "분석 완료. 랭크 [
              <span class="rank-highlight">{{ analysisData.rank }}</span> ]"
            </p>
          </div>
        </div>

        <div class="action-grid">
          <button class="hero-btn diet" @click="openDietPlanModal">
            <div class="btn-bg"></div>
            <div class="btn-icon">🥗</div>
            <div class="btn-text">
              <div class="btn-title">AI 식단 생성</div>
              <div class="btn-desc">맞춤형 식단표</div>
            </div>
            <div class="btn-arrow">→</div>
          </button>

          <button class="hero-btn scan" @click="openFoodScanModal">
            <div class="btn-bg"></div>
            <div class="btn-icon">📸</div>
            <div class="btn-text">
              <div class="btn-title">음식 스캔</div>
              <div class="btn-desc">칼로리 측정기</div>
            </div>
            <div class="btn-arrow">→</div>
          </button>

          <button class="hero-btn body" @click="analyzeBodyStats" :disabled="isScanningBody">
            <div class="btn-bg"></div>
            <div class="btn-icon">🧬</div>
            <div class="btn-text">
              <div class="btn-title">RPG 바디 스캔</div>
              <div class="btn-desc">
                {{ isScanningBody ? "데이터 처리 중..." : "내 캐릭터 확인" }}
              </div>
            </div>
            <div class="btn-arrow">→</div>
          </button>
        </div>

        <div v-if="analysisData" class="result-section">
          <div class="power-card fade-in-up">
            <div class="rank-badge" :style="{ color: getRankColor(analysisData.overallScore) }">
              RANK {{ analysisData.rank }}
            </div>
            <div class="score-val" :style="{ color: getRankColor(analysisData.overallScore) }">
              {{ analysisData.overallScore }} <span class="max">/ 100</span>
            </div>
            <div class="retro-progress">
              <div
                class="fill"
                :style="{
                  width: `${analysisData.overallScore}%`,
                  background: getRankColor(analysisData.overallScore),
                }"
              ></div>
            </div>
            <div class="ai-summary-text">"{{ analysisData.recommendation }}"</div>
          </div>
        </div>
      </div>
    </div>

    <Footer />

    <div v-if="showDietPlanModal" class="modal-overlay" @click.self="showDietPlanModal = false">
      <div class="modal-win survey-modal pop-in">
        <div class="modal-header">
          <span>{{
            dietPlanStep === "survey" ? `DATA INPUT ${surveyStep + 1}/${surveyQuestions.length}` : "PROCESSING..."
          }}</span>
          <button class="close-btn" @click="showDietPlanModal = false">✕</button>
        </div>

        <div v-if="dietPlanStep === 'survey'" class="modal-body">
          <div class="question fade-in">
            <div class="q-emoji floating">
              {{ surveyQuestions[surveyStep].emoji }}
            </div>
            <h3>{{ surveyQuestions[surveyStep].question }}</h3>
          </div>
          <div class="options">
            <button
              v-for="opt in surveyQuestions[surveyStep].options"
              :key="opt.value"
              class="opt-btn"
              @click="selectAnswer(surveyQuestions[surveyStep].id, opt.value)"
            >
              <div class="opt-label">{{ opt.label }}</div>
              <div class="opt-desc">{{ opt.desc }}</div>
            </button>
          </div>
        </div>

        <div v-if="dietPlanStep === 'loading'" class="modal-body loading">
          <div class="hex-spinner"></div>
          <p class="blink-text">AI가 최적의 식단을 계산 중입니다...</p>
        </div>

        <div v-if="dietPlanStep === 'result'" class="modal-body result">
          <div class="summary">{{ selectedDuration }}일 식단 생성 완료!</div>
          <div class="plan-list">
            <div v-for="p in generatedPlan" :key="p.day" class="plan-item">
              <div class="day">DAY {{ p.day }}</div>

              <div class="menu-container" v-if="p.menu && typeof p.menu === 'object'">
                <div class="meal-row">
                  <span class="meal-label morning">아침</span>
                  <span class="meal-text">{{ p.menu.breakfast }}</span>
                </div>
                <div class="meal-row">
                  <span class="meal-label lunch">점심</span>
                  <span class="meal-text">{{ p.menu.lunch }}</span>
                </div>
                <div class="meal-row">
                  <span class="meal-label dinner">저녁</span>
                  <span class="meal-text">{{ p.menu.dinner }}</span>
                </div>
              </div>
              <div class="menu" v-else>{{ p.menu }}</div>

              <div class="quest-row">
                <span class="badge" :class="p.difficulty">{{ p.difficulty }}</span>
                <span class="quest">🎯 {{ p.quest }}</span>
              </div>
              <div class="cal-info">⚡ {{ p.cal }} kcal</div>
            </div>
          </div>
          <button class="apply-btn" @click="confirmDietPlan">시스템 적용</button>
        </div>
      </div>
    </div>

    <div v-if="showFoodScanModal" class="modal-overlay" @click.self="showFoodScanModal = false">
      <div class="modal-win scan-modal pop-in">
        <div class="modal-header">
          <span>{{ scanStep === "result" ? "ANALYSIS COMPLETE" : "VISUAL SCANNER" }}</span>
          <button class="close-btn" @click="showFoodScanModal = false">✕</button>
        </div>

        <div v-if="scanStep === 'upload'" class="modal-body upload-section">
          <input type="file" accept="image/*" id="food-img" @change="handleImageUpload" style="display: none" />
          <label for="food-img" class="viewfinder-label">
            <div class="corner top-left"></div>
            <div class="corner top-right"></div>
            <div class="corner bottom-left"></div>
            <div class="corner bottom-right"></div>

            <div class="center-content">
              <div class="upload-icon-circle">
                <span class="camera-icon">📸</span>
              </div>
              <div class="instruction-text">TOUCH TO ACTIVATE SCANNER</div>
              <div class="sub-text">음식 사진을 촬영/업로드하세요</div>
            </div>
          </label>
        </div>

        <div v-if="scanStep === 'analyzing'" class="modal-body scanner-view">
          <div class="scan-container">
            <img v-if="scannedImage" :src="scannedImage" class="scan-target" />

            <div class="scan-overlay-grid"></div>
            <div class="laser-beam"></div>
            <div class="scan-noise"></div>
            <div class="hud-info top">DETECTING OBJECT...</div>
            <div class="hud-info bottom">CALCULATING CALORIES...</div>
          </div>
        </div>

        <div v-if="scanStep === 'result'" class="modal-body result">
          <img :src="scannedImage" class="preview" />
          <div class="food-name bounce-in">{{ scanResult.emoji }} {{ scanResult.name }}</div>
          <div class="calorie-big pulse-text">🔥 {{ scanResult.calories }} kcal</div>
          <div class="exercise-grid">
            <div class="ex-card">
              <div>🏃 러닝</div>
              <div class="time">{{ scanResult.exercise.running }}분</div>
            </div>
            <div class="ex-card">
              <div>🚶 걷기</div>
              <div class="time">{{ scanResult.exercise.walking }}분</div>
            </div>
            <div class="ex-card">
              <div>🏊 수영</div>
              <div class="time">{{ scanResult.exercise.swimming }}분</div>
            </div>
          </div>
          <button
            class="apply-btn"
            @click="
              scanStep = 'upload';
              scannedImage = null;
              currentFile = null;
            "
          >
            재스캔
          </button>
        </div>
      </div>
    </div>

    <div v-if="showBodyScanModal && bodyScanResult" class="modal-overlay" @click.self="showBodyScanModal = false">
      <div class="modal-win scan-modal pop-in">
        <div class="modal-header">
          <span>CHARACTER STATUS</span>
          <button class="close-btn" @click="showBodyScanModal = false">✕</button>
        </div>
        <div class="modal-body result">
          <div class="rpg-class-title glitch" :data-text="bodyScanResult.class">
            {{ bodyScanResult.class }}
          </div>
          <div class="rpg-desc">{{ bodyScanResult.desc }}</div>

          <div class="bmi-info">BMI: {{ bodyScanResult.bmi }}</div>

          <div class="stats-container">
            <div class="stat-row" v-for="(val, key) in bodyScanResult.stats" :key="key">
              <span class="stat-label">{{ key.toUpperCase() }}</span>
              <div class="stat-bar">
                <div class="stat-fill" :class="key" :style="{ width: val + '%' }"></div>
              </div>
              <span class="stat-val">{{ val }}</span>
            </div>
          </div>

          <button class="apply-btn" @click="showBodyScanModal = false">확인</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url("https://cdn.jsdelivr.net/gh/neodgm/neodgm-webfont@latest/neodgm/style.css");

/* -------------------------------------------
   1. 기본 레이아웃 및 폰트 (가독성 강화)
------------------------------------------- */
.ai-view {
  min-height: 100vh;
  background: #050510; /* 리얼 블랙 */
  color: #fff; /* 기본 글씨색 화이트로 변경 */
  font-family: "NeoDunggeunmo", monospace;
  padding-bottom: 100px;
  position: relative;
  overflow-x: hidden;
}

/* CRT 모니터 주사선 효과 */
.crt-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(rgba(18, 16, 16, 0) 50%, rgba(0, 0, 0, 0.25) 50%);
  background-size: 100% 4px;
  z-index: 10;
  pointer-events: none;
}

.content-wrapper {
  max-width: 480px;
  margin: 0 auto;
  padding: 1rem;
  position: relative;
  z-index: 20;
}

/* 헤더 */
.retro-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  border-bottom: 2px solid #333;
  padding-bottom: 0.5rem;
}
.system-status {
  font-size: 0.7rem;
  color: #00ff00;
  display: flex;
  align-items: center;
  gap: 5px;
  text-shadow: 0 0 5px #00ff00;
}
.status-light {
  width: 8px;
  height: 8px;
  background: #00ff00;
  border-radius: 50%;
  box-shadow: 0 0 8px #00ff00;
}

/* 글리치 텍스트 */
.glitch {
  position: relative;
  color: #fff;
  font-size: 1.5rem;
  font-weight: bold;
}
.glitch::before,
.glitch::after {
  content: attr(data-text);
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}
.glitch::before {
  left: 2px;
  text-shadow: -1px 0 #ff00ff;
  clip: rect(44px, 450px, 56px, 0);
  animation: glitch-anim 5s infinite linear alternate-reverse;
}
.glitch::after {
  left: -2px;
  text-shadow: -1px 0 #00ffff;
  clip: rect(44px, 450px, 56px, 0);
  animation: glitch-anim2 5s infinite linear alternate-reverse;
}

@keyframes glitch-anim {
  0% {
    clip: rect(10px, 9999px, 30px, 0);
  }
  20% {
    clip: rect(80px, 9999px, 100px, 0);
  }
  40% {
    clip: rect(10px, 9999px, 50px, 0);
  }
  60% {
    clip: rect(30px, 9999px, 60px, 0);
  }
  80% {
    clip: rect(70px, 9999px, 90px, 0);
  }
  100% {
    clip: rect(20px, 9999px, 40px, 0);
  }
}
@keyframes glitch-anim2 {
  0% {
    clip: rect(60px, 9999px, 80px, 0);
  }
  20% {
    clip: rect(20px, 9999px, 30px, 0);
  }
  40% {
    clip: rect(90px, 9999px, 100px, 0);
  }
  60% {
    clip: rect(10px, 9999px, 20px, 0);
  }
  80% {
    clip: rect(50px, 9999px, 60px, 0);
  }
  100% {
    clip: rect(30px, 9999px, 40px, 0);
  }
}

/* -------------------------------------------
   2. AI 아바타 (Eye)
------------------------------------------- */
.ai-avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 2rem;
  cursor: pointer;
  animation: float 3s ease-in-out infinite;
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

.cyber-eye-wrapper {
  width: 120px;
  height: 120px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.ring {
  position: absolute;
  border-radius: 50%;
  box-shadow: 0 0 10px rgba(0, 229, 255, 0.5);
}
.ring.outer {
  width: 100%;
  height: 100%;
  border: 3px dashed #00e5ff;
  animation: spin-cw 10s linear infinite;
}
.ring.inner {
  width: 70%;
  height: 70%;
  border: 2px solid #ff0055;
  border-left-color: transparent;
  border-right-color: transparent;
  animation: spin-ccw 5s linear infinite;
}
.core-eye {
  width: 40px;
  height: 40px;
  background: radial-gradient(circle, #fff, #00e5ff);
  border-radius: 50%;
  box-shadow: 0 0 20px #00e5ff;
  position: relative;
  animation: breathe 3s infinite ease-in-out;
}
.pupil {
  width: 12px;
  height: 12px;
  background: #000;
  border-radius: 50%;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.cyber-eye-wrapper.analyzing .ring.outer {
  animation: spin-cw 0.5s linear infinite;
  border-color: #ff0055;
  box-shadow: 0 0 20px #ff0055;
}
.cyber-eye-wrapper.analyzing .core-eye {
  background: radial-gradient(circle, #fff, #ff0055);
  box-shadow: 0 0 30px #ff0055;
  animation: pulse 0.2s infinite;
}

@keyframes spin-cw {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
@keyframes spin-ccw {
  from {
    transform: rotate(360deg);
  }
  to {
    transform: rotate(0deg);
  }
}
@keyframes breathe {
  0%,
  100% {
    transform: scale(1);
    opacity: 0.8;
  }
  50% {
    transform: scale(1.1);
    opacity: 1;
  }
}
@keyframes pulse {
  0% {
    transform: scale(0.9);
  }
  100% {
    transform: scale(1.1);
  }
}

.ai-message {
  margin-top: 1rem;
  text-align: center;
  font-size: 0.9rem;
  min-height: 1.2rem;
  color: #fff;
}
.typing-text {
  color: #00e5ff;
  overflow: hidden;
  white-space: nowrap;
  animation: typing 3s steps(30, end);
}
@keyframes typing {
  from {
    width: 0;
  }
  to {
    width: 100%;
  }
}
.rank-highlight {
  color: #ff0055;
  font-weight: bold;
  font-size: 1.2rem;
}

/* -------------------------------------------
   3. 메인 버튼들
------------------------------------------- */
.action-grid {
  display: grid;
  gap: 1rem;
  margin-bottom: 2rem;
}
.hero-btn {
  position: relative;
  background: rgba(20, 20, 30, 0.8); /* 배경색 추가해서 글씨 보이게 */
  border: 1px solid #333;
  padding: 1.2rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.3s;
  border-radius: 5px;
  color: #fff; /* 글씨색 강제 지정 */
}
.hero-btn .btn-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 0%;
  height: 100%;
  background: rgba(255, 255, 255, 0.1);
  transition: width 0.3s;
  z-index: 1;
}
.hero-btn:hover .btn-bg {
  width: 100%;
}
.hero-btn:hover {
  border-color: #00e5ff;
  box-shadow: 0 0 15px rgba(0, 229, 255, 0.3);
  transform: scale(1.02);
}
.hero-btn .btn-icon {
  font-size: 2rem;
  z-index: 2;
}
.hero-btn .btn-text {
  flex: 1;
  text-align: left;
  z-index: 2;
}
.hero-btn .btn-title {
  font-size: 1.1rem;
  font-weight: bold;
  margin-bottom: 4px;
  color: #fff;
}
.hero-btn .btn-desc {
  font-size: 0.8rem;
  color: #aaa;
}
.hero-btn .btn-arrow {
  font-size: 1.5rem;
  color: #00e5ff;
  z-index: 2;
}

/* -------------------------------------------
   4. 분석 결과 카드 (홈 화면)
------------------------------------------- */
.result-section {
  margin-top: 2rem;
}
.power-card {
  background: #111;
  border: 2px solid #333;
  padding: 1.5rem;
  text-align: center;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.5);
}
.rank-badge {
  font-weight: bold;
  font-size: 1.2rem;
  margin-bottom: 0.5rem;
}
.score-val {
  font-size: 2.5rem;
  font-weight: bold;
  margin-bottom: 1rem;
}
.max {
  font-size: 1rem;
  color: #666;
}
.retro-progress {
  height: 12px;
  background: #222;
  border: 1px solid #555;
  padding: 1px;
  margin-bottom: 1rem;
}
.retro-progress .fill {
  height: 100%;
  transition: width 1s;
}
.ai-summary-text {
  color: #ccc;
  font-size: 0.9rem;
  font-style: italic;
}

/* -------------------------------------------
   5. 모달 공통 스타일
------------------------------------------- */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.85); /* 배경 더 어둡게 */
  z-index: 999;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(5px);
}
.modal-win {
  width: 90%;
  max-width: 450px;
  background: #050510; /* 배경 블랙 */
  border: 2px solid #00e5ff;
  box-shadow: 0 0 20px rgba(0, 229, 255, 0.2);
  display: flex;
  flex-direction: column;
  max-height: 85vh;
}
.pop-in {
  animation: popIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
@keyframes popIn {
  from {
    transform: scale(0.8);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.modal-header {
  background: #00e5ff;
  color: #000;
  padding: 12px;
  display: flex;
  justify-content: space-between;
  font-weight: bold;
  text-transform: uppercase;
  letter-spacing: 1px;
}
.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #000;
}
.modal-body {
  padding: 20px;
  overflow-y: auto;
  color: #fff;
}

/* -------------------------------------------
   6. [중요] 설문조사 & 옵션 버튼
------------------------------------------- */
.question {
  text-align: center;
  margin-bottom: 20px;
}
.question h3 {
  font-size: 1.2rem;
  color: #fff;
  margin-top: 10px;
}
.q-emoji {
  font-size: 3rem;
  animation: float 2s infinite ease-in-out;
}
.options {
  display: grid;
  gap: 10px;
}

.opt-btn {
  background: rgba(255, 255, 255, 0.05); /* 약간 밝은 배경 */
  border: 1px solid #444;
  padding: 15px;
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;
  border-radius: 5px;
  color: #fff; /* 글씨 하얗게 */
}
.opt-btn:hover {
  border-color: #00e5ff;
  background: rgba(0, 229, 255, 0.1);
  transform: translateX(5px);
}
.opt-label {
  font-weight: bold;
  font-size: 1.1rem;
  color: #fff;
}
.opt-desc {
  font-size: 0.8rem;
  color: #888;
  margin-top: 2px;
}

/* -------------------------------------------
   7. 식단 결과 리스트 (수정됨)
------------------------------------------- */
.summary {
  text-align: center;
  color: #00ff00;
  font-size: 1.2rem;
  margin-bottom: 15px;
  font-weight: bold;
}
.plan-list {
  max-height: 300px;
  overflow-y: auto;
  padding-right: 5px;
}

.plan-item {
  background: rgba(255, 255, 255, 0.05); /* 배경 추가 */
  border: 1px solid #333;
  padding: 12px;
  margin-bottom: 10px;
  border-radius: 5px;
}
.plan-item .day {
  color: #00e5ff;
  font-weight: bold;
  font-size: 0.9rem;
  margin-bottom: 5px;
}
.plan-item .menu {
  color: #fff;
  font-size: 1.1rem;
  font-weight: bold;
  margin-bottom: 8px;
}

/* [추가] 아침/점심/저녁 스타일 */
.menu-container {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 10px;
}
.meal-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
  font-size: 0.9rem;
}
.meal-label {
  font-size: 0.7rem;
  font-weight: bold;
  color: #000;
  padding: 2px 5px;
  border-radius: 3px;
  min-width: 35px;
  text-align: center;
}
.meal-label.morning {
  background: #ffd700;
}
.meal-label.lunch {
  background: #00e5ff;
}
.meal-label.dinner {
  background: #ff0055;
}
.meal-text {
  color: #fff;
  flex: 1;
}

.plan-item .quest-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 5px;
}
.plan-item .badge {
  font-size: 0.7rem;
  padding: 2px 6px;
  border-radius: 3px;
  font-weight: bold;
  color: #000;
}
.badge.EASY {
  background: #00ff00;
}
.badge.NORMAL {
  background: #ffaa00;
}
.badge.HARD {
  background: #ff0055;
}

.plan-item .quest {
  color: #ccc;
  font-size: 0.8rem;
}
.plan-item .cal-info {
  text-align: right;
  color: #ffaa00;
  font-size: 0.9rem;
  font-weight: bold;
}

/* -------------------------------------------
   8. 스캔 & 결과창
------------------------------------------- */
.scan-container {
  position: relative;
  width: 100%;
  max-width: 300px;
  height: 300px;
  margin: 0 auto;
  border: 2px solid #00e5ff;
  background: #000;
  overflow: hidden;
  border-radius: 10px;
}
.scan-target {
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0.6;
}
.scan-beam {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 4px;
  background: #00ff00;
  box-shadow: 0 0 15px #00ff00;
  animation: scan-move 2s linear infinite;
  z-index: 5;
}
.scan-grid {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, rgba(0, 255, 0, 0.1) 1px, transparent 1px),
    linear-gradient(rgba(0, 255, 0, 0.1) 1px, transparent 1px);
  background-size: 20px 20px;
  z-index: 4;
}
.scan-text {
  position: absolute;
  bottom: 10px;
  left: 10px;
  color: #00ff00;
  font-size: 0.8rem;
  background: rgba(0, 0, 0, 0.7);
  padding: 2px 5px;
  animation: blink 0.5s infinite;
}
@keyframes scan-move {
  0% {
    top: 0%;
  }
  50% {
    top: 100%;
  }
  100% {
    top: 0%;
  }
}

.upload-label {
  border: 2px dashed #00e5ff;
  padding: 40px;
  text-align: center;
  cursor: pointer;
  transition: 0.3s;
  color: #fff;
}
.upload-label:hover {
  background: rgba(0, 229, 255, 0.1);
  border-style: solid;
}
.upload-icon {
  font-size: 3rem;
  margin-bottom: 10px;
}

/* 음식 결과 */
.preview {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 5px;
  border: 1px solid #333;
}
.food-name {
  text-align: center;
  font-size: 1.5rem;
  margin: 15px 0;
  color: #fff;
  font-weight: bold;
}
.calorie-big {
  text-align: center;
  font-size: 2.5rem;
  color: #ff0055;
  font-weight: bold;
  margin-bottom: 20px;
  text-shadow: 0 0 10px #ff0055;
}
.exercise-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 20px;
}
.ex-card {
  background: #111;
  border: 1px solid #333;
  padding: 10px;
  text-align: center;
  border-radius: 5px;
}
.ex-card div:first-child {
  color: #ccc;
  font-size: 0.8rem;
  margin-bottom: 5px;
}
.ex-card .time {
  font-size: 1.2rem;
  font-weight: bold;
  color: #00ff00;
}

/* RPG 바디 스캔 결과 */
.rpg-class-title {
  font-size: 1.8rem;
  font-weight: bold;
  text-align: center;
  color: #ffd700;
  margin-bottom: 10px;
  text-shadow: 0 0 10px rgba(255, 215, 0, 0.5);
}
.rpg-desc {
  text-align: center;
  color: #ccc;
  margin-bottom: 1.5rem;
  font-size: 0.9rem;
  line-height: 1.4;
}
.bmi-info {
  text-align: center;
  color: #00e5ff;
  font-weight: bold;
  margin-bottom: 1rem;
  border: 1px solid #00e5ff;
  display: inline-block;
  padding: 5px 15px;
  border-radius: 20px;
  margin-left: auto;
  margin-right: auto;
  display: block;
  width: fit-content;
}

.stats-container {
  margin-bottom: 20px;
}
.stat-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}
.stat-label {
  width: 40px;
  font-weight: bold;
  font-size: 0.8rem;
  color: #fff;
}
.stat-bar {
  flex: 1;
  height: 10px;
  background: #222;
  border: 1px solid #444;
  border-radius: 5px;
  overflow: hidden;
}
.stat-fill {
  height: 100%;
}
.stat-fill.hp {
  background: #ff0055;
  box-shadow: 0 0 5px #ff0055;
}
.stat-fill.str {
  background: #ffaa00;
  box-shadow: 0 0 5px #ffaa00;
}
.stat-fill.agi {
  background: #00e5ff;
  box-shadow: 0 0 5px #00e5ff;
}
.stat-fill.def {
  background: #00ff00;
  box-shadow: 0 0 5px #00ff00;
}
.stat-val {
  width: 30px;
  text-align: right;
  font-weight: bold;
  color: #fff;
}

/* 9. 로딩 및 기타 유틸 */
.hex-spinner {
  width: 50px;
  height: 50px;
  background: #00e5ff;
  margin: 0 auto 20px;
  clip-path: polygon(50% 0%, 100% 25%, 100% 75%, 50% 100%, 0% 75%, 0% 25%);
  animation: spin-cw 1s infinite linear;
}
.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
}
.loading-terminal {
  background: #000;
  border: 2px solid #00ff00;
  padding: 1rem;
  min-height: 300px;
  margin-bottom: 2rem;
}
.terminal-screen {
  color: #00ff00;
  font-size: 0.9rem;
  height: 250px;
  overflow: hidden;
}
.blink-cursor {
  animation: blink 0.5s infinite;
}
.loading-bar-container {
  height: 20px;
  border: 2px solid #00ff00;
  padding: 2px;
  margin-top: 1rem;
}
.loading-bar {
  height: 100%;
  background: #00ff00;
  animation: loadBar 2.5s forwards;
}
@keyframes loadBar {
  to {
    width: 100%;
  }
}

.apply-btn {
  width: 100%;
  background: #00ff00;
  color: #000;
  padding: 15px;
  font-weight: bold;
  border: none;
  cursor: pointer;
  margin-top: 10px;
  font-family: inherit;
  text-transform: uppercase;
  border-radius: 5px;
}
.apply-btn:hover {
  background: #fff;
  box-shadow: 0 0 15px #fff;
}
.blink {
  animation: blink 1s infinite;
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
.fade-in {
  animation: fadeIn 0.5s ease-in;
}
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}
.pulse-text {
  animation: pulse 1s infinite;
}

/* -------------------------------------------
   스캔 모달 디자인 리메이크 (깨짐 수정 & 퀄리티 업)
------------------------------------------- */

/* 1. 업로드 화면 (뷰파인더 스타일) */
.upload-section {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  min-height: 300px; /* 높이 확보 */
}

.viewfinder-label {
  position: relative;
  width: 100%;
  height: 250px;
  background: rgba(0, 229, 255, 0.05);
  border: 1px solid rgba(0, 229, 255, 0.3);
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.viewfinder-label:hover {
  background: rgba(0, 229, 255, 0.1);
  box-shadow: 0 0 20px rgba(0, 229, 255, 0.2) inset;
}

/* 네 모서리 장식 (Corner Brackets) */
.corner {
  position: absolute;
  width: 20px;
  height: 20px;
  border: 2px solid #00e5ff;
  transition: all 0.3s;
}
.top-left {
  top: -2px;
  left: -2px;
  border-right: none;
  border-bottom: none;
}
.top-right {
  top: -2px;
  right: -2px;
  border-left: none;
  border-bottom: none;
}
.bottom-left {
  bottom: -2px;
  left: -2px;
  border-right: none;
  border-top: none;
}
.bottom-right {
  bottom: -2px;
  right: -2px;
  border-left: none;
  border-top: none;
}

.viewfinder-label:hover .corner {
  width: 30px;
  height: 30px; /* 호버 시 코너가 커짐 */
}

/* 중앙 아이콘 및 텍스트 */
.center-content {
  text-align: center;
  z-index: 2;
}

.upload-icon-circle {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  border: 2px dashed #00e5ff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 15px;
  animation: rotate-slow 10s linear infinite;
}

.camera-icon {
  font-size: 2.5rem;
  animation: none; /* 아이콘 자체는 회전 안 함 */
}

.instruction-text {
  font-size: 1.1rem;
  font-weight: bold;
  color: #fff;
  letter-spacing: 1px;
  margin-bottom: 5px;
  text-shadow: 0 0 5px #00e5ff;
}

.sub-text {
  font-size: 0.8rem;
  color: #888;
}

@keyframes rotate-slow {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 2. 스캔 중 화면 (사이버펑크 레이저 효과) */
.scanner-view {
  padding: 20px;
  display: flex;
  justify-content: center;
}

.scan-container {
  position: relative;
  width: 100%;
  max-width: 320px;
  height: 320px;
  background: #000;
  border: 2px solid #00ff00; /* 녹색 테두리 */
  border-radius: 5px;
  overflow: hidden;
  box-shadow: 0 0 20px rgba(0, 255, 0, 0.3);
}

.scan-target {
  width: 100%;
  height: 100%;
  object-fit: cover;
  opacity: 0.7;
  filter: grayscale(80%) contrast(1.2); /* 흑백+대비로 스캔 느낌 */
}

/* 강력한 레이저 빔 */
.laser-beam {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 8px; /* 두께 키움 */
  background: #00ff00;
  box-shadow: 0 0 15px #00ff00, 0 0 50px #00ff00;
  animation: scan-up-down 2s cubic-bezier(0.4, 0, 0.2, 1) infinite;
  z-index: 10;
  opacity: 0.8;
}

/* 배경 그리드 (위아래로 움직임) */
.scan-overlay-grid {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 200%; /* 이동을 위해 2배 높이 */
  background: linear-gradient(rgba(0, 255, 0, 0.2) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 255, 0, 0.2) 1px, transparent 1px);
  background-size: 40px 40px;
  animation: grid-scroll 3s linear infinite;
  z-index: 5;
}

/* HUD 텍스트 */
.hud-info {
  position: absolute;
  left: 10px;
  color: #00ff00;
  font-size: 0.75rem;
  font-weight: bold;
  background: rgba(0, 0, 0, 0.8);
  padding: 2px 6px;
  z-index: 15;
}
.hud-info.top {
  top: 10px;
  animation: blink 1s infinite;
}
.hud-info.bottom {
  bottom: 10px;
  animation: blink 1.5s infinite;
}

/* 애니메이션 정의 */
@keyframes scan-up-down {
  0% {
    top: 0%;
    box-shadow: 0 0 10px #00ff00;
  }
  50% {
    top: 98%;
    box-shadow: 0 0 30px #00ff00;
  }
  100% {
    top: 0%;
    box-shadow: 0 0 10px #00ff00;
  }
}

@keyframes grid-scroll {
  0% {
    transform: translateY(0);
  }
  100% {
    transform: translateY(-50%);
  }
}

@keyframes blink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.3;
  }
}
</style>
