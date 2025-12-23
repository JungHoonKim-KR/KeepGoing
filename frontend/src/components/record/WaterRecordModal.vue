<template>
  <div class="modal-overlay" @click="handleOverlayClick">
    <div class="scanlines"></div>

    <div class="retro-modal" @click.stop>
      <div class="modal-header">
        <h2 class="modal-title blink-text">MANA RECOVERY</h2>
        <button @click="closeModal" class="close-btn pixel-btn">✕</button>
      </div>

      <div class="modal-body">
        <div class="potion-section">
          <div class="potion-bottle-container">
            <div class="potion-bottle">
              <div class="potion-liquid" :style="{ height: waterPercentage + '%' }">
                <div class="potion-surface"></div>
                <div class="bubbles"><span></span><span></span><span></span><span></span></div>
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
            <button @click="addWater(100)" class="item-slot" title="Small Potion">
              <div class="item-icon">💧</div>
              <div class="item-name">Small<br />+100</div>
            </button>
            <button @click="addWater(200)" class="item-slot" title="Medium Potion">
              <div class="item-icon">🧪</div>
              <div class="item-name">Medium<br />+200</div>
            </button>
            <button @click="addWater(300)" class="item-slot" title="Large Potion">
              <div class="item-icon">🏺</div>
              <div class="item-name">Large<br />+300</div>
            </button>
            <button @click="addWater(500)" class="item-slot" title="Elixir">
              <div class="item-icon">💎</div>
              <div class="item-name">Elixir<br />+500</div>
            </button>
          </div>
        </div>

        <div class="slider-section">
          <label>FINE TUNE AMOUNT (mL)</label>
          <input
            v-model.number="sliderValue"
            type="range"
            min="0"
            max="5000"
            step="50"
            class="retro-slider"
            @input="updateFromSlider"
          />
        </div>

        <div class="action-footer">
          <button @click="saveWater" class="retro-btn save-btn">SAVE GAME</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useAuthStore } from "@/stores/authStore";
import dayjs from "dayjs";
import { useConfigStore } from "@/stores/configStore";

const emit = defineEmits(["close", "update-water"]);

// Data
const authStore = useAuthStore();
const config = useConfigStore();
const props = defineProps({
  initialAmount: {
    type: Number,
    default: 0.0,
  },
  initialGoal: {
    type: Number,
    default: 2.0,
  },
  dateToUse: {
    type: String,
    required: true,
  },
});

const MEMBER_ID = authStore.memberId;
const API_ENDPOINT = config.API_ENDPOINT;

const todayRecords = ref([]);

// Data
const currentAmount = ref(props.initialAmount); // 내부적으로는 Liter 유지 (API 호환)
const goalAmount = ref(props.initialGoal);

// ✨ [수정 3] 초기 슬라이더 값을 mL 단위 정수로 변환 (예: 1.5L -> 1500)
const sliderValue = ref(Math.round(props.initialAmount * 1000));

// Computed
const waterPercentage = computed(() => {
  if (goalAmount.value === 0) return 0;
  const percentage = (currentAmount.value / goalAmount.value) * 100;
  return Math.min(Math.round(percentage), 100);
});

// Sound (기존 동일)
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
    osc.type = "triangle";
    osc.frequency.setValueAtTime(400, now);
    osc.frequency.linearRampToValueAtTime(200, now + 0.15);
    gain.gain.setValueAtTime(0.1, now);
    gain.gain.linearRampToValueAtTime(0, now + 0.15);
    osc.start(now);
    osc.stop(now + 0.15);
  } else if (type === "save") {
    osc.type = "square";
    osc.frequency.setValueAtTime(523.25, now);
    osc.frequency.setValueAtTime(659.25, now + 0.1);
    osc.frequency.setValueAtTime(783.99, now + 0.2);
    gain.gain.setValueAtTime(0.05, now);
    gain.gain.linearRampToValueAtTime(0, now + 0.4);
    osc.start(now);
    osc.stop(now + 0.4);
  }
};

