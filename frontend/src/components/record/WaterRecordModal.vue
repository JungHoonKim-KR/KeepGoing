<template>
  <div class="water-modal-overlay" @click="handleOverlayClick">
    <div class="water-modal" @click.stop>
      <!-- 헤더 -->
      <div class="modal-header">
        <button @click="closeModal" class="close-btn">
          <span class="close-icon">✕</span>
        </button>
        <h2 class="modal-title">물 마시기</h2>
        <button @click="saveWater" class="save-btn">완료</button>
      </div>

      <!-- 날짜 선택 -->
      <div class="date-section">
        <div class="date-display">{{ formattedDate }}</div>
      </div>

      <!-- 물 섭취량 표시 -->
      <div class="water-display-section">
        <div class="water-cup-container">
          <div class="water-cup">
            <div class="water-level" :style="{ height: waterPercentage + '%' }">
              <div class="water-wave"></div>
            </div>
            <span class="water-icon">💧</span>
          </div>
        </div>

        <div class="water-amount-display">
          <span class="current-amount">{{ currentAmount.toFixed(1) }}</span>
          <span class="unit">L</span>
          <span class="goal-amount">/ {{ goalAmount.toFixed(1) }}L</span>
        </div>

        <div class="percentage-display">{{ waterPercentage }}% 달성</div>
      </div>

      <!-- 빠른 추가 버튼 -->
      <div class="quick-add-section">
        <h3 class="section-title">빠른 추가</h3>
        <div class="quick-add-buttons">
          <button @click="addWater(0.1)" class="quick-btn">
            <span class="quick-icon">💧</span>
            <span class="quick-amount">100ml</span>
          </button>
          <button @click="addWater(0.2)" class="quick-btn">
            <span class="quick-icon">🥤</span>
            <span class="quick-amount">200ml</span>
          </button>
          <button @click="addWater(0.3)" class="quick-btn">
            <span class="quick-icon">🧃</span>
            <span class="quick-amount">300ml</span>
          </button>
          <button @click="addWater(0.5)" class="quick-btn">
            <span class="quick-icon">🍶</span>
            <span class="quick-amount">500ml</span>
          </button>
        </div>
      </div>

      <!-- 슬라이더 -->
      <div class="slider-section">
        <h3 class="section-title">정확한 양 입력</h3>
        <div class="slider-container">
          <input
            v-model="sliderValue"
            type="range"
            min="0"
            max="50"
            step="1"
            class="water-slider"
            @input="updateFromSlider"
          />
          <div class="slider-labels">
            <span>0.0L</span>
            <span>5.0L</span>
          </div>
        </div>
      </div>

      <!-- 오늘의 기록 -->
      <div class="today-records">
        <h3 class="section-title">오늘의 기록</h3>
        <div class="records-list">
          <div
            class="record-item"
            v-for="(record, index) in todayRecords"
            :key="index"
          >
            <div class="record-time">{{ record.time }}</div>
            <div class="record-amount">{{ record.amount }}L</div>
            <button @click="removeRecord(index)" class="remove-btn">✕</button>
          </div>
          <div v-if="todayRecords.length === 0" class="no-records">
            아직 기록이 없습니다
          </div>
        </div>
      </div>

      <!-- 목표 설정 -->
      <div class="goal-section">
        <h3 class="section-title">일일 목표량</h3>
        <div class="goal-input-container">
          <input
            v-model.number="goalAmount"
            type="number"
            step="0.1"
            class="goal-input"
            @input="updatePercentage"
          />
          <span class="goal-unit">L</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import dayjs from "dayjs";

const API_ENDPOINT = "http://localhost:8080";
// Props & Emits
const emit = defineEmits(["close"]);

// Data
const currentAmount = ref(1.5);
const goalAmount = ref(2.0);
const sliderValue = ref(15);

const todayRecords = ref([
  { time: "14:30", amount: "0.5" },
  { time: "12:00", amount: "0.3" },
  { time: "09:30", amount: "0.7" },
]);

// Computed
const formattedDate = computed(() => {
  return dayjs().format("YYYY년 M월 D일");
});

