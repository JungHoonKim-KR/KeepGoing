<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { analyzeDiet } from "../api/diet/dietApi"; // 1. API 함수 임포트
import Footer from "../components/utils/Footer.vue";
const router = useRouter();

// ----------------------------------------------------
// 1. 상태 관리
// ----------------------------------------------------
const isLoading = ref(false); // 초기엔 로딩 아님 (클릭 대기)
const isAnalyzing = ref(false); // 분석 중 상태
const analysisData = ref(null); // 결과 데이터
const bootLogs = ref([]); // 터미널 로그

// 임시 사용자 정보 (나중에 Pinia나 로그인 정보에서 가져오세요)
const MEMBER_ID = 1;
const TODAY_DATE = new Date().toISOString().split("T")[0]; // "2024-05-22"

// ----------------------------------------------------
// 2. 랭크 시스템 (백엔드에서 안 줄 경우 대비용)
// ----------------------------------------------------
const getRankColor = (score) => {
  if (score >= 90) return "#ffd700"; // Gold
  if (score >= 80) return "#00e5ff"; // Cyan
  if (score >= 70) return "#00ff00"; // Green
  if (score >= 50) return "#ffaa00"; // Orange
  return "#ff0055"; // Red
};

// ----------------------------------------------------
// 3. 서버 통신 및 데이터 매핑
// ----------------------------------------------------
const fetchAnalysis = async () => {
  if (isAnalyzing.value) return; // 중복 클릭 방지

  // 상태 변경
  isAnalyzing.value = true;
  isLoading.value = true;
  bootLogs.value = []; // 로그 초기화

  // 부팅 로그 애니메이션 실행
  runBootSequence();

  try {
    // [핵심] 실제 서버 요청
    const data = await analyzeDiet(MEMBER_ID, TODAY_DATE);

    // 백엔드 데이터 -> 프론트엔드 포맷으로 매핑
    analysisData.value = {
      overallScore: data.score, // 점수
      rank: data.rank, // 랭크 (S, A, B...)
      title: data.dailyTitle, // 칭호 (근육 몬스터)

      // 인사이트 (아이콘, 설명)
      insights: data.insights.map((item, index) => ({
        id: index,
        type: item.type, // positive, warning...
        iconType: item.iconType, // sword, skull...
        title: item.title,
        description: item.description,
      })),

      recommendation: data.oneLineSummary, // 한줄평

      // 내일 식단 퀘스트 (recommendations -> questItems)
      questItems: data.recommendations,
    };

    // 로딩 종료 (로그 애니메이션 끝날 때쯤)
    setTimeout(() => {
      isLoading.value = false;
      isAnalyzing.value = false;
    }, 2500); // 2.5초 정도 연출 시간 확보
  } catch (error) {
    alert("서버 연결에 실패했습니다. 백엔드가 켜져있는지 확인해주세요.");
    isLoading.value = false;
    isAnalyzing.value = false;
  }
};

