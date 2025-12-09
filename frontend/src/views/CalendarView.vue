<template>
  <div class="calendar-view retro-theme">
    <div class="scanlines"></div>

    <header class="header">
      <div class="retro-box month-control">
        <button @click="changeMonth(-1)" class="pixel-arrow">◀</button>

        <div class="month-display" @click.stop="openYearMonthModal">
          <span class="label">DATE:</span>
          <span class="value"
            >{{ currentYear }}.{{
              String(currentMonth + 1).padStart(2, "0")
            }}</span
          >
          <span class="blink-cursor">_</span>
        </div>

        <button @click="changeMonth(1)" class="pixel-arrow">▶</button>
      </div>
    </header>

    <div class="content">
      <div class="legend-box">
        <div class="pixel-label-sm">STATUS LEGEND</div>
        <div class="tracking-states">
          <div
            v-for="state in trackingStates"
            :key="state.key"
            class="state-chip"
            :class="state.key"
          >
            <span class="chip-icon">{{ state.emoji }}</span>
            <span class="chip-text">{{ state.label }}</span>
          </div>
        </div>
      </div>

      <div class="calendar-frame">
        <div class="frame-decor tl"></div>
        <div class="frame-decor tr"></div>
        <div class="frame-decor bl"></div>
        <div class="frame-decor br"></div>

        <div class="days-of-week">
          <span
            v-for="day in daysOfWeek"
            :key="day"
            class="weekday-header"
            :class="{ weekend: day === '일' || day === '토' }"
          >
            {{ day }}
          </span>
        </div>

        <div class="date-grid">
          <div
            v-for="(day, index) in calendarDays"
            :key="index"
            class="date-cell-wrapper"
          >
            <button
              v-if="day.isCurrentMonth"
              :class="[
                'date-tile',
                {
                  'is-today': day.isToday,
                  'is-selected': day.isSelected,
                  'has-record': day.records.length > 0,
                  'long-pressing':
                    isLongPress && pressingDateKey === day.dateKey,
                },
              ]"
              @mousedown.prevent="startPress(day)"
              @mouseup.prevent="endPress(day)"
              @mouseleave.prevent="cancelPress"
              @touchstart.prevent="startPress(day)"
              @touchend.prevent="endPress(day)"
              @touchcancel.prevent="cancelPress"
            >
              <span class="tile-number">{{ day.day }}</span>

              <div v-if="day.records.length > 0" class="tile-loot">
                <img
                  v-if="getRecordIconUrl(day.records)"
                  :src="getRecordIconUrl(day.records)"
                  class="loot-icon pixelated"
                />
                <span v-else class="loot-emoji">
                  {{ getRecordEmoji(day.records) }}
                </span>
              </div>

              <div v-if="day.isToday" class="player-cursor">P1</div>
            </button>

            <div v-else class="empty-tile"></div>
          </div>
        </div>
      </div>
    </div>

    <Footer />
  </div>

  <Teleport to="body">
    <div
      v-if="isColorModalOpen"
      class="modal-overlay"
      @click.self="closeColorModal"
    >
      <div class="retro-modal color-select-modal">
        <h2 class="modal-title">EVENT TRIGGERED!</h2>
        <div class="modal-subtitle">
          Day: {{ modalTargetDay?.day }} - Choose Action
        </div>

        <div class="action-list">
          <button
            v-for="state in trackingStates"
            :key="state.key"
            class="action-btn"
            :class="{ active: modalTargetDay?.records?.includes(state.key) }"
            @click="selectColorForRecord(state.key)"
          >
            <span class="action-icon">{{ state.emoji }}</span>
            <span class="action-label">{{ state.label }}</span>
            <span
              class="action-check"
              v-if="modalTargetDay?.records?.includes(state.key)"
              >[EQUIPPED]</span
            >
          </button>
        </div>
        <button @click="closeColorModal" class="retro-btn close-btn">
          CLOSE
        </button>
      </div>
    </div>
  </Teleport>

  <Teleport to="body">
    <div
      v-if="isYearMonthModalOpen"
      class="modal-overlay"
      @click.self="closeYearMonthModal"
    >
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
            <button
              v-for="month in availableMonths"
              :key="month"
              :class="['month-chip', { active: tempSelectedMonth === month }]"
              @click="tempSelectedMonth = month"
            >
              {{ month + 1 }}
            </button>
          </div>
        </div>

        <div class="modal-actions">
          <button @click="closeYearMonthModal" class="retro-btn cancel">
            CANCEL
          </button>
          <button @click="applyYearMonth" class="retro-btn confirm">
            WARP
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, computed, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import Footer from "../components/utils/Footer.vue";

