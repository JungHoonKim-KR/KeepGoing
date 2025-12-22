<template>
  <div class="modal-overlay" @click="handleOverlayClick">
    <div class="modal-container" @click.stop>
      <div class="modal-header">
        <h2 class="modal-title">체중 기록</h2>
        <button @click="closeModal" class="close-btn">✕</button>
      </div>

      <div class="modal-body">
        <!-- 체중 시각화 -->
        <div class="weight-visual-section">
          <div class="body-container">
            <div class="body-outline" :class="{ 'over-target': isOverTarget }">
              <div class="body-fill" :class="{ danger: isOverTarget }" :style="{ height: bodyFillPercentage + '%' }">
                <div class="body-surface"></div>
                <div class="weight-bubbles">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
              </div>
            </div>
          </div>

          <div class="weight-display">
            <input v-model="weightInput" type="number" step="0.1" class="weight-input" @input="updateSlider" />
            <span class="unit">kg</span>
          </div>
          <div class="weight-info">
            <span class="weight-percentage" :class="{ 'warning-text': bodyFillPercentage > 100 }">
              {{ bodyFillPercentage }}%
            </span>

            <span class="target-weight">목표: {{ targetWeight }}kg</span>
          </div>
          <div v-if="isOverTarget" class="warning-message">⚠️ 목표 체중 초과</div>
        </div>

        <!-- 간단한 조정 버튼 -->
        <div class="quick-adjust">
          <button @click="adjustWeight(-0.5)" class="adjust-btn">-0.5</button>
          <button @click="adjustWeight(-0.1)" class="adjust-btn">-0.1</button>
          <button @click="adjustWeight(0.1)" class="adjust-btn">+0.1</button>
          <button @click="adjustWeight(0.5)" class="adjust-btn">+0.5</button>
        </div>

        <!-- 슬라이더 -->
        <div class="slider-section">
          <input
            v-model="weightSlider"
            type="range"
            min="30"
            :max="sliderMax"
            step="0.1"
            class="weight-slider"
            @input="updateInput"
          />
        </div>

        <!-- 최근 기록 (간소화) -->
        <div class="history-section" v-if="recentRecords.length > 0">
          <h3>최근 기록</h3>
          <div class="history-list">
            <div v-for="(record, index) in recentRecords.slice(0, 3)" :key="index" class="history-row">
              <span class="date">{{ record.date }}</span>
              <span class="weight">{{ record.weight }}kg</span>
              <span class="diff" :class="record.diff < 0 ? 'down' : 'up'">
                {{ record.diff > 0 ? "▲" : "▼" }}{{ Math.abs(record.diff) }}
              </span>
            </div>
          </div>
        </div>

        <button @click="saveWeight" class="save-btn">저장하기</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, unref, computed, onMounted, onUnmounted } from "vue";
import { useAuthStore } from "@/stores/authStore";
import { useConfigStore } from "@/stores/configStore";

const emit = defineEmits(["close", "update-weight"]);
const config = useConfigStore();
const authStore = useAuthStore();
const props = defineProps({
  dateToUse: {
    type: String,
    required: true,
  },
});

const weightInput = ref(authStore.weight);
const weightSlider = ref(authStore.weight);
<<<<<<< HEAD
const targetWeight = ref(authStore.targetWeight); // 목표 체중 (API에서 가져올 예정)
=======
const targetWeight = ref(authStore.weight); // 목표 체중 (API에서 가져올 예정)
>>>>>>> ef69063 (목표 체중)
const MEMBER_ID = authStore.memberId;
const API_ENDPOINT = config.API_ENDPOINT;
const formattedDate = computed(() => ref(props.dateToUse));
const recentRecords = ref([]);

// 슬라이더 최대값 (목표 체중 + 30kg)
const sliderMax = computed(() => {
  return Math.max(targetWeight.value + 30, 150);
});

// 목표 체중 초과 여부
const isOverTarget = computed(() => {
  return parseFloat(weightInput.value) > targetWeight.value;
});

