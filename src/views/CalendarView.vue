<script setup>
import { ref, computed, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import Footer from "./utils/Footer.vue";
const router = useRouter();
const route = useRoute();

// ----------------------------------------------------
// 1. 상태 관리 및 Long Press 로직
// ----------------------------------------------------
const currentDate = ref(new Date());
const selectedDate = ref(new Date().toDateString());

// Long Press 관련 상태
const pressTimer = ref(null);
const isLongPress = ref(false);

// 모달 관련 상태
const isColorModalOpen = ref(false);
const modalTargetDay = ref(null);

// 💡 년/월 선택 모달 상태
const isYearMonthModalOpen = ref(false);
const tempSelectedYear = ref(currentDate.value.getFullYear());
const tempSelectedMonth = ref(currentDate.value.getMonth()); // 0부터 시작 (0: 1월)


// 트래킹 상태 정의
const trackingStates = ref([
  {
    key: "ate",
    label: "식사",
    color: "#4CAF50",
    icon: new URL("/src/assets/images/stickers/jinji.png", import.meta.url).href,
  },
  {
    key: "burned",
    label: "운동",
    color: "#F5C857",
    icon: new URL("/src/assets/images/stickers/sad.png", import.meta.url).href,
  },
  {
    key: "weight",
    label: "몸무게",
    color: "#FF3838",
    icon: new URL("/src/assets/images/stickers/smile.png", import.meta.url).href,
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
// 2. 날짜 로직 및 Computed 속성
// ----------------------------------------------------

// 💡 선택 가능한 년도와 월 목록
const availableYears = computed(() => {
  const currentYear = new Date().getFullYear();
  return Array.from({ length: 11 }, (_, i) => currentYear - 5 + i);
});

const availableMonths = computed(() => {
  return Array.from({ length: 12 }, (_, i) => i);
});


const displayMonth = computed(() => {
  return currentDate.value.toLocaleDateString("ko-KR", {
    year: "numeric",
    month: "long",
  });
});

const daysOfWeek = ["일", "월", "화", "수", "목", "금", "토"];

const calendarDays = computed(() => {
  const year = currentDate.value.getFullYear();
  const month = currentDate.value.getMonth();

  const firstDay = new Date(year, month, 1).getDay();
  const lastDate = new Date(year, month + 1, 0).getDate();

  const days = [];

  // 1. 이전 달의 빈 칸 채우기
  for (let i = 0; i < firstDay; i++) {
    days.push({ day: "", isCurrentMonth: false, dateKey: null });
  }

  // 2. 이번 달 날짜 채우기
  for (let i = 1; i <= lastDate; i++) {
    const fullDate = new Date(year, month, i);
    const dateKey = `${year}-${String(month + 1).padStart(2, "0")}-${String(i).padStart(2, "0")}`;

    days.push({
      day: i,
      isCurrentMonth: true,
      isToday: fullDate.toDateString() === new Date().toDateString(),
      isSelected: fullDate.toDateString() === selectedDate.value,
      dateKey: dateKey,
      records: dailyRecords.value[dateKey] || [],
    });
  }

  // 3. 다음 달의 빈 칸 채우기
  const totalCells = 42;
  const remainingCells = totalCells - days.length;
  for (let i = 0; i < remainingCells; i++) {
    days.push({ day: "", isCurrentMonth: false, dateKey: null });
  }

  return days;
});

// ----------------------------------------------------
// 3. 이벤트 핸들러 및 액션 함수
// ----------------------------------------------------

const changeMonth = (delta) => {
  const newDate = new Date(currentDate.value);
  newDate.setMonth(newDate.getMonth() + delta);
  currentDate.value = newDate;
};

const selectToday = () => {
  const today = new Date();
  currentDate.value = today;
  selectedDate.value = today.toDateString();
  selectDayAndNavigate({ dateKey: today.toISOString().slice(0, 10) });
};

const startPress = (day) => {
  if (!day.dateKey) return;

  if (pressTimer.value) clearTimeout(pressTimer.value);

  isLongPress.value = false;

  pressTimer.value = setTimeout(() => {
    isLongPress.value = true;
    openColorModal(day);
  }, 500);
};

const endPress = (day) => {
  clearTimeout(pressTimer.value);
  pressTimer.value = null;

  if (!isLongPress.value) {
    selectDayAndNavigate(day);
  }
  isLongPress.value = false;
};

const cancelPress = () => {
  if (pressTimer.value) clearTimeout(pressTimer.value);
  pressTimer.value = null;
  isLongPress.value = false;
};

const selectDayAndNavigate = (day) => {
  if (day.dateKey) {
    const selectedDateKey = day.dateKey;

    selectedDate.value = new Date(selectedDateKey).toDateString();

    router.push({
      path: "/",
      query: { date: selectedDateKey },
    });
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
  if (modalTargetDay.value && modalTargetDay.value.dateKey) {
    const dateKey = modalTargetDay.value.dateKey;

    const currentRecords = dailyRecords.value[dateKey] || [];
    const isCurrentlySelected = currentRecords.includes(recordKey);

    if (isCurrentlySelected) {
      // 이미 선택된 상태를 다시 클릭하면 해제 (토글 방식)
      dailyRecords.value[dateKey] = [];
    } else {
      // 새로운 상태를 클릭하면 이전 상태 해제 후 새로운 상태 기록 (단일 선택 방식)
      dailyRecords.value[dateKey] = [recordKey];
    }

    dailyRecords.value = { ...dailyRecords.value };

    closeColorModal();
    selectDayAndNavigate(modalTargetDay.value);
  }
};

const getRecordIconUrl = (records) => {
  if (records && records.length > 0) {
    const recordKey = records[0];
    const state = trackingStates.value.find((s) => s.key === recordKey);
    return state ? state.icon : "";
  }
  return "";
};

// 💡 추가된 년/월 모달 함수
const openYearMonthModal = () => {
  tempSelectedYear.value = currentDate.value.getFullYear();
  tempSelectedMonth.value = currentDate.value.getMonth();
  isYearMonthModalOpen.value = true;
};

const closeYearMonthModal = () => {
  isYearMonthModalOpen.value = false;
};

const applyYearMonth = () => {
  const currentDayOfMonth = currentDate.value.getDate();
  let newDate = new Date(tempSelectedYear.value, tempSelectedMonth.value, currentDayOfMonth);

  if (newDate.getMonth() !== tempSelectedMonth.value) {
    newDate = new Date(tempSelectedYear.value, tempSelectedMonth.value + 1, 0);
  }

  currentDate.value = newDate;
  closeYearMonthModal();
};


// ----------------------------------------------------
// 4. 오류 방지 watch
// ----------------------------------------------------
watch(
  () => router.currentRoute.value.path,
  () => {
    if (isColorModalOpen.value) {
      closeColorModal();
    }
  }
);
</script>

<template>
  <div class="calendar-view">
    <header class="header">
      <div class="month-header">
        <button @click="changeMonth(-1)" class="month-btn">◀</button>
        
        <h1 class="month-display" @click.stop="openYearMonthModal">
          {{ displayMonth }}
          <span class="dropdown-icon">▼</span>
        </h1>
        
        <button @click="changeMonth(1)" class="month-btn">▶</button>
      </div>
    </header>

    <div class="content">
      <div class="tracking-states">
        <div
          v-for="state in trackingStates"
          :key="state.key"
          class="state-chip"
          :style="{ backgroundColor: state.color }"
        >
          <img v-if="state.icon" :src="state.icon" :alt="state.label" class="state-chip-icon" />
          {{ state.label }}
        </div>
      </div>

      <div class="calendar-card">
        <div class="days-of-week">
          <span v-for="day in daysOfWeek" :key="day" class="weekday-header">{{ day }}</span>
        </div>

        <div class="date-grid">
          <div v-for="(day, index) in calendarDays" :key="index" class="date-cell-wrapper">
            <button
              v-if="day.isCurrentMonth"
              :class="[
                'date-cell',
                {
                  'is-today': day.isToday,
                  'is-selected': day.isSelected,
                  'has-icon': day.records.length > 0,
                },
              ]"
              @mousedown.prevent="startPress(day)"
              @mouseup.prevent="endPress(day)"
              @mouseleave.prevent="cancelPress"
              @touchstart.prevent="startPress(day)"
              @touchend.prevent="endPress(day)"
              @touchcancel.prevent="cancelPress"
              :aria-label="`${displayMonth} ${day.day}일`"
            >
              <img
                v-if="day.records.length > 0"
                :src="getRecordIconUrl(day.records)"
                :alt="`기록 아이콘`"
                class="date-content date-icon-overlay"
              />
              <span v-else class="date-content date-number">
                {{ day.day }}
              </span>
            </button>
            <span v-else class="empty-cell"></span>
          </div>
        </div>
      </div>
    </div>

   <Footer></Footer>
  </div>

  <Teleport to="body">
    <div v-if="isColorModalOpen" class="modal-overlay" @click.self="closeColorModal">
      <div class="color-modal">
        <h2>{{ modalTargetDay?.day }}일 기록 선택</h2>
        <p class="modal-info">선택 시, 이전 상태는 해제되고 새로운 상태가 기록됩니다.</p>

        <div class="color-options">
          <button
            v-for="state in trackingStates"
            :key="state.key"
            :style="{ '--color-code': state.color }"
            :class="[
              'color-option-btn',
              {
                'is-active': modalTargetDay?.records?.includes(state.key),
              },
            ]"
            @click="selectColorForRecord(state.key)"
          >
            <img v-if="state.icon" :src="state.icon" :alt="state.label" class="color-option-icon" />
            {{ state.label }}
          </button>
        </div>
        <button @click="closeColorModal" class="close-btn">닫기</button>
      </div>
    </div>
  </Teleport>

  <Teleport to="body">
    <div v-if="isYearMonthModalOpen" class="modal-overlay" @click.self="closeYearMonthModal">
      <div class="year-month-modal">
        <h2>날짜 선택</h2>

        <div class="select-group">
          <label for="year-select">년도</label>
          <select id="year-select" v-model.number="tempSelectedYear" class="date-select">
            <option v-for="year in availableYears" :key="year" :value="year">
              {{ year }}년
            </option>
          </select>
        </div>

        <div class="select-group">
          <label for="month-select">월</label>
          <select id="month-select" v-model.number="tempSelectedMonth" class="date-select">
            <option v-for="month in availableMonths" :key="month" :value="month">
              {{ month + 1 }}월
            </option>
          </select>
        </div>

        <div class="modal-actions">
          <button @click="closeYearMonthModal" class="cancel-btn">취소</button>
          <button @click="applyYearMonth" class="apply-btn">적용</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
/* --- 기본 변수 정의 --- */
:root {
  --color-primary: #98d8c8;
  --color-primary-dark: #6fafaa;
  --color-accent: #FF6B9D;
  --color-accent-light: #FFB6D3;
  --color-warning: #FFA726;
  --color-success: #66BB6A;
  --color-text-default: #2C3E50;
  --color-text-secondary: #7F8C8D;
  --color-text-muted: #95A5A6;
  --color-red: #E74C3C;
}

/* --- 캘린더 전체 레이아웃 --- */
.calendar-view {
  min-height: 100vh;
  padding-bottom: 80px;
  /* 💡 수정: 전체 뷰에 그라데이션 적용 */
  background: linear-gradient(135deg, #98d8c8 0%, #6fafaa 100%);
  color: white;
}

.header {
  /* 💡 수정: 배경색과 그림자 제거하여 투명하게 만듦 */
  /* background: linear-gradient(135deg, #98d8c8 0%, #6fafaa 100%); (제거) */
  /* box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12); (제거) */
  color: white;
  padding: 2rem 1.5rem 1rem;
}

.month-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

.month-display {
  font-size: 1.5rem;
  font-weight: bold;
  margin: 0;
  flex-grow: 1;
  text-align: center;
  color: white;
  cursor: pointer; 
}

.dropdown-icon {
  margin-left: 5px;
  opacity: 0.9;
  transition: opacity 0.2s;
  display: none; 
}

.month-btn {
  background: rgba(255, 255, 255, 0.25);
  border: none;
  color: white;
  font-size: 1.2rem;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 8px;
  transition: all 0.2s;
  -webkit-tap-highlight-color: transparent;
}

.month-btn:hover {
  background: rgba(255, 255, 255, 0.35);
}

.month-btn:active {
  transform: scale(0.95);
}

/* --- 트래킹 상태 칩 스타일 --- */
.content {
  padding: 0 1.5rem;
}

.tracking-states {
  display: flex;
  gap: 0.5rem;
  overflow-x: auto;
  white-space: nowrap;
  padding: 1.5rem 0;
  margin-bottom: 0;
}

.state-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0.6rem 1.2rem;
  border-radius: 20px;
  color: white;
  font-size: 0.85rem;
  font-weight: 600;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s, box-shadow 0.2s;
  flex-shrink: 0;
  -webkit-tap-highlight-color: transparent;
}

.state-chip:active {
  transform: translateY(1px);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.08);
}

.state-chip-icon {
  width: 18px;
  height: 18px;
}

/* --- 캘린더 카드 (글래스모피즘) --- */
.calendar-card {
  background: rgba(255, 255, 255, 0.2);
  padding: 1.5rem 1.5rem 2rem 1.5rem;
  border-radius: 1.75rem;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  margin-bottom: 1.5rem;
  overflow: hidden; 
}

/* --- 요일 헤더 --- */
.days-of-week {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  text-align: center;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  padding-bottom: 1rem;
  border-bottom: 2px solid rgba(255, 255, 255, 0.2);
  font-size: 0.9rem;
}

.weekday-header:first-child {
  color: #FFB6D3;
}

.weekday-header:last-child {
  color: #FFB6D3;
}

/* --- 날짜 그리드 --- */
.date-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  text-align: center;
  gap: 4px 0; 
  padding: 1rem 0;
}

.date-cell-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 2px 0;
}

