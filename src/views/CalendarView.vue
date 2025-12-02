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
    color: "#FF69B4",
    icon: new URL("/src/assets/images/stickers/sad.png", import.meta.url).href,
  },
  {
    key: "weight",
    label: "몸무게",
    color: "#FF9800",
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
      dailyRecords.value[dateKey] = [];
    } else {
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
  // 현재 달력의 년/월로 임시 상태 초기화
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

  // 날짜가 다음 달로 넘어갔다면 해당 월의 마지막 날로 조정
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
        <h1 class="month-display">
          {{ displayMonth }}
          <span class="dropdown-icon" @click.stop="openYearMonthModal">▼</span>
        </h1>
        
        <button @click="changeMonth(1)" class="month-btn">▶</button>
      <!--  <button @click="selectToday" class="today-btn">오늘 선택</button> -->
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
                'has-icon': day.records.length > 0, // 아이콘 덮어쓰기 여부
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

   <Footer></Footer>
  </div>

  <Teleport to="body">
    <div v-if="isColorModalOpen" class="modal-overlay" @click.self="closeColorModal">
      <div class="color-modal">
        <h2>{{ modalTargetDay?.day }}일 기록 선택 (단일)</h2>
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
  --color-primary: #667eea;
  --color-secondary: #764ba2;
  --color-text-default: #333;
  --color-text-secondary: #555;
  --color-red: #ff3b30;
  --color-green: #4CAF50;
}

/* --- 캘린더 전체 레이아웃 --- */
.calendar-view {
  min-height: 100vh;
  padding-bottom: 80px;
  background-color: #f7f7f7;
}

.header {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-secondary) 100%);
  color: white;
  padding: 2rem 1.5rem 1rem;
}

.month-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}

/* 월 표시 영역은 클릭 기능 없음 */
.month-display {
  font-size: 1.5rem;
  font-weight: bold;
  margin: 0;
  flex-grow: 1;
  text-align: center;
}

/* 💡 드롭다운 아이콘에만 커서 추가 */
.dropdown-icon {
    cursor: pointer;
    margin-left: 5px; 
}

.month-btn {
  background: none;
  border: none;
  color: white;
  font-size: 1.2rem;
  cursor: pointer;
  padding: 0 10px;
}

.today-btn {
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
  border: none;
  border-radius: 20px;
  padding: 0.5rem 1rem;
  font-size: 0.8rem;
  font-weight: 500;
  cursor: pointer;
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
  border-bottom: 1px solid #eee;
  background-color: white;
  padding: 1rem 1.5rem;
  margin: 0 -1.5rem 1rem;
}

.state-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0.4rem 0.8rem;
  border-radius: 20px;
  color: white;
  font-size: 0.75rem;
  font-weight: 500;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.state-chip-icon {
  width: 16px;
  height: 16px;
}

/* --- 요일 헤더 --- */
.days-of-week {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  text-align: center;
  font-weight: bold;
  color: var(--color-text-secondary);
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #ddd;
}

.weekday-header:first-child {
  color: var(--color-red);
}

/* --- 날짜 그리드 --- */
.date-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  text-align: center;
  gap: 5px 0;
  padding-top: 10px;
}

.date-cell-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 5px 0;
}

.date-cell {
  width: 40px;
  height: 40px;
  display: flex;
  justify-content: center;
  align-items: center;
  background: none;
  border: none;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  position: relative;
  padding: 0;
  color: var(--color-text-default);
}

.is-selected {
  background-color: var(--color-primary);
  color: white !important;
  border-radius: 50%;
  font-weight: bold;
}

.is-today {
  border: 2px solid var(--color-primary);
  border-radius: 50%;
  color: var(--color-primary);
}

.is-selected.is-today {
  border: 2px solid white;
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
}

/* 날짜 숫자가 표시될 때의 스타일 */
.date-number {
  font-size: 1rem;
  line-height: 1;
}

/* 아이콘이 표시될 때의 스타일 */
.date-icon-overlay {
  width: 100%; /* 아이콘을 셀 크기의 70%로 확대 */
  height: 100%;
}

/* --- 기록 도트 스타일 (삭제됨) --- */
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
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.color-modal {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  width: 90%;
  max-width: 400px;
}

.color-modal h2 {
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
  color: var(--color-primary);
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
  border: 2px solid #eee;
  background: #f9f9f9;
  border-radius: 8px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  color: var(--color-text-default);
}

.color-option-btn:hover {
  background: #f0f0f0;
}

.color-option-btn.is-active {
  border-color: var(--color-code);
  background-color: var(--color-code);
  color: white;
  font-weight: bold;
}

.color-option-icon {
  width: 20px;
  height: 20px;
  margin-right: 15px;
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
  padding: 10px;
  background-color: #ccc;
  color: #333;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
}

/* ---------------------------------------------------- */
/* 💡 년/월 선택 모달 스타일 */
/* ---------------------------------------------------- */

.year-month-modal {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  width: 90%;
  max-width: 400px;
}

.year-month-modal h2 {
  font-size: 1.5rem;
  margin-bottom: 1.5rem;
  color: var(--color-primary);
  text-align: center;
}

.select-group {
  display: flex;
  flex-direction: column;
  margin-bottom: 1rem;
}

.select-group label {
  font-weight: bold;
  margin-bottom: 0.5rem;
  color: var(--color-text-default);
}

.date-select {
  padding: 0.75rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 1rem;
  appearance: none;
  background-color: #f9f9f9;
  cursor: pointer;
}

.modal-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 2rem;
  gap: 10px;
}

.cancel-btn {
  flex: 1;
  padding: 10px;
  background-color: #ccc;
  color: #333;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
}

.apply-btn {
  flex: 1;
  padding: 10px;
  background-color: var(--color-primary);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: bold;
}
</style>