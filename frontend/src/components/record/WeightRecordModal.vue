<template>
  <div class="modal-overlay" @click="handleOverlayClick">
    <div class="scanlines"></div>

    <div class="retro-modal" @click.stop>
      <div class="modal-header">
        <h2 class="modal-title blink-text">NEW RECORD</h2>
        <button @click="closeModal" class="close-btn pixel-btn">✕</button>
      </div>

      <div class="modal-body">
        <div class="date-display">DATE: {{ formattedDate }}</div>

        <div class="score-board-section">
          <div class="score-label">CURRENT SCORE (WEIGHT)</div>
          <div class="score-display">
            <input
              v-model="weightInput"
              type="number"
              step="0.1"
              class="retro-input"
              placeholder="00.0"
              @input="updateSlider"
            />
            <span class="unit">kg</span>
          </div>
        </div>

        <div class="control-pad">
          <div class="pad-label">FINE TUNE</div>
          <div class="pad-row">
            <button @click="adjustWeight(-1)" class="pad-btn red">-1.0</button>
            <button @click="adjustWeight(-0.1)" class="pad-btn red">
              -0.1
            </button>
            <div class="pad-spacer"></div>
            <button @click="adjustWeight(0.1)" class="pad-btn green">
              +0.1
            </button>
            <button @click="adjustWeight(1)" class="pad-btn green">+1.0</button>
          </div>

          <div class="slider-wrapper">
            <input
              v-model="weightSlider"
              type="range"
              min="30"
              max="150"
              step="0.1"
              class="retro-slider"
              @input="updateInput"
            />
          </div>
        </div>

        <div class="ranking-section">
          <h3 class="pixel-subtitle">RANKING HISTORY</h3>
          <div class="ranking-list">
            <div class="rank-row header">
              <span>DAY</span>
              <span>SCORE</span>
              <span>DIFF</span>
            </div>
            <div
              class="rank-row"
              v-for="(record, index) in recentRecords"
              :key="index"
            >
              <span class="rank-date">{{ record.date }}</span>
              <span class="rank-score">{{ record.weight }}kg</span>
              <span
                class="rank-diff"
                :class="record.diff < 0 ? 'bonus' : 'penalty'"
              >
                {{ record.diff > 0 ? "+" : "" }}{{ record.diff }}
              </span>
            </div>
          </div>
        </div>

        <div class="cheat-code-section">
          <label class="pixel-subtitle">MEMO / CHEAT CODE</label>
          <textarea
            v-model="memo"
            class="retro-textarea"
            placeholder="ENTER MESSAGE..."
            rows="2"
          ></textarea>
        </div>

        <div class="action-footer">
          <button @click="saveWeight" class="retro-btn save-btn">
            UPDATE RECORD
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import dayjs from "dayjs";
import { useConfigStore } from '@/stores/configStore'; // Pinia Store 경로를 정확히 확인해주세요.
const emit = defineEmits(["close", "update-weight"]);
const config = useConfigStore();
// Data
const weightInput = ref("60.0");
const weightSlider = ref(60);
const memo = ref("");
const MEMBER_ID = config.MEMBER_ID;
const API_ENDPOINT = config.API_ENDPOINT;
const formattedDate = computed(() => config.currentDate);
const getCurrentDateForAPI = config.getCurrentDateForAPI; // 함수이므로 그대로 사용합니다.
// 더미 데이터 (실제 데이터로 교체 가능)
const recentRecords = ref([

]);

// Computed

// 🔊 8-bit 사운드 효과 (모달 내부 조작용)
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
    // 버튼 조작음
    osc.type = "square";
    osc.frequency.setValueAtTime(220, now);
    gain.gain.setValueAtTime(0.05, now);
    gain.gain.exponentialRampToValueAtTime(0.01, now + 0.1);
    osc.start(now);
    osc.stop(now + 0.1);
  } else if (type === "save") {
    // 저장음 (파워업)
    osc.type = "sawtooth";
    osc.frequency.setValueAtTime(110, now);
    osc.frequency.linearRampToValueAtTime(880, now + 0.3);
    gain.gain.setValueAtTime(0.1, now);
    gain.gain.linearRampToValueAtTime(0, now + 0.3);
    osc.start(now);
    osc.stop(now + 0.3);
  }
};

// Methods
const updateSlider = () => {
  const value = parseFloat(weightInput.value) || 0;
  weightSlider.value = Math.max(30, Math.min(150, value));
};

const updateInput = () => {
  // 슬라이더 움직일 때 드르륵 소리 (선택사항)
  // playSound('blip');
  weightInput.value = parseFloat(weightSlider.value).toFixed(1);
};

const adjustWeight = (amount) => {
  playSound("blip");
  const current = parseFloat(weightInput.value) || 0;
  const newWeight = Math.max(30, Math.min(150, current + amount));
  weightInput.value = newWeight.toFixed(1);
  weightSlider.value = newWeight;
};

