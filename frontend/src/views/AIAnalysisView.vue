<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router"; // 라우터는 다른 곳 이동용으로 둠 (현재 로직엔 미사용)
import { analyzeDiet } from "../api/diet/dietApi";
import Footer from "../components/utils/Footer.vue";
import axios from "axios";
import { useConfigStore } from "@/stores/configStore";
import { useAuthStore } from "@/stores/authStore";

const router = useRouter();
const config = useConfigStore();
const authStore = useAuthStore();

// ----------------------------------------------------
// 1. 상태 관리
// ----------------------------------------------------
const isLoading = ref(false);
const isAnalyzing = ref(false);
const analysisData = ref(null);
const bootLogs = ref([]);

// [모달 관련 상태]
const showPlanModal = ref(false);
const planStep = ref('duration'); // 'duration' | 'loading' | 'result'
const selectedDuration = ref(3);
const durations = [3, 5, 7];
const generatedPlan = ref([]); // 생성된 스케줄 데이터 담는 곳

// 사용자 정보
const MEMBER_ID = authStore.memberId;
const TODAY_DATE = new Date().toISOString().split("T")[0];

// ----------------------------------------------------
// 2. 유틸리티 & 로직
// ----------------------------------------------------
const getRankColor = (score) => {
  if (score >= 90) return "#ffd700"; 
  if (score >= 80) return "#00e5ff"; 
  if (score >= 70) return "#00ff00"; 
  if (score >= 50) return "#ffaa00"; 
  return "#ff0055"; 
};

// [식단 분석 요청]
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
    
    // 경험치 업데이트 (필요 시 주석 해제)
    // await updateExp(data.score);

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

// ----------------------------------------------------
// 3. 퀘스트 플랜 모달 로직
// ----------------------------------------------------

// 모달 열기 (초기화)
const openPlanModal = () => {
  if (!analysisData.value) return;
  showPlanModal.value = true;
  planStep.value = 'duration'; // 첫 단계: 기간 선택
  selectedDuration.value = 3;
  generatedPlan.value = [];
};

// 닫기
const closePlanModal = () => {
  showPlanModal.value = false;
};

// [임시 데이터 생성기]
const generateMockData = (days) => {
  const mockPlans = [];
  const menus = [
    { name: "닭가슴살 샐러드", cal: 350, difficulty: "EASY" },
    { name: "현미밥 & 고등어", cal: 500, difficulty: "NORMAL" },
    { name: "그릭 요거트 & 견과류", cal: 200, difficulty: "EASY" },
    { name: "아보카도 덮밥", cal: 450, difficulty: "HARD" },
    { name: "연어 포케", cal: 480, difficulty: "NORMAL" },
    { name: "두부 쉐이크", cal: 250, difficulty: "EASY" }
  ];
  const quests = ["물 2L 마시기", "천천히 씹기", "야식 금지", "10분 산책", "탄산 끊기"];

  for (let i = 1; i <= days; i++) {
    const rMenu = menus[Math.floor(Math.random() * menus.length)];
    const rQuest = quests[Math.floor(Math.random() * quests.length)];
    const tDate = new Date();
    tDate.setDate(tDate.getDate() + i);

    mockPlans.push({
      day: i,
      date: tDate.toLocaleDateString(),
      menu: rMenu.name,
      quest: rQuest,
      cal: rMenu.cal,
      difficulty: rMenu.difficulty
    });
  }
  return mockPlans;
};

// [기간 선택 후 -> 로딩 -> 결과 표시]
const proceedToGeneration = () => {
  planStep.value = 'loading';
  
  // 1.5초 딜레이 후 데이터 생성 완료 처리
  setTimeout(() => {
    generatedPlan.value = generateMockData(selectedDuration.value);
    planStep.value = 'result';
  }, 1500);
};

// [최종 적용]
const applyPlan = () => {
  // 여기서 서버로 확정된 플랜을 전송하는 로직을 추가하면 됩니다.
  alert(`✅ ${selectedDuration.value}일치 퀘스트가 시스템에 등록되었습니다!`);
  closePlanModal();
};

// ----------------------------------------------------
// 4. 애니메이션/기타
// ----------------------------------------------------
const runBootSequence = () => {
  const logs = [
    "INITIALIZING SYSTEM...", "CONNECTING TO NEURAL NET...",
    "SCANNING BIOMETRICS...", "DECRYPTING FOOD LOGS...",
    "CALCULATING POWER LEVEL...", "ACCESS GRANTED.",
  ];
  let logIndex = 0;
  const logInterval = setInterval(() => {
    if (logIndex < logs.length) {
      bootLogs.value.push(logs[logIndex]);
      logIndex++;
    } else {
      clearInterval(logInterval);
    }
  }, 350);
};

