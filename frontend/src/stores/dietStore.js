import { defineStore } from "pinia";
import { useConfigStore } from "@/stores/configStore";

export const useDietStore = defineStore("diet", {
  state: () => ({
    // 1. 초기값은 로컬스토리지에서 가져옴 (새로고침 해도 유지)
    scheduleMap: JSON.parse(localStorage.getItem("diet_schedule_map") || "{}"),
    lastFetchTime: localStorage.getItem("diet_last_fetch") || null,
  }),

  getters: {
    // 오늘 날짜의 식단/퀘스트만 쏙 뽑아오는 Getter
    getTodayQuest: (state) => (dateString) => {
      return state.scheduleMap[dateString] || null;
    },
  },

  actions: {
    async fetchSchedules(memberId) {
      const config = useConfigStore();

      try {
        const response = await fetch(`${config.API_ENDPOINT}/diets/schedule?memberId=${memberId}`);
        if (!response.ok) throw new Error("Failed to fetch schedules");

        const data = await response.json();

        // 2. 데이터 가공 (List -> Map)
        const planMap = {};
        data.forEach((item) => {
          planMap[item.date] = {
            menu: item.menu, // {breakfast:..., lunch:..., dinner:...}
            quest: item.quest, // "닭가슴살 먹기" 등 퀘스트 텍스트
            totalCal: item.totalCal,
          };
        });

        // 3. State 업데이트
        this.scheduleMap = planMap;
        this.lastFetchTime = new Date().toISOString();

        // 4. LocalStorage에 저장 (영구 보관)
        localStorage.setItem("diet_schedule_map", JSON.stringify(planMap));
        localStorage.setItem("diet_last_fetch", this.lastFetchTime);

        console.log("✅ 식단 스케줄 캐싱 완료");
      } catch (error) {
        console.error("❌ 스케줄 로딩 실패:", error);
        // 실패해도 기존 캐시된 데이터가 있다면 state는 유지됨
      }
    },
  },
});
