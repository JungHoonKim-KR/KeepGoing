<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
import { analyzeDiet } from "../api/diet/dietApi";
import Footer from "../components/utils/Footer.vue";
import axios from "axios";
import { useConfigStore } from "@/stores/configStore";
import { useAuthStore } from "@/stores/authStore";

const router = useRouter();
const config = useConfigStore();
const authStore = useAuthStore();

// 상태 관리
const isLoading = ref(false);
const isAnalyzing = ref(false);
const analysisData = ref(null);
const bootLogs = ref([]);

// 모달 상태
const showDietPlanModal = ref(false);
const showFoodScanModal = ref(false);

// 식단 생성 관련
const dietPlanStep = ref('survey');
const surveyStep = ref(0);
const surveyAnswers = ref({});
const generatedPlan = ref([]);
const selectedDuration = ref(3);

// 음식 스캔 관련
const scanStep = ref('upload');
const scannedImage = ref(null);
const scanResult = ref(null);

const MEMBER_ID = authStore.memberId;
const TODAY_DATE = new Date().toISOString().split("T")[0];

// 설문 데이터
const surveyQuestions = [
  { id: 'duration', question: '얼마나 오래 식단을 따를 예정인가요?', emoji: '📅', options: [
    { value: 3, label: '3일', desc: '짧고굵게!' },
    { value: 7, label: '1주일', desc: '딱 일주일만' },
    { value: 14, label: '2주일', desc: '진지하게 도전' }
  ]},
  { id: 'exercise', question: '운동은 얼마나 할 수 있나요?', emoji: '💪', options: [
    { value: 'none', label: '안 함', desc: '운동은 무리..' },
    { value: 'light', label: '가볍게', desc: '산책 정도' },
    { value: 'hard', label: '열심히', desc: '헬스 고고!' }
  ]},
  { id: 'sleep', question: '하루 수면 시간은?', emoji: '😴', options: [
    { value: 'short', label: '6시간 미만', desc: '부족해요' },
    { value: 'normal', label: '6-8시간', desc: '적당해요' },
    { value: 'long', label: '8시간+', desc: '충분해요' }
  ]},
  { id: 'water', question: '물은 하루에 얼마나?', emoji: '💧', options: [
    { value: 'low', label: '1L 미만', desc: '잘 안 마심' },
    { value: 'medium', label: '1-2L', desc: '적당히' },
    { value: 'high', label: '2L+', desc: '많이 마심' }
  ]},
  { id: 'meals', question: '하루 몇 끼?', emoji: '🍽️', options: [
    { value: 2, label: '2끼', desc: '간헐적 단식' },
    { value: 3, label: '3끼', desc: '규칙적으로' },
    { value: 5, label: '5끼', desc: '소식 다끼' }
  ]},
  { id: 'favorite', question: '꼭 먹고 싶은 음식?', emoji: '❤️', options: [
    { value: 'none', label: '없음', desc: '다 괜찮아' },
    { value: 'chicken', label: '치킨', desc: '치킨은 참지' },
    { value: 'pizza', label: '피자', desc: '피자 러버' }
  ]}
];

const getRankColor = (score) => {
  if (score >= 90) return "#ffd700"; 
  if (score >= 80) return "#00e5ff"; 
  if (score >= 70) return "#00ff00"; 
  if (score >= 50) return "#ffaa00"; 
  return "#ff0055"; 
};

// 기존 분석
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
      insights: data.insights.map((item, index) => ({
        id: index,
        type: item.type,
        iconType: item.iconType,
        title: item.title,
        description: item.description,
      })),
      recommendation: data.oneLineSummary,
      questItems: data.recommendations,
    };
    setTimeout(() => {
      isLoading.value = false;
      isAnalyzing.value = false;
    }, 2500);
  } catch (error) {
    console.error("분석 실패:", error);
    alert("서버 연결에 실패했습니다.");
    isLoading.value = false;
    isAnalyzing.value = false;
  }
};

