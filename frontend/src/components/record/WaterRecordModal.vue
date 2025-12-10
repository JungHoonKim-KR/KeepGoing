<template>
  <div class="modal-overlay" @click="handleOverlayClick">
    <div class="scanlines"></div>

    <div class="retro-modal" @click.stop>
      <div class="modal-header">
        <h2 class="modal-title blink-text">MANA RECOVERY</h2>
        <button @click="closeModal" class="close-btn pixel-btn">✕</button>
      </div>

      <div class="modal-body">
        <div class="date-display">Date: {{ formattedDate }}</div>

        <div class="potion-section">
          <div class="potion-bottle-container">
            <div class="potion-bottle">
              <div
                class="potion-liquid"
                :style="{ height: waterPercentage + '%' }"
              >
                <div class="potion-surface"></div>
                <div class="bubbles">
                  <span></span><span></span><span></span><span></span>
                </div>
              </div>
              <div class="bottle-shine"></div>
            </div>
            <div class="potion-value">
              <span class="current">{{ currentAmount.toFixed(1) }}</span>
              <span class="divider">/</span>
              <span class="max">{{ goalAmount.toFixed(1) }} L</span>
            </div>
            <div class="mp-label">MP (Hydration) {{ waterPercentage }}%</div>
          </div>
        </div>

        <div class="inventory-section">
          <h3 class="pixel-subtitle">SELECT ITEM</h3>
          <div class="item-grid">
            <button
              @click="addWater(0.1)"
              class="item-slot"
              title="Small Potion"
            >
              <div class="item-icon">💧</div>
              <div class="item-name">Small<br />+100</div>
            </button>
            <button
              @click="addWater(0.2)"
              class="item-slot"
              title="Medium Potion"
            >
              <div class="item-icon">🧪</div>
              <div class="item-name">Medium<br />+200</div>
            </button>
            <button
              @click="addWater(0.3)"
              class="item-slot"
              title="Large Potion"
            >
              <div class="item-icon">🏺</div>
              <div class="item-name">Large<br />+300</div>
            </button>
            <button @click="addWater(0.5)" class="item-slot" title="Elixir">
              <div class="item-icon">💎</div>
              <div class="item-name">Elixir<br />+500</div>
            </button>
          </div>
        </div>

        <div class="slider-section">
          <label>FINE TUNE AMOUNT</label>
          <input
            v-model="sliderValue"
            type="range"
            min="0"
            max="50"
            step="1"
            class="retro-slider"
            @input="updateFromSlider"
          />
        </div>

        <div class="log-section">
          <h3 class="pixel-subtitle">SYSTEM LOG</h3>
          <div class="log-console">
            <div v-if="todayRecords.length === 0" class="log-line empty">
              > No records found...
            </div>
            <div
              v-for="(record, index) in todayRecords"
              :key="index"
              class="log-line"
            >
              <span class="time">[{{ record.time }}]</span>
              <span class="msg">Recovered {{ record.amount }}L MP</span>
              <button @click="removeRecord(index)" class="delete-x">[x]</button>
            </div>
          </div>
        </div>

        <div class="action-footer">
          <button @click="saveWater" class="retro-btn save-btn">
            SAVE GAME
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
const emit = defineEmits(["close"]);

// Data
const currentAmount = ref(0.0);
const goalAmount = ref(2.0);
const sliderValue = ref(15);const config = useConfigStore();

const MEMBER_ID = config.MEMBER_ID;
const API_ENDPOINT = config.API_ENDPOINT;
const formattedDate = computed(() => config.currentDate); 
const getCurrentDateForAPI = config.getCurrentDateForAPI; // 함수이므로 그대로 사용합니다.
const todayRecords = ref([

]);

// Computed
const waterPercentage = computed(() => {
  const percentage = (currentAmount.value / goalAmount.value) * 100;
  return Math.min(Math.round(percentage), 100);
});

// 🔊 8-bit 사운드 효과
const playSound = (type) => {
  const AudioContext = window.AudioContext || window.webkitAudioContext;
  if (!AudioContext) return;

  const ctx = new AudioContext();
  const osc = ctx.createOscillator();
  const gain = ctx.createGain();

  osc.connect(gain);
  gain.connect(ctx.destination);

  const now = ctx.currentTime;

  if (type === "glug") {
    // 물 마시는 소리
    osc.type = "triangle";
    osc.frequency.setValueAtTime(400, now);
    osc.frequency.linearRampToValueAtTime(200, now + 0.15);
    gain.gain.setValueAtTime(0.1, now);
    gain.gain.linearRampToValueAtTime(0, now + 0.15);
    osc.start(now);
    osc.stop(now + 0.15);
  } else if (type === "save") {
    // 저장 성공 소리
    osc.type = "square";
    osc.frequency.setValueAtTime(523.25, now); // C
    osc.frequency.setValueAtTime(659.25, now + 0.1); // E
    osc.frequency.setValueAtTime(783.99, now + 0.2); // G
    gain.gain.setValueAtTime(0.05, now);
    gain.gain.linearRampToValueAtTime(0, now + 0.4);
    osc.start(now);
    osc.stop(now + 0.4);
  }
};

