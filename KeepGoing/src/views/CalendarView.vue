<script setup>
import { ref, computed } from "vue";
import { useRouter } from "vue-router";
import { watch } from "vue"; // watch 함수 import 추가

const router = useRouter();

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

// 트래킹 상태 정의 (water 항목 제거됨)
const trackingStates = ref([
  { key: "ate", label: "먹었어요", color: "#4CAF50", icon: "/assets/images/stickers/jinji.png" },
  { key: "burned", label: "태웠어요", color: "#FF69B4", icon: "/assets/images/stickers/sad.png" },
  { key: "weight", label: "몸무게", color: "#FF9800", icon: "assets/images/stickers/smile.png" },
]);

// 임시 데이터 (water 기록 제거됨)
const dailyRecords = ref({
  "2025-11-16": ["ate", "weight"],
  "2025-11-22": ["ate", "burned"], // water 제거
  "2025-11-28": ["ate", "weight"],
  "2025-12-05": ["weight"],
});

// ----------------------------------------------------
// 2. 날짜 로직 및 Computed 속성
// ----------------------------------------------------

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
  currentDate.value = new Date();
  selectedDate.value = new Date().toDateString();
  selectDayAndNavigate({ dateKey: new Date().toISOString().slice(0, 10) });
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

    const currentRecords = dailyRecords.value[dateKey] ? [...dailyRecords.value[dateKey]] : [];
    const index = currentRecords.indexOf(recordKey);

    if (index > -1) {
      currentRecords.splice(index, 1);
    } else {
      currentRecords.push(recordKey);
    }

    dailyRecords.value = {
      ...dailyRecords.value,
      [dateKey]: currentRecords,
    };

    closeColorModal();
    selectDayAndNavigate(modalTargetDay.value);
  }
};

// ----------------------------------------------------
// 4. 오류 방지 watch (라우트 변경 시 모달 강제 닫기)
// ----------------------------------------------------
// 라우트가 변경될 때 모달이 열려 있으면 닫아서 렌더링 충돌을 방지합니다.
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
        <h1 class="month-display">{{ displayMonth }}</h1>
        <button @click="changeMonth(1)" class="month-btn">▶</button>
        <button @click="selectToday" class="today-btn">오늘 선택</button>
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
            {{ day.day }}

            <div class="record-indicators">
              <span
                v-for="recordKey in day.records"
                :key="recordKey"
                class="record-dot"
                :style="{ backgroundColor: trackingStates.find((s) => s.key === recordKey)?.color }"
                :title="trackingStates.find((s) => s.key === recordKey)?.label"
              ></span>
            </div>
          </button>
          <span v-else class="empty-cell"></span>
        </div>
      </div>
    </div>

    <nav class="bottom-nav">
      <button @click="$router.push('/')" class="nav-btn">🏠 홈</button>
      <button @click="$router.push('/calendar')" class="nav-btn active">📅 캘린더</button>
      <button @click="$router.push('/ai-analysis')" class="nav-btn">🤖 AI 분석</button>
      <button @click="$router.push('/profile')" class="nav-btn">👤 마이페이지</button>
    </nav>
  </div>

  <Teleport to="body">
    <div v-if="isColorModalOpen" class="modal-overlay" @click.self="closeColorModal">
      <div class="color-modal">
        <h2>{{ modalTargetDay?.day }}일 기록 선택</h2>
        <p class="modal-info">추가할 트래킹 상태를 선택하거나 해제하세요.</p>

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
</template>

<style scoped>
/* --- 기본 변수 정의 --- */
:root {
  --color-primary: #667eea;
  --color-secondary: #764ba2;
  --color-text-default: #333;
  --color-text-secondary: #555;
  --color-red: #ff3b30;
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

.month-display {
  font-size: 1.5rem;
  font-weight: bold;
  margin: 0;
  flex-grow: 1;
  text-align: center;
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
  /* SVG 색상을 흰색으로 변경하여 칩 배경색 위에서 보이게 함 */
  filter: invert(1);
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
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: none;
  border: none;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  position: relative;
  padding-top: 5px;
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

/* --- 기록 도트 스타일 --- */
.record-indicators {
  position: absolute;
  bottom: -5px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 2px;
}

.record-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  display: inline-block;
}

.empty-cell {
  visibility: hidden;
}

/* --- 하단 네비게이션 --- */
.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  display: flex;
  justify-content: space-around;
  padding: 1rem;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.nav-btn {
  background: none;
  border: none;
  padding: 0.5rem;
  font-size: 0.75rem;
  cursor: pointer;
  color: var(--color-text-secondary);
}

.nav-btn.active {
  color: var(--color-primary);
  font-weight: bold;
}

/* --- 모달 스타일 --- */

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

/* 템플릿 수정 B 관련 스타일: 아이콘 스타일 */
.color-option-icon {
  width: 20px;
  height: 20px;
  margin-right: 15px;
  filter: invert(0); /* 기본 상태 (아이콘 색상 유지) */
  transition: filter 0.2s;
}

.color-option-btn.is-active .color-option-icon {
  filter: invert(1); /* 활성화 시 아이콘을 흰색으로 변경 */
}

/* 활성화 상태일 때 체크 표시 */
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
</style>