// 식단 생성 모달
const openDietPlanModal = () => {
  showDietPlanModal.value = true;
  dietPlanStep.value = 'survey';
  surveyStep.value = 0;
  surveyAnswers.value = {};
};

const selectAnswer = (questionId, value) => {
  surveyAnswers.value[questionId] = value;
  if (questionId === 'duration') selectedDuration.value = value;
  
  if (surveyStep.value < surveyQuestions.length - 1) {
    setTimeout(() => surveyStep.value++, 300);
  } else {
    setTimeout(() => generateDietPlan(), 300);
  }
};

const generateDietPlan = () => {
  dietPlanStep.value = 'loading';
  setTimeout(() => {
    const menus = [
      { name: "닭가슴살 샐러드", cal: 350, difficulty: "EASY" },
      { name: "현미밥 & 고등어", cal: 500, difficulty: "NORMAL" },
      { name: "그릭 요거트", cal: 200, difficulty: "EASY" }
    ];
    const quests = ["물 2L", "천천히 씹기", "야식 금지"];
    
    generatedPlan.value = Array.from({ length: selectedDuration.value }, (_, i) => {
      const tDate = new Date();
      tDate.setDate(tDate.getDate() + i + 1);
      return {
        day: i + 1,
        date: tDate.toLocaleDateString(),
        menu: menus[Math.floor(Math.random() * menus.length)].name,
        quest: quests[Math.floor(Math.random() * quests.length)],
        cal: menus[Math.floor(Math.random() * menus.length)].cal,
        difficulty: menus[Math.floor(Math.random() * menus.length)].difficulty
      };
    });
    dietPlanStep.value = 'result';
  }, 2000);
};

// 음식 스캔 모달
const openFoodScanModal = () => {
  showFoodScanModal.value = true;
  scanStep.value = 'upload';
  scannedImage.value = null;
  scanResult.value = null;
};

const handleImageUpload = (event) => {
  const file = event.target.files[0];
  if (!file) return;
  
  const reader = new FileReader();
  reader.onload = (e) => {
    scannedImage.value = e.target.result;
    analyzeFoodImage();
  };
  reader.readAsDataURL(file);
};

const analyzeFoodImage = () => {
  scanStep.value = 'analyzing';
  setTimeout(() => {
    const mockFoods = [
      { name: "치킨", calories: 1850, emoji: "🍗" },
      { name: "피자", calories: 2100, emoji: "🍕" },
      { name: "햄버거", calories: 1200, emoji: "🍔" }
    ];
    const food = mockFoods[Math.floor(Math.random() * mockFoods.length)];
    scanResult.value = {
      ...food,
      exercise: {
        running: Math.round(food.calories / 11.2),
        walking: Math.round(food.calories / 4.4),
        swimming: Math.round(food.calories / 13.2)
      }
    };
    scanStep.value = 'result';
  }, 2000);
};

const runBootSequence = () => {
  const logs = ["INITIALIZING SYSTEM...", "CONNECTING...", "ACCESS GRANTED."];
  let logIndex = 0;
  const logInterval = setInterval(() => {
    if (logIndex < logs.length) {
      bootLogs.value.push(logs[logIndex++]);
    } else {
      clearInterval(logInterval);
    }
  }, 350);
};
</script>

