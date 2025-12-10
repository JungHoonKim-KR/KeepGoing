import api from "../index"; // 위에서 만든 설정 가져오기

// 1. 회원가입
// POST /api/auth/signup
export const signupApi = (userData) => {
  return api.post("/auth/signup", userData);
};

// 2. 로그인
// POST /api/auth/login
export const loginApi = (credentials) => {
  return api.post("/auth/login", credentials);
};

// 3. 로그아웃
// POST /api/auth/logout?memberId=...
export const logoutApi = (memberId) => {
  return api.post(`/auth/logout?memberId=${memberId}`);
};

// (선택) 토큰 재발급
export const reissueApi = (refreshToken) => {
  return api.post("/auth/reissue", refreshToken);
};
