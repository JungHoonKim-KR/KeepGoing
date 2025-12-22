<template>
  <div class="calendar-view retro-theme">

    <header class="header">
      <div class="retro-box month-control">
        <button @click="changeMonth(-1)" class="pixel-arrow">◀</button>
        <div class="month-display" @click.stop="openYearMonthModal">
          <span class="value">{{ currentYear }}.{{ String(currentMonth + 1).padStart(2, "0") }}</span>
          <!-- <span class="blink-cursor">_</span> -->
        </div>
        <button @click="changeMonth(1)" class="pixel-arrow">▶</button>
      </div>

      <div class="category-tabs">
        <button 
          v-for="cat in categories" 
          :key="cat.id"
          class="tab-btn"
          :class="{ active: currentCategory === cat.id }"
          @click="changeCategory(cat.id)"
        >
          {{ cat.label }}
        </button>
      </div>
    </header>

    <div class="content">
      <div class="legend-box">
        <div class="pixel-label-sm">EVALUATION GUIDE</div>
        <div class="tracking-states">
          <div v-for="grade in grades" :key="grade.key" class="state-chip" :class="grade.key">
            <img :src="grade.src" class="chip-img pixelated" alt="icon" />
            <span class="chip-text">{{ grade.label }}</span>
          </div>
        </div>
      </div>

      <div class="calendar-frame">
        <div class="frame-decor tl"></div><div class="frame-decor tr"></div>
        <div class="frame-decor bl"></div><div class="frame-decor br"></div>

        <div class="days-of-week">
          <span v-for="day in daysOfWeek" :key="day" class="weekday-header" :class="{ weekend: day === '일' || day === '토' }">
            {{ day }}
          </span>
        </div>

        <div class="date-grid">
          <div v-for="(day, index) in calendarDays" :key="index" class="date-cell-wrapper">
            <button
              v-if="day.isCurrentMonth"
              class="date-tile"
              :class="{ 
                'is-today': day.isToday, 
                'is-selected': day.isSelected,
                'long-pressing': isLongPress && pressingDateKey === day.dateKey,
                'has-record': !!getDayEvaluation(day.dateKey) 
              }"
              @mousedown.prevent="startPress(day)"
              @touchstart.prevent="startPress(day)"
              @mouseup.prevent="endPress(day)"
              @touchend.prevent="endPress(day)"
              @mouseleave.prevent="cancelPress"
              @touchcancel.prevent="cancelPress"
            >
              <span class="tile-number">{{ day.day }}</span>

              <div v-if="getDayEvaluation(day.dateKey)" class="tile-loot">
                <img 
                  :src="getGradeObj(getDayEvaluation(day.dateKey))?.src"
                  class="loot-img pixelated"
                  alt="stamp"
                />
              </div>

              <div v-if="day.isToday" class="player-cursor">P1</div>
            </button>
            <div v-else class="empty-tile"></div>
          </div>
        </div>
      </div>
    </div>

    <Footer />

    <Teleport to="body">
      <div v-if="isColorModalOpen" class="modal-overlay" @click.self="closeColorModal">
        <div class="retro-modal color-select-modal">
          <h2 class="modal-title">{{ getCategoryLabel(currentCategory) }} CHECK!</h2>
          <div class="modal-subtitle">{{ modalTargetDay?.dateKey }}</div>

          <div class="action-list">
            <button
              v-for="grade in grades"
              :key="grade.key"
              class="action-btn"
              :class="{ active: getDayEvaluation(modalTargetDay?.dateKey) === grade.key }"
              @click="selectEvaluation(grade.key)"
            >
              <img :src="grade.src" class="action-img pixelated" alt="icon" />
              <span class="action-label">{{ grade.label }}</span>
              <span class="action-check" v-if="getDayEvaluation(modalTargetDay?.dateKey) === grade.key">✔</span>
            </button>
          </div>
          
          <button 
            v-if="getDayEvaluation(modalTargetDay?.dateKey)" 
            @click="removeEvaluation" 
            class="retro-btn delete-btn"
          >
            RESET
          </button>
          
          <button @click="closeColorModal" class="retro-btn close-btn">CLOSE</button>
        </div>
      </div>
    </Teleport>

    <Teleport to="body">
      <div v-if="isYearMonthModalOpen" class="modal-overlay" @click.self="closeYearMonthModal">
        <div class="retro-modal time-modal">
          <h2 class="modal-title">TIME WARP</h2>
          <div class="control-group">
            <label>YEAR</label>
            <div class="stepper">
              <button @click="tempSelectedYear--" class="step-btn">-</button>
              <span class="step-val">{{ tempSelectedYear }}</span>
              <button @click="tempSelectedYear++" class="step-btn">+</button>
            </div>
          </div>
          <div class="control-group">
            <label>MONTH</label>
            <div class="month-grid">
              <button v-for="month in availableMonths" :key="month" :class="['month-chip', { active: tempSelectedMonth === month }]" @click="tempSelectedMonth = month">
                {{ month + 1 }}
              </button>
            </div>
          </div>
          <div class="modal-actions">
            <button @click="closeYearMonthModal" class="retro-btn cancel">CANCEL</button>
            <button @click="applyYearMonth" class="retro-btn confirm">WARP</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useAuthStore } from "@/stores/authStore";