const router = useRouter();
const route = useRoute();

// === 🔊 Sound FX (간단 버전) ===
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

// ----------------------------------------------------
// 1. 상태 관리
// ----------------------------------------------------
const currentDate = ref(new Date());
const selectedDate = ref(new Date().toDateString());
const pressTimer = ref(null);
const isLongPress = ref(false);
const pressingDateKey = ref(null); // 롱프레스 중인 날짜 시각효과용

const isColorModalOpen = ref(false);
const modalTargetDay = ref(null);
const isYearMonthModalOpen = ref(false);
const tempSelectedYear = ref(currentDate.value.getFullYear());
const tempSelectedMonth = ref(currentDate.value.getMonth());

// RPG 테마에 맞춘 트래킹 상태 (이모지 추가)
const trackingStates = ref([
  {
    key: "ate",
    label: "HP 회복 (식사)",
    color: "#4CAF50",
    emoji: "🍖", // RPG 고기
    icon: new URL("/src/assets/images/stickers/jinji.png", import.meta.url)
      .href,
  },
  {
    key: "burned",
    label: "EXP 획득 (운동)",
    color: "#F5C857",
    emoji: "⚔️", // 전투/운동
    icon: new URL("/src/assets/images/stickers/sad.png", import.meta.url).href,
  },
  {
    key: "weight",
    label: "RANK 갱신 (체중)",
    color: "#FF3838",
    emoji: "🏆", // 랭킹
    icon: new URL("/src/assets/images/stickers/smile.png", import.meta.url)
      .href,
  },
]);

// 임시 데이터
const dailyRecords = ref({
  "2025-11-16": ["ate"],
  "2025-11-22": ["burned"],
  "2025-11-28": ["weight"],
  "2025-12-05": ["ate"],
});

// ----------------------------------------------------
// 2. Computed
// ----------------------------------------------------
const availableYears = computed(() => {
  const currentYear = new Date().getFullYear();
  return Array.from({ length: 11 }, (_, i) => currentYear - 5 + i);
});

const availableMonths = computed(() => Array.from({ length: 12 }, (_, i) => i));

const currentYear = computed(() => currentDate.value.getFullYear());
const currentMonth = computed(() => currentDate.value.getMonth());

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
    // 로컬 시간 기준 날짜 문자열 생성 (YYYY-MM-DD)
    const yearStr = fullDate.getFullYear();
    const monthStr = String(fullDate.getMonth() + 1).padStart(2, "0");
    const dayStr = String(fullDate.getDate()).padStart(2, "0");
    const dateKey = `${yearStr}-${monthStr}-${dayStr}`;

    days.push({
      day: i,
      isCurrentMonth: true,
      isToday: fullDate.toDateString() === new Date().toDateString(),
      isSelected: fullDate.toDateString() === selectedDate.value,
      dateKey: dateKey,
      records: dailyRecords.value[dateKey] || [],
    });
  }

  const totalCells = 42;
  const remainingCells = totalCells - days.length;
  for (let i = 0; i < remainingCells; i++) {
    days.push({ day: "", isCurrentMonth: false, dateKey: null });
  }
  return days;
});

