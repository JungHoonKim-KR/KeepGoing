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

/**
 * 1. AI 식단 분석 요청 (기존)
 * POST /diets/analyze?memberId=1&date=2024-05-20
 */
export const analyzeDiet = async (memberId, date) => {
  try {
    const response = await api.post("/diets/analyze", null, {
      params: {
        memberId: memberId,
        date: date,
      },
    });
    return response.data;
  } catch (error) {
    console.error("API Error (Analyze):", error);
    throw error;
  }
};

/**
 * 2. [New] AI 맞춤형 식단 생성 요청
 * POST /diets/generate
 * payload: { survey: {...}, userProfile: {...} }
 */
export const generateDietPlanApi = async (payload) => {
  try {
    const response = await api.post("/diets/generate", payload);
    return response.data;
  } catch (error) {
    console.error("API Error (Generate):", error);
    throw error;
  }
};

/**
 * 3. [수정됨] RPG 바디 스캔
 * POST /diets/body-scan?memberId=1
 * (더 이상 payload로 키/몸무게를 보내지 않음)
 */
export const scanBodyApi = async (memberId) => {
  try {
    // 두 번째 인자(body)는 null, 세 번째 인자(config)에 params 설정
    const response = await api.post("/diets/body-scan", null, {
      params: { memberId },
    });
    return response.data;
  } catch (error) {
    console.error("API Error (Body Scan):", error);
    throw error;
  }
};

/**
 * 4. [New] 음식 사진 스캔 (파일 업로드)
 * POST /diets/scan
 * method: multipart/form-data
 */
export const scanFoodImageApi = async (file) => {
  try {
    const formData = new FormData();
    formData.append("file", file); // 백엔드 @RequestParam("file") 이름과 일치해야 함

    const response = await api.post("/diets/scan", formData, {
      headers: {
        // [중요] 이 요청만 JSON이 아닌 Multipart로 헤더를 덮어씀
        "Content-Type": "multipart/form-data",
      },
    });
    return response.data;
  } catch (error) {
    console.error("API Error (Food Scan):", error);
    throw error;
  }
};
export const applyDietPlanApi = async (memberId, planList) => {
  // 백엔드 DTO 구조에 맞춤
  const payload = {
    memberId: memberId,
    plans: planList,
  };

  const response = await api.post("/diets/schedule", payload);
  return response.data;
};
