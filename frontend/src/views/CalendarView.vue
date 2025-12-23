<template>
  <div class="calendar-view retro-theme">
    <div class="scanlines"></div>

    <header class="header">
      <div class="retro-box month-control">
        <button @click="changeMonth(-1)" class="pixel-arrow">◀</button>
        <div class="month-display" @click.stop="openYearMonthModal">
          <span class="value">{{ currentYear }}.{{ String(currentMonth + 1).padStart(2, "0") }}</span>
        </div>
        <button @click="changeMonth(1)" class="pixel-arrow">▶</button>
      </div>
    </header>

    <div class="content">
      <div class="legend-box">
        <div class="tracking-states">
          <div v-for="grade in grades" :key="grade.key" class="state-chip" :class="grade.key">
            <span class="rank-char">{{ grade.key }}</span>
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
            :class="{ weekend: day === 'SUN' || day === 'SAT' }"
          >
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
                'has-record': !!getDayRank(day.dateKey),
                'has-plan': !!getDayPlan(day.dateKey),
              }"
              @click="handleDateClick(day)"
            >
              <span class="tile-number">{{ day.day }}</span>

              <div v-if="getDayPlan(day.dateKey)" class="plan-dot"></div>

              <div v-if="getDayRank(day.dateKey)" class="tile-loot">
                <span class="rank-stamp" :class="getDayRank(day.dateKey)">
                  {{ getDayRank(day.dateKey) }}
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

    <Teleport to="body">
      <div v-if="isColorModalOpen" class="modal-overlay" @click.self="closeColorModal">
        <div class="retro-modal color-select-modal">
          <h2 class="modal-title">DAILY LOG</h2>
          <div class="modal-subtitle">{{ modalTargetDay?.dateKey }}</div>

          <div v-if="getDayPlan(modalTargetDay?.dateKey)" class="mission-log-box">
            <div class="box-header">
              <span class="pixel-label-xs">▼ TODAY'S MENU</span>
              <span class="total-cal">{{ getDayPlan(modalTargetDay?.dateKey).totalCal }} kcal</span>
            </div>

            <div class="mission-content">
              <div class="menu-list">
                <div v-for="(item, idx) in getDayPlan(modalTargetDay?.dateKey).menus" :key="idx" class="menu-item">
                  <div class="menu-info">
                    <span class="menu-name">{{ item.name }}</span>
                  </div>
                  <div class="menu-meta">
                    <span class="mini-tag cal">{{ item.cal }}</span>
                  </div>
                </div>
              </div>

              <div class="quest-row">
                <span class="icon">⚔️</span>
                <span class="quest-text">{{ getDayPlan(modalTargetDay?.dateKey).quest }}</span>
              </div>
            </div>
          </div>

          <div v-else class="no-plan-msg">NO MISSION DATA</div>

          <button v-if="getDayRank(modalTargetDay?.dateKey)" @click="removeRank" class="retro-btn delete-btn">
            RESET RANK
          </button>

          <div class="modal-nav-row">
            <button class="retro-btn nav-btn" @click="goToDetail">GO TO DETAIL</button>
            <button class="retro-btn close-btn" @click="closeColorModal">CLOSE</button>
          </div>
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

const grades = [
  { key: "S", label: "SPECIAL", color: "#FFD700" },
  { key: "A", label: "EXCELLENT", color: "#FF0055" },
  { key: "B", label: "GOOD", color: "#00E5FF" },
  { key: "C", label: "SOSO", color: "#FFA500" },
  { key: "F", label: "FAIL", color: "#888888" },
];

// 데이터 저장소
const dailyDataMap = ref({}); // 랭크 데이터 (S, A, B...)
const dailyPlanMap = ref({}); // 식단 계획 데이터

const currentDate = ref(new Date());
const selectedDate = ref(new Date().toDateString());

const isColorModalOpen = ref(false);
const modalTargetDay = ref(null);
const isYearMonthModalOpen = ref(false);
const tempSelectedYear = ref(currentDate.value.getFullYear());
const tempSelectedMonth = ref(currentDate.value.getMonth());

// 사운드 효과
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

// Getter 함수들
const getDayRank = (dateKey) => dailyDataMap.value[dateKey] || null;
const getDayPlan = (dateKey) => dailyPlanMap.value[dateKey] || null;

const formatDateKey = (date) => {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
};

// [API] 랭크(평가) 조회
const fetchEvaluations = async (year, month) => {
  const apiMonth = month + 1;
  const url = `${API_ENDPOINT}/diets/evaluations?memberId=${MEMBER_ID}&year=${year}&month=${apiMonth}`;

  try {
    const res = await fetch(url);
    if (!res.ok) throw new Error("Fetch failed");
    const data = await res.json();

    const map = {};
    data.forEach((item) => {
      map[item.date] = item.rank;
    });
    dailyDataMap.value = map;
  } catch (e) {
    console.error(e);
    dailyDataMap.value = {};
  }
};