// ----------------------------------------------------
// 3. Methods
// ----------------------------------------------------
const changeMonth = (delta) => {
  playSound("select");
  const newDate = new Date(currentDate.value);
  newDate.setMonth(newDate.getMonth() + delta);
  currentDate.value = newDate;
};

const startPress = (day) => {
  if (!day.dateKey) return;
  if (pressTimer.value) clearTimeout(pressTimer.value);
  isLongPress.value = false;
  pressingDateKey.value = day.dateKey;

  pressTimer.value = setTimeout(() => {
    isLongPress.value = true;
    playSound("warp"); // 롱프레스 성공음
    openColorModal(day);
  }, 500);
};

const endPress = (day) => {
  clearTimeout(pressTimer.value);
  pressTimer.value = null;
  pressingDateKey.value = null;

  if (!isLongPress.value) {
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
    selectedDate.value = new Date(day.dateKey).toDateString();
    router.push({ path: "/", query: { date: day.dateKey } });
  }
};

const openColorModal = (day) => {
  modalTargetDay.value = day;
  isColorModalOpen.value = true;
};
const closeColorModal = () => {
  isColorModalOpen.value = false;
  modalTargetDay.value = null;
};

const selectColorForRecord = (recordKey) => {
  playSound("select");
  if (modalTargetDay.value && modalTargetDay.value.dateKey) {
    const dateKey = modalTargetDay.value.dateKey;
    const currentRecords = dailyRecords.value[dateKey] || [];

    // 토글 로직
    if (currentRecords.includes(recordKey)) {
      dailyRecords.value[dateKey] = [];
    } else {
      dailyRecords.value[dateKey] = [recordKey];
    }

    closeColorModal();
    // 강제 반응성 트리거 (Vue3 ref 객체 교체)
    dailyRecords.value = { ...dailyRecords.value };
  }
};

const getRecordIconUrl = (records) => {
  if (records && records.length > 0) {
    const state = trackingStates.value.find((s) => s.key === records[0]);
    // 이미지 파일이 실제 존재하는지 확인하기 어려우므로,
    // 예제에서는 일단 icon 속성을 반환. 없으면 이모지 사용.
    return state ? state.icon : "";
  }
  return "";
};

const getRecordEmoji = (records) => {
  if (records && records.length > 0) {
    const state = trackingStates.value.find((s) => s.key === records[0]);
    return state ? state.emoji : "🚩";
  }
  return "";
};

// 년/월 모달
const openYearMonthModal = () => {
  playSound("select");
  tempSelectedYear.value = currentDate.value.getFullYear();
  tempSelectedMonth.value = currentDate.value.getMonth();
  isYearMonthModalOpen.value = true;
};
const closeYearMonthModal = () => {
  isYearMonthModalOpen.value = false;
};

const applyYearMonth = () => {
  playSound("warp");
  const currentDayOfMonth = currentDate.value.getDate();
  // 말일 처리
  let newDate = new Date(
    tempSelectedYear.value,
    tempSelectedMonth.value,
    currentDayOfMonth
  );
  if (newDate.getMonth() !== tempSelectedMonth.value) {
    newDate = new Date(tempSelectedYear.value, tempSelectedMonth.value + 1, 0);
  }
  currentDate.value = newDate;
  closeYearMonthModal();
};

watch(
  () => router.currentRoute.value.path,
  () => {
    if (isColorModalOpen.value) closeColorModal();
  }
);
</script>

<style scoped>
/* 폰트: 둥근모꼴 */
@import url("https://cdn.jsdelivr.net/gh/neodgm/neodgm-webfont@latest/neodgm/style.css");

.calendar-view.retro-theme {
  min-height: 100vh;
  padding-bottom: 80px;
  background-color: #202028;
  font-family: "NeoDunggeunmo", monospace;
  color: #e0e0e0;
  overflow-x: hidden;
}

/* 스캔라인 */
.scanlines {
  position: fixed;
  top: 0;
  left: 0;
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
  z-index: 999;
}

