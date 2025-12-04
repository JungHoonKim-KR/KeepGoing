<template>
  <div class="meal-modal-overlay" @click="handleOverlayClick">
    <div class="meal-modal" @click.stop>
      <!-- 헤더 -->
      <div class="modal-header">
        <button @click="closeModal" class="close-btn">
          <span class="close-icon">✕</span>
        </button>
        <h2 class="modal-title">식사 기록</h2>
        <button @click="saveMeal" class="save-btn">완료</button>
      </div>

      <!-- 날짜 선택 -->
      <div class="date-section">
        <div class="date-display">{{ formattedDate }}</div>
      </div>

      <!-- 식사 시간 선택 -->
      <div class="meal-type-section">
        <h3 class="section-title">식사 시간</h3>
        <div class="meal-type-buttons">
          <button
            v-for="type in mealTypes"
            :key="type.id"
            :class="['meal-type-btn', { active: selectedMealType === type.id }]"
            @click="selectedMealType = type.id"
          >
            <span class="meal-emoji">{{ type.emoji }}</span>
            <span class="meal-name">{{ type.name }}</span>
          </button>
        </div>
      </div>

      <!-- 음식 입력 -->
      <div class="food-input-section">
        <h3 class="section-title">무엇을 드셨나요?</h3>
        <div class="food-input-container">
          <input
            v-model="foodName"
            type="text"
            class="food-input"
            placeholder="예: 김치찌개, 삼겹살, 샐러드..."
            @keyup.enter="addFood"
          />
          <button @click="addFood" class="add-btn">추가</button>
        </div>

        <!-- 추가된 음식 목록 -->
        <div v-if="foodList.length > 0" class="food-list">
          <div v-for="(food, index) in foodList" :key="index" class="food-item">
            <span class="food-item-name">{{ food }}</span>
            <button @click="removeFood(index)" class="remove-food-btn">
              ✕
            </button>
          </div>
        </div>
      </div>

      <!-- 사진 추가 (선택) -->
      <div class="photo-section">
        <h3 class="section-title">사진 (선택)</h3>
        <div class="photo-upload-area" @click="triggerFileInput">
          <input
            ref="fileInput"
            type="file"
            accept="image/*"
            style="display: none"
            @change="handleFileUpload"
          />
          <div v-if="!photoPreview" class="photo-placeholder">
            <span class="camera-icon">📷</span>
            <span class="photo-text">사진 추가하기</span>
          </div>
          <div v-else class="photo-preview">
            <img :src="photoPreview" alt="식사 사진" />
            <button @click.stop="removePhoto" class="remove-photo-btn">
              ✕
            </button>
          </div>
        </div>
      </div>

      <!-- 메모 (선택) -->
      <div class="memo-section">
        <h3 class="section-title">메모 (선택)</h3>
        <textarea
          v-model="memo"
          class="memo-input"
          placeholder="맛있었던 점, 느낀 점을 자유롭게 적어보세요..."
          rows="3"
        ></textarea>
      </div>

      <!-- 오늘의 식사 기록 -->
      <div class="today-meals-section">
        <h3 class="section-title">오늘의 식사</h3>
        <div class="today-meals-grid">
          <div
            v-for="meal in todayMeals"
            :key="meal.type"
            :class="['meal-card', { completed: meal.completed }]"
          >
            <span class="meal-card-emoji">{{ meal.emoji }}</span>
            <span class="meal-card-name">{{ meal.name }}</span>
            <span v-if="meal.completed" class="check-icon">✓</span>
          </div>
        </div>
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
const selectedMealType = ref("breakfast");
const foodName = ref("");
const foodList = ref([]);
const memo = ref("");
const photoPreview = ref(null);
const fileInput = ref(null);

const mealTypes = [
  { id: "breakfast", name: "아침", emoji: "🌅" },
  { id: "lunch", name: "점심", emoji: "🌞" },
  { id: "dinner", name: "저녁", emoji: "🌙" },
  { id: "snack", name: "간식", emoji: "🍪" },
];

const todayMeals = ref([
  { type: "breakfast", name: "아침", emoji: "🌅", completed: false },
  { type: "lunch", name: "점심", emoji: "🌞", completed: true },
  { type: "dinner", name: "저녁", emoji: "🌙", completed: false },
  { type: "snack", name: "간식", emoji: "🍪", completed: false },
]);

// Computed
const formattedDate = computed(() => {
  return dayjs().format("YYYY년 M월 D일");
});

// Methods
const addFood = () => {
  if (foodName.value.trim()) {
    foodList.value.push(foodName.value.trim());
    foodName.value = "";
  }
};

const removeFood = (index) => {
  foodList.value.splice(index, 1);
};

const triggerFileInput = () => {
  fileInput.value?.click();
};

const handleFileUpload = (event) => {
  const file = event.target.files?.[0];
  if (file) {
    const reader = new FileReader();
    reader.onload = (e) => {
      photoPreview.value = e.target?.result;
    };
    reader.readAsDataURL(file);
  }
};

const removePhoto = () => {
  photoPreview.value = null;
  if (fileInput.value) {
    fileInput.value.value = "";
  }
};

const closeModal = () => {
  emit("close");
};

const handleOverlayClick = (e) => {
  if (e.target === e.currentTarget) {
    closeModal();
  }
};

