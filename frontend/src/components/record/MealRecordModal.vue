<template>
  <div class="modal-overlay" @click="handleOverlayClick">
    <div class="scanlines"></div>

    <div class="retro-modal" @click.stop>
      <div class="modal-header">
        <div class="header-title">
          <span class="icon">💾</span> SYSTEM: MEAL_LOG.EXE
        </div>
        <button @click="closeModal" class="pixel-close-btn">X</button>
      </div>

      <div class="modal-body">
        <div class="terminal-text">
          > CONNECTED TO SERVER...<br />
          > DATE: {{ formattedDate }}
        </div>

        <div class="section-container">
          <div class="pixel-label">1. SELECT MISSION</div>
          <div class="mission-grid">
            <button
              v-for="time in mealTimes"
              :key="time.id"
              :class="[
                'mission-card',
                { active: selectedMealTime === time.name },
              ]"
              @click="selectMealTime(time.name)"
            >
              <div class="mission-icon">{{ getPixelIcon(time.id) }}</div>
              <div class="mission-name">{{ time.name }}</div>
              <div
                class="selection-indicator"
                v-if="selectedMealTime === time.name"
              >
                ◀
              </div>
            </button>
          </div>
        </div>

        <div class="section-container">
          <div class="pixel-label">2. SCAN ITEMS (FOOD)</div>

          <div class="terminal-input-box">
            <span class="prompt">INPUT ></span>
            <input
              :value="foodName"
              @input="handleInput"
              @keydown="handleKeydown"
              type="text"
              class="retro-input"
              placeholder="Enter item name..."
              autocomplete="off"
            />

            <div v-if="isLoading" class="loading-status">[PROCESSING...]</div>

            <button @click="manualAdd" class="retro-btn-sm">ADD</button>

            <ul v-if="suggestions.length > 0" class="retro-dropdown">
              <li
                v-for="(suggestion, index) in suggestions"
                :key="index"
                @click="selectFood(suggestion)"
                @mouseover="selectedFoodIndex = index"
                :class="[
                  'dropdown-item',
                  { active: index === selectedFoodIndex },
                ]"
              >
                {{ suggestion.name }}
              </li>
            </ul>
          </div>

          <div class="inventory-box">
            <div class="inventory-header">=== CURRENT INVENTORY ===</div>
            <div v-if="selectedFoodList.length === 0" class="empty-msg">
              NO ITEMS DETECTED.
            </div>
            <div v-else class="inventory-list">
              <div
                v-for="(foodItem, index) in selectedFoodList"
                :key="index"
                class="inventory-slot"
              >
                <div class="slot-info">
                  <span class="slot-icon">🍖</span>
                  <span class="slot-name">{{ foodItem.name }}</span>
                </div>
                <div class="slot-controls">
                  <button
                    @click="changeFoodCount(index, -1)"
                    :disabled="foodItem.servings <= 1"
                    class="qty-btn"
                  >
                    -
                  </button>
                  <span class="slot-qty">x{{ foodItem.servings }}</span>
                  <button @click="changeFoodCount(index, 1)" class="qty-btn">
                    +
                  </button>
                  <button @click="removeFood(index)" class="trash-btn">
                    🗑
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="section-container">
          <div class="pixel-label">3. UPLOAD EVIDENCE</div>
          <div class="evidence-box" @click="triggerFileInput">
            <input
              ref="fileInput"
              type="file"
              accept="image/*"
              style="display: none"
              @change="handleFileUpload"
            />

            <div v-if="!photoPreview" class="placeholder-content">
              <div class="scan-icon">[ + ]</div>
              <span>CLICK TO SCAN IMAGE</span>
            </div>

            <div v-else class="preview-content">
              <img :src="photoPreview" alt="Evidence" />
              <div class="scan-overlay">
                <div class="corner tl"></div>
                <div class="corner tr"></div>
                <div class="corner bl"></div>
                <div class="corner br"></div>
                <div class="rec-badge">REC ●</div>
              </div>
              <button @click.stop="removePhoto" class="delete-evidence-btn">
                DISCARD
              </button>
            </div>
          </div>
        </div>

        <div class="section-container">
          <div class="pixel-label">4. DATA LOG</div>
          <textarea
            v-model="memo"
            class="retro-textarea"
            placeholder="Additional notes..."
            rows="2"
          ></textarea>
        </div>

        <div class="footer-actions">
          <button @click="saveMeal" class="retro-btn-lg">
            <span class="btn-text">SAVE TO DATABASE</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import dayjs from "dayjs";

const emit = defineEmits(["close"]);
const API_ENDPOINT = "http://localhost:8080";