const initAudioContext = () => {};
</script>

<template>
  <div class="ai-view retro-theme" @click="initAudioContext">

    <div class="content-wrapper">
      <div class="retro-header">
        <div class="system-status">
          <span class="status-light blink"></span> SYSTEM_ONLINE
        </div>
        <h1 class="page-title">MAINFRAME ANALYSIS</h1>
      </div>

      <div v-if="isLoading" class="loading-terminal">
        <div class="terminal-screen">
          <div v-for="(log, index) in bootLogs" :key="index" class="log-line">> {{ log }}</div>
          <div class="cursor-line">> <span class="blink-cursor">_</span></div>
        </div>
        <div class="loading-bar-container"><div class="loading-bar"></div></div>
      </div>

      <div v-else class="dashboard-container">
        <div class="ai-avatar-section clickable" @click="fetchAnalysis">
          <div class="cyber-eye-container">
            <div class="eye-ring" :class="{ 'fast-spin': isAnalyzing }"></div>
            <div class="eye-iris">
              <div class="eye-pupil"></div>
              <div class="eye-glint"></div>
            </div>
            <div class="scanning-beam"></div>
          </div>
          <div class="ai-message-box">
            <p v-if="!analysisData" class="blink-text">"시스템 대기 중... [터치하여 분석 시작]"</p>
            <p v-else class="typing-effect">"분석 완료. 랭크 [{{ analysisData.rank }}] 달성."</p>
          </div>
        </div>

        <div v-if="analysisData" class="result-section pop-in">
          <div class="power-card">
            <div class="card-deco tl"></div><div class="card-deco tr"></div>
            <div class="card-deco bl"></div><div class="card-deco br"></div>

            <div class="rank-badge" 
                 :style="{ color: getRankColor(analysisData.overallScore), borderColor: getRankColor(analysisData.overallScore), boxShadow: `4px 4px 0 ${getRankColor(analysisData.overallScore)}33` }">
              RANK {{ analysisData.rank }}
            </div>

            <div class="score-row">
              <div class="score-label">POWER LEVEL</div>
              <div class="score-val" :style="{ color: getRankColor(analysisData.overallScore) }">
                {{ analysisData.overallScore }} <span class="max">/ 100</span>
              </div>
            </div>
            <div class="retro-progress">
              <div class="fill" :style="{ width: `${analysisData.overallScore}%`, background: getRankColor(analysisData.overallScore), boxShadow: `0 0 10px ${getRankColor(analysisData.overallScore)}` }"></div>
            </div>
          </div>

          <div class="insight-grid">
            <div v-for="(item, idx) in analysisData.insights" :key="item.id" class="insight-card pop-in" :class="item.type" :style="{ animationDelay: `${idx * 0.1}s` }">
              <div class="icon-box">
                <span v-if="item.iconType === 'sword'" style="font-size:24px">⚔️</span>
                 <span v-else-if="item.iconType === 'skull'" style="font-size:24px">💀</span>
                 <span v-else-if="item.iconType === 'scale'" style="font-size:24px">⚖️</span>
                 <span v-else style="font-size:24px">📜</span>
              </div>
              <div class="text-box">
                <div class="card-title">{{ item.title }}</div>
                <div class="card-desc">{{ item.description }}</div>
              </div>
            </div>
          </div>

          <div class="advice-terminal">
            <div class="terminal-header">/// ORACLE_ADVICE.TXT ///</div>
            <div class="terminal-body">{{ analysisData.recommendation }}</div>
          </div>

          <div class="action-buttons">
            <button class="retro-btn primary" @click="openPlanModal">
              <span class="btn-icon">📜</span> VIEW QUEST PLAN
            </button>
          </div>
        </div>
      </div>
    </div>
    <Footer />

    <div v-if="showPlanModal" class="modal-overlay" @click.self="closePlanModal">
      <div class="modal-window pop-in">
        
        <div class="modal-header">
          <span v-if="planStep === 'duration'">>>> SELECT_DURATION</span>
          <span v-else-if="planStep === 'loading'">>>> PROCESSING...</span>
          <span v-else>>>> PLAN_PREVIEW</span>
          <button class="close-btn" @click="closePlanModal">X</button>
        </div>

        <div v-if="planStep === 'duration'" class="modal-body">
          <p class="modal-desc">생성할 퀘스트 플랜의 기간을 설정하십시오.</p>
          <div class="duration-options">
            <button v-for="day in durations" :key="day" class="duration-btn" :class="{ active: selectedDuration === day }" @click="selectedDuration = day">
              {{ day }} DAYS
            </button>
          </div>
          <div class="modal-actions">
            <button class="retro-btn confirm" @click="proceedToGeneration">GENERATE PLAN</button>
          </div>
        </div>

        <div v-if="planStep === 'loading'" class="modal-body loading-state">
          <div class="spinner"></div>
          <p class="blink-text">CALCULATING OPTIMAL PATH...</p>
        </div>

        <div v-if="planStep === 'result'" class="modal-body result-state">
          <div class="plan-summary">
             TOTAL: <span class="highlight">{{ selectedDuration }} DAYS</span> QUEST
          </div>
          
          <div class="scroll-area">
            <div v-for="plan in generatedPlan" :key="plan.day" class="plan-item">
              <div class="plan-date">
                <span class="day-tag">DAY {{ plan.day }}</span>
                <span class="date-lbl">{{ plan.date }}</span>
              </div>
              <div class="plan-content">
                <div class="plan-menu">{{ plan.menu }}</div>
                <div class="plan-quest">🎯 {{ plan.quest }}</div>
                <div class="plan-meta">
                  <span>{{ plan.cal }}kcal</span>
                  <span :class="['diff-tag', plan.difficulty]">{{ plan.difficulty }}</span>
                </div>
              </div>
            </div>
            <div class="scroll-hint">... SCROLL FOR MORE ...</div>
          </div>

          <div class="modal-actions split">
            <button class="retro-btn cancel" @click="closePlanModal">CANCEL</button>
            <button class="retro-btn apply" @click="applyPlan">ACCEPT & APPLY</button>
          </div>
        </div>

      </div>
    </div>

  </div>