const saveMeal = () => {
  console.log("식사 기록 저장:", {
    type: selectedMealType.value,
    foods: foodList.value,
    photo: photoPreview.value ? "uploaded" : null,
    memo: memo.value,
  });
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
.meal-modal-overlay {
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
.meal-modal {
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
  color: #98d8c8;
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

/* 섹션 타이틀 */
.section-title {
  font-size: 1rem;
  font-weight: 600;
  color: #111827;
  margin-bottom: 1rem;
}

/* 식사 시간 선택 */
.meal-type-section {
  padding: 1.5rem;
  border-top: 8px solid #f9fafb;
}

.meal-type-buttons {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0.75rem;
}

.meal-type-btn {
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

.meal-type-btn:active {
  transform: scale(0.95);
}

.meal-type-btn.active {
  background: linear-gradient(135deg, #98d8c8 0%, #6fafaa 100%);
  border-color: #6fafaa;
}

.meal-type-btn.active .meal-emoji,
.meal-type-btn.active .meal-name {
  filter: brightness(1.2);
}

.meal-emoji {
  font-size: 2rem;
}

.meal-name {
  font-size: 0.85rem;
  font-weight: 600;
  color: #374151;
}

.meal-type-btn.active .meal-name {
  color: white;
}

/* 음식 입력 */
.food-input-section {
  padding: 1.5rem;
  border-top: 8px solid #f9fafb;
}

.food-input-container {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.food-input {
  flex: 1;
  padding: 0.875rem 1rem;
  border: 2px solid #e5e7eb;
  border-radius: 0.875rem;
  font-size: 0.95rem;
  outline: none;
  transition: border-color 0.2s;
}

.food-input:focus {
  border-color: #98d8c8;
}

.add-btn {
  background: linear-gradient(135deg, #98d8c8 0%, #6fafaa 100%);
  color: white;
  border: none;
  padding: 0.875rem 1.5rem;
  border-radius: 0.875rem;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  transition: transform 0.2s;
}

.add-btn:active {
  transform: scale(0.95);
}

/* 음식 목록 */
.food-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.food-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: #f9fafb;
  padding: 0.5rem 0.75rem;
  border-radius: 2rem;
  font-size: 0.9rem;
}

.food-item-name {
  color: #374151;
}

.remove-food-btn {
  background: none;
  border: none;
  color: #ef4444;
  font-size: 1rem;
  cursor: pointer;
  padding: 0;
  line-height: 1;
  -webkit-tap-highlight-color: transparent;
}

/* 사진 업로드 */
.photo-section {
  padding: 1.5rem;
  border-top: 8px solid #f9fafb;
}

.photo-upload-area {
  border: 2px dashed #d1d5db;
  border-radius: 1rem;
  padding: 2rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  -webkit-tap-highlight-color: transparent;
}

.photo-upload-area:active {
  transform: scale(0.98);
  background: #f9fafb;
}

.photo-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}

.camera-icon {
  font-size: 3rem;
}

.photo-text {
  font-size: 0.9rem;
  color: #6b7280;
}

.photo-preview {
  position: relative;
}

.photo-preview img {
  width: 100%;
  max-height: 200px;
  object-fit: cover;
  border-radius: 0.875rem;
}

.remove-photo-btn {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  background: rgba(0, 0, 0, 0.6);
  color: white;
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  font-size: 1.25rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  -webkit-tap-highlight-color: transparent;
}

/* 메모 */
.memo-section {
  padding: 1.5rem;
  border-top: 8px solid #f9fafb;
}

.memo-input {
  width: 100%;
  padding: 1rem;
  border: 2px solid #e5e7eb;
  border-radius: 0.875rem;
  font-size: 0.9rem;
  color: #111827;
  resize: none;
  font-family: inherit;
  outline: none;
  transition: border-color 0.2s;
}

.memo-input:focus {
  border-color: #98d8c8;
}

.memo-input::placeholder {
  color: #9ca3af;
}

/* 오늘의 식사 */
.today-meals-section {
  padding: 1.5rem;
  border-top: 8px solid #f9fafb;
}

.today-meals-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0.75rem;
}

.meal-card {
  background: #f9fafb;
  padding: 1rem 0.5rem;
  border-radius: 1rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  position: relative;
  border: 2px solid transparent;
}

.meal-card.completed {
  background: linear-gradient(135deg, #e0f2f1 0%, #b2dfdb 100%);
  border-color: #4db6ac;
}

.meal-card-emoji {
  font-size: 1.75rem;
}

.meal-card-name {
  font-size: 0.8rem;
  color: #6b7280;
  font-weight: 500;
}

.meal-card.completed .meal-card-name {
  color: #00796b;
  font-weight: 600;
}

.check-icon {
  position: absolute;
  top: 0.25rem;
  right: 0.25rem;
  background: #4db6ac;
  color: white;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: bold;
}

/* 모바일 최적화 */
@media (max-width: 390px) {
  .meal-type-buttons {
    grid-template-columns: repeat(2, 1fr);
  }

  .meal-emoji {
    font-size: 1.75rem;
  }

  .today-meals-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* 스크롤바 스타일링 */
.meal-modal::-webkit-scrollbar {
  width: 6px;
}

.meal-modal::-webkit-scrollbar-track {
  background: transparent;
}

.meal-modal::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}

.meal-modal::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}
</style>
