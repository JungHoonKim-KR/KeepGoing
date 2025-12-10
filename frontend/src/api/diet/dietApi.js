import axios from "axios";

// Spring Boot 서버 주소
const BASE_URL = "http://localhost:8080";

const api = axios.create({
  baseURL: BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

/**
 * AI 식단 분석 요청
 * POST /diets/analyze?memberId=1&date=2024-05-20
 */
export const analyzeDiet = async (memberId, date) => {
  try {
    // post(url, data, config) 형태
    // RequestParam으로 보내므로 data는 null, params에 객체 전달
    const response = await api.post("/diets/analyze", null, {
      params: {
        memberId: "1",
        date: "2024-05-20",
      },
    });
    return response.data; // 백엔드에서 준 JSON 리턴
  } catch (error) {
    console.error("API Error:", error);
    throw error;
  }
};