/* 헤더 */
.header {
  padding: 1.5rem;
}
.retro-box {
  background: #000;
  border: 2px solid #fff;
  box-shadow: 4px 4px 0 #000;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
}
.pixel-arrow {
  background: #333;
  color: #fff;
  border: 1px solid #fff;
  width: 32px;
  height: 32px;
  cursor: pointer;
}
.pixel-arrow:active {
  background: #fff;
  color: #000;
}

.month-display {
  display: flex;
  align-items: baseline;
  gap: 5px;
  cursor: pointer;
}
.label {
  color: #888;
  font-size: 0.8rem;
}
.value {
  color: #00e5ff;
  font-size: 1.2rem;
  text-shadow: 0 0 5px #00e5ff;
}
.blink-cursor {
  animation: blink 1s infinite;
}

/* 범례 */
.content {
  padding: 0 1rem;
}
.legend-box {
  margin-bottom: 1rem;
}
.pixel-label-sm {
  font-size: 0.7rem;
  color: #ffd700;
  margin-bottom: 5px;
  border-bottom: 1px dashed #555;
  display: inline-block;
}
.tracking-states {
  display: flex;
  gap: 10px;
  overflow-x: auto;
  padding-bottom: 5px;
}
.state-chip {
  background: #222;
  border: 1px solid #555;
  padding: 4px 8px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 0.7rem;
}
.state-chip.ate {
  border-color: #4caf50;
  color: #4caf50;
}
.state-chip.burned {
  border-color: #f5c857;
  color: #f5c857;
}
.state-chip.weight {
  border-color: #ff3838;
  color: #ff3838;
}

/* 캘린더 프레임 */
.calendar-frame {
  background: #111;
  border: 2px solid #333;
  padding: 10px;
  position: relative;
  margin-bottom: 2rem;
}
.frame-decor {
  position: absolute;
  width: 10px;
  height: 10px;
  border: 2px solid #fff;
}
.tl {
  top: -2px;
  left: -2px;
  border-right: none;
  border-bottom: none;
}
.tr {
  top: -2px;
  right: -2px;
  border-left: none;
  border-bottom: none;
}
.bl {
  bottom: -2px;
  left: -2px;
  border-right: none;
  border-top: none;
}
.br {
  bottom: -2px;
  right: -2px;
  border-left: none;
  border-top: none;
}

.days-of-week {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  text-align: center;
  margin-bottom: 10px;
  border-bottom: 1px solid #333;
}
.weekday-header {
  font-size: 0.8rem;
  color: #888;
  padding: 5px 0;
}
.weekday-header.weekend {
  color: #ff0055;
}

/* 날짜 그리드 */
.date-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}
.date-tile {
  width: 100%;
  aspect-ratio: 1;
  background: #222;
  border: 1px solid #444;
  position: relative;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 0;
  color: #aaa;
}
.date-tile:active {
  transform: translateY(2px);
}
.tile-number {
  position: absolute;
  top: 2px;
  left: 3px;
  font-size: 0.7rem;
}

/* 상태별 타일 스타일 */
.is-today {
  border-color: #00e5ff;
  background: #001a1a;
  box-shadow: inset 0 0 5px #00e5ff;
}
.is-selected {
  background: #333;
  border: 2px solid #fff;
  color: #fff;
}
.has-record {
  background: #2a2a2a;
}

/* 롱프레스 효과 */
.long-pressing {
  animation: shake 0.5s infinite;
  background: #ff0055 !important;
  color: #fff;
}
@keyframes shake {
  0% {
    transform: translate(1px, 1px) rotate(0deg);
  }
  10% {
    transform: translate(-1px, -2px) rotate(-1deg);
  }
  20% {
    transform: translate(-3px, 0px) rotate(1deg);
  }
  30% {
    transform: translate(3px, 2px) rotate(0deg);
  }
  40% {
    transform: translate(1px, -1px) rotate(1deg);
  }
  50% {
    transform: translate(-1px, 2px) rotate(-1deg);
  }
  60% {
    transform: translate(-3px, 1px) rotate(0deg);
  }
  70% {
    transform: translate(3px, 1px) rotate(-1deg);
  }
  80% {
    transform: translate(-1px, -1px) rotate(1deg);
  }
  90% {
    transform: translate(1px, 2px) rotate(0deg);
  }
  100% {
    transform: translate(1px, -2px) rotate(-1deg);
  }
}

