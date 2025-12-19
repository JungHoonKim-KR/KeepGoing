<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";
// API 함수 경로가 맞는지 확인해주세요
import { analyzeDiet } from "../api/diet/dietApi"; 
import Footer from "../components/utils/Footer.vue";

const router = useRouter();

// ----------------------------------------------------
// 1. 상태 관리 변수들
// ----------------------------------------------------
const isLoading = ref(false);     // 로딩 상태
const isAnalyzing = ref(false);   // 분석 중 상태 (중복 방지)
const analysisData = ref(null);   // 분석 결과 데이터
const bootLogs = ref([]);         // 터미널 로그 배열

// [New] 모달 & 아이템 관련 상태
const showLootModal = ref(false); // 모달 표시 여부
const selectedKeywords = ref([]); // 사용자가 선택한 키워드

// 임시 사용자 정보
const MEMBER_ID = 1;
const TODAY_DATE = new Date().toISOString().split("T")[0];

// ----------------------------------------------------
// 2. 유틸리티 함수 (랭크 색상 등)
// ----------------------------------------------------
const getRankColor = (score) => {
  if (score >= 90) return "#ffd700"; // Gold
  if (score >= 80) return "#00e5ff"; // Cyan
  if (score >= 70) return "#00ff00"; // Green
  if (score >= 50) return "#ffaa00"; // Orange
  return "#ff0055"; // Red
};

// ----------------------------------------------------
// 3. 모달 관련 로직 (New)
// ----------------------------------------------------
const openLootModal = () => {
  showLootModal.value = true;
};

const closeLootModal = () => {
  showLootModal.value = false;
  // 필요하다면 여기서 selectedKeywords.value를 백엔드로 전송
  console.log("선택된 키워드:", selectedKeywords.value);
};

const toggleKeyword = (keywordName) => {
  if (selectedKeywords.value.includes(keywordName)) {
    selectedKeywords.value = selectedKeywords.value.filter(k => k !== keywordName);
  } else {
    selectedKeywords.value.push(keywordName);
  }
};

// ----------------------------------------------------
// 4. 서버 통신 및 데이터 처리
// ----------------------------------------------------
const fetchAnalysis = async () => {
  if (isAnalyzing.value) return; 

  isAnalyzing.value = true;
  isLoading.value = true;
  bootLogs.value = [];
  
  // 부팅 로그 시작
  runBootSequence();

  try {
    // API 호출
    const data = await analyzeDiet(MEMBER_ID, TODAY_DATE);

    // 데이터 매핑
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

      // [New] 키워드 데이터 (백엔드에 없으면 가짜 데이터 사용)
      miningKeywords: data.keywords || [
         { name: "닭가슴살", rarity: "COMMON" },
         { name: "현미밥", rarity: "COMMON" },
         { name: "아보카도", rarity: "RARE" },
         { name: "스테이크", rarity: "EPIC" },
         { name: "프로틴", rarity: "LEGEND" },
         { name: "사과", rarity: "COMMON" }
      ]
    };

    // 연출을 위한 딜레이 후 로딩 종료
    setTimeout(() => {
      isLoading.value = false;
      isAnalyzing.value = false;
    }, 2500);

  } catch (error) {
    alert("서버 연결 실패: 백엔드 상태를 확인하세요.");
    isLoading.value = false;
    isAnalyzing.value = false;
  }
};