// 부팅 로그 애니메이션
const runBootSequence = () => {
  const logs = [
    "INITIALIZING SYSTEM...",
    "CONNECTING TO NEURAL NET...",
    "SCANNING BIOMETRICS...",
    "DECRYPTING FOOD LOGS...",
    "CALCULATING POWER LEVEL...",
    "ACCESS GRANTED.",
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
  // 퀘스트 플랜(내일 식단) 페이지로 이동하거나 모달 띄우기
  // router.push("/ai-analysis/diet-plan");
  alert(
    "내일의 퀘스트: \n" +
      analysisData.value.questItems
        .map((q) => `- ${q.menu}: ${q.reason}`)
        .join("\n")
  );
};
</script>
<template>
  <div class="ai-view retro-theme">
    <div class="scanlines"></div>

    <div class="content-wrapper">
      <div class="retro-header">
        <div class="system-status">
          <span class="status-light blink"></span>
          SYSTEM_ONLINE
        </div>
        <h1 class="page-title">MAINFRAME ANALYSIS</h1>
      </div>

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
            <div class="card-deco tl"></div>
            <div class="card-deco tr"></div>
            <div class="card-deco bl"></div>
            <div class="card-deco br"></div>

            <div
              class="rank-badge"
              :style="{
                color: getRankColor(analysisData.overallScore),
                borderColor: getRankColor(analysisData.overallScore),
                boxShadow: `4px 4px 0 ${getRankColor(
                  analysisData.overallScore
                )}33`,
              }"
            >
              RANK {{ analysisData.rank || getRank(analysisData.overallScore) }}
            </div>

            <div class="score-row">
              <div class="score-label">POWER LEVEL</div>
              <div
                class="score-val"
                :style="{ color: getRankColor(analysisData.overallScore) }"
              >
                {{ analysisData.overallScore }} <span class="max">/ 100</span>
              </div>
            </div>

            <div class="retro-progress">
              <div
                class="fill"
                :style="{
                  width: `${analysisData.overallScore}%`,
                  background: getRankColor(analysisData.overallScore),
                  boxShadow: `0 0 10px ${getRankColor(
                    analysisData.overallScore
                  )}`,
                }"
              ></div>
            </div>
          </div>

          <div class="insight-grid">
            <div
              v-for="(item, idx) in analysisData.insights"
              :key="item.id"
              class="insight-card pop-in"
              :class="item.type"
              :style="{ animationDelay: `${idx * 0.1}s` }"
            >
              <div class="icon-box">
                <svg
                  v-if="item.iconType === 'sword'"
                  viewBox="0 0 24 24"
                  class="animated-icon sword"
                >
                  <path d="M14.5 4l-8.5 8.5 2 2 8.5-8.5z" fill="currentColor" />
                  <path d="M4 14.5l2-2 2 2-2 2z" fill="currentColor" />
                </svg>
                <svg
                  v-if="item.iconType === 'skull'"
                  viewBox="0 0 24 24"
                  class="animated-icon skull"
                >
                  <circle cx="9" cy="9" r="2" fill="currentColor" />
                  <circle cx="15" cy="9" r="2" fill="currentColor" />
                  <path d="M8 15h8" stroke="currentColor" stroke-width="2" />
                  <path
                    d="M12 2a10 10 0 0 0-10 10c0 5.5 4.5 10 10 10s10-4.5 10-10A10 10 0 0 0 12 2zm0 18a8 8 0 1 1 0-16 8 8 0 0 1 0 16z"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  />
                </svg>
                <svg
                  v-if="item.iconType === 'scale'"
                  viewBox="0 0 24 24"
                  class="animated-icon scale"
                >
                  <path d="M12 2L2 12h20L12 2z" fill="currentColor" />
                  <rect
                    x="11"
                    y="12"
                    width="2"
                    height="10"
                    fill="currentColor"
                  />
                </svg>
                <svg
                  v-if="item.iconType === 'scroll'"
                  viewBox="0 0 24 24"
                  class="animated-icon scroll"
                >
                  <rect
                    x="4"
                    y="4"
                    width="16"
                    height="16"
                    rx="2"
                    stroke="currentColor"
                    stroke-width="2"
                    fill="none"
                  />
                  <line
                    x1="8"
                    y1="8"
                    x2="16"
                    y2="8"
                    stroke="currentColor"
                    stroke-width="2"
                  />
                  <line
                    x1="8"
                    y1="12"
                    x2="16"
                    y2="12"
                    stroke="currentColor"
                    stroke-width="2"
                  />
                </svg>
              </div>
              <div class="text-box">
                <div class="card-title">{{ item.title }}</div>
                <div class="card-desc">{{ item.description }}</div>
              </div>
            </div>
          </div>

          <div class="advice-terminal">
            <div class="terminal-header">/// ORACLE_ADVICE.TXT ///</div>
            <div class="terminal-body">
              {{ analysisData.recommendation }}
            </div>
          </div>

          <div class="action-buttons">
            <button class="retro-btn primary" @click="goToAIDietPlan">
              <span class="btn-icon">📜</span> VIEW QUEST PLAN
            </button>
          </div>
        </div>
      </div>
      <router-view></router-view>
    </div>
    <Footer />
  </div>
</template>

<style scoped>
/* 폰트: 둥근모꼴 */
@import url("https://cdn.jsdelivr.net/gh/neodgm/neodgm-webfont@latest/neodgm/style.css");

.ai-view {
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
</style>
