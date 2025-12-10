import { defineStore } from "pinia";
import axios from "axios";

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

      // Axios 헤더에 토큰 자동 탑재 (이제 요청할 때마다 토큰 신경 안 써도 됨)
      axios.defaults.headers.common[
        "Authorization"
      ] = `Bearer ${data.accessToken}`;
    },
    // 로그아웃
    logout() {
      this.$reset(); // 상태 초기화
      delete axios.defaults.headers.common["Authorization"];
      localStorage.removeItem("auth"); // 저장된 정보 삭제
    },
  },
  persist: true, // 중요: 새로고침해도 데이터 유지
});