// 체중에 따른 채움 퍼센트 계산 (30kg = 0%, 목표체중 = 100%)
const bodyFillPercentage = computed(() => {
  const weight = parseFloat(weightInput.value) || 30;
  const target = targetWeight.value;

  // 목표 체중이 기본값(30kg)보다 작거나 같을 경우 예외 처리
  if (target <= 30) return 100;

  // 비율 계산: (현재 - 30) / (목표 - 30)
  const percentage = ((weight - 30) / (target - 30)) * 100;

  // 0% 미만으로만 떨어지지 않게 하고, 100%가 넘어도 그대로 반환 (Math.min 제거)
  return Math.max(Math.round(percentage), 0);
});

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
    osc.type = "triangle";
    osc.frequency.setValueAtTime(300, now);
    osc.frequency.linearRampToValueAtTime(150, now + 0.1);
    gain.gain.setValueAtTime(0.1, now);
    gain.gain.linearRampToValueAtTime(0, now + 0.1);
    osc.start(now);
    osc.stop(now + 0.1);
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

const updateSlider = () => {
  const value = parseFloat(weightInput.value) || 30;
  weightSlider.value = Math.max(30, Math.min(sliderMax.value, value));
};

const updateInput = () => {
  weightInput.value = parseFloat(weightSlider.value).toFixed(1);
};

const adjustWeight = (amount) => {
  playSound("blip");
  const current = parseFloat(weightInput.value) || 60;
  const newWeight = Math.max(30, Math.min(sliderMax.value, current + amount));
  weightInput.value = newWeight.toFixed(1);
  weightSlider.value = newWeight;
};

const closeModal = () => emit("close");

const handleOverlayClick = (e) => {
  if (e.target === e.currentTarget) closeModal();
};

const saveWeight = async () => {
  playSound("save");

  const weightData = {
    memberId: unref(MEMBER_ID),
    weight: parseFloat(weightInput.value),
    date: unref(formattedDate.value),
    memo: "",
  };

  try {
    const response = await fetch(`${API_ENDPOINT}/api/member/weight`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(weightData),
    });
    if (!response.ok) throw new Error("Save Failed");
    emit("update-weight", parseFloat(weightInput.value));
  } catch (error) {
    console.error("Save Failed:", error);
  }

  setTimeout(() => closeModal(), 300);
};

<<<<<<< HEAD
=======
// 목표 체중 가져오기
async function fetchTargetWeight() {
  try {
    const response = await fetch(`${API_ENDPOINT}/api/member?memberId=${MEMBER_ID}`);
    if (!response.ok) throw new Error("Failed to fetch target weight");

    const data = await response.json();
    if (data && data.targetWeight) {
      targetWeight.value = data.targetWeight;
    }
  } catch (error) {
    console.error("Error fetching target weight:", error);
    // 기본값 70kg 유지
  }
}

>>>>>>> ef69063 (목표 체중)
async function fetchWeightLogs() {
  const baseURL = `${API_ENDPOINT}/api/member/weight/logs`;
  const params = new URLSearchParams({
    memberId: MEMBER_ID,
    date: unref(formattedDate.value),
  });
  const url = `${baseURL}?${params.toString()}`;

  try {
    const response = await fetch(url);
    if (!response.ok) throw new Error("Network response was not ok");

    const data = await response.json();

    if (data && Array.isArray(data.memberWeightResponseDtos) && data.memberWeightResponseDtos.length > 0) {
      recentRecords.value = data.memberWeightResponseDtos.map((item) => ({
        date: item.date,
        weight: item.weight,
        diff: item.diff,
      }));

      const latestRecord = recentRecords.value[0];
      weightInput.value = latestRecord.weight.toFixed(1);
      weightSlider.value = latestRecord.weight;
    } else {
      weightInput.value = "60.0";
      weightSlider.value = 60;
    }
  } catch (error) {
    console.error("Error fetching weight logs:", error);
    weightInput.value = "60.0";
    weightSlider.value = 60;
  }
}

