import { createRouter, createWebHistory } from "vue-router";
import HomeView from "../views/HomeView.vue";
import LoginView from "@/views/LoginView.vue"; // LoginView import 확인

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 1. 로그인 페이지 (누구나 접근 가능)
    {
      path: "/login",
      name: "login",
      component: LoginView,
    },
    // router/index.js 파일에서 routes 배열 안에 추가
    {
      path: "/signup",
      name: "signup",
      component: () => import("../views/SignupView.vue"),
    },
    // 2. 홈 화면 (🔒 로그인 필요)
    {
      path: "/",
      name: "home",
      component: HomeView,
      meta: { requiresAuth: true }, // 인증 필요 표시
    },
    // 3. 캘린더 (🔒 로그인 필요)
    {
      path: "/calendar",
      name: "calendar",
      component: () => import("../views/CalendarView.vue"),
      meta: { requiresAuth: true },
    },
    // 4. AI 분석 (🔒 로그인 필요)
    {
      path: "/ai-analysis",
      name: "ai-analysis",
      component: () => import("../views/AIAnalysisView.vue"),
      meta: { requiresAuth: true },
      children: [
        {
          path: "diet-plan",
          name: "dietPlan",
          component: () => import("../views/AiDietPlan.vue"),
        },
      ],
    },
    // 5. 프로필 (🔒 로그인 필요)
    {
      path: "/profile",
      name: "profile",
      component: () => import("../views/ProfileView.vue"),
      meta: { requiresAuth: true },
    },
  ],
});

// 🛡️ 네비게이션 가드 (문지기 설정)
router.beforeEach((to, from, next) => {
  // 로그인 성공 시 LoginView에서 저장했던 토큰 확인
  const isAuthenticated = localStorage.getItem("userToken");

  // 1. 로그인이 필요한 페이지(requiresAuth)에 접근하는데, 토큰이 없다면?
  if (to.meta.requiresAuth && !isAuthenticated) {
    next("/login"); // 로그인 페이지로 강제 이동
  }
  // 2. 이미 로그인 상태인데 로그인 페이지로 가려고 하면?
  else if (to.path === "/login" && isAuthenticated) {
    next("/"); // 홈으로 돌려보냄
  }
  // 3. 그 외에는 통과
  else {
    next();
  }
});

export default router;
