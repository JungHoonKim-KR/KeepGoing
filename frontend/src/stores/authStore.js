import { defineStore } from "pinia";
import { logoutApi } from "@/api/auth/auth";
export const useAuthStore = defineStore("auth", {
state: () => ({
  // 토큰 정보
  accessToken: null,
  refreshToken: null,

  // 기본 회원 정보
  memberId: null,
  level: null,
  exp: null,
  name: null,

  // [추가된 신체 및 프로필 정보]
  height: null,
  weight: null,
  age: null,
  gender: null,
  activity: null,
  goal: null,
  targetWeight: null,
  targetWater: null,
  profileCharacter: null,

  // 상태 관리 플래그
  isAuthenticated: false,
}),
  actions: {
    // 로그인 성공 시 호출
    setLoginState(data) {
        // 백엔드 record 필드명과 스토어 state 필드명을 일치시키면 자동 매핑 가능
        Object.keys(data).forEach(key => {
          if (this.$state.hasOwnProperty(key)) {
            this[key] = data[key];
          }
        });
        this.isAuthenticated = true;
    },
    updateLevelInfo(payload) {
      this.level = payload.level;
      this.exp = payload.exp;
  },
    async logout() {
      try {
        // 1. 백엔드에 "나 갈게, DB 토큰 지워줘" 요청 (실패해도 진행)
        if (this.memberId) {
          await logoutApi(this.memberId);
        }
      } catch (error) {
        console.warn("로그아웃 서버 처리 실패:", error);
      } finally {
        // 1. 상태 초기화
      this.accessToken = null;
      this.refreshToken = null;
      this.memberId = null;
      this.level = null;
      this.exp = null;
      this.name = null;
      this.height = null;
      this.weight = null;
      this.age = null;
      this.gender = null;
      this.activity = null;
      this.goal = null;
      this.targetWeight = null;
      this.targetWater = null;
      this.profileCharacter = null;
      this.isAuthenticated = false;

        // 2. [중요] 저장된 데이터(Persist) 강제 삭제
        // Pinia Persist가 'auth'라는 이름으로 로컬스토리지에 저장합니다.
        localStorage.removeItem("auth");

        // 3. 페이지 새로고침하며 이동 (메모리 완전 초기화)
        window.location.href = "/login";
      }
    },
  },
  persist: true,
});
