<template>
  <div class="weight-modal-overlay" @click="handleOverlayClick">
    <div class="weight-modal" @click.stop>
      <!-- 헤더 -->
      <div class="modal-header">
        <button @click="closeModal" class="close-btn">
          <span class="close-icon">✕</span>
        </button>
        <h2 class="modal-title">체중 기록</h2>
        <button @click="saveWeight" class="save-btn">완료</button>
      </div>

      <!-- 날짜 선택 -->
      <div class="date-section">
        <div class="date-display">{{ formattedDate }}</div>
      </div>

      <!-- 체중 입력 섹션 -->
      <div class="weight-input-section">
        <div class="weight-display-large">
          <input
            v-model="weightInput"
            type="number"
            step="0.1"
            class="weight-input"
            placeholder="0.0"
            @input="updateSlider"
          />
          <span class="weight-unit-large">kg</span>
        </div>

        <!-- 슬라이더 -->
        <div class="slider-container">
          <input
            v-model="weightSlider"
            type="range"
            min="30"
            max="150"
            step="0.1"
            class="weight-slider"
            @input="updateInput"
          />
          <div class="slider-labels">
            <span>30kg</span>
            <span>150kg</span>
          </div>
        </div>
      </div>

      <!-- 빠른 조정 버튼 -->
      <div class="quick-adjust">
        <button @click="adjustWeight(-1)" class="adjust-btn">-1.0</button>
        <button @click="adjustWeight(-0.5)" class="adjust-btn">-0.5</button>
        <button @click="adjustWeight(-0.1)" class="adjust-btn">-0.1</button>
        <button @click="adjustWeight(0.1)" class="adjust-btn">+0.1</button>
        <button @click="adjustWeight(0.5)" class="adjust-btn">+0.5</button>
        <button @click="adjustWeight(1)" class="adjust-btn">+1.0</button>
      </div>

      <!-- 최근 기록 -->
      <div class="recent-records">
        <h3 class="section-title">최근 기록</h3>
        <div class="records-list">
          <div class="record-item">
            <div class="record-date">어제</div>
            <div class="record-weight">70.3kg</div>
            <div class="record-change decrease">-0.2kg</div>
          </div>
          <div class="record-item">
            <div class="record-date">2일 전</div>
            <div class="record-weight">70.5kg</div>
            <div class="record-change increase">+0.3kg</div>
          </div>
          <div class="record-item">
            <div class="record-date">3일 전</div>
            <div class="record-weight">70.2kg</div>
            <div class="record-change decrease">-0.1kg</div>
          </div>
        </div>
      </div>

      <!-- 메모 (선택사항) -->
      <div class="memo-section">
        <label class="memo-label">메모 (선택)</label>
        <textarea
          v-model="memo"
          class="memo-input"
          placeholder="오늘의 컨디션이나 특이사항을 기록해보세요..."
          rows="3"
        ></textarea>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import dayjs from "dayjs";

// Props & Emits
const emit = defineEmits(["close"]);

// Data
const weightInput = ref("70.0");
const weightSlider = ref(70);
const memo = ref("");

// Computed
const formattedDate = computed(() => {
  return dayjs().format("YYYY년 M월 D일");
});

// Methods
const updateSlider = () => {
  const value = parseFloat(weightInput.value) || 70;
  weightSlider.value = Math.max(30, Math.min(150, value));
};

const updateInput = () => {
  weightInput.value = parseFloat(weightSlider.value).toFixed(1);
};

const adjustWeight = (amount) => {
  const current = parseFloat(weightInput.value) || 70;
  const newWeight = Math.max(30, Math.min(150, current + amount));
  weightInput.value = newWeight.toFixed(1);
  weightSlider.value = newWeight;
};

const closeModal = () => {
  emit("close");
};

const handleOverlayClick = (e) => {
  // 오버레이 직접 클릭시에만 닫기
  if (e.target === e.currentTarget) {
    closeModal();
  }
};