</template>

<style scoped>
@import url("https://cdn.jsdelivr.net/gh/neodgm/neodgm-webfont@latest/neodgm/style.css");

/* ================= 기존 스타일 (레이아웃 등) ================= */
.ai-view {
  display: flex; align-items: center; min-height: 100vh;
  background-color: #050510; color: #e0e0e0; font-family: "NeoDunggeunmo", monospace;
  padding-bottom: 100px; overflow-x: hidden;
}

.content-wrapper { max-width: 480px; margin: 0 auto; padding: 1rem; position: relative; z-index: 100; }
.retro-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; border-bottom: 2px solid #333; padding-bottom: 0.5rem; }
.system-status { font-size: 0.7rem; color: #00ff00; display: flex; align-items: center; gap: 5px; }
.status-light { width: 8px; height: 8px; background: #00ff00; border-radius: 50%; box-shadow: 0 0 5px #00ff00; }
.blink { animation: blink 1s infinite; }
.page-title { font-size: 1.2rem; color: #fff; text-shadow: 2px 2px #000; margin: 0; }
.loading-terminal { background: #000; border: 2px solid #00ff00; padding: 1rem; border-radius: 5px; min-height: 300px; display: flex; flex-direction: column; justify-content: space-between; }
.terminal-screen { color: #00ff00; font-size: 0.9rem; line-height: 1.5; }
.blink-cursor { animation: blink 0.5s infinite; }
.loading-bar-container { height: 20px; border: 2px solid #00ff00; padding: 2px; margin-top: 1rem; }
.loading-bar { height: 100%; background: #00ff00; width: 0%; animation: loadBar 2.5s ease-out forwards; }
@keyframes loadBar { to { width: 100%; } }
.ai-avatar-section { display: flex; flex-direction: column; align-items: center; margin-bottom: 2rem; }
.cyber-eye-container { width: 100px; height: 100px; position: relative; display: flex; align-items: center; justify-content: center; }
.eye-ring { width: 100%; height: 100%; border: 4px solid #00e5ff; border-radius: 50%; border-left-color: transparent; border-right-color: transparent; animation: spin 4s linear infinite; box-shadow: 0 0 15px #00e5ff; }
.eye-ring.fast-spin { animation: spin 1s linear infinite; }
.eye-iris { width: 60px; height: 60px; background: rgba(0, 229, 255, 0.2); border-radius: 50%; display: flex; align-items: center; justify-content: center; position: relative; border: 2px solid #00e5ff; animation: pulseEye 3s infinite; }
.eye-pupil { width: 20px; height: 20px; background: #fff; border-radius: 50%; box-shadow: 0 0 10px #00e5ff; animation: lookAround 5s infinite; }
.scanning-beam { position: absolute; width: 100%; height: 2px; background: #00ff00; top: 0; animation: scanDown 2s linear infinite; opacity: 0.5; box-shadow: 0 0 5px #00ff00; }
.ai-message-box { margin-top: 1rem; background: rgba(0, 229, 255, 0.1); border: 1px solid #00e5ff; padding: 0.5rem 1rem; border-radius: 15px 15px 15px 0; color: #00e5ff; font-size: 0.8rem; }
.power-card { background: #111; border: 2px solid #333; padding: 1.5rem; position: relative; margin-bottom: 1.5rem; box-shadow: 0 10px 20px rgba(0, 0, 0, 0.5); }
.card-deco { position: absolute; width: 10px; height: 10px; border: 2px solid #fff; }
.tl { top: -2px; left: -2px; border-right: 0; border-bottom: 0; }
.tr { top: -2px; right: -2px; border-left: 0; border-bottom: 0; }
.bl { bottom: -2px; left: -2px; border-right: 0; border-top: 0; }
.br { bottom: -2px; right: -2px; border-left: 0; border-top: 0; }
.rank-badge { position: absolute; top: -15px; right: 10px; background: #000; border: 2px solid; padding: 2px 10px; font-weight: bold; font-size: 1.2rem; transform: rotate(5deg); box-shadow: 4px 4px 0 rgba(0, 0, 0, 0.5); }
.score-row { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 0.5rem; }
.score-label { color: #888; font-size: 0.8rem; }
.score-val { font-size: 2.5rem; font-weight: bold; line-height: 1; text-shadow: 0 0 10px currentColor; }
.score-val .max { font-size: 1rem; color: #555; }
.retro-progress { height: 12px; background: #222; border: 1px solid #555; padding: 1px; }
.retro-progress .fill { height: 100%; transition: width 1s ease-out; box-shadow: 0 0 10px currentColor; }
.insight-grid { display: grid; gap: 1rem; margin-bottom: 1.5rem; }
.insight-card { background: rgba(255, 255, 255, 0.05); border: 1px solid #333; padding: 1rem; display: flex; gap: 1rem; align-items: flex-start; transition: transform 0.2s; backdrop-filter: blur(5px); }
.insight-card:hover { transform: translateX(5px); background: rgba(255, 255, 255, 0.08); }
.insight-card.positive { border-left: 4px solid #00ff00; }
.insight-card.warning { border-left: 4px solid #ff0055; }
.insight-card.suggestion { border-left: 4px solid #00e5ff; }
.icon-box { width: 40px; height: 40px; flex-shrink: 0; display:flex; align-items:center; justify-content:center; }
.text-box { flex: 1; }
.card-title { font-size: 0.9rem; font-weight: bold; margin-bottom: 4px; color: #fff; }
.card-desc { font-size: 0.8rem; color: #aaa; line-height: 1.3; }
.advice-terminal { background: #000; border: 1px solid #666; margin-bottom: 1.5rem; }
.terminal-header { background: #333; color: #fff; padding: 5px 10px; font-size: 0.7rem; border-bottom: 1px solid #666; }
.terminal-body { padding: 1rem; color: #ffd700; font-size: 0.85rem; line-height: 1.4; }
.retro-btn { flex: 1; padding: 12px; border: 2px solid #fff; font-family: inherit; font-size: 0.9rem; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 8px; box-shadow: 4px 4px 0 #000; transition: transform 0.1s; background: #00e5ff; color: #000; }
.retro-btn:active { transform: translate(4px, 4px); box-shadow: none; }

/* ========================================== */
/* [수정됨] 모달 통합 스타일                  */
/* ========================================== */
.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.85); z-index: 999;
  display: flex; align-items: center; justify-content: center; backdrop-filter: blur(5px);
}
.modal-window {
  width: 90%; max-width: 450px; 
  background: #000; border: 2px solid #00e5ff;
  box-shadow: 0 0 20px rgba(0, 229, 255, 0.3);
  display: flex; flex-direction: column;
  max-height: 80vh; /* 화면 높이의 80%까지만 */
}
.modal-header {
  background: #00e5ff; color: #000; padding: 10px; font-weight: bold;
  display: flex; justify-content: space-between; align-items: center;
  flex-shrink: 0;
}
.close-btn { background: none; border: none; font-weight: bold; cursor: pointer; color: #000; font-size: 1.2rem; }
.modal-body {
  padding: 20px; 
  display: flex; flex-direction: column;
  overflow: hidden; /* 내부 스크롤을 위해 숨김 */
}
.loading-state {
  align-items: center; justify-content: center; min-height: 200px;
}
.spinner {
  width: 40px; height: 40px; border: 4px solid #333; border-top-color: #00ff00;
  border-radius: 50%; animation: spin 1s infinite linear; margin-bottom: 15px;
}
.modal-desc { color: #fff; margin-bottom: 20px; font-size: 0.9rem; text-align: center; }

/* 기간 선택 버튼들 */
.duration-options { display: flex; gap: 10px; justify-content: center; margin-bottom: 20px; }
.duration-btn {
  padding: 10px 20px; background: #111; border: 1px solid #555;
  color: #888; cursor: pointer; font-family: inherit; transition: all 0.2s;
}
.duration-btn.active {
  border-color: #00e5ff; color: #00e5ff; box-shadow: 0 0 10px #00e5ff inset; background: #002233;
}

/* 결과 리스트 (스크롤 영역) */
.plan-summary {
  text-align: center; color: #aaa; font-size: 0.9rem; margin-bottom: 15px; padding-bottom: 10px; border-bottom: 1px solid #333;
}
.plan-summary .highlight { color: #00ff00; font-weight: bold; }

.scroll-area {
  overflow-y: auto;
  max-height: 350px; /* 여기서 높이 제한 (대략 3개 반 정도 보임) */
  padding-right: 5px;
  margin-bottom: 15px;
}
/* 스크롤바 커스텀 */
.scroll-area::-webkit-scrollbar { width: 6px; }
.scroll-area::-webkit-scrollbar-thumb { background: #00ff00; border-radius: 3px; }
.scroll-area::-webkit-scrollbar-track { background: #111; }

/* 리스트 아이템 스타일 */
.plan-item {
  background: rgba(255, 255, 255, 0.05); border: 1px solid #333;
  margin-bottom: 10px; padding: 12px; display: flex; gap: 12px; align-items: center;
}
.plan-date {
  display: flex; flex-direction: column; align-items: center; 
  border-right: 1px solid #444; padding-right: 10px; min-width: 55px;
}
.day-tag { font-size: 0.8rem; font-weight: bold; color: #00e5ff; margin-bottom: 4px; }
.date-lbl { font-size: 0.65rem; color: #888; }

.plan-content { flex: 1; }
.plan-menu { font-size: 0.95rem; color: #fff; font-weight: bold; margin-bottom: 4px; }
.plan-quest { font-size: 0.8rem; color: #ccc; margin-bottom: 6px; }
.plan-meta { display: flex; gap: 6px; font-size: 0.75rem; color: #888; }
.diff-tag { border: 1px solid; padding: 0 4px; border-radius: 3px; font-size: 0.7rem; }
.diff-tag.EASY { color: #00ff00; border-color: #00ff00; }
.diff-tag.NORMAL { color: #ffff00; border-color: #ffff00; }
.diff-tag.HARD { color: #ff0055; border-color: #ff0055; }

.scroll-hint { text-align: center; font-size: 0.7rem; color: #555; animation: blink 2s infinite; }

/* 하단 버튼 액션 */
.modal-actions { margin-top: auto; }
.modal-actions.split { display: flex; gap: 10px; }
.retro-btn.confirm { width: 100%; background: #00ff00; color: #000; border: none; box-shadow: none; }
.retro-btn.confirm:hover { background: #ccffcc; }

.retro-btn.cancel { background: #333; color: #fff; border: 1px solid #666; }
.retro-btn.apply { background: #00e5ff; color: #000; border: none; }

@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
@keyframes pulseEye { 0%, 100% { transform: scale(1); } 50% { transform: scale(0.9); } }
@keyframes lookAround { 0%, 100% { transform: translate(0, 0); } 25% { transform: translate(-5px, -2px); } 50% { transform: translate(5px, 2px); } 75% { transform: translate(0, 5px); } }
@keyframes scanDown { 0% { top: 0; opacity: 0; } 50% { opacity: 1; } 100% { top: 100%; opacity: 0; } }
@keyframes swing { 0%, 100% { transform: rotate(0deg); } 50% { transform: rotate(15deg); } }
@keyframes shake { 0% { transform: translate(0, 0); } 25% { transform: translate(1px, 1px); } 75% { transform: translate(-1px, -1px); } }
@keyframes balance { 0%, 100% { transform: rotate(0deg); } 25% { transform: rotate(5deg); } 75% { transform: rotate(-5deg); } }
@keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-5px); } }
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }
.pop-in { animation: popIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards; opacity: 0; transform: scale(0.9); }
@keyframes popIn { to { opacity: 1; transform: scale(1); } }
</style>