onMounted(async () => {
  document.body.style.overflow = "hidden";
  await fetchWeightLogs();
});

onUnmounted(() => (document.body.style.overflow = ""));
</script>

<style scoped>
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
  padding: 10px;
  overflow-y: auto;
}

.modal-container {
  background: #000022;
  width: 95%;
  max-width: 400px;
  border: 4px solid #fff;
  box-shadow: 0 0 20px rgba(0, 229, 255, 0.4);
  animation: popIn 0.3s ease-out;
  margin: 10px auto;
}

@keyframes popIn {
  from {
    transform: scale(0.9);
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
}

/* 체중 시각화 섹션 */
.weight-visual-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 1rem;
}

.body-container {
  display: flex;
  justify-content: center;
  margin-bottom: 0.5rem;
}

.body-outline {
  width: 120px;
  height: 160px;
  position: relative;
  background: rgba(255, 255, 255, 0.3);
  overflow: hidden;
  transition: border-color 0.3s;
  clip-path: polygon(
    /* 머리 */ 40% 0%,
    60% 0%,
    65% 5%,
    65% 12%,
    /* 오른쪽 어깨 */ 75% 15%,
    85% 20%,
    90% 25%,
    90% 35%,
    /* 오른쪽 팔 */ 95% 38%,
    95% 50%,
    90% 52%,
    85% 50%,
    /* 오른쪽 몸통 */ 80% 48%,
    78% 60%,
    75% 75%,
    72% 85%,
    /* 오른쪽 다리 */ 70% 90%,
    65% 100%,
    62% 100%,
    58% 95%,
    55% 85%,
    52% 75%,
    /* 중앙 하체 */ 50% 70%,
    48% 75%,
    45% 85%,
    42% 95%,
    38% 100%,
    35% 100%,
    /* 왼쪽 다리 */ 30% 90%,
    28% 85%,
    25% 75%,
    22% 60%,
    /* 왼쪽 몸통 */ 20% 48%,
    15% 50%,
    10% 52%,
    5% 50%,
    /* 왼쪽 팔 */ 5% 38%,
    10% 35%,
    10% 25%,
    15% 20%,
    /* 왼쪽 어깨 */ 25% 15%,
    35% 12%,
    35% 5%,
    40% 0%
  );
}

.body-outline.over-target {
  border-color: #ff0055;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%,
  100% {
    border-color: #ff0055;
    box-shadow: 0 0 10px rgba(255, 0, 85, 0.5);
  }
  50% {
    border-color: #ff5588;
    box-shadow: 0 0 20px rgba(255, 0, 85, 0.8);
  }
}

.body-fill {
  position: absolute;
  bottom: 0;
  width: 100%;
  background: linear-gradient(180deg, rgba(255, 193, 7, 0.8), rgba(255, 152, 0, 0.9));
  transition: height 0.3s ease-out, background 0.3s ease-out;
  box-shadow: 0 0 15px rgba(255, 193, 7, 0.5);
}

.body-fill.danger {
  background: linear-gradient(180deg, rgba(255, 0, 85, 0.8), rgba(255, 50, 100, 0.9));
  box-shadow: 0 0 15px rgba(255, 0, 85, 0.7);
}

.body-surface {
  width: 100%;
  height: 5px;
  background: rgba(255, 255, 255, 0.6);
}

.weight-bubbles {
  position: relative;
  width: 100%;
  height: 100%;
}

.weight-bubbles span {
  position: absolute;
  bottom: -10px;
  width: 6px;
  height: 6px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 50%;
  animation: bubbleUp 3s infinite;
}

.weight-bubbles span:nth-child(1) {
  left: 25%;
  animation-delay: 0s;
}

.weight-bubbles span:nth-child(2) {
  left: 50%;
  animation-delay: 1s;
}

.weight-bubbles span:nth-child(3) {
  left: 75%;
  animation-delay: 2s;
}

@keyframes bubbleUp {
  0% {
    transform: translateY(0);
    opacity: 1;
  }
  100% {
    transform: translateY(-150px);
    opacity: 0;
  }
}