const saveWeight = () => {
  // TODO: 실제 저장 로직 구현
  console.log("체중 저장:", weightInput.value, "kg");
  console.log("메모:", memo.value);
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
.weight-modal-overlay {
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
.weight-modal {
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
  color: #667eea;
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

/* 체중 입력 섹션 */
.weight-input-section {
  padding: 2rem 1.5rem;
  background: linear-gradient(135deg, #f9fafb 0%, #f3f4f6 100%);
}

.weight-display-large {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 0.5rem;
  margin-bottom: 2rem;
}

.weight-input {
  font-size: 4rem;
  font-weight: 700;
  color: #111827;
  border: none;
  background: transparent;
  text-align: right;
  width: 180px;
  outline: none;
  padding: 0;
}

.weight-input::placeholder {
  color: #d1d5db;
}

.weight-input::-webkit-outer-spin-button,
.weight-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

.weight-input[type="number"] {
  -moz-appearance: textfield;
}

.weight-unit-large {
  font-size: 2rem;
  color: #6b7280;
  font-weight: 600;
}

/* 슬라이더 */
.slider-container {
  margin-top: 1rem;
}

.weight-slider {
  width: 100%;
  height: 8px;
  border-radius: 1rem;
  outline: none;
  background: linear-gradient(to right, #e5e7eb 0%, #667eea 50%, #e5e7eb 100%);
  -webkit-appearance: none;
  cursor: pointer;
}

.weight-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  cursor: pointer;
  border: 3px solid #667eea;
  transition: transform 0.2s;
}

.weight-slider::-webkit-slider-thumb:active {
  transform: scale(1.2);
}

.weight-slider::-moz-range-thumb {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  cursor: pointer;
  border: 3px solid #667eea;
  transition: transform 0.2s;
}

.weight-slider::-moz-range-thumb:active {
  transform: scale(1.2);
}

.slider-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 0.5rem;
  font-size: 0.75rem;
  color: #9ca3af;
}

/* 빠른 조정 버튼 */
.quick-adjust {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 0.5rem;
  padding: 1.5rem;
}

.adjust-btn {
  background: white;
  border: 1px solid #e5e7eb;
  padding: 0.75rem 0.5rem;
  border-radius: 0.75rem;
  font-size: 0.85rem;
  font-weight: 600;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s;
  -webkit-tap-highlight-color: transparent;
  touch-action: manipulation;
}

.adjust-btn:active {
  transform: scale(0.95);
  background: #f3f4f6;
}

/* 최근 기록 */
.recent-records {
  padding: 1.5rem;
  border-top: 8px solid #f9fafb;
}

.section-title {
  font-size: 1rem;
  font-weight: 600;
  color: #111827;
  margin-bottom: 1rem;
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.record-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem;
  background: #f9fafb;
  border-radius: 0.875rem;
}

.record-date {
  font-size: 0.9rem;
  color: #6b7280;
  flex: 1;
}

.record-weight {
  font-size: 1rem;
  font-weight: 600;
  color: #111827;
  flex: 1;
  text-align: center;
}

.record-change {
  font-size: 0.85rem;
  font-weight: 600;
  flex: 1;
  text-align: right;
}

.record-change.decrease {
  color: #10b981;
}

.record-change.increase {
  color: #ef4444;
}

/* 메모 섹션 */
.memo-section {
  padding: 1.5rem;
  border-top: 8px solid #f9fafb;
}

.memo-label {
  display: block;
  font-size: 0.9rem;
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.75rem;
}

.memo-input {
  width: 100%;
  padding: 1rem;
  border: 1px solid #e5e7eb;
  border-radius: 0.875rem;
  font-size: 0.9rem;
  color: #111827;
  resize: none;
  font-family: inherit;
  outline: none;
  transition: border-color 0.2s;
}

.memo-input:focus {
  border-color: #667eea;
}

.memo-input::placeholder {
  color: #9ca3af;
}

/* 모바일 최적화 */
@media (max-width: 390px) {
  .weight-input {
    font-size: 3.5rem;
    width: 160px;
  }

  .weight-unit-large {
    font-size: 1.75rem;
  }

  .quick-adjust {
    grid-template-columns: repeat(3, 1fr);
  }
}

/* 스크롤바 스타일링 */
.weight-modal::-webkit-scrollbar {
  width: 6px;
}

.weight-modal::-webkit-scrollbar-track {
  background: transparent;
}

.weight-modal::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}

.weight-modal::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}
</style>
