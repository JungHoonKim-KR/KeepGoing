import { defineStore } from "pinia";
import axios from "axios";
import router from "../router";
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
    async logout() {
      try {
        // 1. 백엔드에 "나 갈게, DB 토큰 지워줘" 요청 (실패해도 진행)
        if (this.memberId) {
          await axios.post(
            `http://localhost:8080/api/auth/logout?memberId=${this.memberId}`
          );
        }
      } catch (error) {
        console.warn("로그아웃 서버 처리 실패(무시하고 진행):", error);
      } finally {
        // 2. 클라이언트 정보 싹 지우기
        this.accessToken = null;
        this.refreshToken = null;
        this.memberId = null;
        this.name = null;
        this.isAuthenticated = false;

        // 3. 헤더 삭제
        delete axios.defaults.headers.common["Authorization"];

        // 4. 로그인 페이지로 튕겨내기
        window.location.href = "/login"; // <-- 이걸 쓰면 import 없어도 되고 확실하게 이동합니다.
        alert("시스템 접속을 종료합니다.");
      }
    },
  },
  persist: true,
});