.weight-display {
  display: flex;
  align-items: baseline;
  gap: 0.5rem;
  margin-bottom: 0.3rem;
}

.weight-input {
  background: transparent;
  border: none;
  color: white;
  font-family: "NeoDunggeunmo", monospace;
  font-size: 2.5rem;
  width: 120px;
  text-align: center;
  outline: none;
  font-weight: bold;
  transition: color 0.3s;
}

.weight-input.over-target-text {
  color: #ff0055;
}

.weight-input::-webkit-inner-spin-button,
.weight-input::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.unit {
  font-size: 1.5rem;
  color: #888;
}

.weight-info {
  display: flex;
  gap: 1rem;
  align-items: center;
  margin-bottom: 0.3rem;
}

.weight-percentage {
  font-size: 0.8rem;
  color: #ffd700;
}
.warning-text {
  color: #ff0055 !important; /* Red */
}

.target-weight {
  font-size: 0.75rem;
  color: #888;
}

.warning-message {
  font-size: 0.8rem;
  color: #ff0055;
  background: rgba(255, 0, 85, 0.1);
  padding: 4px 12px;
  border-radius: 12px;
  border: 1px solid #ff0055;
  animation: blink 1.5s infinite;
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

/* 간단한 조정 버튼 */
.quick-adjust {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  margin-bottom: 1rem;
}

.adjust-btn {
  background: #222;
  border: 2px solid #555;
  color: #fff;
  padding: 12px 4px;
  cursor: pointer;
  font-family: "NeoDunggeunmo", monospace;
  font-size: 0.85rem;
  transition: all 0.1s;
}

.adjust-btn:active {
  border-color: #39ff14;
  background: #333;
  transform: scale(0.95);
}

/* 슬라이더 */
.slider-section {
  margin-bottom: 1rem;
  padding: 0 5px;
}

.weight-slider {
  width: 100%;
  height: 12px;
  -webkit-appearance: none;
  background: #333;
  border: 2px solid #fff;
  outline: none;
}

.weight-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 24px;
  height: 24px;
  background: #39ff14;
  border: 2px solid #fff;
  cursor: pointer;
  box-shadow: 2px 2px 0 #000;
}

/* 최근 기록 (간소화) */
.history-section {
  margin-bottom: 1rem;
}

.history-section h3 {
  font-size: 0.9rem;
  border-bottom: 2px solid #333;
  margin-bottom: 0.5rem;
  color: #39ff14;
  padding-bottom: 0.3rem;
}

.history-list {
  background: rgba(0, 0, 0, 0.3);
  padding: 5px;
  border: 1px solid #333;
}

.history-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 8px;
  border-bottom: 1px dashed #333;
  font-size: 0.8rem;
}

.history-row:last-child {
  border-bottom: none;
}

.history-row .date {
  color: #888;
  flex: 1;
}

.history-row .weight {
  color: #fff;
  flex: 1;
  text-align: center;
  font-weight: bold;
}

.history-row .diff {
  flex: 1;
  text-align: right;
  font-weight: bold;
}

.history-row .diff.down {
  color: #00ff62;
}

.history-row .diff.up {
  color: #ff0055;
}

/* 저장 버튼 */
.save-btn {
  width: 100%;
  background: #39ff14;
  color: #000;
  border: 2px solid #fff;
  padding: 12px;
  font-size: 1rem;
  font-family: "NeoDunggeunmo", monospace;
  font-weight: bold;
  cursor: pointer;
  box-shadow: 4px 4px 0 #000;
  transition: transform 0.1s;
}

.save-btn:active {
  transform: translate(4px, 4px);
  box-shadow: none;
}

@media (max-width: 450px) {
  .modal-overlay {
    align-items: flex-start;
  }

  .weight-input {
    font-size: 2rem;
    width: 100px;
  }

  .adjust-btn {
    padding: 10px 2px;
    font-size: 0.75rem;
  }

  .body-outline {
    width: 100px;
    height: 130px;
  }
}
</style>
