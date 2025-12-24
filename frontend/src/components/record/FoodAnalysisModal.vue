<template>
  <div class="modal-overlay" @click="$emit('close')">
    <div class="retro-modal analysis-modal" @click.stop>
      <div class="modal-header">
        <div class="header-title"><span class="icon">📡</span> ANALYZE FOOD</div>
        <button @click="$emit('close')" class="pixel-close-btn">X</button>
      </div>
      <div class="modal-body">
        <div class="pixel-label">INPUT FOOD NAME</div>
        <div class="terminal-input-box">
          <span class="prompt">FOOD ></span>
          <input v-model="foodName" @keyup.enter="analyzeFood" class="retro-input" ref="inputRef" />
          <button @click="analyzeFood" :disabled="isLoading" class="retro-btn-sm search-btn">
            {{ isLoading ? "..." : "등록" }}
          </button>
        </div>

        <div v-if="error" class="error-message">
          <p>ERROR: {{ error }}</p>
        </div>

        <div v-if="isLoading" class="loading-indicator">
          <p>ANALYZING... <span class="blink-dots">>>></span></p>
        </div>

        <div v-if="analysisResult" class="result-box">
          <!-- Case 2: Registrable new food -->
          <div v-if="analysisResult.isFood === 2" class="success-result">
            <div class="result-header">ANALYSIS: NEW FOOD</div>
            <p class="result-text">
              <span class="food-name">'{{ analysisResult.name }}'</span> 등록 완료!
            </p>
            <div class="stats-grid">
              <div><span>KCAL:</span> {{ analysisResult.energy }}</div>
              <div><span>CARBO:</span> {{ analysisResult.carbohydrate }}g</div>
              <div><span>PROTEIN:</span> {{ analysisResult.protein }}g</div>
              <div><span>FAT:</span> {{ analysisResult.fat }}g</div>
            </div>
            <button @click="$emit('close')" class="retro-btn-lg register-btn">확인</button>
          </div>
          <!-- Case 1: Already existing food -->
          <div v-else-if="analysisResult.isFood === 1" class="info-result">
            <div class="result-header">ANALYSIS: Existed FOOD</div>
            <p class="result-text">
              <span class="food-name">'{{ analysisResult.name }}'</span>: 이미 등록된 음식입니다.
            </p>
            <div class="stats-grid">
              <div><span>KCAL:</span> {{ analysisResult.energy }}</div>
              <div><span>CARBO:</span> {{ analysisResult.carbohydrate }}g</div>
              <div><span>PROTEIN:</span> {{ analysisResult.protein }}g</div>
              <div><span>FAT:</span> {{ analysisResult.fat }}g</div>
            </div>
            <button @click="$emit('close')" class="retro-btn-lg add-btn">확인</button>
          </div>
          <!-- Case 0: Not a food -->
          <div v-else class="failure-result">
            <div class="result-header">ANALYSIS: FAILED</div>
            <p class="result-text">
              <span class="food-name">'{{ foodName }}'</span> 은(는) 음식으로 인식되지 않습니다.
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { updateLevelApi } from "@/api/member/memberApi";
import { useAuthStore } from "@/stores/authStore";

const emit = defineEmits(["close", "register"]);

const foodName = ref("");
const isLoading = ref(false);
const analysisResult = ref(null);
const error = ref("");
const inputRef = ref(null);

const authStore = useAuthStore();
const API_ENDPOINT = "http://localhost:8080";

async function analyzeFood() {
  if (!foodName.value.trim()) return;
  isLoading.value = true;
  analysisResult.value = null;
  error.value = "";

  try {
    const response = await fetch(`${API_ENDPOINT}/food/analyze?foodName=${encodeURIComponent(foodName.value)}`);
    if (!response.ok) {
      throw new Error("API Request Failed");
    }
    const data = await response.json();

    if (data.isFood === 2) {
      // New food analyzed, call the level-up API
      const payload = {
        id: authStore.memberId,
        score: 50 // Award 10 EXP for new food
      };
      const levelUpResponse = await updateLevelApi(payload);
      // Update the auth store with the new level/exp
      authStore.updateLevelInfo(levelUpResponse);
    }
    
    analysisResult.value = data;
  } catch (e) {
    error.value = e.message || "An error occurred during analysis.";
  } finally {
    isLoading.value = false;
  }
}