const waterPercentage = computed(() => {
  const percentage = (currentAmount.value / goalAmount.value) * 100;
  return Math.min(Math.round(percentage), 100);
});

// Methods
const addWater = (amount) => {
  currentAmount.value = Math.min(currentAmount.value + amount, 5.0);
  sliderValue.value = Math.round(currentAmount.value * 10);

  // 기록 추가
  const now = dayjs().format("HH:mm");
  todayRecords.value.unshift({
    time: now,
    amount: amount.toFixed(1),
  });
};

const updateFromSlider = () => {
  currentAmount.value = sliderValue.value / 10;
};

const updatePercentage = () => {
  // 목표량이 변경되면 퍼센트 자동 업데이트
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

const saveWater = async() => {
  console.log("물 섭취량 저장:", currentAmount.value, "L");

  const hydrationData = {
      memberId : 1,
      waterAmount : currentAmount.value,
  }
  try{
          const response = await fetch(`${API_ENDPOINT}/diets/hydration`,{
              method:'POST',
              headers:{
                  'Content-Type' : 'application/json'
              },
              body:JSON.stringify(hydrationData)
          });

          if (!response.ok) {
              console.error(`저장 API 실패: ${response.status}`);
              throw new Error(`저장 API 호출 실패 (Status: ${response.status})`);
          }
      }
  catch(error){
      console.error("식사 기록 저장 중 오류 발생:", error);
  }


  closeModal();
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
/* 모달 오버레이 */
.water-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 9999;
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

/* 모달 컨테이너 */
.water-modal {
  background: white;
  width: 100%;
  max-width: 500px;
  max-height: 90vh;
  border-radius: 1.5rem 1.5rem 0 0;
  overflow-y: auto;
  animation: slideUp 0.3s ease-out;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.15);
}

@keyframes slideUp {
  from {
    transform: translateY(100%);
  }
  to {
    transform: translateY(0);
  }
}

/* 헤더 */
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid #f3f4f6;
  position: sticky;
  top: 0;
  background: white;
  z-index: 10;
}

.modal-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

.close-btn,
.save-btn {
  background: none;
  border: none;
  font-size: 1rem;
  cursor: pointer;
  padding: 0.5rem;
  -webkit-tap-highlight-color: transparent;
  touch-action: manipulation;
  transition: transform 0.2s;
}

.close-btn:active,
.save-btn:active {
  transform: scale(0.9);
}

.close-icon {
  font-size: 1.5rem;
  color: #6b7280;
}

.save-btn {
  color: #3b82f6;
  font-weight: 600;
}

/* 날짜 섹션 */
.date-section {
  padding: 1rem 1.5rem;
  text-align: center;
}

.date-display {
  font-size: 0.95rem;
  color: #6b7280;
  font-weight: 500;
}

/* 물 표시 섹션 */
.water-display-section {
  padding: 2rem 1.5rem;
  background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%);
  text-align: center;
}

.water-cup-container {
  display: flex;
  justify-content: center;
  margin-bottom: 2rem;
}

.water-cup {
  width: 120px;
  height: 160px;
  background: rgba(255, 255, 255, 0.5);
  border: 3px solid #3b82f6;
  border-radius: 0 0 1rem 1rem;
  position: relative;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(59, 130, 246, 0.3);
}

.water-level {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(180deg, #60a5fa 0%, #3b82f6 100%);
  transition: height 0.5s ease-out;
}

.water-wave {
  position: absolute;
  top: -10px;
  left: -50%;
  width: 200%;
  height: 20px;
  background: radial-gradient(
    circle,
    rgba(255, 255, 255, 0.3) 30%,
    transparent 30%
  );
  animation: wave 3s infinite linear;
}

@keyframes wave {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(50%);
  }
}

.water-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-size: 3rem;
  opacity: 0.3;
}