.date-cell {
  width: 100%; 
  max-width: 44px; 
  aspect-ratio: 1 / 1; 
  
  display: flex;
  justify-content: center;
  align-items: center;
  background: rgba(255, 255, 255, 0.15);
  border: 2px solid transparent;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  position: relative;
  padding: 0;
  color: white;
  border-radius: 12px;
  transition: all 0.2s ease;
  -webkit-tap-highlight-color: transparent;
  touch-action: manipulation;
  user-select: none;
}

.date-cell:hover {
  background-color: rgba(255, 255, 255, 0.25);
  transform: scale(1.08);
  border-color: rgba(255, 255, 255, 0.4);
}

.date-cell:active {
  transform: scale(0.97);
}

.is-today {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.6);
  font-weight: 600;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.is-selected {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.6);
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 💡 아이콘/날짜 오버레이 스타일 */
.date-content {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 12px;
}

.has-icon .date-content {
  background: rgba(255, 255, 255, 0.1);
}

.date-number {
  font-size: 1rem;
  line-height: 1;
}

.date-icon-overlay {
  width: 28px;
  height: 28px;
  object-fit: contain;
}

.empty-cell {
  visibility: hidden;
}

/* --- 모달 스타일 (기록/색상) --- */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(44, 62, 80, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  backdrop-filter: blur(2px);
}