function registerFood() {
  // Allow adding if isFood is 1 (exists) or 2 (new and registrable)
  if (analysisResult.value && (analysisResult.value.isFood === 1 || analysisResult.value.isFood === 2)) {
    const foodData = {
      name: analysisResult.value.foodName,
      energy: analysisResult.value.kcal,
      carbohydrate: analysisResult.value.carbo,
      protein: analysisResult.value.protein,
      fat: analysisResult.value.fat,
    };
    emit("register", foodData);
    emit("close");
  }
}

onMounted(() => {
  inputRef.value?.focus();
});
</script>

<style scoped>
/* Inherit styles from MealRecordModal by using same class names */
@import url("https://cdn.jsdelivr.net/gh/neodgm/neodgm-webfont@latest/neodgm/style.css");

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.95);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000; /* Higher than parent modal */
  font-family: "NeoDunggeunmo", monospace;
  color: #00ff00;
}

.retro-modal.analysis-modal {
  width: 95%;
  max-width: 420px;
  border-color: #ffd700;
  box-shadow: 0 0 20px rgba(255, 215, 0, 0.2);
  background: #0d1117;
  border: 4px solid #30363d;
  display: flex;
  flex-direction: column;
  z-index: 2;
  overflow: hidden;
}

.modal-header {
  background: #161b22;
  border-bottom: 2px solid #30363d;
  padding: 10px 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-title {
  font-size: 1rem;
  color: #fff;
  display: flex;
  align-items: center;
  gap: 8px;
}
.pixel-close-btn {
  background: #ff0055;
  color: #fff;
  border: 2px solid #fff;
  width: 28px;
  height: 28px;
  font-weight: bold;
  cursor: pointer;
  box-shadow: 2px 2px 0 #000;
}

.modal-body {
  padding: 15px;
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.pixel-label {
  font-size: 0.9rem;
  color: #ffd700;
  text-shadow: 1px 1px 0 #000;
}

.terminal-input-box {
  display: flex;
  align-items: center;
  background: #000;
  border: 2px solid #30363d;
  padding: 8px;
  position: relative;
}
.prompt {
  color: #00ff00;
  margin-right: 8px;
  font-weight: bold;
}
.retro-input {
  flex: 1;
  background: transparent;
  border: none;
  color: #fff;
  font-family: inherit;
  font-size: 1rem;
  outline: none;
}
.retro-btn-sm.search-btn {
  background: #ffd700;
  color: #000;
  border-color: #000;
}

.error-message {
  background: #4d0000;
  border: 2px solid #ff0055;
  color: #fff;
  padding: 10px;
  text-align: center;
}

.result-box {
  border: 2px solid #30363d;
  background: #161b22;
  padding: 15px;
}
.result-header {
  font-size: 0.8rem;
  text-align: center;
  margin-bottom: 10px;
  padding-bottom: 5px;
  border-bottom: 1px dashed #30363d;
}
.success-result .result-header {
  color: #00ff00;
}
.info-result .result-header {
  color: #00e5ff;
}
.failure-result .result-header {
  color: #ff0055;
}

.result-text {
  text-align: center;
  margin-bottom: 15px;
}
.food-name {
  color: #ffd700;
  font-weight: bold;
}

.stats-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  background: #0d1117;
  padding: 10px;
  margin-bottom: 15px;
  border: 1px solid #30363d;
  font-size: 0.9rem;
}
.stats-grid span {
  color: #8b949e;
}

.retro-btn-lg {
  width: 100%;
  color: #fff;
  border: 2px solid #fff;
  padding: 12px;
  font-family: inherit;
  cursor: pointer;
  box-shadow: 4px 4px 0 #000;
}

.retro-btn-lg.register-btn {
  background: #238636; /* Green */
}

.retro-btn-lg.add-btn {
  background: #1f6feb; /* Blue */
}

.loading-indicator {
  text-align: center;
  font-size: 1.2rem;
  color: #00ff00; /* Neon green */
  padding: 20px 0;
  border: 1px dashed #004400;
  background: #0d1117;
  margin-top: 15px;
}

.blink-dots {
  animation: blink 1s infinite step-end;
}

@keyframes blink {
  from, to { opacity: 0; }
  50% { opacity: 1; }
}
</style>