// 부팅 로그 애니메이션
const runBootSequence = () => {
  const logs = [
    "INITIALIZING SYSTEM...", "CONNECTING TO NEURAL NET...",
    "SCANNING BIOMETRICS...", "DECRYPTING FOOD LOGS...",
    "CALCULATING POWER LEVEL...", "ACCESS GRANTED."
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

const goToAIDietPlan = () => {
  alert("퀘스트 플랜 페이지로 이동합니다 (구현 필요)");
  // router.push("/ai-analysis/diet-plan");
};
</script>
<template>
  <div class="ai-view retro-theme">
    <div class="scanlines"></div>

    <div class="content-wrapper">
      
      <div v-if="isLoading" class="loading-terminal">
        <div class="terminal-screen">
          <div v-for="(log, index) in bootLogs" :key="index" class="log-line">
            > {{ log }}
          </div>
          <div class="cursor-line">> <span class="blink-cursor">_</span></div>
        </div>
        <div class="loading-bar-container">
          <div class="loading-bar"></div>
        </div>
      </div>

      <div v-else class="dashboard-container">
        
        <div class="ai-avatar-section clickable" @click="fetchAnalysis" v-if="!analysisData">
          <div class="cyber-eye-container">
            <div class="eye-ring" :class="{ 'fast-spin': isAnalyzing }"></div>
            <div class="eye-iris">
              <div class="eye-pupil"></div>
              <div class="eye-glint"></div>
            </div>
            <div class="scanning-beam"></div>
          </div>
          <div class="ai-message-box">
            <p v-if="!analysisData" class="blink-text">
              "시스템 대기 중... [터치하여 분석 시작]"
            </p>
            <p v-else class="typing-effect">
              "분석 완료. 랭크 [{{ analysisData.rank }}] 달성."
            </p>
          </div>
        </div>

        <div v-if="analysisData" class="result-section pop-in">
          
          <div class="power-card">
            <div class="card-deco tl"></div><div class="card-deco tr"></div>
            <div class="card-deco bl"></div><div class="card-deco br"></div>

            <div class="rank-badge" :style="{ color: getRankColor(analysisData.overallScore), borderColor: getRankColor(analysisData.overallScore) }">
              RANK {{ analysisData.rank }}
            </div>

            <div class="score-row">
              <div class="score-label">POWER LEVEL</div>
              <div class="score-val" :style="{ color: getRankColor(analysisData.overallScore) }">
                {{ analysisData.overallScore }} <span class="max">/ 100</span>
              </div>
            </div>
            <div class="retro-progress">
              <div class="fill" :style="{ width: `${analysisData.overallScore}%`, background: getRankColor(analysisData.overallScore) }"></div>
            </div>
          </div>

          <div class="insight-grid">
            <div v-for="(item, idx) in analysisData.insights" :key="item.id" class="insight-card pop-in" :class="item.type" :style="{ animationDelay: `${idx * 0.1}s` }">
              <div class="icon-box">
                <span v-if="item.iconType === 'sword'">⚔️</span>
                <span v-else-if="item.iconType === 'skull'">💀</span>
                <span v-else-if="item.iconType === 'scale'">⚖️</span>
                <span v-else>📜</span>
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
            <button class="retro-btn primary" @click="goToAIDietPlan">
              <span class="btn-icon">📜</span> VIEW QUEST
            </button>
            <button class="retro-btn secondary" @click="openLootModal">
              <span class="btn-icon">⛏️</span> LOOT BOX
            </button>
          </div>
        </div>
      </div>
      
      <router-view></router-view>
    </div>

    <div v-if="showLootModal" class="loot-modal-overlay">
      <div class="loot-modal-content">
        <h2 class="loot-title">/// ITEM_DROP_DETECTED ///</h2>
        <p class="loot-desc">오늘 식단에서 발견된 키워드입니다.<br>스와이프하여 확인하세요.</p>
        
        <div class="card-scroll-container">
          <div 
            v-for="(item, idx) in analysisData.miningKeywords" 
            :key="idx"
            class="loot-card"
            :class="{ 
              'selected': selectedKeywords.includes(item.name),
              'rare': item.rarity === 'RARE',
              'epic': item.rarity === 'EPIC',
              'legend': item.rarity === 'LEGEND'
            }"
            @click="toggleKeyword(item.name)"
          >
            <div class="card-header">{{ item.rarity || 'COMMON' }}</div>
            <div class="card-icon">🍖</div> 
            <div class="card-name">{{ item.name }}</div>
            <div class="card-check" v-if="selectedKeywords.includes(item.name)">V</div>
          </div>
        </div>

        <button class="retro-btn primary full-width" @click="closeLootModal">
          CONFIRM SELECTION
        </button>
      </div>
    </div>
    <Footer />
  </div>
</template>

<style scoped>
/* 폰트: 둥근모꼴 */
@import url("https://cdn.jsdelivr.net/gh/neodgm/neodgm-webfont@latest/neodgm/style.css");

.ai-view {
  display: flex;
  align-items: center;
  min-height: 100vh;
  background-color: #050510;
  color: #e0e0e0;
  font-family: "NeoDunggeunmo", monospace;
  padding-bottom: 100px;
  overflow-x: hidden;
}

/* 스캔라인 */
.scanlines {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  background: linear-gradient(rgba(18, 16, 16, 0) 50%, rgba(0, 0, 0, 0.25) 50%),
    linear-gradient(
      90deg,
      rgba(255, 0, 0, 0.06),
      rgba(0, 255, 0, 0.02),
      rgba(0, 0, 255, 0.06)
    );
  background-size: 100% 4px, 6px 100%;
  z-index: 99;
}

.content-wrapper {
  max-width: 480px;
  margin: 0 auto;
  padding: 1rem;
  position: relative;
  z-index: 100;
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
}
.status-light {
  width: 8px;
  height: 8px;
  background: #00ff00;
  border-radius: 50%;
  box-shadow: 0 0 5px #00ff00;
}
.blink {
  animation: blink 1s infinite;
}
.page-title {
  font-size: 1.2rem;
  color: #fff;
  text-shadow: 2px 2px #000;
  margin: 0;
}

/* 1. 로딩 터미널 */
.loading-terminal {
  background: #000;
  border: 2px solid #00ff00;
  padding: 1rem;
  border-radius: 5px;
  min-height: 300px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.terminal-screen {
  color: #00ff00;
  font-size: 0.9rem;
  line-height: 1.5;
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
  width: 0%;
  animation: loadBar 2.5s ease-out forwards;
}
@keyframes loadBar {
  to {
    width: 100%;
  }
}

/* 2. 대시보드 - AI 아바타 (눈) */
.ai-avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 2rem;
}
.cyber-eye-container {
  width: 100px;
  height: 100px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
/* 눈 테두리 */
.eye-ring {
  width: 100%;
  height: 100%;
  border: 4px solid #00e5ff;
  border-radius: 50%;
  border-left-color: transparent;
  border-right-color: transparent;
  animation: spin 4s linear infinite;
  box-shadow: 0 0 15px #00e5ff;
}
/* 눈동자 */
.eye-iris {
  width: 60px;
  height: 60px;
  background: rgba(0, 229, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  border: 2px solid #00e5ff;
  animation: pulseEye 3s infinite;
}
.eye-pupil {
  width: 20px;
  height: 20px;
  background: #fff;
  border-radius: 50%;
  box-shadow: 0 0 10px #00e5ff;
  animation: lookAround 5s infinite;
}
.scanning-beam {
  position: absolute;
  width: 100%;
  height: 2px;
  background: #00ff00;
  top: 0;
  animation: scanDown 2s linear infinite;
  opacity: 0.5;
  box-shadow: 0 0 5px #00ff00;
}

/* AI 메시지 */
.ai-message-box {
  margin-top: 1rem;
  background: rgba(0, 229, 255, 0.1);
  border: 1px solid #00e5ff;
  padding: 0.5rem 1rem;
  border-radius: 15px 15px 15px 0;
  color: #00e5ff;
  font-size: 0.8rem;
}

/* 파워 카드 */
.power-card {
  background: #111;
  border: 2px solid #333;
  padding: 1.5rem;
  position: relative;
  margin-bottom: 1.5rem;
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.5);
}
.card-deco {
  position: absolute;
  width: 10px;
  height: 10px;
  border: 2px solid #fff;
}
.tl {
  top: -2px;
  left: -2px;
  border-right: 0;
  border-bottom: 0;
}
.tr {
  top: -2px;
  right: -2px;
  border-left: 0;
  border-bottom: 0;
}
.bl {
  bottom: -2px;
  left: -2px;
  border-right: 0;
  border-top: 0;
}
.br {
  bottom: -2px;
  right: -2px;
  border-left: 0;
  border-top: 0;
}

.rank-badge {
  position: absolute;
  top: -15px;
  right: 10px;
  background: #000;
  border: 2px solid;
  padding: 2px 10px;
  font-weight: bold;
  font-size: 1.2rem;
  transform: rotate(5deg);
  box-shadow: 4px 4px 0 rgba(0, 0, 0, 0.5);
}

.score-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 0.5rem;
}
.score-label {
  color: #888;
  font-size: 0.8rem;
}
.score-val {
  font-size: 2.5rem;
  font-weight: bold;
  line-height: 1;
  text-shadow: 0 0 10px currentColor;
}
.score-val .max {
  font-size: 1rem;
  color: #555;
}

.retro-progress {
  height: 12px;
  background: #222;
  border: 1px solid #555;
  padding: 1px;
}
.retro-progress .fill {
  height: 100%;
  transition: width 1s ease-out;
  box-shadow: 0 0 10px currentColor;
}

/* 인사이트 그리드 */
.insight-grid {
  display: grid;
  gap: 1rem;
  margin-bottom: 1.5rem;
}
.insight-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid #333;
  padding: 1rem;
  display: flex;
  gap: 1rem;
  align-items: flex-start;
  transition: transform 0.2s;
  backdrop-filter: blur(5px);
}
.insight-card:hover {
  transform: translateX(5px);
  background: rgba(255, 255, 255, 0.08);
}