import { useConfigStore } from "@/stores/configStore";
import Footer from "../components/utils/Footer.vue";

const router = useRouter();
const route = useRoute();
const config = useConfigStore();
const authStore = useAuthStore();

const MEMBER_ID = authStore.memberId;
const API_ENDPOINT = config.API_ENDPOINT;

// === 🔊 Sound FX ===
const playSound = (type) => {
  const AudioContext = window.AudioContext || window.webkitAudioContext;
  if (!AudioContext) return;
  const ctx = new AudioContext();
  const osc = ctx.createOscillator();
  const gain = ctx.createGain();
  osc.connect(gain);
  gain.connect(ctx.destination);
  const now = ctx.currentTime;

  if (type === "select") {
    osc.type = "square";
    osc.frequency.setValueAtTime(440, now);
    gain.gain.setValueAtTime(0.05, now);
    gain.gain.exponentialRampToValueAtTime(0.01, now + 0.1);
    osc.start(now);
    osc.stop(now + 0.1);
  } else if (type === "warp") {
    osc.type = "sawtooth";
    osc.frequency.setValueAtTime(200, now);
    osc.frequency.linearRampToValueAtTime(800, now + 0.2);
    gain.gain.setValueAtTime(0.05, now);
    gain.gain.linearRampToValueAtTime(0, now + 0.2);
    osc.start(now);
    osc.stop(now + 0.2);
  }
};

// --- 1. 데이터 정의 ---

// 이미지 경로 헬퍼
const getAssetUrl = (path) => {
  return new URL(path, import.meta.url).href;
};

// 카테고리 정의
const categories = [
  { id: 'DIET', label: '식단' },
  { id: 'WATER', label: '수분' },
  { id: 'WEIGHT', label: '몸무게' },
  { id: 'BOWEL', label: '배변' },
  { id: 'PERIOD', label: '생리' },
];
const currentCategory = ref('DIET');

// 🌟 [수정됨] 모든 등급에 이미지 경로 할당 (SOSO.png 가정)
const grades = [
  { 
    key: 'GOOD', 
    label: '좋음', 
    src: getAssetUrl('../assets/images/stickers/GOOD.png'), 
    color: '#4CAF50' 
  },
  { 
    key: 'SOSO', 
    label: '보통', 
    src: getAssetUrl('../assets/images/stickers/SOSO.png'), 
    color: '#F5C857' 
  },
  { 
    key: 'BAD',  
    label: '나쁨', 
    src: getAssetUrl('../assets/images/stickers/BAD.png'), 
    color: '#FF3838' 
  },
];

// 데이터 저장소
const dailyDataMap = ref({});