const closeModal = () => emit("close");

const handleOverlayClick = (e) => {
  if (e.target === e.currentTarget) closeModal();
};

const saveWeight = async () => {
  playSound("save");
  // API 호출 로직은 여기에 추가
  const weightData = {
    memberId: MEMBER_ID,
    weight: weightInput.value,
    date : formattedDate.value,
    memo: memo.value,

  };

  try {
    const response = await fetch(`${API_ENDPOINT}/api/member/weight`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(weightData),
    });
    if (!response.ok) throw new Error("Save Failed");
    emit("update-weight"); // 새로운 이벤트 발생
  } catch (error) {
    console.error("Critical Failure:", error);
    closeModal();
  }
  // 소리 들을 시간 주고 닫기
  setTimeout(() => closeModal(), 400);
};

async function fetchWeightLogs() {
    const baseURL = `${API_ENDPOINT}/api/member/weight/logs`;
    const params = new URLSearchParams({
        memberId : MEMBER_ID,
        date: getCurrentDateForAPI(), // ✅ formattedDate는 YYYY.MM.DD 형식일 수 있으므로 API 함수 사용 권장
    });
    const url = `${baseURL}?${params.toString()}`;
    
    try{
        const response = await fetch(url);
        if (!response.ok) throw new Error("Network response was not ok");
        
        const data = await response.json();
        
        // 🌟 1. 조건문 수정: 배열이 객체 안에 있고, 그 배열의 길이가 0보다 클 때만 실행
        if(data && Array.isArray(data.memberWeightResponseDtos) && data.memberWeightResponseDtos.length > 0) {
            
            // 2. 데이터 매핑 및 할당
            recentRecords.value = data.memberWeightResponseDtos.map(item =>({
                date: item.date,
                weight: item.weight,
                diff: item.diff // 템플릿에서도 diff를 사용하므로 change 대신 diff 사용 유지
            }));

            // 3. 최신 기록(index 0)으로 weightInput 및 memo 초기화
            const latestRecord = recentRecords.value[0];
            
            weightInput.value = latestRecord.weight.toFixed(1);
            weightSlider.value = latestRecord.weight;
            
            // memo는 응답 객체의 memo 필드를 사용
            memo.value = data.memo || ""; 
            
        } else {
            // 데이터가 없거나 배열이 비어있을 경우 (안전한 초기화)
            recentRecords.value = [];
            weightInput.value = "0.0"; 
            weightSlider.value = 0;
            memo.value = "";
        }

    } catch (error) {
        console.error("Error fetching weight logs:", error);
        // 실패 시 Mock 데이터로 대체하거나 빈 상태로 유지
        recentRecords.value = []; 
        weightInput.value = "0.0";
    }
}
onMounted(async () => {
  document.body.style.overflow = "hidden";
  fetchWeightLogs();
 
});
onUnmounted(() => (document.body.style.overflow = ""));
</script>

<style scoped>
/* 폰트 임포트 */
@import url("https://cdn.jsdelivr.net/gh/neodgm/neodgm-webfont@latest/neodgm/style.css");

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  /* 모바일에서 콘텐츠가 잘릴 경우 스크롤 가능하도록 flex-start와 overflow 설정 */
  align-items: flex-start;
  justify-content: center;
  z-index: 9999;
  font-family: "NeoDunggeunmo", monospace;
  overflow-y: auto;
  padding: 10px; /* 모바일에서 모달이 화면 끝에 붙지 않도록 여백 추가 */
}

/* 스캔라인 */
.scanlines {
  position: absolute;
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
}

.retro-modal {
  background: #2a0a29; /* 보라색 어두운 배경 */
  width: 95%; /* 모바일에서 더 넓게 사용 */
  max-width: 400px;
  border: 4px double #d500f9; /* 네온 퍼플 테두리 */
  box-shadow: 0 0 20px rgba(213, 0, 249, 0.5);
  display: flex;
  flex-direction: column;
  animation: slideUp 0.3s ease-out;
  color: #fff;
  /* 모바일에서 스크롤을 위해 높이 유동적으로 설정 */
  margin-top: 20px;
}