/* 카드 타입별 색상 */
.insight-card.positive {
  border-left: 4px solid #00ff00;
}
.insight-card.positive .icon-box {
  color: #00ff00;
}

.insight-card.warning {
  border-left: 4px solid #ff0055;
}
.insight-card.warning .icon-box {
  color: #ff0055;
}

.insight-card.suggestion {
  border-left: 4px solid #00e5ff;
}
.insight-card.suggestion .icon-box {
  color: #00e5ff;
}

/* 움직이는 아이콘 */
.icon-box {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
}
.animated-icon {
  width: 100%;
  height: 100%;
}

/* 아이콘 애니메이션 정의 */
.sword {
  animation: swing 2s infinite ease-in-out;
  transform-origin: bottom left;
}
.skull {
  animation: shake 0.5s infinite;
}
.scale {
  animation: balance 3s infinite ease-in-out;
  transform-origin: center;
}
.scroll {
  animation: float 3s infinite ease-in-out;
}

.text-box {
  flex: 1;
}
.card-title {
  font-size: 0.9rem;
  font-weight: bold;
  margin-bottom: 4px;
  color: #fff;
}
.card-desc {
  font-size: 0.8rem;
  color: #aaa;
  line-height: 1.3;
}

/* 어드바이스 터미널 */
.advice-terminal {
  background: #000;
  border: 1px solid #666;
  margin-bottom: 1.5rem;
}
.terminal-header {
  background: #333;
  color: #fff;
  padding: 5px 10px;
  font-size: 0.7rem;
  border-bottom: 1px solid #666;
}
.terminal-body {
  padding: 1rem;
  color: #ffd700;
  font-size: 0.85rem;
  line-height: 1.4;
}