// Methods
const addWater = (amount) => {
  playSound("glug");
  currentAmount.value = Math.min(currentAmount.value + amount, 5.0);
  sliderValue.value = Math.round(currentAmount.value * 10);

  const now = dayjs().format("HH:mm");
  todayRecords.value.unshift({
    time: now,
    amount: amount.toFixed(1),
  });
};

const updateFromSlider = () => {
  currentAmount.value = sliderValue.value / 10;
};

const removeRecord = (index) => {
  const removedAmount = parseFloat(todayRecords.value[index].amount);
  currentAmount.value = Math.max(currentAmount.value - removedAmount, 0);
  sliderValue.value = Math.round(currentAmount.value * 10);
  todayRecords.value.splice(index, 1);
};

const closeModal = () => {
  emit("close");
};

const handleOverlayClick = (e) => {
  if (e.target === e.currentTarget) {
    closeModal();
  }
};

const saveWater = async () => {
  playSound("save");
  console.log("SAVE GAME :: Water Amount:", currentAmount.value, "L");

  const hydrationData = {
    memberId: MEMBER_ID,
    waterAmount: currentAmount.value,
  };

  try {
    const response = await fetch(`${API_ENDPOINT}/diets/hydration`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(hydrationData),
    });

    if (!response.ok) {
      console.error(`Error Status: ${response.status}`);
      throw new Error("Save Failed");
    }
    // 성공 시 딜레이를 주어 소리 듣게 함
    setTimeout(() => closeModal(), 300);
  } catch (error) {
    console.error("Save Error:", error);
    // 에러나도 일단 닫거나 사용자 알림 (여기선 닫음)
    closeModal();
  }
};

// Lifecycle
onMounted(() => {
  document.body.style.overflow = "hidden";



});
onUnmounted(() => {
  document.body.style.overflow = "";
});
</script>

<style scoped>
/* 폰트 로드 (이미 Home에 있다면 생략 가능) */
@import url("https://cdn.jsdelivr.net/gh/neodgm/neodgm-webfont@latest/neodgm/style.css");

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  font-family: "NeoDunggeunmo", monospace;
  overflow-y: auto;
  padding: 10px;
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

