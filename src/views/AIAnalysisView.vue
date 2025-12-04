<script setup>
import { ref, onMounted } from 'vue';
import { Sparkles, TrendingUp } from 'lucide-vue-next';
import Footer from '../components/utils/Footer.vue';  
// import Footer from './utils/Footer.vue'; // ⚠️ 주의: 실제 사용 시 이 컴포넌트가 프로젝트에 존재해야 합니다.

// ----------------------------------------------------
// 1. 상태 관리
// ----------------------------------------------------
const isLoading = ref(true);
const analysisData = ref(null);

// ----------------------------------------------------
// 2. 데이터 로드 시뮬레이션
// ----------------------------------------------------
onMounted(() => {
    // 실제 API 호출로 교체해야 하는 부분입니다.
    const timer = setTimeout(() => {
        analysisData.value = {
            overallScore: 78,
            insights: [
                {
                    id: 1,
                    type: 'positive',
                    title: '단백질 섭취 우수',
                    description: '오늘 단백질 목표를 충족했습니다. 근력 유지에 좋습니다.',
                    icon: '✓'
                },
                {
                    id: 2,
                    type: 'warning',
                    title: '나트륨 섭취량 높음',
                    description: '권장 수치의 120% 수준입니다. 소금 섭취를 줄여보세요.',
                    icon: '!'
                },
                {
                    id: 3,
                    type: 'positive',
                    title: '균형잡힌 영양소 비율',
                    description: '탄수화물, 단백질, 지방의 균형이 건강합니다.',
                    icon: '✓'
                },
                {
                    id: 4,
                    type: 'suggestion',
                    title: '내일을 위한 제안',
                    description: '채소 섭취를 50g 정도 늘리면 더 좋은 결과를 기대할 수 있습니다.',
                    icon: '→'
                }
            ],
            recommendation: '현재 식단은 좋은 방향입니다. 나트륨 조절과 채소 다양성을 높이면 더욱 개선될 수 있습니다.',
            trend: 'up' // 또는 'down', 'stable'
        };
        isLoading.value = false;
    }, 2000); // 2초 후 데이터 로드 시뮬레이션

    // 컴포넌트 언마운트 시 타이머 정리
    return () => clearTimeout(timer);
});

// ----------------------------------------------------
// 3. 스타일 클래스 매핑 함수
// ----------------------------------------------------
const getInsightClass = (type) => {
    switch(type) {
        case 'positive': return 'card-positive';
        case 'warning': return 'card-warning';
        case 'suggestion': return 'card-suggestion';
        default: return 'card-default';
    }
};

const getScoreClass = (score) => {
    // analysisData.value가 null일 수 있으므로 안전하게 처리
    if (!score) return 'score-mid';
    if (score >= 80) return 'score-high';
    if (score >= 60) return 'score-mid';
    return 'score-low';
};
</script>