/* 버튼 */
.action-buttons {
  display: flex;
  gap: 10px;
}
.retro-btn {
  flex: 1;
  padding: 12px;
  border: 2px solid #fff;
  font-family: inherit;
  font-size: 0.9rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: 4px 4px 0 #000;
  transition: transform 0.1s;
}
.retro-btn:active {
  transform: translate(4px, 4px);
  box-shadow: none;
}
.retro-btn.primary {
  background: #00e5ff;
  color: #000;
}
.retro-btn.secondary {
  background: #222;
  color: #fff;
}

/* 애니메이션 키프레임 */
@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
@keyframes pulseEye {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(0.9);
  }
}
@keyframes lookAround {
  0%,
  100% {
    transform: translate(0, 0);
  }
  25% {
    transform: translate(-5px, -2px);
  }
  50% {
    transform: translate(5px, 2px);
  }
  75% {
    transform: translate(0, 5px);
  }
}
@keyframes scanDown {
  0% {
    top: 0;
    opacity: 0;
  }
  50% {
    opacity: 1;
  }
  100% {
    top: 100%;
    opacity: 0;
  }
}
@keyframes swing {
  0%,
  100% {
    transform: rotate(0deg);
  }
  50% {
    transform: rotate(15deg);
  }
}
@keyframes shake {
  0% {
    transform: translate(0, 0);
  }
  25% {
    transform: translate(1px, 1px);
  }
  75% {
    transform: translate(-1px, -1px);
  }
}
@keyframes balance {
  0%,
  100% {
    transform: rotate(0deg);
  }
  25% {
    transform: rotate(5deg);
  }
  75% {
    transform: rotate(-5deg);
  }
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
    opacity: 0;
  }
}
.pop-in {
  animation: popIn 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards;
  opacity: 0;
  transform: scale(0.9);
}
@keyframes popIn {
  to {
    opacity: 1;
    transform: scale(1);
  }
}
/* ========================================= */
/* [추가됨] 아이템 채굴 모달 및 카드 스타일 */
/* ========================================= */