// [API] 식단 스케쥴 조회 (Mock Data 대신 실제 API 호출)
const fetchSchedules = async () => {
  try {
    // memberId를 파라미터로 전송
    const response = await fetch(`${API_ENDPOINT}/diets/schedule?memberId=${MEMBER_ID}`);
    if (!response.ok) throw new Error("Failed to fetch schedules");

    const data = await response.json();

    // List -> Map 변환
    const planMap = {};
    data.forEach((item) => {
      planMap[item.date] = {
        menus: item.menus, // List<MenuDto>
        quest: item.quest,
        totalCal: item.totalCal,
      };
    });

    dailyPlanMap.value = planMap;
  } catch (error) {
    console.error("스케쥴 로딩 실패:", error);
    dailyPlanMap.value = {};
  }
};

// [API] 랭크 삭제
const removeRank = async () => {
  playSound("select");
  if (!modalTargetDay.value) return;
  const dateKey = modalTargetDay.value.dateKey;

  // UI 즉시 반영 (Optimistic Update)
  delete dailyDataMap.value[dateKey];

  try {
    const url = `${API_ENDPOINT}/diets/evaluation?memberId=${MEMBER_ID}&date=${dateKey}`;
    await fetch(url, { method: "DELETE" });
  } catch (e) {
    // 실패 시 롤백용 재조회
    fetchEvaluations(currentYear.value, currentMonth.value);
  }
};

// UI 인터랙션
const handleDateClick = (day) => {
  if (!day.dateKey) return;
  playSound("select");
  modalTargetDay.value = day;
  isColorModalOpen.value = true;
};

const goToDetail = () => {
  if (modalTargetDay.value?.dateKey) {
    playSound("warp");
    isColorModalOpen.value = false;
    router.push({ path: "/", query: { date: modalTargetDay.value.dateKey } });
  }
};

const closeColorModal = () => {
  isColorModalOpen.value = false;
  modalTargetDay.value = null;
};

// Computed: 날짜 계산
const currentYear = computed(() => currentDate.value.getFullYear());
const currentMonth = computed(() => currentDate.value.getMonth());
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
    const dateKey = formatDateKey(fullDate);

    days.push({
      day: i,
      isCurrentMonth: true,
      isToday: fullDate.toDateString() === new Date().toDateString(),
      isSelected: fullDate.toDateString() === selectedDate.value,
      dateKey: dateKey,
    });
  }
  while (days.length < 42) days.push({ day: "", isCurrentMonth: false, dateKey: null });
  return days;
});

const changeMonth = (delta) => {
  playSound("select");
  const newDate = new Date(currentDate.value);
  newDate.setMonth(newDate.getMonth() + delta);
  currentDate.value = newDate;

  // 월 변경 시 평가 데이터 다시 로드
  fetchEvaluations(newDate.getFullYear(), newDate.getMonth());
};

const openYearMonthModal = () => {
  playSound("select");
  tempSelectedYear.value = currentYear.value;
  tempSelectedMonth.value = currentMonth.value;
  isYearMonthModalOpen.value = true;
};
const closeYearMonthModal = () => {
  isYearMonthModalOpen.value = false;
};
const applyYearMonth = () => {
  playSound("warp");
  const newDate = new Date(tempSelectedYear.value, tempSelectedMonth.value, 1);
  currentDate.value = newDate;
  closeYearMonthModal();
  fetchEvaluations(newDate.getFullYear(), newDate.getMonth());
};

watch(
  () => router.currentRoute.value.path,
  () => {
    if (isColorModalOpen.value) closeColorModal();
  }
);

