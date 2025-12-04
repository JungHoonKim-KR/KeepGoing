import { createRouter, createWebHistory } from "vue-router";
import HomeView from "../views/HomeView.vue";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: "/",
            name: "home",
            component: HomeView,
        },
        {
            path: "/calendar",
            name: "calendar",
            component: () => import("../views/CalendarView.vue"),
        },
        // AI 분석 부모 라우트
        {
            path: "/ai-analysis",
            name: "ai-analysis",
            component: () => import("../views/AIAnalysisView.vue"), // AIAnalysisView.vue가 이 경로를 담당
            children: [
                // AI Diet Plan 자식 라우트
                {
                    path: "diet-plan", // 최종 경로는 /ai-analysis/diet-plan
                    name: "dietPlan",
                    component: () => import("../views/AiDietPlan.vue"),
                }
            ]
        },
        {
            path: "/profile",
            name: "profile",
            component: () => import("../views/ProfileView.vue"),
        },
    ],
});

export default router;