/* 모달 오버레이 */
.loot-modal-overlay {
  position: fixed;
  top: 0; left: 0;
  width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.85);
  backdrop-filter: blur(5px);
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  animation: fadeIn 0.3s ease;
}

/* 모달 본문 */
.loot-modal-content {
  width: 90%;
  max-width: 420px;
  background: #111;
  border: 2px solid #00ff00;
  padding: 1.5rem;
  text-align: center;
  box-shadow: 0 0 20px rgba(0, 255, 0, 0.3);
  position: relative;
}

.loot-title {
  color: #00ff00;
  margin-bottom: 0.5rem;
  font-size: 1.2rem;
  text-shadow: 2px 2px #000;
  animation: blink 2s infinite;
}

.loot-desc {
  color: #aaa;
  font-size: 0.8rem;
  margin-bottom: 1.5rem;
}

/* 가로 스크롤 컨테이너 (핵심) */
.card-scroll-container {
  display: flex;
  overflow-x: auto; /* 가로 스크롤 */
  gap: 15px;
  padding: 10px 5px 20px 5px;
  scroll-snap-type: x mandatory; /* 카드 단위 스냅 */
  -webkit-overflow-scrolling: touch;
  margin-bottom: 1rem;
}

/* 스크롤바 디자인 */
.card-scroll-container::-webkit-scrollbar { height: 6px; }
.card-scroll-container::-webkit-scrollbar-thumb { background: #333; border-radius: 4px; }

/* 개별 아이템 카드 */
.loot-card {
  flex: 0 0 130px; /* 카드 고정 너비 */
  height: 170px;
  background: #222;
  border: 2px solid #555;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: space-between;
  padding: 1rem;
  scroll-snap-align: center; /* 중앙 정렬 스냅 */
  position: relative;
  transition: all 0.2s;
  cursor: pointer;
}

/* 선택된 상태 효과 */
.loot-card.selected {
  border-color: #00ff00;
  background: #002200;
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 255, 0, 0.4);
}
.card-check {
  position: absolute;
  top: 5px; right: 5px;
  color: #00ff00;
  font-weight: bold;
}

/* 등급별 색상 처리 */
.loot-card.rare { border-color: #00e5ff; }
.loot-card.epic { border-color: #d000ff; }
.loot-card.legend { border-color: #ffd700; box-shadow: 0 0 10px #ffd700; }

.card-header {
  font-size: 0.7rem;
  color: #888;
  background: #000;
  padding: 2px 6px;
  border-radius: 4px;
}
.card-icon {
  font-size: 2.5rem;
  filter: drop-shadow(0 0 5px rgba(255,255,255,0.3));
}
.card-name {
  font-size: 0.9rem;
  color: #fff;
  font-weight: bold;
}

.full-width {
  width: 100%;
}

@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.9); }
  to { opacity: 1; transform: scale(1); }
}
</style>