// ✨ [수정 4] 물 추가 로직 (정수 계산 적용)
const addWater = (amountInMl) => {
  playSound("glug");

  // 1. 현재 Liter 값을 mL 정수로 변환 (소수점 오차 제거를 위해 Math.round 사용)
  let currentMl = Math.round(currentAmount.value * 1000);

  // 2. 정수끼리 더하기
  let newMl = currentMl + amountInMl;

  // 3. 최대치 제한 (5000mL = 5L)
  newMl = Math.min(newMl, 5000);

  // 4. 다시 Liter로 변환하여 저장
  currentAmount.value = newMl / 1000;

  // 5. 슬라이더 동기화
  sliderValue.value = newMl;

  // 기록용 (Liter 단위 문자열로 저장)
  const now = dayjs().format("HH:mm");
  todayRecords.value.unshift({
    time: now,
    amount: (amountInMl / 1000).toFixed(1), // 기록엔 0.1L 형태로 저장
  });
};

// ✨ [수정 5] 슬라이더 조작 (정수 -> 소수 변환)
const updateFromSlider = () => {
  // 슬라이더 값(mL)을 1000으로 나누어 Liter로 변환
  currentAmount.value = sliderValue.value / 1000;
};

// ✨ [수정 6] 기록 삭제 로직 (정수 계산 적용)
const removeRecord = (index) => {
  const removedAmountStr = todayRecords.value[index].amount; // "0.1"
  const removedMl = Math.round(parseFloat(removedAmountStr) * 1000); // 100

  let currentMl = Math.round(currentAmount.value * 1000);
  let newMl = Math.max(currentMl - removedMl, 0);

  currentAmount.value = newMl / 1000;
  sliderValue.value = newMl;

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
    waterAmount: currentAmount.value, // 최종적으로는 소수점 형태(Liter)로 전송
  };

  try {
    const response = await fetch(`${API_ENDPOINT}/diets/hydration`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(hydrationData),
    });

    if (!response.ok) {
      throw new Error("Save Failed");
    }
    emit("update-water", currentAmount.value);
    setTimeout(() => closeModal(), 300);
  } catch (error) {
    console.error("Save Error:", error);
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
/* 기존 스타일 그대로 유지 */
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

.scanlines {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none;
  background: linear-gradient(rgba(18, 16, 16, 0) 50%, rgba(0, 0, 0, 0.25) 50%),
    linear-gradient(90deg, rgba(255, 0, 0, 0.06), rgba(0, 255, 0, 0.02), rgba(0, 0, 255, 0.06));
  background-size: 100% 4px, 6px 100%;
}

.retro-modal {
  background: #000022;
  width: 95%;
  max-width: 400px;
  border: 4px solid #fff;
  box-shadow: 0 0 20px rgba(0, 229, 255, 0.4), inset 0 0 20px rgba(0, 0, 0, 0.5);
  position: relative;
  display: flex;
  flex-direction: column;
  animation: popIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
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
  padding-top: 0.5rem;
}

.potion-section {
  display: flex;
  justify-content: center;
  margin-bottom: 1rem;
}

.potion-bottle-container {
  display: flex;
  flex-direction: column;
  align-items: center;
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

.pixel-subtitle {
  font-size: 0.9rem;
  border-bottom: 2px solid #333;
  margin-bottom: 0.5rem;
  color: #ffd700;
}

.inventory-section {
  margin-bottom: 1rem;
}

.item-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 8px;
}

.item-slot {
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

.slider-section {
  margin-bottom: 1rem;
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

.action-footer {
  text-align: center;
  padding-top: 0.5rem;
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

@media (max-width: 450px) {
  .retro-modal {
    padding-bottom: 0;
    margin: 10px auto;
  }
  .modal-overlay {
    align-items: flex-start;
  }
}
</style>
