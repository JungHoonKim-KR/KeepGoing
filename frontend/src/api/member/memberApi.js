import axios from "axios";
// Spring Boot 서버 주소
const BASE_URL = "http://localhost:8080";

// 기본 인스턴스 (Content-Type: application/json)
const api = axios.create({
  baseURL: BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

export const updateLevelApi = async (payload) => {
    const response = await api.post('/api/member/level', payload);
    return response.data;
};