<template>
    <div class="container">
        <div class="wrapper">
            <div class="header-section">
                <div class="title-row">
                    <Sparkles class="icon-sparkle" />
                    <h1>AI 식단 분석</h1>
                </div>
                <p class="subtitle">당신의 식단을 AI Agent가 분석하고 있습니다</p>
            </div>

            <div v-if="isLoading" class="loading-container">
                <div class="loading-box">
                    <div class="pulse-effect">
                        <svg viewBox="0 0 200 220" class="agent-svg">
                            <defs>
                                <radialGradient id="agentGradient" cx="50%" cy="40%">
                                    <stop offset="0%" stop-color="#60a5fa" stop-opacity="0.6" />
                                    <stop offset="100%" stop-color="#3b82f6" stop-opacity="0.3" />
                                </radialGradient>
                                <filter id="glow">
                                    <feGaussianBlur stdDeviation="3" result="coloredBlur"/>
                                    <feMerge>
                                        <feMergeNode in="coloredBlur"/>
                                        <feMergeNode in="SourceGraphic"/>
                                    </feMerge>
                                </filter>
                            </defs>
                            <circle cx="100" cy="90" r="75" fill="url(#agentGradient)" filter="url(#glow)" />
                            <circle cx="100" cy="70" r="35" fill="#f3f4f6" />
                            <circle cx="88" cy="65" r="4" fill="#1f2937" />
                            <circle cx="112" cy="65" r="4" fill="#1f2937" />
                            <circle cx="89" cy="63" r="1.5" fill="#fff" />
                            <circle cx="113" cy="63" r="1.5" fill="#fff" />
                            <circle cx="100" cy="75" r="3" fill="#e5e7eb" />
                            <path d="M 95 82 Q 100 85 105 82" stroke="#d1d5db" stroke-width="2" fill="none" stroke-linecap="round" />
                            <ellipse cx="100" cy="125" rx="28" ry="35" fill="#e0e7ff" />
                            <ellipse cx="72" cy="120" rx="12" ry="28" fill="#f3f4f6" transform="rotate(-20 72 120)" />
                            <ellipse cx="128" cy="120" rx="12" ry="28" fill="#f3f4f6" transform="rotate(20 128 120)" />
                            <circle cx="62" cy="135" r="8" fill="#fcd34d" />
                            <circle cx="138" cy="135" r="8" fill="#fcd34d" />
                            <g opacity="0.7">
                                <circle cx="45" cy="60" r="3" fill="#a78bfa" />
                                <line x1="45" y1="50" x2="45" y2="70" stroke="#a78bfa" stroke-width="1.5" />
                                <line x1="35" y1="60" x2="55" y2="60" stroke="#a78bfa" stroke-width="1.5" />
                            </g>
                            <g opacity="0.7">
                                <circle cx="155" cy="50" r="2.5" fill="#c084fc" />
                                <line x1="155" y1="42" x2="155" y2="58" stroke="#c084fc" stroke-width="1.5" />
                                <line x1="147" y1="50" x2="163" y2="50" stroke="#c084fc" stroke-width="1.5" />
                            </g>
                        </svg>
                    </div>
                </div>
                <div class="loading-dots">
                    <div class="dot" v-for="i in 3" :key="i" :style="{ animationDelay: `${i * 0.15}s` }"></div>
                </div>
            </div>

            <div v-else class="content-container">
                <div class="main-score-card">
                    <div class="score-content">
                        <div class="agent-wrapper">
                            <svg viewBox="0 0 200 220" class="agent-svg">
                                <defs>
                                    <radialGradient id="agentGradient2" cx="50%" cy="40%">
                                        <stop offset="0%" stop-color="#60a5fa" stop-opacity="0.6" />
                                        <stop offset="100%" stop-color="#3b82f6" stop-opacity="0.3" />
                                    </radialGradient>
                                    <filter id="glow2">
                                        <feGaussianBlur stdDeviation="3" result="coloredBlur"/>
                                        <feMerge>
                                            <feMergeNode in="coloredBlur"/>
                                            <feMergeNode in="SourceGraphic"/>
                                        </feMerge>
                                    </filter>
                                </defs>
                                <circle cx="100" cy="90" r="75" fill="url(#agentGradient2)" filter="url(#glow2)" />
                                <circle cx="100" cy="70" r="35" fill="#f3f4f6" />
                                <circle cx="88" cy="65" r="4" fill="#1f2937" />
                                <circle cx="112" cy="65" r="4" fill="#1f2937" />
                                <circle cx="89" cy="63" r="1.5" fill="#fff" />
                                <circle cx="113" cy="63" r="1.5" fill="#fff" />
                                <circle cx="100" cy="75" r="3" fill="#e5e7eb" />
                                <path d="M 95 82 Q 100 85 105 82" stroke="#d1d5db" stroke-width="2" fill="none" stroke-linecap="round" />
                                <ellipse cx="100" cy="125" rx="28" ry="35" fill="#e0e7ff" />
                                <ellipse cx="72" cy="120" rx="12" ry="28" fill="#f3f4f6" transform="rotate(-20 72 120)" />
                                <ellipse cx="128" cy="120" rx="12" ry="28" fill="#f3f4f6" transform="rotate(20 128 120)" />
                                <circle cx="62" cy="135" r="8" fill="#fcd34d" />
                                <circle cx="138" cy="135" r="8" fill="#fcd34d" />
                                <g opacity="0.7">
                                    <circle cx="45" cy="60" r="3" fill="#a78bfa" />
                                    <line x1="45" y1="50" x2="45" y2="70" stroke="#a78bfa" stroke-width="1.5" />
                                    <line x1="35" y1="60" x2="55" y2="60" stroke="#a78bfa" stroke-width="1.5" />
                                </g>
                                <g opacity="0.7">
                                    <circle cx="155" cy="50" r="2.5" fill="#c084fc" />
                                    <line x1="155" y1="42" x2="155" y2="58" stroke="#c084fc" stroke-width="1.5" />
                                    <line x1="147" y1="50" x2="163" y2="50" stroke="#c084fc" stroke-width="1.5" />
                                </g>
                            </svg>
                        </div>
                        <p class="score-label">분석 완료!</p>
                        <div class="score-display">
                            <div class="score-number-box">
                                <span :class="['score-value', getScoreClass(analysisData.overallScore)]">
                                    {{ analysisData.overallScore }}
                                </span>
                                <span class="score-total">/100</span>
                            </div>
                            <div v-if="analysisData.trend === 'up'" class="trend-badge">
                                <TrendingUp class="icon-trend" />
                                <span>전일 대비 +5</span>
                            </div>
                        </div>
                    </div>
                    
                    <div class="progress-track">
                        <div 
                            class="progress-fill"
                            :style="{ width: `${analysisData.overallScore}%` }"
                        ></div>
                    </div>
                </div>

                <div class="insights-section">
                    <h2 class="section-title">
                        <Sparkles class="icon-sm" />
                        AI의 분석 결과
                    </h2>
                    <div 
                        v-for="(insight, idx) in analysisData.insights" 
                        :key="insight.id"
                        :class="['insight-card', getInsightClass(insight.type)]"
                        :style="{ animationDelay: `${idx * 100}ms` }"
                    >
                        <div class="card-body">
                            <div class="icon-circle">
                                {{ insight.icon }}
                            </div>
                            <div class="text-content">
                                <h3>{{ insight.title }}</h3>
                                <p>{{ insight.description }}</p>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="advice-card">
                    <div class="card-body">
                        <Sparkles class="icon-advice" />
                        <div class="text-content">
                            <h3>AI Agent의 조언</h3>
                            <p>{{ analysisData.recommendation }}</p>
                        </div>
                    </div>
                </div>

                <div class="button-group">
                    <button class="btn btn-primary">상세 분석 보기</button>
                    <button class="btn btn-secondary">식단 개선안</button>
                </div>
            </div>
        </div>
        
        <div class="footer-spacer"></div>
    </div>

    <div class="footer-fixed temporary-footer">
        <Footer></Footer>>
    </div>