/* 아이템/플레이어 표시 */
.tile-loot {
  width: 70%;
  height: 70%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
}
.loot-icon {
  width: 100%;
  height: 100%;
  object-fit: contain;
}
.loot-emoji {
  font-size: 1.2rem;
  filter: drop-shadow(0 0 2px rgba(255, 255, 255, 0.5));
}
.player-cursor {
  position: absolute;
  bottom: 1px;
  right: 1px;
  font-size: 0.5rem;
  background: #00e5ff;
  color: #000;
  padding: 0 2px;
}
.empty-tile {
  background: transparent;
  border: 1px dashed #222;
  opacity: 0.5;
}

/* === 모달 공통 === */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  font-family: "NeoDunggeunmo", monospace;
}
.retro-modal {
  background: #202028;
  border: 4px solid #fff;
  padding: 1.5rem;
  width: 90%;
  max-width: 350px;
  box-shadow: 8px 8px 0 rgba(0, 0, 0, 0.5);
  color: #fff;
}
.modal-title {
  color: #ff0055;
  margin-top: 0;
  text-align: center;
  font-size: 1.2rem;
  text-shadow: 2px 2px #000;
}
.modal-subtitle {
  text-align: center;
  color: #888;
  font-size: 0.8rem;
  margin-bottom: 1rem;
  border-bottom: 1px dashed #555;
  padding-bottom: 0.5rem;
}

/* 기록 선택 모달 */
.action-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 1.5rem;
}
.action-btn {
  background: #000;
  border: 2px solid #555;
  color: #fff;
  padding: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}
.action-btn:hover,
.action-btn.active {
  border-color: #00e5ff;
  background: #111;
}
.action-btn.active .action-label {
  color: #00e5ff;
  font-weight: bold;
}
.action-check {
  margin-left: auto;
  font-size: 0.7rem;
  color: #00e5ff;
}
.close-btn {
  width: 100%;
}

/* 년/월 모달 */
.time-modal {
  border-color: #00e5ff;
}
.time-modal .modal-title {
  color: #00e5ff;
}
.control-group {
  margin-bottom: 1.5rem;
}
.control-group label {
  display: block;
  color: #ffd700;
  font-size: 0.8rem;
  margin-bottom: 5px;
}

.stepper {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  background: #000;
  padding: 5px;
  border: 1px solid #333;
}
.step-btn {
  width: 30px;
  height: 30px;
  background: #333;
  color: #fff;
  border: 1px solid #fff;
  cursor: pointer;
}
.step-val {
  font-size: 1.2rem;
  width: 80px;
  text-align: center;
}

.month-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 5px;
}
.month-chip {
  background: #222;
  color: #aaa;
  border: 1px solid #444;
  padding: 8px 0;
  cursor: pointer;
  font-family: inherit;
}
.month-chip.active {
  background: #00e5ff;
  color: #000;
  border-color: #fff;
}

.modal-actions {
  display: flex;
  gap: 10px;
}
.retro-btn {
  flex: 1;
  padding: 10px;
  border: 2px solid #fff;
  font-family: inherit;
  cursor: pointer;
  font-weight: bold;
}
.retro-btn.cancel {
  background: #333;
  color: #fff;
}
.retro-btn.confirm {
  background: #00e5ff;
  color: #000;
}
.retro-btn.close-btn {
  background: #ff0055;
  color: #fff;
}

/* Utils */
.pixelated {
  image-rendering: pixelated;
}
@keyframes blink {
  50% {
    opacity: 0;
  }
}
</style>