/* 모달 본체 */
.retro-modal {
  background: #000022; /* 심해/우주 색상 */
  width: 95%; /* 모바일에서 95%로 확장 */
  max-width: 400px;
  border: 4px solid #fff;
  box-shadow: 0 0 20px rgba(0, 229, 255, 0.4), inset 0 0 20px rgba(0, 0, 0, 0.5);
  position: relative;
  display: flex;
  flex-direction: column;
  animation: popIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  /* 모바일에서 모달이 화면 높이를 넘어가면 스크롤 가능하게 */
  min-height: min-content;
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

/* 헤더 */
.modal-header {
  background: #fff;
  padding: 0.5rem 1rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-title {
  margin: 0;
  font-size: 1.2rem;
  color: #000;
  letter-spacing: -1px;
}
.blink-text {
  animation: blink 2s infinite;
}
@keyframes blink {
  50% {
    opacity: 0.5;
  }
}

.close-btn {
  background: #000;
  color: #fff;
  border: none;
  font-size: 1.2rem;
  width: 32px;
  height: 32px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-body {
  padding: 1rem;
  color: #fff;
  /* 모바일에서 세로 공간을 위해 패딩 조절 */
  padding-top: 0.5rem;
}

.date-display {
  text-align: right;
  font-size: 0.8rem;
  color: #00e5ff;
  margin-bottom: 0.5rem; /* 마진 감소 */
  font-family: monospace;
}

/* 포션 디스플레이 */
.potion-section {
  display: flex;
  justify-content: center;
  margin-bottom: 1rem; /* 마진 감소 */
}

.potion-bottle-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  /* 작은 화면에서도 병 크기 유지 */
  transform: scale(0.95);
}

.potion-bottle {
  width: 100px;
  height: 140px;
  border: 4px solid #fff;
  border-radius: 40px 40px 10px 10px;
  position: relative;
  background: rgba(255, 255, 255, 0.1);
  overflow: hidden;
  margin-bottom: 0.5rem;
}

.potion-liquid {
  position: absolute;
  bottom: 0;
  width: 100%;
  background: #00e5ff;
  box-shadow: 0 0 15px #00e5ff;
  transition: height 0.3s ease-out;
}

.potion-surface {
  width: 100%;
  height: 5px;
  background: #fff;
}

/* 기포 애니메이션 */
.bubbles span {
  position: absolute;
  bottom: -10px;
  width: 4px;
  height: 4px;
  background: #fff;
  animation: bubbleUp 2s infinite;
}
.bubbles span:nth-child(1) {
  left: 20%;
  animation-delay: 0s;
}
.bubbles span:nth-child(2) {
  left: 50%;
  animation-delay: 0.5s;
}
.bubbles span:nth-child(3) {
  left: 80%;
  animation-delay: 1.2s;
}

@keyframes bubbleUp {
  0% {
    transform: translateY(0);
    opacity: 1;
  }
  100% {
    transform: translateY(-100px);
    opacity: 0;
  }
}

.potion-value {
  font-size: 1.5rem;
  text-shadow: 2px 2px #000;
}
.potion-value .current {
  color: #00e5ff;
  font-weight: bold;
}
.potion-value .max {
  font-size: 1rem;
  color: #888;
}
.mp-label {
  font-size: 0.8rem;
  color: #00e5ff;
  margin-top: 5px;
}

/* 아이템 그리드 */
.pixel-subtitle {
  font-size: 0.9rem;
  border-bottom: 2px solid #333;
  margin-bottom: 0.5rem;
  color: #ffd700; /* Gold */
}

.inventory-section {
  margin-bottom: 1rem; /* 마진 감소 */
}

.item-grid {
  /* 4열 대신 flex-wrap으로 유연하게 처리 */
  display: flex;
  flex-wrap: wrap; 
  justify-content: space-between;
  gap: 8px;
}

.item-slot {
  /* 4개 대신 2개가 한 줄에 오도록 48% 설정 */
  flex-basis: calc(50% - 4px); 
  background: #222;
  border: 2px solid #555;
  color: #fff;
  padding: 8px 4px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.item-slot:active {
  border-color: #00e5ff;
  background: #333;
  transform: scale(0.95);
}

.item-icon {
  font-size: 1.5rem;
}
.item-name {
  font-size: 0.6rem;
  text-align: center;
  line-height: 1.2;
  font-family: monospace;
}

/* 슬라이더 */
.slider-section {
  margin-bottom: 1rem; /* 마진 감소 */
}
.slider-section label {
  font-size: 0.7rem;
  color: #888;
  display: block;
  margin-bottom: 5px;
}

.retro-slider {
  width: 100%;
  height: 12px;
  -webkit-appearance: none;
  background: #333;
  border: 2px solid #fff;
  outline: none;
}
.retro-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 20px;
  height: 20px;
  background: #00e5ff;
  border: 2px solid #fff;
  cursor: pointer;
  box-shadow: 2px 2px 0 #000;
}

/* 로그 (System Log) */
.log-section {
  margin-bottom: 1rem;
}
.log-console {
  background: rgba(0, 0, 0, 0.5);
  border: 2px solid #333;
  /* 높이 축소하여 공간 확보 */
  height: 80px; 
  overflow-y: auto;
  padding: 5px;
  font-size: 0.75rem;
  font-family: monospace;
}
.log-line {
  border-bottom: 1px dashed #333;
  padding: 4px 0;
  display: flex;
  align-items: center;
}
.log-line .time {
  color: #ffd700;
  margin-right: 5px;
}
.log-line .msg {
  flex: 1;
  color: #ccc;
  /* 작은 화면에서 텍스트가 잘리지 않도록 */
  word-break: break-all; 
}
.delete-x {
  background: none;
  border: none;
  color: #ff0055;
  cursor: pointer;
  font-family: monospace;
}
.log-line.empty {
  color: #555;
  justify-content: center;
  /* 높이 축소에 맞춰 패딩 조절 */
  padding-top: 15px; 
  border: none;
}

/* 버튼 */
.action-footer {
  text-align: center;
  padding-top: 0.5rem; /* 버튼 위 공간 추가 */
}
.retro-btn {
  background: #00e5ff;
  color: #000;
  border: 2px solid #fff;
  padding: 10px 30px;
  font-size: 1rem;
  font-family: "NeoDunggeunmo", monospace;
  cursor: pointer;
  box-shadow: 4px 4px 0 #000;
  transition: transform 0.1s;
}
.retro-btn:active {
  transform: translate(4px, 4px);
  box-shadow: none;
}

/* 450px 이하 화면을 위한 미디어 쿼리 */
@media (max-width: 450px) {
    .retro-modal {
        /* 모바일 환경에서 모달의 높이가 화면을 초과하는 경우를 대비하여 padding-bottom 제거 */
        padding-bottom: 0; 
        /* 모바일에서 모달이 화면의 중앙이 아닌 상단에서 시작하도록 조정 */
        margin: 10px auto; 
    }
    .modal-overlay {
        align-items: flex-start; /* 모달을 상단에 정렬 */
    }
}
</style>