// === 🔊 8-bit Sound FX ===
const playSound = (type) => {
  const AudioContext = window.AudioContext || window.webkitAudioContext;
  if (!AudioContext) return;
  const ctx = new AudioContext();
  const osc = ctx.createOscillator();
  const gain = ctx.createGain();
  osc.connect(gain);
  gain.connect(ctx.destination);
  const now = ctx.currentTime;

  if (type === "type") {
    // 타이핑 소리
    osc.type = "square";
    osc.frequency.setValueAtTime(800, now);
    gain.gain.setValueAtTime(0.02, now);
    gain.gain.exponentialRampToValueAtTime(0.001, now + 0.05);
    osc.start(now);
    osc.stop(now + 0.05);
  } else if (type === "blip") {
    // 선택/클릭
    osc.type = "square";
    osc.frequency.setValueAtTime(440, now);
    gain.gain.setValueAtTime(0.05, now);
    gain.gain.exponentialRampToValueAtTime(0.01, now + 0.1);
    osc.start(now);
    osc.stop(now + 0.1);
  } else if (type === "coin") {
    // 아이템 추가
    osc.type = "triangle";
    osc.frequency.setValueAtTime(900, now);
    osc.frequency.linearRampToValueAtTime(1200, now + 0.1);
    gain.gain.setValueAtTime(0.05, now);
    gain.gain.linearRampToValueAtTime(0, now + 0.2);
    osc.start(now);
    osc.stop(now + 0.2);
  } else if (type === "save") {
    // 저장 성공
    osc.type = "square";
    osc.frequency.setValueAtTime(440, now);
    osc.frequency.setValueAtTime(554, now + 0.1);
    osc.frequency.setValueAtTime(659, now + 0.2);
    gain.gain.setValueAtTime(0.05, now);
    gain.gain.linearRampToValueAtTime(0, now + 0.4);
    osc.start(now);
    osc.stop(now + 0.4);
  }
};

// === Icons ===
const getPixelIcon = (id) => {
  const icons = {
    breakfast: "⚡", // Energy
    lunch: "🔋", // Battery
    dinner: "🌙", // Moon
    snack: "💊", // Pill/Potion
  };
  return icons[id] || "❓";
};

// === Utils ===
const debounce = (func, delay) => {
  let timeoutId;
  return function (...args) {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => func.apply(this, args), delay);
  };
};

// === Data ===
const selectedMealTime = ref("아침");
const foodName = ref("");
const selectedFoodList = ref([]);
const memo = ref("");
const photoPreview = ref(null);
const fileInput = ref(null);
const suggestions = ref([]);
const isLoading = ref(false);
const selectedFoodIndex = ref(0);
const isSelectingFood = ref(false);

const mealTimes = [
  { id: "breakfast", name: "아침" },
  { id: "lunch", name: "점심" },
  { id: "dinner", name: "저녁" },
  { id: "snack", name: "간식" },
];

const formattedDate = computed(() => dayjs().format("YYYY-MM-DD HH:mm:ss"));

// === API Logic ===
async function fetchSuggestions(query) {
  isLoading.value = true;
  let suggestionsList = [];
  try {
    const url = `${API_ENDPOINT}/food?foodName=${encodeURIComponent(query)}`;
    const response = await fetch(url);
    if (!response.ok) throw new Error("API Error");
    const data = await response.json();
    if (Array.isArray(data)) {
      suggestionsList = data
        .filter((food) => food && food.name && food.name.includes(query.trim()))
        .filter(
          (food, index, self) =>
            food.name && self.findIndex((f) => f.name === food.name) === index
        );
    }
  } catch (error) {
    console.error("Scan Failed:", error);
  } finally {
    isLoading.value = false;
    return suggestionsList;
  }
}

const debouncedSearch = debounce(async (query) => {
  if (query.trim().length < 1) {
    suggestions.value = [];
    return;
  }
  if (isSelectingFood.value) return;
  const results = await fetchSuggestions(query.trim());
  suggestions.value = results;
  selectedFoodIndex.value = 0;
}, 300);

const handleInput = (event) => {
  playSound("type");
  foodName.value = event.target.value;
  if (isSelectingFood.value) return;
  debouncedSearch(foodName.value);
};

// === Logic ===
const selectMealTime = (name) => {
  playSound("blip");
  selectedMealTime.value = name;
};

const addFood = (food) => {
  playSound("coin");
  if (food && typeof food === "object" && food["name"]) {
    selectedFoodList.value.push({ ...food, servings: 1 });
  } else if (foodName.value.trim() !== "") {
    selectedFoodList.value.push({ name: foodName.value.trim(), servings: 1 });
  }
  foodName.value = "";
  suggestions.value = [];
  selectedFoodIndex.value = 0;
};

const manualAdd = () => {
  playSound("blip");
  addFood();
};

