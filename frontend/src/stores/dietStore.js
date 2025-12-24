// stores/dietStore.js
import { defineStore } from "pinia";
import { useConfigStore } from "@/stores/configStore";

export const useDietStore = defineStore("diet", {
  state: () => ({
    // 1. 식단 스케줄 데이터 (기존 유지)
    scheduleMap: JSON.parse(localStorage.getItem("diet_schedule_map") || "{}"),
    lastFetchTime: localStorage.getItem("diet_last_fetch") || null,

    // 2. [NEW] 월별 랭크(평가) 데이터
    evaluationMap: {},
  }),

  getters: {
    getTodayQuest: (state) => (dateString) => {
      return state.scheduleMap[dateString] || null;
    },
    // 날짜별 랭크 가져오기 편의 함수
    getDayRank: (state) => (dateString) => {
      return state.evaluationMap[dateString] || null;
    },
    // 날짜별 식단 가져오기 편의 함수
    getDayPlan: (state) => (dateString) => {
      return state.scheduleMap[dateString] || null;
    },
  },

  actions: {
    // [API] 식단 스케쥴 조회 (기존 코드 유지)
    async fetchSchedules(memberId) {
      const config = useConfigStore();
      try {
        const response = await fetch(`${config.API_ENDPOINT}/diets/schedule?memberId=${memberId}`);
        if (!response.ok) throw new Error("Failed to fetch schedules");

        const data = await response.json();

        const planMap = {};
        data.forEach((item) => {
          planMap[item.date] = {
            menu: item.menu,
            quest: item.quest,
            totalCal: item.totalCal,
          };
        });

        this.scheduleMap = planMap;
        this.lastFetchTime = new Date().toISOString();

        localStorage.setItem("diet_schedule_map", JSON.stringify(planMap));
        localStorage.setItem("diet_last_fetch", this.lastFetchTime);

        console.log("✅ 식단 스케줄 캐싱 완료");
      } catch (error) {
        console.error("❌ 스케줄 로딩 실패:", error);
      }
    },

    // [NEW] 월별 랭크(평가) 데이터 조회
    async fetchEvaluations(memberId, year, month) {
      const config = useConfigStore();
      // JS의 month는 0부터 시작하므로 API 호출 시 +1
      const apiMonth = month + 1;
      const url = `${config.API_ENDPOINT}/diets/evaluations?memberId=${memberId}&year=${year}&month=${apiMonth}`;

      try {
        const res = await fetch(url);
        if (!res.ok) throw new Error("Fetch evaluations failed");

        const data = await res.json();

        const map = {};
        data.forEach((item) => {
          map[item.date] = item.rank;
        });

        // State 업데이트 (해당 월의 데이터로 덮어쓰기)
        this.evaluationMap = map;
      } catch (e) {
        console.error("❌ 랭크 데이터 로딩 실패:", e);
        this.evaluationMap = {};
      }
    },

    // [NEW] 랭크 삭제
    async deleteEvaluation(memberId, dateKey) {
      const config = useConfigStore();

      // 1. 낙관적 업데이트 (UI 반응성을 위해 State에서 먼저 삭제)
      const originalRank = this.evaluationMap[dateKey]; // 실패 시 복구용
      delete this.evaluationMap[dateKey];

      try {
        const url = `${config.API_ENDPOINT}/diets/evaluation?memberId=${memberId}&date=${dateKey}`;
        const res = await fetch(url, { method: "DELETE" });

        if (!res.ok) throw new Error("Delete failed");
        console.log(`🗑️ ${dateKey} 랭크 삭제 완료`);
      } catch (e) {
        console.error("❌ 랭크 삭제 실패:", e);
        // 실패 시 롤백
        if (originalRank) {
          this.evaluationMap[dateKey] = originalRank;
        }
        alert("랭크 삭제에 실패했습니다.");
      }
    },
  },
});