const currentDate = ref(new Date());
const selectedDate = ref(new Date().toDateString());
const isColorModalOpen = ref(false);
const modalTargetDay = ref(null);
const isYearMonthModalOpen = ref(false);
const tempSelectedYear = ref(currentDate.value.getFullYear());
const tempSelectedMonth = ref(currentDate.value.getMonth());

// --- 2. Helper Methods ---

const getCategoryLabel = (catId) => categories.find(c => c.id === catId)?.label;

const getDayEvaluation = (dateKey) => {
  if (!dailyDataMap.value[dateKey]) return null;
  return dailyDataMap.value[dateKey][currentCategory.value];
};

// 🌟 [수정됨] 등급 키로 전체 객체 반환 (src 사용을 위해)
const getGradeObj = (gradeKey) => {
  return grades.find(g => g.key === gradeKey);
};

const changeCategory = (catId) => {
  playSound("select");
  currentCategory.value = catId;
};

// --- 3. API 연동 ---

const fetchEvaluations = async (year, month) => {
  const apiMonth = month + 1;
  const url = `${API_ENDPOINT}/diets/evaluations?memberId=${MEMBER_ID}&year=${year}&month=${apiMonth}`;
  
  try {
    const res = await fetch(url);
    if (!res.ok) throw new Error("Fetch failed");
    const data = await res.json();
    
    const map = {};
    data.forEach(item => {
      if (!map[item.date]) map[item.date] = {};
      map[item.date][item.category] = item.evaluation;
    });
    
    dailyDataMap.value = map;
  } catch (e) {
    console.error(e);
    dailyDataMap.value = {};
  }
};

const selectEvaluation = async (gradeKey) => {
  playSound("select");
  if (!modalTargetDay.value) return;
  const dateKey = modalTargetDay.value.dateKey;
  
  // UI 즉시 반영
  if (!dailyDataMap.value[dateKey]) dailyDataMap.value[dateKey] = {};
  dailyDataMap.value[dateKey][currentCategory.value] = gradeKey;
  
  closeColorModal();
  
  // 서버 전송
  try {
    await fetch(`${API_ENDPOINT}/diets/evaluation`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        memberId: MEMBER_ID,
        date: dateKey,
        category: currentCategory.value, 
        evaluation: gradeKey
      })
    });
  } catch (e) {
    console.error("저장 실패");
    fetchEvaluations(currentYear.value, currentMonth.value);
  }
};

const removeEvaluation = async () => {
  playSound("select");
  if (!modalTargetDay.value) return;
  const dateKey = modalTargetDay.value.dateKey;
  
  if (dailyDataMap.value[dateKey]) {
    delete dailyDataMap.value[dateKey][currentCategory.value];
  }
  closeColorModal();
  
  try {
    const url = `${API_ENDPOINT}/diets/evaluation?memberId=${MEMBER_ID}&date=${dateKey}&category=${currentCategory.value}`;
    await fetch(url, { method: "DELETE" });
  } catch (e) {
    fetchEvaluations(currentYear.value, currentMonth.value);
  }
};

// --- 4. 캘린더 로직 ---

const currentYear = computed(() => currentDate.value.getFullYear());
const currentMonth = computed(() => currentDate.value.getMonth());
const availableYears = computed(() => {
  const y = new Date().getFullYear();
  return Array.from({ length: 11 }, (_, i) => y - 5 + i);
});
const availableMonths = computed(() => Array.from({ length: 12 }, (_, i) => i));
const daysOfWeek = ["SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"];

