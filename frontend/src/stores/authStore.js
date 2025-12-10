import { defineStore } from "pinia";
import { logoutApi } from "@/api/auth/auth";
export const useAuthStore = defineStore("auth", {
  state: () => ({
    accessToken: null,
    refreshToken: null,
    memberId: null,
    name: null,
    isAuthenticated: false,
  }),
  actions: {
    // 로그인 성공 시 호출
    setLoginState(data) {
      this.accessToken = data.accessToken;
      this.refreshToken = data.refreshToken;
      this.memberId = data.memberId;
      this.name = data.name;
      this.isAuthenticated = true;
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
        this.name = null;
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