.color-modal {
  background: white;
  padding: 2rem;
  border-radius: 1.75rem;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  width: 90%;
  max-width: 400px;
}

.color-modal h2 {
  font-size: 1.3rem;
  margin-bottom: 0.5rem;
  color: var(--color-text-default);
  font-weight: 700;
}

.modal-info {
  font-size: 0.9rem;
  color: var(--color-text-secondary);
  margin-bottom: 1.5rem;
}

.color-options {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 1.5rem;
}

.color-option-btn {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 1rem;
  border: 2px solid rgba(152, 216, 200, 0.3);
  background: rgba(255, 255, 255, 0.8);
  border-radius: 0.875rem;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  color: var(--color-text-default);
  font-weight: 500;
  -webkit-tap-highlight-color: transparent;
  touch-action: manipulation;
  user-select: none;
}

.color-option-btn:hover {
  background: rgba(152, 216, 200, 0.2);
  border-color: rgba(152, 216, 200, 0.6);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.color-option-btn:active {
  transform: scale(0.97);
}

.color-option-btn.is-active {
  border-color: var(--color-code);
  background: var(--color-code);
  color: white;
  font-weight: bold;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.color-option-icon {
  width: 24px;
  height: 24px;
  margin-right: 12px;
  transition: filter 0.2s;
}

.color-option-btn.is-active::after {
  content: "✓";
  position: absolute;
  right: 15px;
  font-size: 1.5rem;
  font-weight: bold;
  color: white;
}

.close-btn {
  width: 100%;
  padding: 1rem 2rem;
  background: rgba(255, 255, 255, 0.1);
  color: white;
  border: 1.5px solid rgba(255, 255, 255, 0.6);
  border-radius: 3rem;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 600;
  transition: all 0.2s ease;
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
  -webkit-tap-highlight-color: transparent;
  touch-action: manipulation;
  user-select: none;
}

.close-btn:hover {
  background: white;
  color: #6fafaa;
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.close-btn:active {
  transform: scale(0.95);
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 1);
}

/* ---------------------------------------------------- */
/* 💡 년/월 선택 모달 스타일 */
/* ---------------------------------------------------- */

.year-month-modal {
  background: white;
  padding: 2rem;
  border-radius: 1.75rem;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  width: 90%;
  max-width: 400px;
}

.year-month-modal h2 {
  font-size: 1.3rem;
  margin-bottom: 1.5rem;
  color: var(--color-text-default);
  text-align: center;
  font-weight: 700;
}

.select-group {
  display: flex;
  flex-direction: column;
  margin-bottom: 1rem;
}

.select-group label {
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: var(--color-text-default);
  font-size: 0.95rem;
}

.date-select {
  padding: 0.85rem;
  border: 2px solid rgba(152, 216, 200, 0.3);
  border-radius: 0.875rem;
  font-size: 1rem;
  appearance: none;
  background-color: white;
  cursor: pointer;
  transition: all 0.2s;
  font-weight: 500;
  color: var(--color-text-default);
  -webkit-tap-highlight-color: transparent;
}

.date-select:hover {
  border-color: rgba(152, 216, 200, 0.6);
  background-color: rgba(152, 216, 200, 0.05);
}

.date-select:focus {
  outline: none;
  border-color: #98d8c8;
  box-shadow: 0 0 0 3px rgba(152, 216, 200, 0.2);
}

.modal-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 2rem;
  gap: 10px;
}

/* 취소 버튼 스타일 (기준) */
.cancel-btn {
  flex: 1;
  padding: 0.85rem;
  background-color: rgba(255, 255, 255, 0.8);
  color: var(--color-text-default);
  border: 2px solid rgba(152, 216, 200, 0.3);
  border-radius: 0.875rem;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 600;
  transition: all 0.2s;
  -webkit-tap-highlight-color: transparent;
  touch-action: manipulation;
  user-select: none;
}

.cancel-btn:hover {
  background-color: rgba(152, 216, 200, 0.2);
  border-color: rgba(152, 216, 200, 0.6);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.cancel-btn:active {
  transform: scale(0.97);
}

/* 💡 적용 버튼 스타일: cancel-btn과 동일하게 설정 */
.apply-btn {
  flex: 1;
  padding: 0.85rem;
  background-color: rgba(255, 255, 255, 0.8); 
  color: var(--color-text-default); 
  border: 2px solid rgba(152, 216, 200, 0.3); 
  border-radius: 0.875rem; 
  cursor: pointer;
  font-size: 1rem;
  font-weight: 600;
  transition: all 0.2s;
  -webkit-tap-highlight-color: transparent;
  touch-action: manipulation;
  user-select: none;
}

.apply-btn:hover {
  background-color: rgba(152, 216, 200, 0.2);
  border-color: rgba(152, 216, 200, 0.6);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  color: var(--color-text-default);
}

.apply-btn:active {
  transform: scale(0.97);
}
</style>