const calendarDays = computed(() => {
  const year = currentDate.value.getFullYear();
  const month = currentDate.value.getMonth();
  const firstDay = new Date(year, month, 1).getDay();
  const lastDate = new Date(year, month + 1, 0).getDate();
  const days = [];

  for (let i = 0; i < firstDay; i++) {
    days.push({ day: "", isCurrentMonth: false, dateKey: null });
  }

  for (let i = 1; i <= lastDate; i++) {
    const fullDate = new Date(year, month, i);
    const dateKey = `${fullDate.getFullYear()}-${String(fullDate.getMonth() + 1).padStart(2, "0")}-${String(fullDate.getDate()).padStart(2, "0")}`;

    days.push({
      day: i,
      isCurrentMonth: true,
      isToday: fullDate.toDateString() === new Date().toDateString(),
      isSelected: fullDate.toDateString() === selectedDate.value,
      dateKey: dateKey,
    });
  }
  const totalCells = 42;
  const remainingCells = totalCells - days.length;
  for (let i = 0; i < remainingCells; i++) {
    days.push({ day: "", isCurrentMonth: false, dateKey: null });
  }
  return days;
});

const changeMonth = (delta) => {
  playSound("select");
  const newDate = new Date(currentDate.value);
  newDate.setMonth(newDate.getMonth() + delta);
  currentDate.value = newDate;
  fetchEvaluations(newDate.getFullYear(), newDate.getMonth());
};

// 롱프레스 로직 (마우스 뗄 때 이동 방지 로직 포함)
const pressTimer = ref(null);
const isLongPress = ref(false);
const pressingDateKey = ref(null);

const startPress = (day) => {
  if (!day.dateKey) return;
  if (pressTimer.value) clearTimeout(pressTimer.value);
  isLongPress.value = false;
  pressingDateKey.value = day.dateKey;

  pressTimer.value = setTimeout(() => {
    isLongPress.value = true;
    playSound("warp");
    modalTargetDay.value = day;
    isColorModalOpen.value = true;
    pressingDateKey.value = null; // 모달 뜨면 흔들림 중지
  }, 500);
};

const endPress = (day) => {
  clearTimeout(pressTimer.value);
  pressTimer.value = null;
  pressingDateKey.value = null;

  // 🌟 [핵심] 롱프레스가 아니고, 모달도 안 떴을 때만 이동
  if (!isLongPress.value && !isColorModalOpen.value) {
    playSound("select");
    selectDayAndNavigate(day);
  }
  isLongPress.value = false;
};

const cancelPress = () => {
  if (pressTimer.value) clearTimeout(pressTimer.value);
  pressTimer.value = null;
  isLongPress.value = false;
  pressingDateKey.value = null;
};

const selectDayAndNavigate = (day) => {
  if (day.dateKey) {
    selectedDate.value = day.dateKey;
    router.push({ path: "/", query: { date: day.dateKey } });
  }
};

const closeColorModal = () => {
  isColorModalOpen.value = false;
  modalTargetDay.value = null;
};

const openYearMonthModal = () => {
  playSound("select");
  tempSelectedYear.value = currentDate.value.getFullYear();
  tempSelectedMonth.value = currentDate.value.getMonth();
  isYearMonthModalOpen.value = true;
};
const closeYearMonthModal = () => { isYearMonthModalOpen.value = false; };

const applyYearMonth = () => {
  playSound("warp");
  const currentDayOfMonth = currentDate.value.getDate();
  let newDate = new Date(tempSelectedYear.value, tempSelectedMonth.value, currentDayOfMonth);
  if (newDate.getMonth() !== tempSelectedMonth.value) {
    newDate = new Date(tempSelectedYear.value, tempSelectedMonth.value + 1, 0);
  }
  currentDate.value = newDate;
  closeYearMonthModal();
  fetchEvaluations(newDate.getFullYear(), newDate.getMonth());
};

watch(() => router.currentRoute.value.path, () => {
  if (isColorModalOpen.value) closeColorModal();
});

onMounted(() => {
  fetchEvaluations(currentYear.value, currentMonth.value);
});
</script>

<style scoped>
@import url("https://cdn.jsdelivr.net/gh/neodgm/neodgm-webfont@latest/neodgm/style.css");

.calendar-view.retro-theme {
  min-height: 100vh;
  padding-bottom: 80px;
  background-color: #202028;
  font-family: "NeoDunggeunmo", monospace;
  color: #e0e0e0;
  overflow-x: hidden;
}