<template>
  <div class="ai-view">
    <div class="content-wrapper">
      <div class="retro-header">
        <div class="system-status"><span class="status-light blink"></span> ONLINE</div>
        <h1 class="page-title">AI HEALTH LAB</h1>
      </div>

      <div v-if="isLoading" class="loading-terminal">
        <div class="terminal-screen">
          <div v-for="(log, index) in bootLogs" :key="index" class="log-line">> {{ log }}</div>
          <div class="cursor-line">> <span class="blink-cursor">_</span></div>
        </div>
        <div class="loading-bar-container"><div class="loading-bar"></div></div>
      </div>

      <div v-else class="dashboard-container">
        <!-- AI 아바타 -->
        <div class="ai-avatar" @click="fetchAnalysis">
          <div class="cyber-eye">
            <div class="eye-ring" :class="{ 'fast-spin': isAnalyzing }"></div>
            <div class="eye-pupil"></div>
          </div>
          <div class="ai-message">
            <p v-if="!analysisData" class="blink-text">"시스템 대기 중..."</p>
            <p v-else>"분석 완료. 랭크 [{{ analysisData.rank }}]"</p>
          </div>
        </div>

        <!-- 메인 액션 버튼 -->
        <div class="action-grid">
          <button class="hero-btn diet" @click="openDietPlanModal">
            <div class="btn-icon">🥗</div>
            <div class="btn-text">
              <div class="btn-title">AI 식단 생성</div>
              <div class="btn-desc">2지선다로 쉽게!</div>
            </div>
            <div class="btn-arrow">→</div>
          </button>

          <button class="hero-btn scan" @click="openFoodScanModal">
            <div class="btn-icon">📸</div>
            <div class="btn-text">
              <div class="btn-title">음식 스캔</div>
              <div class="btn-desc">칼로리 폭발!</div>
            </div>
            <div class="btn-arrow">→</div>
          </button>
        </div>

        <!-- 기존 분석 결과 -->
        <div v-if="analysisData" class="result-section">
          <div class="power-card">
            <div class="rank-badge" :style="{ color: getRankColor(analysisData.overallScore) }">
              RANK {{ analysisData.rank }}
            </div>
            <div class="score-val" :style="{ color: getRankColor(analysisData.overallScore) }">
              {{ analysisData.overallScore }} <span class="max">/ 100</span>
            </div>
            <div class="retro-progress">
              <div class="fill" :style="{ width: `${analysisData.overallScore}%`, background: getRankColor(analysisData.overallScore) }"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <Footer />

    <!-- 식단 생성 모달 -->
    <div v-if="showDietPlanModal" class="modal-overlay" @click.self="showDietPlanModal = false">
      <div class="modal-win survey-modal">
        <div class="modal-header">
          <span>{{ dietPlanStep === 'survey' ? `설문 ${surveyStep + 1}/${surveyQuestions.length}` : '식단 생성중' }}</span>
          <button class="close-btn" @click="showDietPlanModal = false">✕</button>
        </div>

        <div v-if="dietPlanStep === 'survey'" class="modal-body">
          <div class="question">
            <div class="q-emoji">{{ surveyQuestions[surveyStep].emoji }}</div>
            <h3>{{ surveyQuestions[surveyStep].question }}</h3>
          </div>
          <div class="options">
            <button v-for="opt in surveyQuestions[surveyStep].options" :key="opt.value"
              class="opt-btn" @click="selectAnswer(surveyQuestions[surveyStep].id, opt.value)">
              <div class="opt-label">{{ opt.label }}</div>
              <div class="opt-desc">{{ opt.desc }}</div>
            </button>
          </div>
        </div>

        <div v-if="dietPlanStep === 'loading'" class="modal-body loading">
          <div class="spinner"></div>
          <p class="blink-text">AI가 식단을 만들고 있습니다...</p>
        </div>

        <div v-if="dietPlanStep === 'result'" class="modal-body result">
          <div class="summary">{{ selectedDuration }}일 식단 완성!</div>
          <div class="plan-list">
            <div v-for="p in generatedPlan" :key="p.day" class="plan-item">
              <div class="day">DAY {{ p.day }}</div>
              <div class="menu">{{ p.menu }}</div>
              <div class="quest">🎯 {{ p.quest }}</div>
            </div>
          </div>
          <button class="apply-btn" @click="showDietPlanModal = false">적용하기</button>
        </div>
      </div>
    </div>

    <!-- 음식 스캔 모달 -->
    <div v-if="showFoodScanModal" class="modal-overlay" @click.self="showFoodScanModal = false">
      <div class="modal-win scan-modal">
        <div class="modal-header">
          <span>{{ scanStep === 'upload' ? '음식 촬영' : '분석 완료!' }}</span>
          <button class="close-btn" @click="showFoodScanModal = false">✕</button>
        </div>

        <div v-if="scanStep === 'upload'" class="modal-body upload">
          <input type="file" accept="image/*" id="food-img" @change="handleImageUpload" style="display:none" />
          <label for="food-img" class="upload-label">
            <div class="upload-icon">📸</div>
            <div>음식 사진 업로드</div>
          </label>
        </div>

        <div v-if="scanStep === 'analyzing'" class="modal-body loading">
          <img :src="scannedImage" class="preview" />
          <div class="spinner"></div>
          <p class="blink-text">AI 분석 중...</p>
        </div>

        <div v-if="scanStep === 'result'" class="modal-body result">
          <img :src="scannedImage" class="preview" />
          <div class="food-name">{{ scanResult.emoji }} {{ scanResult.name }}</div>
          <div class="calorie-big">🔥 {{ scanResult.calories }} kcal</div>
          <div class="exercise-grid">
            <div class="ex-card">
              <div>🏃 달리기</div>
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
          <button class="apply-btn" @click="scanStep = 'upload'; scannedImage = null">다시 스캔</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url("https://cdn.jsdelivr.net/gh/neodgm/neodgm-webfont@latest/neodgm/style.css");