// 라이프사이클
onMounted(() => {
  // 1. 랭크 데이터 조회
  fetchEvaluations(currentYear.value, currentMonth.value);
  // 2. 식단 스케쥴 데이터 조회
  fetchSchedules();
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

.scanlines {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  background: linear-gradient(rgba(18, 16, 16, 0) 50%, rgba(0, 0, 0, 0.1) 50%),
    linear-gradient(90deg, rgba(255, 0, 0, 0.03), rgba(0, 255, 0, 0.01), rgba(0, 0, 255, 0.03));
  background-size: 100% 3px, 4px 100%;
  z-index: 999;
}

.header {
  padding: 1rem 1rem 0 1rem;
}
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
  background: #333;
  color: #fff;
  border: 1px solid #fff;
  width: 32px;
  height: 32px;
  cursor: pointer;
}
.month-display {
  display: flex;
  align-items: baseline;
  gap: 5px;
  cursor: pointer;
}
.value {
  color: #00e5ff;
  font-size: 1.2rem;
  text-shadow: 0 0 5px #00e5ff;
}

.content {
  padding: 0 1rem;
}
.legend-box {
  margin-bottom: 1rem;
}
.tracking-states {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
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
.rank-char {
  font-weight: bold;
  font-size: 0.8rem;
}
.state-chip.S .rank-char {
  color: #ffd700;
}
.state-chip.A .rank-char {
  color: #ff0055;
}
.state-chip.B .rank-char {
  color: #00e5ff;
}
.state-chip.C .rank-char {
  color: #ffa500;
}
.state-chip.F .rank-char {
  color: #888;
}

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

.date-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}
.date-cell-wrapper {
  aspect-ratio: 1;
}
.date-tile {
  width: 100%;
  height: 100%;
  background: #222;
  border: 1px solid #444;
  position: relative;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 0;
  color: #aaa;
  transition: transform 0.1s;
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
.is-today {
  border-color: #00e5ff;
  background: #001a1a;
}
.is-selected {
  background: #333;
  border: 2px solid #fff;
  color: #fff;
}

/* Plan Dot (식단 표시) */
.plan-dot {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 4px;
  height: 4px;
  background-color: #ffd700;
  box-shadow: 0 0 3px #ffd700;
}

.tile-loot {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
}
.rank-stamp {
  font-size: 1.5rem;
  font-weight: bold;
  font-family: "NeoDunggeunmo", monospace;
  animation: popIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  line-height: 1;
  text-shadow: 2px 2px 0px #000;
}
.rank-stamp.S {
  color: #ffd700;
  text-shadow: 0 0 10px #ffd700, 2px 2px 0 #000;
}
.rank-stamp.A {
  color: #ff0055;
  text-shadow: 0 0 5px #ff0055, 2px 2px 0 #000;
}
.rank-stamp.B {
  color: #00e5ff;
}
.rank-stamp.C {
  color: #ffa500;
}
.rank-stamp.F {
  color: #888;
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

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}
.retro-modal {
  background: #202028;
  border: 4px solid #fff;
  padding: 1.5rem;
  width: 90%;
  max-width: 350px;
  box-shadow: 8px 8px 0 rgba(0, 0, 0, 0.5);
  color: #fff;
  display: flex;
  flex-direction: column;
}
.modal-title {
  color: #ff0055;
  margin: 0 0 0.5rem 0;
  text-align: center;
  font-size: 1.2rem;
}
.modal-subtitle {
  text-align: center;
  color: #888;
  font-size: 0.8rem;
  margin-bottom: 1rem;
}

/* Mission Log Box */
.mission-log-box {
  background: #111;
  border: 2px dashed #555;
  padding: 10px;
  margin-bottom: 1rem;
  text-align: left;
}
.box-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  border-bottom: 1px solid #333;
  padding-bottom: 4px;
}
.pixel-label-xs {
  font-size: 0.6rem;
  color: #ffd700;
  letter-spacing: 1px;
}
.total-cal {
  font-size: 0.7rem;
  color: #00e5ff;
}

.mission-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.menu-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  max-height: 120px;
  overflow-y: auto;
}
.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.85rem;
}
.menu-info {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #fff;
}
.menu-name {
  font-weight: bold;
}
.menu-meta {
  display: flex;
  gap: 4px;
}
.mini-tag {
  font-size: 0.6rem;
  padding: 1px 4px;
  border-radius: 3px;
  color: #000;
}
.mini-tag.cal {
  background: #ccc;
}

.quest-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
  font-size: 0.8rem;
  color: #aaa;
  border-top: 1px solid #333;
  padding-top: 6px;
}
.quest-text {
  color: #ffd700;
}
.no-plan-msg {
  color: #555;
  font-size: 0.8rem;
  text-align: center;
  margin-bottom: 1rem;
  border: 2px dashed #333;
  padding: 10px;
}

.modal-nav-row {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}
.retro-btn {
  flex: 1;
  padding: 5px;
  border: 2px solid #fff;
  font-family: inherit;
  cursor: pointer;
  font-weight: bold;
  font-size: 0.9rem;
  transition: transform 0.1s;
}
.retro-btn:active {
  transform: scale(0.95);
}
.nav-btn {
  background: #00e5ff;
  color: #000;
  border-color: #fff;
}
.close-btn {
  background: #000;
  color: #fff;
}
.delete-btn {
  width: 100%;
  margin-top: 5px;
  background: #333;
  color: #ff3838;
  border-color: #ff3838;
}

.time-modal {
  border-color: #00e5ff;
}
.time-modal .modal-title {
  color: #00e5ff;
}
.control-group {
  margin-bottom: 1rem;
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
}
.step-val {
  font-size: 1.2rem;
  min-width: 60px;
  text-align: center;
}
.month-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 4px;
}
.month-chip {
  background: #222;
  color: #aaa;
  border: 1px solid #444;
  padding: 8px 0;
  cursor: pointer;
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
.confirm {
  background: #00e5ff;
  color: #000;
}
.cancel {
  background: #333;
  color: #fff;
}

@keyframes popIn {
  from {
    transform: scale(0);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}
</style>