const changeFoodCount = (index, delta) => {
  playSound("blip");
  const foodItem = selectedFoodList.value[index];
  const servings = foodItem.servings + delta;
  if (servings >= 1) foodItem.servings = servings;
};

const removeFood = (index) => {
  playSound("blip");
  selectedFoodList.value.splice(index, 1);
};

function selectFood(food) {
  isSelectingFood.value = true;
  addFood(food);
  setTimeout(() => {
    isSelectingFood.value = false;
  }, 100);
}

const triggerFileInput = () => {
  playSound("blip");
  fileInput.value?.click();
};
const handleFileUpload = (event) => {
  const file = event.target.files?.[0];
  if (file) {
    const reader = new FileReader();
    reader.onload = (e) => {
      playSound("coin");
      photoPreview.value = e.target?.result;
    };
    reader.readAsDataURL(file);
  }
};
const removePhoto = () => {
  playSound("blip");
  photoPreview.value = null;
  if (fileInput.value) fileInput.value.value = "";
};

const saveMeal = async () => {
  playSound("save");
  const mealData = {
    memberId: 1,
    mealTime: selectedMealTime.value,
    foods: selectedFoodList.value,
  };
  try {
    const response = await fetch(`${API_ENDPOINT}/diets/meal`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(mealData),
    });
    if (!response.ok) throw new Error("Save Failed");
    setTimeout(() => closeModal(), 500);
  } catch (error) {
    console.error("Critical Failure:", error);
    closeModal();
  }
};

function handleKeydown(event) {
  const maxIndex = suggestions.value.length - 1;
  if (event.key === "ArrowDown") {
    event.preventDefault();
    selectedFoodIndex.value = Math.min(maxIndex, selectedFoodIndex.value + 1);
  } else if (event.key === "ArrowUp") {
    event.preventDefault();
    selectedFoodIndex.value = Math.max(0, selectedFoodIndex.value - 1);
  } else if (event.key === "Enter") {
    event.preventDefault();
    if (suggestions.value.length > 0 && selectedFoodIndex.value >= 0) {
      selectFood(suggestions.value[selectedFoodIndex.value]);
    } else if (foodName.value.trim() !== "") {
      addFood();
    }
  }
}

const closeModal = () => {
  emit("close");
};
const handleOverlayClick = (e) => {
  if (e.target === e.currentTarget) closeModal();
};

onMounted(() => {
  document.body.style.overflow = "hidden";
});
onUnmounted(() => {
  document.body.style.overflow = "";
});
</script>

<style scoped>
/* 레트로 폰트 */
@import url("https://cdn.jsdelivr.net/gh/neodgm/neodgm-webfont@latest/neodgm/style.css");

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.9);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  font-family: "NeoDunggeunmo", monospace;
  color: #00ff00; /* Terminal Green */
}

/* Scanlines */
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
  z-index: 1;
}