.ai-view { min-height:100vh; background:#050510; color:#e0e0e0; font-family:"NeoDunggeunmo",monospace; padding-bottom:100px; }
.content-wrapper { max-width:480px; margin:0 auto; padding:1rem; }
.retro-header { display:flex; justify-content:space-between; align-items:center; margin-bottom:2rem; border-bottom:2px solid #333; padding-bottom:0.5rem; }
.system-status { font-size:0.7rem; color:#00ff00; display:flex; align-items:center; gap:5px; }
.status-light { width:8px; height:8px; background:#00ff00; border-radius:50%; box-shadow:0 0 5px #00ff00; }
.blink { animation:blink 1s infinite; }
.page-title { font-size:1.2rem; color:#fff; }

.ai-avatar { display:flex; flex-direction:column; align-items:center; margin-bottom:2rem; cursor:pointer; }
.cyber-eye { width:100px; height:100px; position:relative; display:flex; align-items:center; justify-content:center; }
.eye-ring { width:100%; height:100%; border:4px solid #00e5ff; border-radius:50%; animation:spin 4s linear infinite; }
.eye-ring.fast-spin { animation:spin 1s linear infinite; }
.eye-pupil { width:20px; height:20px; background:#fff; border-radius:50%; position:absolute; }
.ai-message { margin-top:1rem; color:#00e5ff; font-size:0.8rem; }

.action-grid { display:grid; gap:1rem; margin-bottom:2rem; }
.hero-btn { background:linear-gradient(135deg,#1a1a2e,#16213e); border:2px solid; padding:1.2rem; display:flex; align-items:center; gap:1rem; cursor:pointer; transition:all 0.3s; border-radius:10px; }
.hero-btn.diet { border-color:#00ff00; }
.hero-btn.scan { border-color:#ff00ff; }
.hero-btn:hover { transform:translateY(-5px); box-shadow:0 10px 30px rgba(0,255,0,0.3); }
.btn-icon { font-size:3rem; }
.btn-text { flex:1; text-align:left; }
.btn-title { font-size:1.2rem; font-weight:bold; color:#fff; }
.btn-desc { font-size:0.8rem; color:#888; }
.btn-arrow { font-size:2rem; color:#00e5ff; }

.power-card { background:#111; border:2px solid #333; padding:1.5rem; margin-bottom:1.5rem; }
.rank-badge { font-weight:bold; font-size:1.2rem; }
.score-val { font-size:2.5rem; font-weight:bold; }
.retro-progress { height:12px; background:#222; border:1px solid #555; padding:1px; }
.retro-progress .fill { height:100%; transition:width 1s; }

.modal-overlay { position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.85); z-index:999; display:flex; align-items:center; justify-content:center; }
.modal-win { width:90%; max-width:450px; background:#000; border:2px solid #00e5ff; max-height:80vh; display:flex; flex-direction:column; }
.modal-header { background:#00e5ff; color:#000; padding:10px; display:flex; justify-content:space-between; font-weight:bold; }
.close-btn { background:none; border:none; cursor:pointer; color:#000; font-size:1.2rem; }
.modal-body { padding:20px; overflow-y:auto; }

.question { text-align:center; margin-bottom:20px; }
.q-emoji { font-size:3rem; }
.options { display:grid; gap:10px; }
.opt-btn { background:#111; border:1px solid #555; padding:15px; cursor:pointer; transition:all 0.2s; }
.opt-btn:hover { border-color:#00e5ff; background:#002233; }
.opt-label { font-weight:bold; color:#fff; }
.opt-desc { font-size:0.8rem; color:#888; }

.loading { display:flex; flex-direction:column; align-items:center; justify-content:center; min-height:200px; }
.spinner { width:40px; height:40px; border:4px solid #333; border-top-color:#00ff00; border-radius:50%; animation:spin 1s infinite; margin-bottom:15px; }

.summary { text-align:center; color:#00ff00; font-size:1.2rem; margin-bottom:15px; }
.plan-list { max-height:300px; overflow-y:auto; }
.plan-item { background:rgba(255,255,255,0.05); border:1px solid #333; padding:10px; margin-bottom:10px; }
.day { color:#00e5ff; font-weight:bold; }
.menu { color:#fff; }
.quest { color:#ccc; font-size:0.8rem; }

.upload-label { display:flex; flex-direction:column; align-items:center; gap:10px; padding:40px; border:2px dashed #00e5ff; cursor:pointer; }
.upload-icon { font-size:4rem; }
.preview { width:100%; max-height:200px; object-fit:cover; border-radius:10px; margin-bottom:15px; }
.food-name { font-size:1.5rem; font-weight:bold; color:#fff; text-align:center; margin-bottom:10px; }
.calorie-big { font-size:3rem; font-weight:bold; color:#ff0055; text-align:center; margin-bottom:20px; animation:pulse 1s infinite; }
.exercise-grid { display:grid; grid-template-columns:repeat(3,1fr); gap:10px; margin-bottom:20px; }
.ex-card { background:#111; border:1px solid #333; padding:15px; text-align:center; }
.time { font-size:1.5rem; font-weight:bold; color:#00ff00; margin-top:5px; }

.apply-btn { width:100%; background:#00ff00; color:#000; border:none; padding:15px; font-weight:bold; cursor:pointer; font-family:inherit; }
.apply-btn:hover { background:#ccffcc; }

@keyframes spin { from { transform:rotate(0deg); } to { transform:rotate(360deg); } }
@keyframes blink { 0%,100% { opacity:1; } 50% { opacity:0.5; } }
@keyframes pulse { 0%,100% { transform:scale(1); } 50% { transform:scale(1.1); } }
.loading-terminal { background:#000; border:2px solid #00ff00; padding:1rem; min-height:300px; }
.terminal-screen { color:#00ff00; font-size:0.9rem; }
.blink-cursor { animation:blink 0.5s infinite; }
.loading-bar-container { height:20px; border:2px solid #00ff00; padding:2px; margin-top:1rem; }
.loading-bar { height:100%; background:#00ff00; animation:loadBar 2.5s forwards; }
@keyframes loadBar { to { width:100%; } }
</style>