.water-amount-display {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.current-amount {
  font-size: 3.5rem;
  font-weight: 700;
  color: #1e40af;
}

.unit {
  font-size: 1.5rem;
  color: #3b82f6;
  font-weight: 600;
}

.goal-amount {
  font-size: 1.25rem;
  color: #6b7280;
}

.percentage-display {
  font-size: 1.1rem;
  color: #1e40af;
  font-weight: 600;
}

/* 빠른 추가 버튼 */
.quick-add-section {
  padding: 1.5rem;
  border-top: 8px solid #f9fafb;
}

.section-title {
  font-size: 1rem;
  font-weight: 600;
  color: #111827;
  margin-bottom: 1rem;
}

.quick-add-buttons {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0.75rem;
}

.quick-btn {
  background: white;
  border: 2px solid #e5e7eb;
  padding: 1rem 0.5rem;
  border-radius: 1rem;
  cursor: pointer;
  transition: all 0.2s;
  -webkit-tap-highlight-color: transparent;
  touch-action: manipulation;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.quick-btn:active {
  transform: scale(0.95);
  background: #eff6ff;
  border-color: #3b82f6;
}

.quick-icon {
  font-size: 2rem;
}

.quick-amount {
  font-size: 0.85rem;
  font-weight: 600;
  color: #374151;
}

/* 슬라이더 */
.slider-section {
  padding: 1.5rem;
  border-top: 8px solid #f9fafb;
}

.slider-container {
  margin-top: 1rem;
}

.water-slider {
  width: 100%;
  height: 8px;
  border-radius: 1rem;
  outline: none;
  background: linear-gradient(to right, #e5e7eb 0%, #3b82f6 50%, #e5e7eb 100%);
  -webkit-appearance: none;
  cursor: pointer;
}

.water-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  cursor: pointer;
  border: 3px solid #3b82f6;
  transition: transform 0.2s;
}

.water-slider::-webkit-slider-thumb:active {
  transform: scale(1.2);
}

.water-slider::-moz-range-thumb {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  cursor: pointer;
  border: 3px solid #3b82f6;
  transition: transform 0.2s;
}

.water-slider::-moz-range-thumb:active {
  transform: scale(1.2);
}

.slider-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 0.5rem;
  font-size: 0.75rem;
  color: #9ca3af;
}

/* 오늘의 기록 */
.today-records {
  padding: 1.5rem;
  border-top: 8px solid #f9fafb;
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  max-height: 200px;
  overflow-y: auto;
}

.record-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem;
  background: #f9fafb;
  border-radius: 0.875rem;
}

.record-time {
  font-size: 0.9rem;
  color: #6b7280;
  flex: 1;
}

.record-amount {
  font-size: 1rem;
  font-weight: 600;
  color: #3b82f6;
  flex: 1;
  text-align: center;
}

.remove-btn {
  background: none;
  border: none;
  color: #ef4444;
  font-size: 1.25rem;
  cursor: pointer;
  padding: 0.25rem 0.5rem;
  -webkit-tap-highlight-color: transparent;
}

.remove-btn:active {
  transform: scale(0.9);
}

.no-records {
  text-align: center;
  padding: 2rem;
  color: #9ca3af;
  font-size: 0.9rem;
}

/* 목표 설정 */
.goal-section {
  padding: 1.5rem;
  border-top: 8px solid #f9fafb;
}

.goal-input-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin-top: 1rem;
}

.goal-input {
  width: 120px;
  padding: 0.75rem 1rem;
  border: 2px solid #e5e7eb;
  border-radius: 0.875rem;
  font-size: 1.25rem;
  font-weight: 600;
  text-align: center;
  outline: none;
  transition: border-color 0.2s;
}

.goal-input:focus {
  border-color: #3b82f6;
}

.goal-input::-webkit-outer-spin-button,
.goal-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.goal-input[type="number"] {
  -moz-appearance: textfield;
}

.goal-unit {
  font-size: 1.25rem;
  font-weight: 600;
  color: #6b7280;
}

/* 모바일 최적화 */
@media (max-width: 390px) {
  .current-amount {
    font-size: 3rem;
  }

  .quick-add-buttons {
    grid-template-columns: repeat(2, 1fr);
  }

  .water-cup {
    width: 100px;
    height: 140px;
  }
}

/* 스크롤바 스타일링 */
.water-modal::-webkit-scrollbar {
  width: 6px;
}

.water-modal::-webkit-scrollbar-track {
  background: transparent;
}

.water-modal::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}

.water-modal::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}
</style>