.retro-modal {
  background: #0d1117;
  width: 95%;
  max-width: 480px;
  max-height: 90vh;
  border: 4px solid #30363d;
  box-shadow: 0 0 20px rgba(0, 255, 0, 0.2);
  display: flex;
  flex-direction: column;
  z-index: 2;
  overflow: hidden;
  animation: modalPop 0.2s cubic-bezier(0.18, 0.89, 0.32, 1.28);
}
@keyframes modalPop {
  from {
    transform: scale(0.9);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

/* Header */
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
.pixel-close-btn:active {
  transform: translate(2px, 2px);
  box-shadow: none;
}

.modal-body {
  padding: 15px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.terminal-text {
  font-size: 0.8rem;
  color: #58a6ff;
  margin-bottom: 5px;
  line-height: 1.4;
  border-bottom: 1px dashed #30363d;
  padding-bottom: 10px;
}

/* Sections */
.section-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.pixel-label {
  font-size: 0.9rem;
  color: #ffd700;
  text-shadow: 1px 1px 0 #000;
}

/* 1. Mission Grid (Meal Time) */
.mission-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}
.mission-card {
  background: #21262d;
  border: 2px solid #30363d;
  color: #8b949e;
  padding: 10px 5px;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  position: relative;
  transition: all 0.1s;
}
.mission-card.active {
  background: #238636;
  color: #fff;
  border-color: #fff;
  box-shadow: 0 0 10px #238636;
}
.mission-icon {
  font-size: 1.5rem;
}
.mission-name {
  font-size: 0.8rem;
  font-weight: bold;
}
.selection-indicator {
  position: absolute;
  top: 2px;
  right: 2px;
  font-size: 0.6rem;
  color: #ffd700;
  animation: blink 1s infinite;
}
@keyframes blink {
  50% {
    opacity: 0;
  }
}

/* 2. Input Box */
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
.retro-btn-sm {
  background: #1f6feb;
  color: #fff;
  border: 1px solid #fff;
  padding: 4px 10px;
  font-family: inherit;
  font-size: 0.8rem;
  cursor: pointer;
}
.loading-status {
  color: #ff0055;
  font-size: 0.8rem;
  margin-right: 10px;
  animation: blink 0.5s infinite;
}

/* Dropdown */
.retro-dropdown {
  position: absolute;
  top: 100%;
  left: -2px;
  width: calc(100% + 4px);
  background: #0d1117;
  border: 2px solid #1f6feb;
  list-style: none;
  padding: 0;
  margin: 0;
  z-index: 10;
  max-height: 200px;
  overflow-y: auto;
}
.dropdown-item {
  padding: 10px;
  border-bottom: 1px solid #30363d;
  cursor: pointer;
  color: #c9d1d9;
}
.dropdown-item.active {
  background: #1f6feb;
  color: #fff;
}

/* Inventory */
.inventory-box {
  border: 2px solid #30363d;
  background: #161b22;
  padding: 10px;
  min-height: 80px;
}
.inventory-header {
  font-size: 0.7rem;
  color: #8b949e;
  text-align: center;
  margin-bottom: 10px;
  border-bottom: 1px solid #30363d;
  padding-bottom: 5px;
}
.empty-msg {
  text-align: center;
  color: #484f58;
  padding: 10px;
  font-size: 0.8rem;
}
.inventory-slot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #21262d;
  border: 1px solid #30363d;
  padding: 5px 8px;
  margin-bottom: 5px;
}
.slot-info {
  display: flex;
  align-items: center;
  gap: 8px;
}
.slot-name {
  color: #fff;
  font-size: 0.9rem;
}
.slot-controls {
  display: flex;
  align-items: center;
  gap: 5px;
}
.qty-btn {
  width: 20px;
  height: 20px;
  background: #000;
  border: 1px solid #8b949e;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.slot-qty {
  min-width: 25px;
  text-align: center;
  color: #ffd700;
  font-size: 0.9rem;
}
.trash-btn {
  background: none;
  border: none;
  font-size: 1rem;
  cursor: pointer;
  margin-left: 5px;
}

/* 3. Evidence (Photo) */
.evidence-box {
  border: 2px dashed #30363d;
  background: #0d1117;
  padding: 15px;
  text-align: center;
  cursor: pointer;
  position: relative;
}
.evidence-box:hover {
  border-color: #58a6ff;
  background: #161b22;
}
.placeholder-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #8b949e;
  gap: 5px;
  font-size: 0.8rem;
}
.scan-icon {
  font-size: 1.5rem;
  color: #58a6ff;
}

.preview-content img {
  width: 100%;
  max-height: 200px;
  object-fit: contain;
  border: 1px solid #30363d;
}
.scan-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  padding: 10px;
  box-sizing: border-box;
}
.corner {
  position: absolute;
  width: 15px;
  height: 15px;
  border-color: #ff0055;
  border-style: solid;
}
.tl {
  top: 10px;
  left: 10px;
  border-width: 2px 0 0 2px;
}
.tr {
  top: 10px;
  right: 10px;
  border-width: 2px 2px 0 0;
}
.bl {
  bottom: 10px;
  left: 10px;
  border-width: 0 0 2px 2px;
}
.br {
  bottom: 10px;
  right: 10px;
  border-width: 0 2px 2px 0;
}
.rec-badge {
  position: absolute;
  top: 15px;
  left: 15px;
  color: #ff0055;
  font-size: 0.7rem;
  animation: blink 1s infinite;
  background: rgba(0, 0, 0, 0.5);
  padding: 2px 5px;
}
.delete-evidence-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  background: #ff0055;
  color: #fff;
  border: 1px solid #fff;
  font-family: inherit;
  font-size: 0.7rem;
  padding: 4px 8px;
  cursor: pointer;
}

/* 4. Memo */
.retro-textarea {
  width: 100%;
  background: #0d1117;
  border: 2px solid #30363d;
  color: #fff;
  padding: 10px;
  font-family: inherit;
  font-size: 0.9rem;
  outline: none;
  box-sizing: border-box;
}
.retro-textarea:focus {
  border-color: #58a6ff;
}

/* Footer */
.footer-actions {
  margin-top: 10px;
  text-align: center;
}
.retro-btn-lg {
  width: 100%;
  background: #238636;
  color: #fff;
  border: 2px solid #fff;
  padding: 15px;
  font-family: inherit;
  cursor: pointer;
  box-shadow: 4px 4px 0 #000;
  position: relative;
  overflow: hidden;
}
.retro-btn-lg:active {
  transform: translate(4px, 4px);
  box-shadow: none;
}
.btn-text {
  font-size: 1.1rem;
  font-weight: bold;
  letter-spacing: 1px;
}

/* Mobile Optimization */
@media (max-width: 390px) {
  .mission-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