</template>

<style scoped>
/* 푸터 겹침 방지를 위한 CSS 수정 */
.footer-fixed {
    position: fixed; /* 화면 하단에 고정 */
    bottom: 0;
    left: 0;
    width: 100%;
    z-index: 1000; /* 다른 요소 위에 표시 */
}



/* 푸터 높이만큼 하단 공간을 띄워 컨텐츠가 가려지지 않게 합니다. */
.footer-spacer {
    height: 100px; /* Footer 컴포넌트의 예상 높이 (필요에 따라 조절) */
    width: 100%;
}
/* ------------------------------------------------------------------- */


/* --- 기본 레이아웃 및 배경 --- */
.container {
    min-height: 100vh;
    background-color: #000000;
    color: #ffffff;
    padding: 1rem;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
}

.wrapper {
    max-width: 42rem; /* max-w-2xl */
    margin: 0 auto;
}

/* --- 헤더 --- */
.header-section {
    margin-bottom: 1.5rem;
}

.title-row {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin-bottom: 0.5rem;
}

.title-row h1 {
    font-size: 1.5rem;
    font-weight: 700;
}

.icon-sparkle {
    width: 1.25rem;
    height: 1.25rem;
    color: #c084fc; /* purple-400 */
}

.subtitle {
    color: #9ca3af; /* gray-400 */
    font-size: 0.875rem;
}

