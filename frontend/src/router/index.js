import { createRouter, createWebHistory } from "vue-router";
import HomeView from "../views/HomeView.vue";
import LoginView from "../views/LoginView.vue";
import SignupView from "../views/SignupView.vue";
import { useAuthStore } from "../stores/authStore";
import AiDietPlan from "../views/AIDietPlan.vue";
import CalendarView from "../views/CalendarView.vue";
import AIAnalysisView from "../views/AIAnalysisView.vue";
import ProfileView from "../views/ProfileView.vue";
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // 1. 로그인 페이지 (누구나 접근 가능)
    {
      path: "/login",
      name: "login",
      component: LoginView,
      meta: { noAuth: true, hideHeader: true }, // 🔒 로그인한 사람은 접근 금지
    },
    // router/index.js 파일에서 routes 배열 안에 추가
    {
      path: "/signup",
      name: "signup",
      component: SignupView,
      meta: { noAuth: true, hideHeader: true }, // 🔒 로그인한 사람은 접근 금지
    },
    // 2. 홈 화면 (🔒 로그인 필요)
    {
      path: "/",
      name: "home",
      component: HomeView,
      meta: { requiresAuth: true },
    },
    // 3. 캘린더 (🔒 로그인 필요)
    {
      path: "/calendar",
      name: "calendar",
      component: CalendarView,
      meta: { requiresAuth: true },
    },
    // 4. AI 분석 (🔒 로그인 필요)
    {
      path: "/ai-analysis",
      name: "ai-analysis",
      component: AIAnalysisView,
      meta: { requiresAuth: true },
      children: [
        {
          path: "diet-plan",
          name: "dietPlan",
          component: AiDietPlan,
        },
      ],
    },
    // 5. 프로필 (🔒 로그인 필요)
    {
      path: "/profile",
      name: "profile",
      component: ProfileView,
      meta: { requiresAuth: true },
    },
  ],
});

// 🚧 전역 가드 (검문소) 설정
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();

  // 1. [로그인 필수] 페이지인데 로그인을 안 했다? -> 로그인 페이지로 강제 이동
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    alert("로그인이 필요한 서비스입니다.");
    next("/login");
    return;
  }

  // 2. [로그인 금지] 페이지(로그인/회원가입)인데 이미 로그인을 했다? -> 메인으로 튕겨내기
  if (to.meta.noAuth && authStore.isAuthenticated) {
    next("/");
    return;
  }

  // 3. 통과
  next();
});

export default router;