@keyframes slideUp {
  from {
    transform: translateY(50px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.modal-header {
  background: #000;
  padding: 0.8rem 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 2px solid #d500f9;
}

.modal-title {
  margin: 0;
  font-size: 1.2rem;
  color: #d500f9;
  text-shadow: 0 0 5px #d500f9;
}
.blink-text {
  animation: blink 1.5s infinite;
}
@keyframes blink {
  50% {
    opacity: 0.3;
  }
}

.close-btn {
  background: none;
  border: none;
  color: #fff;
  font-size: 1.2rem;
  cursor: pointer;
}

.modal-body {
  /* 세로 여백 감소 */
  padding: 1rem 1rem;
}

.date-display {
  text-align: right;
  font-size: 0.8rem;
  color: #d500f9;
  margin-bottom: 0.8rem; /* 마진 감소 */
}

/* 스코어 보드 (체중 입력) */
.score-board-section {
  background: #000;
  border: 2px solid #555;
  /* 패딩 감소 */
  padding: 0.8rem;
  margin-bottom: 1rem; /* 마진 감소 */
  text-align: center;
}
.score-label {
  font-size: 0.8rem;
  color: #888;
  margin-bottom: 0.5rem;
}
.score-display {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 5px;
}

.retro-input {
  background: transparent;
  border: none;
  color: #fff;
  font-family: "NeoDunggeunmo", monospace;
  /* 모바일에서 폰트 크기 약간 감소 */
  font-size: 2.5rem;
  width: 120px; /* 너비 조절 */
  text-align: right;
  outline: none;
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
}
.unit {
  font-size: 1.5rem;
  color: #d500f9;
}

/* 컨트롤 패드 */
.control-pad {
  margin-bottom: 1rem; /* 마진 감소 */
}
.pad-label {
  font-size: 0.7rem;
  color: #aaa;
  margin-bottom: 5px;
  text-align: center;
}
.pad-row {
  display: flex;
  justify-content: space-between;
  gap: 5px;
  margin-bottom: 10px;
}
.pad-spacer {
  flex: 1;
}

.pad-btn {
  border: 2px solid #fff;
  color: #fff;
  /* 패딩 조절로 버튼 크기 유연하게 */
  padding: 8px 5px;
  /* flex-grow 추가로 남은 공간 나눠 가지게 함 (반응형 개선) */
  flex-grow: 1;
  font-family: inherit;
  font-size: 0.8rem;
  cursor: pointer;
  box-shadow: 2px 2px 0 #000;
  transition: transform 0.1s;
}
.pad-btn:active {
  transform: translate(2px, 2px);
  box-shadow: none;
}
.pad-btn.red {
  background: #ff0055;
}
.pad-btn.green {
  background: #00e5ff;
  color: #000;
}

.slider-wrapper {
  padding: 0 5px;
}
.retro-slider {
  width: 100%;
  height: 10px;
  -webkit-appearance: none;
  background: #333;
  border: 2px solid #fff;
  outline: none;
}
.retro-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 20px;
  height: 20px;
  background: #d500f9;
  border: 2px solid #fff;
  cursor: pointer;
}

/* 랭킹 리스트 */
.ranking-section {
  margin-bottom: 1rem; /* 마진 감소 */
}
.pixel-subtitle {
  font-size: 0.9rem;
  border-bottom: 2px solid #555;
  margin-bottom: 0.5rem;
  color: #ffd700;
  display: block;
}

.ranking-list {
  background: rgba(0, 0, 0, 0.3);
  padding: 5px;
  border: 1px solid #333;
  /* 작은 화면에서 스크롤바가 생기지 않도록 높이 제한 */
  max-height: 120px;
  overflow-y: auto;
}
.rank-row {
  display: flex;
  justify-content: space-between;
  padding: 4px;
  font-size: 0.8rem;
}
.rank-row.header {
  color: #888;
  border-bottom: 1px dashed #555;
  margin-bottom: 5px;
}
/* 랭킹 리스트 열 너비 확보 */
.rank-row span {
  flex-basis: 33%;
  text-align: center;
}
.rank-date {
  color: #ccc;
  text-align: left !important;
  flex-basis: 34%;
}
.rank-score {
  color: #fff;
  text-align: center !important;
}
.rank-diff {
  text-align: right !important;
  flex-basis: 32%;
}
.rank-diff.bonus {
  color: #00e5ff;
}
.rank-diff.penalty {
  color: #ff0055;
}

/* 치트키 (메모) */
.cheat-code-section {
  margin-bottom: 1rem;
}
.retro-textarea {
  width: 100%;
  background: #111;
  border: 2px solid #555;
  color: #fff;
  padding: 8px;
  font-family: inherit;
  font-size: 0.9rem;
  outline: none;
  box-sizing: border-box;
}
.retro-textarea:focus {
  border-color: #d500f9;
}

/* 저장 버튼 */
.action-footer {
  text-align: center;
  margin-top: 0.8rem; /* 마진 감소 */
}
.retro-btn {
  background: #d500f9;
  color: #fff;
  border: 2px solid #fff;
  /* 패딩 조절 */
  padding: 10px 25px;
  font-size: 1rem;
  font-family: inherit;
  cursor: pointer;
  box-shadow: 4px 4px 0 #000;
}
.retro-btn:active {
  transform: translate(4px, 4px);
  box-shadow: none;
}
</style>