/* 헤더 */
.header { padding: 1rem 1rem 0 1rem; }
.retro-box {
  background: #000;
  border: 2px solid #fff;
  box-shadow: 4px 4px 0 #000;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  margin-bottom: 1rem;
}
.pixel-arrow {
  background: #333; color: #fff; border: 1px solid #fff; width: 32px; height: 32px; cursor: pointer;
}
.month-display { display: flex; align-items: baseline; gap: 5px; cursor: pointer; }
.value { color: #00e5ff; font-size: 1.2rem; text-shadow: 0 0 5px #00e5ff; }
.blink-cursor { animation: blink 1s infinite; }

/* 탭 스타일 */
.category-tabs {
  display: flex;
  gap: 5px;
  padding-bottom: 1rem;
  overflow-x: auto;
}
.tab-btn {
  background: #111;
  color: #888;
  border: 2px solid #555;
  padding: 8px 10px;
  font-family: inherit;
  cursor: pointer;
  white-space: nowrap;
  font-size: 0.8rem;
  flex: 1;
  transition: all 0.2s;
}
.tab-btn.active {
  background: #00e5ff;
  color: #000;
  border-color: #fff;
  font-weight: bold;
  box-shadow: 0 0 8px #00e5ff;
  transform: translateY(-2px);
}

/* 범례 */
.content { padding: 0 1rem; }
.legend-box { margin-bottom: 1rem; }
.pixel-label-sm {
  font-size: 0.7rem; color: #ffd700; margin-bottom: 5px; border-bottom: 1px dashed #555; display: inline-block;
}
.tracking-states { display: flex; gap: 10px; }
.state-chip {
  background: #222; border: 1px solid #555; padding: 4px 8px; border-radius: 4px; display: flex; align-items: center; gap: 5px; font-size: 0.7rem;
}
.state-chip.GOOD { border-color: #4CAF50; color: #4CAF50; }
.state-chip.SOSO { border-color: #F5C857; color: #F5C857; }
.state-chip.BAD  { border-color: #FF3838; color: #FF3838; }

/* 캘린더 프레임 */
.calendar-frame {
  background: #111; border: 2px solid #333; padding: 10px; position: relative; margin-bottom: 2rem;
}
.frame-decor { position: absolute; width: 10px; height: 10px; border: 2px solid #fff; }
.tl { top: -2px; left: -2px; border-right: none; border-bottom: none; }
.tr { top: -2px; right: -2px; border-left: none; border-bottom: none; }
.bl { bottom: -2px; left: -2px; border-right: none; border-top: none; }
.br { bottom: -2px; right: -2px; border-left: none; border-top: none; }

.days-of-week {
  display: grid; grid-template-columns: repeat(7, 1fr); text-align: center; margin-bottom: 10px; border-bottom: 1px solid #333;
}
.weekday-header { font-size: 0.8rem; color: #888; padding: 5px 0; }
.weekday-header.weekend { color: #ff0055; }

.date-grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 4px; }
.date-tile {
  width: 100%; aspect-ratio: 1; background: #222; border: 1px solid #444; position: relative; cursor: pointer; display: flex; justify-content: center; align-items: center; padding: 0; color: #aaa;
}
.date-tile:active { transform: translateY(2px); }
.tile-number { position: absolute; top: 2px; left: 3px; font-size: 0.7rem; }

.is-today { border-color: #00e5ff; background: #001a1a; box-shadow: inset 0 0 5px #00e5ff; }
.is-selected { background: #333; border: 2px solid #fff; color: #fff; }
.has-record { background: #2a2a2a; border-color: #666; }
.long-pressing { animation: shake 0.5s infinite; background: #ff0055 !important; color: #fff; }

@keyframes shake {
  0% { transform: translate(1px, 1px) rotate(0deg); }
  10% { transform: translate(-1px, -2px) rotate(-1deg); }
  30% { transform: translate(3px, 2px) rotate(0deg); }
  50% { transform: translate(-1px, 2px) rotate(-1deg); }
  70% { transform: translate(3px, 1px) rotate(-1deg); }
  90% { transform: translate(1px, 2px) rotate(0deg); }
  100% { transform: translate(1px, -2px) rotate(-1deg); }
}

.tile-loot {
  width: 70%; height: 70%; display: flex; align-items: center; justify-content: center; z-index: 1;
}
.player-cursor {
  position: absolute; bottom: 1px; right: 1px; font-size: 0.5rem; background: #00e5ff; color: #000; padding: 0 2px;
}
.empty-tile { background: transparent; border: 1px dashed #222; opacity: 0.5; }

/* 🌟 이미지 스타일 추가 */
.pixelated {
  image-rendering: pixelated; 
}
.loot-img {
  width: 80%; height: 80%; object-fit: contain;
}
.chip-img {
  width: 16px; height: 16px; margin-right: 5px;
}
.action-img {
  width: 24px; height: 24px; margin-right: 10px;
}

/* 모달 */
.modal-overlay {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0, 0, 0, 0.8); display: flex; justify-content: center; align-items: center; z-index: 1000;
}
.retro-modal {
  background: #202028; border: 4px solid #fff; padding: 1.5rem; width: 90%; max-width: 350px; box-shadow: 8px 8px 0 rgba(0, 0, 0, 0.5); color: #fff;
}
.modal-title { color: #ff0055; margin-top: 0; text-align: center; font-size: 1.2rem; text-shadow: 2px 2px #000; }
.modal-subtitle { text-align: center; color: #888; font-size: 0.8rem; margin-bottom: 1rem; border-bottom: 1px dashed #555; padding-bottom: 0.5rem; }

.action-list { display: flex; flex-direction: column; gap: 10px; margin-bottom: 1rem; }
.action-btn {
  background: #000; border: 2px solid #555; color: #fff; padding: 12px; display: flex; align-items: center; gap: 10px; cursor: pointer; transition: all 0.2s; font-family: inherit;
}
.action-btn:hover, .action-btn.active { border-color: #00e5ff; background: #111; }
.action-btn.active .action-label { color: #00e5ff; font-weight: bold; }
.action-check { margin-left: auto; font-size: 0.9rem; color: #00e5ff; }

.delete-btn { width: 100%; margin-top: 10px; background: #333; color: #ff3838; border: 1px solid #ff3838; padding: 10px; cursor: pointer; font-family: inherit; }
.close-btn { width: 100%; margin-top: 10px; background: #000; color: #fff; border: 1px solid #fff; padding: 10px; cursor: pointer; font-family: inherit; }

/* 년/월 모달 등 기타 스타일 */
.time-modal { border-color: #00e5ff; }
.time-modal .modal-title { color: #00e5ff; }
.control-group { margin-bottom: 1.5rem; }
.control-group label { display: block; color: #ffd700; font-size: 0.8rem; margin-bottom: 5px; }
.stepper { display: flex; justify-content: center; align-items: center; gap: 10px; background: #000; padding: 5px; border: 1px solid #333; }
.step-btn { width: 30px; height: 30px; background: #333; color: #fff; border: 1px solid #fff; cursor: pointer; }
.step-val { font-size: 1.2rem; width: 80px; text-align: center; }
.month-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 5px; }
.month-chip { background: #222; color: #aaa; border: 1px solid #444; padding: 8px 0; cursor: pointer; font-family: inherit; }
.month-chip.active { background: #00e5ff; color: #000; border-color: #fff; }
.modal-actions { display: flex; gap: 10px; }
.retro-btn { flex: 1; padding: 10px; border: 2px solid #fff; font-family: inherit; cursor: pointer; font-weight: bold; }
.retro-btn.cancel { background: #333; color: #fff; }
.retro-btn.confirm { background: #00e5ff; color: #000; }

@keyframes blink { 50% { opacity: 0; } }
</style>