/* --- 로딩 애니메이션 --- */
.loading-container {
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.loading-box {
    background: linear-gradient(to right, #1f2937, #111827);
    border-radius: 1.5rem;
    padding: 3rem;
    border: 1px solid rgba(55, 65, 81, 0.5);
    display: flex;
    justify-content: center;
}

.pulse-effect {
    animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}

.agent-svg {
    width: 8rem;
    height: 8rem;
}

.loading-dots {
    display: flex;
    justify-content: center;
    gap: 0.75rem;
}

.dot {
    width: 0.5rem;
    height: 0.5rem;
    background-color: #4b5563;
    border-radius: 9999px;
    animation: bounce 1s infinite;
}

/* --- 메인 점수 카드 (어두운 카드 - 원본 세로 배치) --- */
.main-score-card {
    background: linear-gradient(to bottom right, #111827, #1f2937, #000000); /* gray-900 to black */
    border-radius: 1.5rem;
    padding: 2rem;
    border: 1px solid rgba(55, 65, 81, 0.5);
    backdrop-filter: blur(4px);
    margin-bottom: 1.5rem;
}

.score-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 1.5rem;
}

.agent-wrapper {
    margin-bottom: 1rem;
}

.agent-svg {
    width: 8rem;
    height: 8rem;
}

.score-label {
    color: #9ca3af;
    font-size: 0.875rem;
    margin-bottom: 0.75rem;
}

.score-number-box {
    display: flex;
    align-items: baseline;
    justify-content: center;
    gap: 0.5rem;
    margin-bottom: 0.5rem;
}

.score-value {
    font-size: 3.75rem; /* 6xl */
    font-weight: 700;
}

/* 점수 색상 조건부 */
.score-high { color: #f59e0b; /* amber-400 (이미지처럼 노란색) */ }
.score-mid { color: #fbbf24; }
.score-low { color: #f87171; }

.score-total {
    color: #9ca3af;
    font-size: 1.25rem;
}

.trend-badge {
    display: inline-flex;
    align-items: center;
    gap: 0.25rem;
    background-color: rgba(16, 185, 129, 0.2);
    padding: 0.25rem 0.75rem;
    border-radius: 9999px;
    border: 1px solid rgba(16, 185, 129, 0.3);
    font-size: 0.75rem;
    color: #6ee7b7; /* emerald-300 */
}

.icon-trend {
    width: 1rem;
    height: 1rem;
    color: #34d399;
}

/* 프로그레스 바 */
.progress-track {
    width: 100%;
    background-color: rgba(55, 65, 81, 0.3);
    border-radius: 9999px;
    height: 0.625rem; /* 2.5 */
    overflow: hidden;
    border: 1px solid rgba(75, 85, 99, 0.3);
}

.progress-fill {
    height: 100%;
    background: linear-gradient(to right, #a855f7, #ec4899); /* purple to pink */
    border-radius: 9999px;
    transition: width 1s ease-out;
}

/* --- 인사이트 카드 영역 --- */
.insights-section {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
    margin-bottom: 1.5rem;
}

.section-title {
    font-size: 0.875rem;
    font-weight: 600;
    color: #d1d5db;
    padding: 0 0.25rem;
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.icon-sm {
    width: 1rem;
    height: 1rem;
    color: #c084fc;
}

.insight-card {
    border-radius: 0.75rem;
    padding: 1rem;
    backdrop-filter: blur(4px);
    border: 1px solid transparent;
    /* 애니메이션 */
    opacity: 0;
    animation: slideUp 0.5s ease-out forwards;
}

.card-body {
    display: flex;
    gap: 0.75rem;
}

/* 카드 타입별 스타일 */
.card-positive {
    background: linear-gradient(to right, rgba(16, 185, 129, 0.1), rgba(5, 150, 105, 0.05));
    border-color: rgba(16, 185, 129, 0.3);
}

.card-warning {
    background: linear-gradient(to right, rgba(245, 158, 11, 0.1), rgba(217, 119, 6, 0.05));
    border-color: rgba(245, 158, 11, 0.3);
}

.card-suggestion {
    background: linear-gradient(to right, rgba(59, 130, 246, 0.1), rgba(37, 99, 235, 0.05));
    border-color: rgba(59, 130, 246, 0.3);
}

.icon-circle {
    flex-shrink: 0;
    width: 2rem;
    height: 2rem;
    border-radius: 9999px;
    background-color: rgba(55, 65, 81, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 0.875rem;
    font-weight: 600;
    color: #fff;
}

.text-content {
    flex: 1;
    min-width: 0;
}

.text-content h3 {
    font-weight: 600;
    font-size: 0.875rem;
    margin-bottom: 0.25rem;
}

.text-content p {
    font-size: 0.75rem;
    color: #d1d5db; /* gray-300 */
    line-height: 1.4;
}

/* --- 조언 카드 --- */
.advice-card {
    background: linear-gradient(to right, rgba(99, 102, 241, 0.1), rgba(168, 85, 247, 0.1));
    border-radius: 0.75rem;
    padding: 1rem;
    border: 1px solid rgba(99, 102, 241, 0.2);
    margin-bottom: 1.5rem;
}

.icon-advice {
    width: 1.25rem;
    height: 1.25rem;
    color: #818cf8;
    flex-shrink: 0;
    margin-top: 0.125rem;
}

/* --- 버튼 그룹 --- */
.button-group {
    display: flex;
    gap: 0.75rem;
}

.btn {
    flex: 1;
    padding: 0.75rem;
    border-radius: 0.75rem;
    font-weight: 600;
    font-size: 0.875rem;
    cursor: pointer;
    border: none;
    transition: all 0.2s;
}

.btn-primary {
    background: linear-gradient(to right, #9333ea, #db2777); /* purple to pink */
    color: white;
}
.btn-primary:hover {
    opacity: 0.9;
}

.btn-secondary {
    background-color: #1f2937;
    color: #e5e7eb;
    border: 1px solid #4b5563;
}
.btn-secondary:hover {
    background-color: #374151;
}

/* --- 키프레임 애니메이션 --- */
@keyframes pulse {
    0%, 100% { opacity: 1; }
    50% { opacity: .5; }
}

@keyframes bounce {
    0%, 100% { transform: translateY(-25%); animation-timing-function: cubic-bezier(0.8,0,1,1); }
    50% { transform: none; animation-timing-function: cubic-bezier(0,0,0.2,1); }
}

@keyframes slideUp {
    from {
        opacity: 0;
        transform: translateY(10px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}
</style>