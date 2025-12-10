import axios from "axios";
import { useAuthStore } from "@/stores/authStore";

// 1. Axios 인스턴스 생성 (기지국 설치)
const instance = axios.create({
  baseURL: "http://localhost:8080/api", // 서버 공통 주소
  timeout: 5000, // 5초 타임아웃
  headers: {
    "Content-Type": "application/json",
  },
});

// 2. 요청 인터셉터 (전화 걸기 전 가로채기)
// 요청을 보낼 때마다 Pinia 스토어에서 토큰을 꺼내 헤더에 '자동으로' 붙여줍니다.
instance.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore();
    // 토큰이 있다면 헤더에 추가 (Bearer Token)
    if (authStore.accessToken) {
      config.headers["Authorization"] = `Bearer ${authStore.accessToken}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 3. 응답 인터셉터 (전화 받기 전 가로채기)
instance.interceptors.response.use(
  (response) => response,
  (error) => {
    // 예: 토큰 만료(401) 에러가 오면 자동으로 로그아웃 시키기 등 가능
    return Promise.reject(error);
  }
);

export default instance;
