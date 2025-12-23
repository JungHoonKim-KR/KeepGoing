<template>
  <div class="home-view retro-theme" @click="initAudioContext">
    <section class="page daily-page">
      <div class="page-content">
        <div class="retro-header">
          <span class="blinking-cursor">▶</span> PLAYER_DATE:
          {{ formattedDate }}
        </div>

        <div class="pixel-box main-stat-box">
          <div class="stat-header">
            <span class="label">(Kal)</span>
            <span class="val">{{ currentEnergy }} / {{ maxEnergy }}</span>
          </div>
          <div class="retro-progress-container" @click="triggerLevelUp">
            <div class="retro-progress-bar hp-bar" :style="{ width: hpPercent + '%' }"></div>
            <div class="click-hint">CLICK BAR TO LEVEL UP!</div>
          </div>
        </div>

        <div class="game-screen-container" @click="handleScreenClick">
          <div class="pixel-border">
            <div class="screen-bg">
              <div class="level-badge" :class="{ 'level-up-anim': isLevelingUp }">
                {{ isLevelingUp ? "LEVEL UP!" : `Lv.${currentLevel}` }}
              </div>

              <img
                :src="currentCharacterImage"
                alt="Character"
                class="character-gif pixelated"
                :class="{ bounce: isBouncing }"
              />

              <div class="screen-xp-area">
                <div class="screen-xp-label">EXP</div>
                <div class="screen-xp-bar">
                  <div class="screen-xp-fill" :style="{ width: currentLevelExpPercent + '%' }"></div>
                </div>
              </div>

              <div class="edit-hint">Click screen to change character</div>
            </div>
          </div>
        </div>

        <div class="pixel-box stats-box">
          <h3 class="box-title">PLAYER STATS</h3>
          <div class="stat-row" v-for="(stat, idx) in stats" :key="idx">
            <div class="stat-icon">{{ stat.label }}</div>
            <div class="stat-bar-group">
              <div class="retro-bar-bg">
                <div class="retro-bar-fill" :class="stat.class" :style="{ width: stat.percent }"></div>
              </div>
              <span class="stat-val">{{ stat.val }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="page meal-page">
      <div class="page-content">  
        
        <button v-if="recommendedMeals.length > 0" class="quest-trigger-btn" @click="showQuestModal = true">
          >
          <span>📜</span> VIEW DAILY QUEST
        </button>

        <Teleport to="body">
          <div v-if="showQuestModal" class="quest-modal-overlay" @click.self="showQuestModal = false">
            <div class="quest-paper pixel-box">
              <div class="retro-header-sm" style="border-color: #5d4037; color: #3e2723; margin-bottom: 15px">
                <span style="font-weight: bold">📜 DAILY QUEST LOG</span>
                <button class="close-quest-btn" @click="showQuestModal = false">✖</button>
              </div>

              <div class="rec-list">
                <div v-for="(rec, idx) in recommendedMeals" :key="idx" class="rec-item dark-mode">
                  <div class="rec-icon-badge">{{ rec.icon }}</div>
                  <div class="rec-info">
                    <div class="rec-type">{{ rec.type }}</div>
                    <div class="rec-menu">{{ rec.menu }}</div>
                  </div>
                  <div class="rec-cal">{{ rec.cal }}kcal</div>
                </div>

                <div v-if="recommendedMeals.length === 0" class="rec-empty dark-text">
                  <span>오늘의 추천 식단은 없습니다!</span>
                </div>
              </div>

              <div v-if="recommendedMeals.length != 0"class="quest-footer">
                <p>"이대로 먹으면 경험치 보너스!"</p>
                <button class="retro-btn sm-btn quest-confirm-btn" @click="showQuestModal = false">확인 (OK)</button>
              </div>
            </div>
          </div>
        </Teleport>

        <div v-if="todayMeals.length === 0" class="pixel-card interactive" @click="handleMealClick">
          <div class="card-inner">
            <h2>식단 기록</h2>
            <div class="icon-8bit">🍗</div>
            <p class="pixel-text">오늘의 식사를<br />기록하지 않았습니다.</p>
            <button class="retro-btn press-start">INSERT COIN</button>
          </div>
        </div>

        <div v-else class="meal-log-container">
          <div class="retro-header-sm">
            <span>INVENTORY (MEALS)</span>
            <span class="total-xp">TOTAL XP: {{ todayMeals.reduce((acc, cur) => acc + cur.cal, 0) }}</span>
          </div>

          <div class="meal-list">
            <div v-for="meal in todayMeals" :key="meal.id" class="meal-slot" @click="handleMealClick">
              <div class="slot-icon-box">{{ meal.icon }}</div>
              <div class="slot-info">
                <div class="slot-top">
                  <span class="meal-type-badge">{{ meal.type }}</span>
                  <span class="meal-cal">{{ meal.cal }} XP</span>
                </div>
                <div class="meal-name">{{ meal.name }}</div>
              </div>
            </div>

            <div v-if="!isAllMealsRecorded" class="meal-slot add-slot" @click="handleMealClick">
              <span class="plus-icon">+</span>
              <span class="add-text">ADD NEW ITEM</span>
            </div>
          </div>
        </div>

        <div class="ai-btn-container">
          <button
            class="ai-analyze-btn"
            :class="{ active: isAllMealsRecorded }"
            :disabled="!isAllMealsRecorded"
            @click="startAIAnalysis"
          >
            <div class="btn-shimmer" v-if="isAllMealsRecorded"></div>

            <div class="btn-content">
              <span class="btn-icon">
                <template v-if="isAllMealsRecorded">⚡</template>
                <template v-else>🔒</template>
              </span>
              <span class="btn-text">
                {{ isAllMealsRecorded ? "AI STRATEGY ANALYSIS" : `LOCKED (${recordedCount}/4)` }}
              </span>
            </div>

            <div class="corner-line top-left"></div>
            <div class="corner-line bottom-right"></div>
          </button>
        </div>
      </div>
    </section>

    <section class="page water-page">
      <div class="page-content split-layout">
        <div class="pixel-card interactive blue-theme half-card" @click="handleWaterClick">
          <template v-if="waterData.water === 0">
            <h1 class="page-title pixel-font">수분 섭취</h1>
            <div class="empty-state-icon">💧</div>
            <p class="pixel-text-center">기록이 없습니다.</p>
            <button class="retro-btn blue-btn sm-btn">START</button>
          </template>

          <template v-else>
            <h1 class="page-title pixel-font">수분 섭취</h1>

            <div class="mana-bar-container">
              <div
                class="mana-bar-fill"
                :style="{
                  width: Math.min((waterData.water / waterData.goal) * 100, 100) + '%',
                }"
              >
                <div class="glare-effect"></div>
              </div>
              <div class="mana-text-overlay">
                {{ waterData.water }}L <span class="divider">/</span> {{ waterData.goal }}L
              </div>
            </div>
            <button class="retro-btn blue-btn sm-btn" style="margin-top: auto">DRINK</button>
          </template>
        </div>

        <div class="pixel-card interactive green-theme half-card" @click="handleWeightClick">
          <template v-if="weightData.weight == 0.0">
            <h1 class="page-title pixel-font">체중 기록</h1>
            <div class="empty-state-icon">⚖️</div>
            <p class="pixel-text-center">기록이 없습니다.</p>
            <button class="retro-btn green-btn sm-btn">RECORD</button>
          </template>

          <template v-else>
            <div class="weight-dashboard">
              <h1 class="page-title pixel-font">체중 기록</h1>
              <div class="score-display">
                <span class="score-val">{{ weightData.weight }}</span>
                <span class="score-unit">KG</span>
                <div class="score-change" :class="weightData.diff > 0 ? 'bad' : 'good'">
                  <span class="change-icon">{{ weightData.diff > 0 ? "▲" : "▼" }}</span>
                  {{ Math.abs(weightData.diff) }}kg
                  <span class="change-text">{{ weightData.diff > 0 ? "(WARN)" : "(GOOD)" }}</span>
                </div>
              </div>
            </div>
            <button class="retro-btn green-btn sm-btn" style="margin-top: auto">UPDATE</button>
          </template>
        </div>
      </div>
    </section>

    <div v-if="showCharModal" class="modal-overlay" @click.self="showCharModal = false">
      <div class="pixel-card char-select-modal" @click.stop>
        <div class="retro-header-sm">SELECT CHARACTER</div>
        <div class="char-grid">
          <div
            v-for="char in characterList"
            :key="char.id"
            class="char-slot"
            :class="{
              locked: char.isLocked,
              selected: char.id === selectedCharId,
            }"
            @click="selectCharacter(char)"
          >
            <div v-if="char.isLocked" class="lock-overlay">🔒</div>
            <img :src="char.src" class="grid-char-img" />
            <span class="char-num">NO.{{ char.id }}</span>
          </div>
        </div>
        <button class="retro-btn" @click="showCharModal = false">CLOSE</button>
      </div>
    </div>

    <div v-if="showModal" class="modal-overlay" @click="closeModal"></div>
    <MealRecordModal
      v-if="showMealModal"
      @close="closeMealModal"
      :date-to-use="formattedDate"
      :initial-meal-data="todayMealMap"
    />
    <WaterRecordModal
      v-if="showWaterModal"
      @close="closeWaterModal"
      @update-water="handleWaterUpdate"
      :initial-amount="waterData.water"
      :initial-goal="waterData.goal"
      :date-to-use="formattedDate"
    />
    <WeightRecordModal
      v-if="showWeightModal"
      @close="closeWeightModal"
      @update-weight="handleWeightUpdate"
      :date-to-use="formattedDate"
    />

    <Transition name="fade">
      <div v-if="isAiLoading" class="loading-overlay">
        <div class="loading-content">
          <div class="loading-icon">🤖</div>
          <div class="loading-status-bar">
            <div class="status-fill"></div>
          </div>
          <p class="loading-text">{{ loadingText }}</p>
        </div>
      </div>
    </Transition>

    <template>
      <Teleport to="body">
        <Transition name="retro-modal">
          <div v-if="isResultModalOpen" class="ai-result-overlay" @click.self="closeResultModal">
            <div class="ai-result-modal retro-pixel-border">
              <div class="modal-header-section">
                <h2 class="modal-main-title">
                  <span class="glitch" :data-text="analysisResult?.dailyTitle">{{ analysisResult?.dailyTitle }}</span>
                </h2>
              </div>

              <div class="modal-body-scroll">
                <div class="score-card-retro premium-border">
                  <div class="rank-aura-container">
                    <div class="rank-aura" :class="'aura-' + analysisResult?.rank"></div>
                    <div class="rank-container">
                      <div class="rank-label">CLASS</div>
                      <div class="rank-visual-mega" :class="'rank-' + analysisResult?.rank">
                        {{ analysisResult?.rank }}
                      </div>
                    </div>
                  </div>

                  <div class="score-container-mega">
                    <div class="score-label-neon">TOTAL GAINED XP</div>
                    <div class="score-number-glitch" :data-text="analysisResult?.score">
                      {{ analysisResult?.score }}
                    </div>
                    <div class="xp-bar-mini">
                      <div class="xp-bar-fill"></div>
                    </div>
                  </div>
                </div>

                <div class="physical-stats-retro">
                  <h3 class="section-title-retro">▶ ENERGY_METRICS</h3>

                  <div class="energy-dashboard">
                    <div class="calorie-gauge pixel-box">
                      <span class="gauge-label">TOTAL ENERGY</span>
                      <div class="gauge-val blink-text">
                        🔥 {{ analysisResult?.totalCalories || 0 }}
                        <span class="unit">kcal</span>
                      </div>
                    </div>

                    <div class="physical-stats-retro">
                      <div class="training-grid">
                        <div
                          v-for="(ex, i) in analysisResult?.recommendedExercises"
                          :key="i"
                          class="training-card interactive-card"
                          @click="searchAndPlayYoutube(ex.name, ex.time, i)"
                        >
                          <div class="card-icon floating">{{ ex.emoji }}</div>
                          <div class="card-info">
                            <div class="card-name">{{ ex.name }}</div>
                            <div class="card-time">{{ ex.time }}분</div>
                          </div>
                          <div class="youtube-hint">▶ Play</div>
                        </div>
                      </div>
                    </div>

                    <div v-if="currentVideoId || isVideoLoading" class="video-player-section">
                      <div class="pixel-box video-box">
                        <div v-if="isVideoLoading" class="video-loading">
                          <span class="blink-text">SEARCHING SATELLITE...</span>
                        </div>

                        <iframe
                          v-else
                          width="100%"
                          height="315"
                          :src="`https://www.youtube.com/embed/${currentVideoId}?autoplay=1`"
                          title="YouTube video player"
                          frameborder="0"
                          allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                          allowfullscreen
                        ></iframe>

                        <button class="close-video-btn" @click="closeVideo">✖ CLOSE VIDEO</button>
                      </div>
                    </div>
                  </div>
                </div>

                <div class="insights-section">
                  <h3 class="section-title-retro">▶ ANALYSIS_LOG</h3>
                  <div class="insight-list-retro">
                    <div
                      v-for="(item, idx) in analysisResult?.insights"
                      :key="idx"
                      class="insight-card-retro"
                      :class="item.type"
                    >
                      <div class="insight-header">
                        <span class="status-dot"></span>
                        <h4 class="insight-title">[{{ item.title }}]</h4>
                      </div>
                      <p class="insight-description">{{ item.description }}</p>
                    </div>
                  </div>
                </div>

                <div class="summary-box-retro">
                  <div class="npc-thumb">🤖</div>
                  <p class="summary-text">
                    {{ analysisResult?.oneLineSummary }}
                  </p>
                </div>
              </div>

              <button class="retro-confirm-btn" @click="closeResultModal">RETURN TO MENU (A)</button>
            </div>
          </div>
        </Transition>
      </Teleport>
    </template>
    <Footer></Footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from "vue";
import { useConfigStore } from "@/stores/configStore";
import { useAuthStore } from "@/stores/authStore";
import { useRoute } from "vue-router";
import { useDietStore } from "@/stores/dietStore"; // import 추가
import { analyzeDiet } from "@/api/diet/dietApi";
import Footer from "@/components/utils/Footer.vue";
import dayjs from "dayjs";
import confetti from "canvas-confetti";
import axios from "axios";

import WaterRecordModal from "@/components/record/WaterRecordModal.vue";
import WeightRecordModal from "@/components/record/WeightRecordModal.vue";
import MealRecordModal from "@/components/record/MealRecordModal.vue";

const YOUTUBE_API_KEY = "AIzaSyBwl61AGUcuiXLBjEv6d9I8cHsCPtJpU94";
const dietStore = useDietStore(); // 스토어 사용
const authStore = useAuthStore();
const config = useConfigStore();
const route = useRoute();
const MEMBER_ID = authStore.memberId;
const API_ENDPOINT = config.API_ENDPOINT;

// 캐릭터 유지 로직
const selectedCharId = ref(authStore.profileCharacter || 1);
watch(
  () => authStore.profileCharacter,
  (newVal) => {
    if (newVal) selectedCharId.value = newVal;
  }
);

const recommendedMeals = computed(() => {
  // 0. 오늘 날짜 키값 준비 (YYYY-MM-DD 형식이라고 가정)
  // formattedDate.value가 "2025-12-24" 형태여야 합니다.
  const todayKey = formattedDate.value; 

  // 1. LocalStorage에서 schedule 가져오기
  const storedJson = localStorage.getItem('schedule');

  // 데이터가 아예 없으면 빈 배열 반환
  if (!storedJson) return [];

  try {
    const schedule = JSON.parse(storedJson);
    
    // 객체의 키(날짜들)를 추출하고 정렬
    const dateKeys = Object.keys(schedule).sort();

    // 요구사항: 스케줄이 비어있거나, "첫 번째 식단의 날짜가 오늘이 아니면" 없는 것으로 처리
    if (dateKeys.length === 0 || dateKeys[0] !== todayKey) {
      return [];
    }

    // 오늘 날짜 데이터 가져오기
    const todayData = schedule[todayKey];

    // 방어 코드: 오늘 날짜 키는 있는데 내용(menu)이 잘못되었을 경우
    if (!todayData || !todayData.menu) return [];

    // 칼로리 숫자만 추출하는 헬퍼 함수
    // 예: "오트밀... (약 350kcal)" -> 350 반환
    const extractCal = (text) => {
      const match = text.match(/약\s*(\d+)kcal/);
      return match ? parseInt(match[1], 10) : 0;
    };

    // 2. 화면에 맞게 매핑
    const { breakfast, lunch, dinner } = todayData.menu;

    return [
      { 
        type: "아침", 
        menu: breakfast || "식단 없음", 
        cal: extractCal(breakfast || ""), 
        icon: "🥪" 
      },
      { 
        type: "점심", 
        menu: lunch || "식단 없음", 
        cal: extractCal(lunch || ""), 
        icon: "🍱" 
      },
      { 
        type: "저녁", 
        menu: dinner || "식단 없음", 
        cal: extractCal(dinner || ""), 
        icon: "🥗" 
      },
    ];

  } catch (e) {
    console.error("스케줄 파싱 중 오류 발생:", e);
    return [];
  }
});

// [NEW] 추천 식단 & 모달 로직
const showQuestModal = ref(false);

const fetchRecommendedDietToday = async () => {
  try {
    


    recommendedMeals.value = [
      { type: "아침", menu: "통밀빵 샌드위치 & 아메리카노", cal: 450, icon: "🥪" },
      { type: "점심", menu: "현미밥, 닭가슴살 장조림, 김치", cal: 700, icon: "🍱" },
      { type: "저녁", menu: "연어 샐러드 & 오리엔탈 드레싱", cal: 500, icon: "🥗" },
    ];
  } catch (e) {
    console.error("추천 식단 로드 실패", e);
  }
};

/* --- 기존 로직 --- */
const currentVideoId = ref(null);
const currentPlayingIndex = ref(-1);
const isVideoLoading = ref(false);

const searchAndPlayYoutube = async (name, time, index) => {
  if (!name) return;
  currentPlayingIndex.value = index;
  isVideoLoading.value = true;
  currentVideoId.value = null;

  const query = `${name} ${time}분 운동`;
  try {
    const response = await axios.get("https://www.googleapis.com/youtube/v3/search", {
      params: {
        part: "snippet",
        q: query,
        type: "video",
        maxResults: 1,
        key: YOUTUBE_API_KEY,
      },
    });
    if (response.data.items.length > 0) {
      currentVideoId.value = response.data.items[0].id.videoId;
    } else {
      alert("관련 영상을 찾을 수 없습니다.");
      currentPlayingIndex.value = -1;
    }
  } catch (error) {
    console.error("API Error:", error);
    alert("영상 검색에 실패했습니다.");
    currentPlayingIndex.value = -1;
  } finally {
    isVideoLoading.value = false;
  }
};

const closeVideo = () => {
  currentVideoId.value = null;
  currentPlayingIndex.value = -1;
};

const isAiLoading = ref(false);
const loadingText = ref("AI 분석 서버 연결 중...");

const formattedDate = computed(() => {
  const routeDate = route.query.date;
  return routeDate ? dayjs(routeDate).format("YYYY-MM-DD") : dayjs().format("YYYY-MM-DD");
});

const todayMealMap = ref({ 아침: null, 점심: null, 저녁: null, 간식: null });
const todayMeals = computed(() => {
  if (!todayMealMap.value) return [];
  const mealIcons = { 아침: "🍳", 점심: "🍖", 저녁: "🍲", 간식: "🍰" };
  return Object.entries(todayMealMap.value)
    .filter(([_, meal]) => meal !== null)
    .map(([type, meal], idx) => ({
      id: meal.id ?? idx,
      type,
      icon: mealIcons[type] || "🍽️",
      cal: Math.round(meal.energy || 0),
      name: meal.foods?.map((f) => f.name).join(", ") || "단식",
    }));
});

const isAllMealsRecorded = computed(() => {
  if (!todayMeals.value) return false;
  const requiredTypes = ["아침", "점심", "저녁", "간식"];
  const recordedTypes = todayMeals.value.map((d) => d.type);
  return requiredTypes.every((type) => recordedTypes.includes(type));
});

const recordedCount = computed(() => {
  return todayMeals.value ? todayMeals.value.length : 0;
});

const isResultModalOpen = ref(false);
const analysisResult = ref(null);

const closeResultModal = () => {
  isResultModalOpen.value = false;
};

const startAIAnalysis = async () => {
  if (!isAllMealsRecorded.value) {
    alert("오늘의 4가지 식단(아침, 점심, 저녁, 간식)을 모두 기록해야 분석이 가능합니다!");
    return;
  }
  if (isAiLoading.value) return;

  isAiLoading.value = true;
  const messages = ["🎯 데이터 스캔 중...", "🥩 영양 분석 중...", "🤖 AI 전략 수립 중...", "✨ 결과 정리 중..."];
  let msgIndex = 0;
  loadingText.value = messages[0];
  const msgInterval = setInterval(() => {
    msgIndex = (msgIndex + 1) % messages.length;
    loadingText.value = messages[msgIndex];
  }, 800);

  try {
    const data = await analyzeDiet(MEMBER_ID, formattedDate.value);
    analysisResult.value = {
      score: data.score,
      rank: data.rank,
      dailyTitle: data.dailyTitle,
      oneLineSummary: data.oneLineSummary,
      insights: data.insights,
      totalCalories: data.totalCalories,
      recommendedExercises: data.recommendedExercises,
    };
    await new Promise((resolve) => setTimeout(resolve, 2500));
    isResultModalOpen.value = true;
  } catch (error) {
    console.error("AI 분석 호출 실패:", error);
    analysisResult.value = {
      score: 85,
      rank: "A",
      dailyTitle: "균형잡힌 하루였습니다!",
      oneLineSummary: "단백질 섭취가 우수하고, 전체적인 영양 밸런스가 좋습니다. 내일도 화이팅!",
      insights: [
        {
          type: "good",
          iconType: "muscle",
          title: "단백질 섭취 우수",
          description: "목표 대비 120% 달성으로 근육 성장에 도움이 됩니다.",
        },
        {
          type: "warning",
          iconType: "warning",
          title: "탄수화물 다소 높음",
          description: "권장량보다 15% 높습니다. 저녁 식사량을 조절해보세요.",
        },
        { type: "good", iconType: "check", title: "수분 섭취 적정", description: "하루 2L 목표를 달성했습니다." },
      ],
      totalCalories: 2150,
      recommendedExercises: [
        { name: "가벼운 조깅", time: 30, emoji: "🏃" },
        { name: "스쿼트", time: 15, emoji: "🏋️" },
        { name: "전신 스트레칭", time: 10, emoji: "🧘" },
      ],
    };
    await new Promise((resolve) => setTimeout(resolve, 1000));
    isResultModalOpen.value = true;
  } finally {
    clearInterval(msgInterval);
    isAiLoading.value = false;
  }
};

const currentLevel = computed(() => authStore.level || 1);
const currentLevelExpPercent = computed(() => authStore.exp || 0);
const showCharModal = ref(false);

const getCharImage = (id) => {
  return new URL(`../assets/images/characters/${id}.png`, import.meta.url).href;
};

const currentCharacterImage = computed(() => getCharImage(selectedCharId.value));

const characterList = computed(() => {
  return Array.from({ length: 16 }, (_, i) => {
    const id = i + 1;
    return {
      id,
      src: getCharImage(id),
      isLocked: id > currentLevel.value,
    };
  });
});

const selectCharacter = async (char) => {
  if (char.isLocked) return;
  try {
    const response = await fetch(`${API_ENDPOINT}/api/member/character`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ memberId: MEMBER_ID, characterNumber: char.id }),
    });

    if (response.ok) {
      selectedCharId.value = char.id;
      authStore.profileCharacter = char.id;
      playRetroSound("coin");
      showCharModal.value = false;
    } else {
      alert("변경 실패");
    }
  } catch (error) {
    console.error("API 호출 중 에러 발생:", error);
    alert("서버 통신 중 오류가 발생했습니다.");
  }
};

const handleScreenClick = () => {
  showCharModal.value = true;
};

const waterData = ref({ water: 0, goal: 2.0 });
const weightData = ref({ weight: 0.0, diff: 0.0 });

const showModal = ref(false);
const showWaterModal = ref(false);
const showWeightModal = ref(false);
const showMealModal = ref(false);

const isLevelingUp = ref(false);
const isBouncing = ref(false);
const dialogText = ref('"오늘도 힘내보자구!"');

const maxEnergy = 3000;
const currentEnergy = computed(() => {
  return Object.values(todayMealMap.value)
    .reduce((acc, meal) => acc + (meal ? meal.energy : 0), 0)
    .toFixed(0);
});
const hpPercent = computed(() => Math.min((currentEnergy.value / maxEnergy) * 100, 100).toFixed(0));

const stats = computed(() => {
  const tProtein = Object.values(todayMealMap.value).reduce((acc, meal) => acc + (meal ? meal.protein : 0), 0);
  const tCarb = Object.values(todayMealMap.value).reduce((acc, meal) => acc + (meal ? meal.carbohydrate : 0), 0);
  const tFat = Object.values(todayMealMap.value).reduce((acc, meal) => acc + (meal ? meal.fat : 0), 0);
  const total = tProtein + tCarb + tFat;
  const getP = (v) => (total > 0 ? ((v / total) * 100).toFixed(0) : 0);
  return [
    { label: "⚡탄수화물", class: "carb", percent: `${getP(tCarb)}%`, val: `${tCarb.toFixed(1)}g` },
    { label: "🛡️단백질", class: "protein", percent: `${getP(tProtein)}%`, val: `${tProtein.toFixed(1)}g` },
    { label: "🔮지방", class: "fat", percent: `${getP(tFat)}%`, val: `${tFat.toFixed(1)}g` },
  ];
});

const initAudioContext = () => {};
const playRetroSound = (type) => {
  if (type === "coin" || type === "jump") {
    isBouncing.value = true;
    setTimeout(() => (isBouncing.value = false), 500);
  }
};
const triggerLevelUp = () => {
  if (isLevelingUp.value) return;
  isLevelingUp.value = true;
  dialogText.value = "LEVEL UP! 능력이 상승했다!";
  confetti({ particleCount: 120, spread: 70, origin: { y: 0.6 } });
  setTimeout(() => {
    isLevelingUp.value = false;
    dialogText.value = '"다음 레벨로 가보자!"';
  }, 3000);
};

const handleMealClick = () => (showMealModal.value = true);
const closeMealModal = async () => {
  showMealModal.value = false;
  await fetchDailyDiet();
};
const closeWaterModal = () => (showWaterModal.value = false);
const closeWeightModal = () => (showWeightModal.value = false);
const handleWaterClick = () => (showWaterModal.value = true);
const handleWeightClick = () => (showWeightModal.value = true);
const handleWaterUpdate = async (newAmount) => (waterData.value.water = newAmount);
const handleWeightUpdate = async (newWeight) => {
  if (newWeight) weightData.value.weight = newWeight;
  await fetchWeightData();
};
const closeModal = () => (showModal.value = false);

async function fetchDailyDiet() {
  const url = `${API_ENDPOINT}/diets/meal-daily?memberId=${MEMBER_ID}&date=${formattedDate.value}`;
  try {
    const res = await fetch(url);
    if (res.ok) todayMealMap.value = await res.json();
  } catch (e) {
    console.error(e);
  }
}

async function fetchHydrationData() {
  const url = `${API_ENDPOINT}/diets/hydration?memberId=${MEMBER_ID}&date=${formattedDate.value}`;
  try {
    const res = await fetch(url);
    if (res.ok) waterData.value.water = await res.json();
  } catch (e) {
    console.error(e);
  }
}

async function fetchWeightData() {
  const url = `${API_ENDPOINT}/api/member/weight?memberId=${MEMBER_ID}&date=${formattedDate.value}`;
  try {
    const res = await fetch(url);
    if (res.ok) {
      const d = await res.json();
      weightData.value.weight = d.weight;
      weightData.value.diff = d.diff;
    }
  } catch (e) {
    console.error(e);
  }
}


// [API] 식단 스케쥴 조회 (수정됨: 객체 매핑)
async function fetchSchedules() {
  try {
    const response = await fetch(`${API_ENDPOINT}/diets/schedule?memberId=${MEMBER_ID}`);
    if (!response.ok) throw new Error("Failed to fetch schedules");

    const data = await response.json();

    // List -> Map 변환
    const planMap = {};
    data.forEach((item) => {
      planMap[item.date] = {
        // 백엔드에서 menu가 이미 객체 {breakfast:..., lunch:..., dinner:...} 로 옴
        menu: item.menu,
        quest: item.quest,
        totalCal: item.totalCal,
      };
    });

    localStorage.setItem("schedule", JSON.stringify(planMap));
  } catch (error) {
    console.error("스케쥴 로딩 실패:", error);
    dailyPlanMap.value = {};
  }
};

onMounted(async () => {
  await fetchDailyDiet();
  await fetchHydrationData();
  await fetchWeightData();
  await fetchSchedules();
  // await fetchRecommendedDiet(); // 추천 식단
});
</script>

<style scoped>
@import url("https://cdn.jsdelivr.net/gh/neodgm/neodgm-webfont@latest/neodgm/style.css");

.retro-theme {
  --bg-color: #202028;
  --text-color: #e0e0e0;
  --primary-color: #ff0055;
  --secondary-color: #00e5ff;
  --accent-color: #ffcc00;
  font-family: "NeoDunggeunmo", monospace;
  background-color: var(--bg-color);
  color: var(--text-color);
  height: 100vh;
  overflow-y: scroll;
  scroll-snap-type: y mandatory;
}

/* --- [NEW] 퀘스트 트리거 버튼 --- */
.quest-trigger-btn {
  width: 100%;
  background: #2d2d3a;
  border: 2px dashed #ffd700;
  color: #ffd700;
  padding: 12px;
  font-family: "NeoDunggeunmo";
  font-size: 0.9rem;
  cursor: pointer;
  margin-bottom: 15px;
  transition: all 0.2s;
  text-shadow: 1px 1px 0 #000;
}
.quest-trigger-btn:hover {
  background: rgba(255, 215, 0, 0.1);
  transform: scale(1.02);
}
.blink-icon {
  animation: blink 1s infinite;
  margin-right: 5px;
}

/* --- [NEW] 퀘스트 모달 오버레이 (z-index 높음) --- */
.quest-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.85);
  z-index: 20000; /* 🔥 매우 높은 z-index */
  display: flex;
  justify-content: center;
  align-items: center;
  backdrop-filter: blur(3px);
  animation: popIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}

.quest-paper {
  width: 90%;
  max-width: 350px;
  background: #e6dac3; /* 종이 색상 */
  color: #3e2723; /* 글자 어둡게 */
  border: 4px solid #5d4037;
  padding: 15px;
  box-shadow: 10px 10px 0 rgba(0, 0, 0, 0.5);
}

.close-quest-btn {
  background: transparent;
  border: none;
  font-size: 1.2rem;
  color: #5d4037;
  cursor: pointer;
  font-weight: bold;
}

.rec-item.dark-mode {
  background: rgba(93, 64, 55, 0.1);
  border: 1px solid rgba(93, 64, 55, 0.3);
  margin-bottom: 8px;
}
.rec-item.dark-mode .rec-type {
  color: #8d6e63;
  font-weight: bold;
}
.rec-item.dark-mode .rec-menu {
  color: #3e2723;
}
.rec-item.dark-mode .rec-cal {
  color: #d84315;
}
.rec-empty.dark-text {
  color:black;
}

.quest-footer {
  margin-top: 15px;
  text-align: center;
  font-size: 0.8rem;
  color: #5d4037;
}
.quest-confirm-btn {
  margin-top: 10px;
  background: #5d4037;
  border-color: #8d6e63;
}

@keyframes popIn {
  from {
    transform: scale(0.8);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

/* --- [기존 스타일들 - rec-item 등은 공용 사용] --- */
.rec-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 10px;
}
.rec-item {
  display: flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.1);
  padding: 6px 10px;
  border-radius: 4px;
  border: 1px dashed rgba(255, 255, 255, 0.3);
}
.rec-icon-badge {
  font-size: 1.2rem;
  margin-right: 10px;
  filter: drop-shadow(2px 2px 0 rgba(0, 0, 0, 0.5));
}
.rec-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.rec-type {
  font-size: 0.65rem;
  color: #ffd700;
  margin-bottom: 2px;
}
.rec-menu {
  font-size: 0.85rem;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.rec-cal {
  font-size: 0.75rem;
  color: #00ff00;
  margin-left: 10px;
  font-weight: bold;
}
.rec-empty {
  text-align: center;
  color: #aaa;
  font-size: 0.8rem;
  padding: 10px;
}

/* ... (나머지 기존 스타일 유지) ... */
.section-title-tag {
  display: inline-block;
  background-color: #000;
  color: #fff;
  border: 2px solid #fff;
  padding: 4px 12px;
  font-size: 0.8rem;
  margin: 0 auto 0.1rem;
  width: fit-content;
  align-self: center;
  box-shadow: 4px 4px 0px rgba(0, 0, 0, 0.5);
  letter-spacing: 1px;
}
.daily-page .section-title-tag {
  border-color: var(--secondary-color);
  color: var(--secondary-color);
}
.meal-page .section-title-tag {
  border-color: #ffd700;
  color: #ffd700;
}
.water-page .section-title-tag {
  border-color: #00e5ff;
  color: #00e5ff;
}
.weight-page .section-title-tag {
  border-color: #d500f9;
  color: #d500f9;
}
.meal-type-badge {
  font-size: 0.65rem;
  background: #ff0055;
  color: #fff;
  padding: 2px 6px;
  border: 1px solid #fff;
  display: inline-block;
  line-height: 1;
  text-shadow: 1px 1px 0 #000;
}
.slot-top {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 8px;
  margin-bottom: 4px;
}
.meal-cal {
  font-size: 0.75rem;
  color: #00e5ff;
}
.page {
  min-height: 100vh;
  scroll-snap-align: start;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  padding-bottom: 6rem;
  padding-left: 1rem;
  padding-right: 1rem;
  box-sizing: border-box;
}
.page-content {
  width: 100%;
  max-width: 600px;
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
}
.page-content.split-layout {
  height: 100%;
  max-height: 70vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 1.5rem;
}
.half-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 0;
}
.half-card .page-title {
  margin-bottom: 0.5rem;
  font-size: 1.1rem;
}
.empty-state-icon {
  font-size: 2.5rem;
  margin: 0.5rem 0;
}
.weight-dashboard {
  font-size: 2rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
  width: 100%;
}
.score-display {
  background: rgba(0, 0, 0, 0.2);
  padding: 5px 15px;
  border-radius: 4px;
  margin: 5px 0;
}
.score-val {
  font-size: 2.8rem;
  color: #fff;
  font-weight: bold;
  line-height: 1;
  text-shadow: 2px 2px 0 #000;
  letter-spacing: 2px;
}
.score-unit {
  font-size: 1rem;
  color: #888;
  margin-top: 2px;
  margin-bottom: 10px;
  font-family: monospace;
}
.score-change {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 0.85rem;
  background: rgba(0, 0, 0, 0.4);
  border: 1px solid transparent;
  width: 100%;
  justify-content: center;
  box-sizing: border-box;
}
.score-change.good {
  color: #ffd700;
  border-color: #ffd700;
  background: rgba(0, 229, 255, 0.1);
}
.score-change.bad {
  color: #ff0055;
  border-color: #ff0055;
  background: rgba(255, 0, 85, 0.1);
}
.change-icon {
  font-size: 0.7rem;
}
.change-text {
  font-size: 0.7rem;
  opacity: 0.8;
  margin-left: auto;
}
@media (max-height: 700px) {
  .page-content.split-layout {
    gap: 0.8rem;
  }
  .page {
    padding-top: 2rem;
    padding-bottom: 5rem;
  }
  .pixel-box,
  .pixel-card {
    padding: 0.6rem;
  }
}
.pixel-box,
.pixel-card {
  border: 4px solid #fff;
  box-shadow: 4px 4px 0px 0px rgba(0, 0, 0, 0.5);
  position: relative;
  transition: transform 0.1s;
  cursor: pointer;
}
.pixel-box {
  /* background: #2d2d3a; */
  padding: 0.8rem;
}
.pixel-card {
  padding: 1rem;
  text-align: center;
  background: #e6dac3;
  color: #3e2723;
  border-color: #5d4037;
}
.interactive:active {
  transform: translate(2px, 2px);
  box-shadow: 2px 2px 0 rgba(0, 0, 0, 0.5);
}
.page.daily-page {
  background: #222034;
}
.meal-page {
  background: #4b692f;
}
.water-page {
  background: #1a1a24;
}
.weight-page {
  background: #2a0a29;
}
.retro-header {
  text-align: center;
  color: var(--secondary-color);
  margin-bottom: 0.2rem;
  font-size: 1.1rem;
}
.blinking-cursor {
  animation: blink 1s step-end infinite;
}
@keyframes blink {
  50% {
    opacity: 0;
  }
}
.retro-progress-container {
  height: 20px;
  background: #333;
  border: 2px solid #fff;
  padding: 2px;
  position: relative;
  margin-top: 5px;
}
.retro-progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #ff0055, #ff5500);
  transition: width 0.5s steps(10);
}
.click-hint {
  position: absolute;
  top: -18px;
  right: 0;
  font-size: 0.6rem;
  color: var(--accent-color);
  animation: blink 0.5s infinite alternate;
}
.game-screen-container .pixel-border {
  border: 6px solid #444;
  background: #8fb8ca;
  border-radius: 6px;
  overflow: hidden;
}
.screen-bg {
  background: url("https://i.pinimg.com/originals/10/78/3f/10783f947938361b02390a382c44843b.png") repeat-x bottom;
  background-size: cover;
  width: 100%;
  height: 150px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
}
.edit-hint {
  position: absolute;
  top: 5px;
  right: 5px;
  font-size: 0.6rem;
  color: rgba(255, 255, 255, 0.6);
  animation: blink 2s infinite;
}
.character-gif {
  width: auto;
  max-width: 90%;
  height: 200px;
  image-rendering: pixelated;
  margin-bottom: 5px;
  object-fit: contain;
}
.bounce {
  animation: bounce 0.5s infinite alternate;
}
@keyframes bounce {
  from {
    transform: translateY(0);
  }
  to {
    transform: translateY(-10px);
  }
}

.level-badge {
  position: absolute;
  top: 8px;
  left: 8px;
  background: rgba(0, 0, 0, 0.7);
  color: #fff;
  padding: 2px 6px;
  font-size: 0.7rem;
  border: 2px solid #fff;
  z-index: 10;
}
.screen-xp-area {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  display: flex;
  align-items: center;
  background: rgba(0, 0, 0, 0.6);
  padding: 3px 8px;
  gap: 8px;
  box-sizing: border-box;
}
.screen-xp-label {
  font-size: 0.6rem;
  color: #00e5ff;
  font-weight: bold;
}
.screen-xp-bar {
  flex: 1;
  height: 6px;
  background: #222;
  border: 1px solid #777;
  overflow: hidden;
}
.screen-xp-fill {
  height: 100%;
  background: #00e5ff;
  transition: width 0.5s;
  box-shadow: 0 0 5px #00e5ff;
}
.box-title {
  margin: 0 0 0.5rem 0;
  font-size: 0.9rem;
  color: #aaa;
}
.stat-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 0.3rem;
}
.stat-icon {
  width: 70px;
  font-size: 0.75rem;
}
.stat-bar-group {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
}
.retro-bar-bg {
  flex: 1;
  height: 10px;
  background: #111;
  border: 1px solid #555;
}
.retro-bar-fill {
  height: 100%;
}
.retro-bar-fill.carb {
  background: #ffd700;
}
.retro-bar-fill.protein {
  background: #ff0055;
}
.retro-bar-fill.fat {
  background: #00e5ff;
}
.stat-val {
  font-size: 0.75rem;
  min-width: 35px;
  text-align: right;
}
.retro-btn {
  margin-top: 0.8rem;
  background: #ff0055;
  color: #fff;
  border: 2px solid #fff;
  padding: 8px 16px;
  cursor: pointer;
  box-shadow: 3px 3px 0 #000;
  font-size: 0.9rem;
}
.blue-theme {
  border-color: #00ffff;
  color: #00ffff;
  background: rgba(0, 20, 40, 0.6);
}
.blue-btn {
  background: #00ffff;
  color: #000;
  box-shadow: 3px 3px 0 rgba(0, 255, 255, 0.4);
}
.green-theme {
  border-color: #39ff14;
  color: #39ff14;
  background: rgba(10, 30, 10, 0.6);
}
.green-btn {
  background: #39ff14;
  color: #000;
  box-shadow: 3px 3px 0 rgba(57, 255, 20, 0.4);
}
.green-theme .score-display {
  border-color: #39ff14;
  box-shadow: inset 0 0 10px rgba(57, 255, 20, 0.1);
}
.green-theme .score-val {
  color: #fff;
  text-shadow: 0 0 5px rgba(57, 255, 20, 0.5);
}
.green-theme .score-unit {
  color: #8fbc8f;
}
.meal-log-container {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.retro-header-sm {
  display: flex;
  justify-content: space-between;
  border-bottom: 2px dashed #fff;
  padding-bottom: 5px;
  font-size: 0.85rem;
  color: #ffd700;
}
.meal-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 55vh;
  overflow-y: auto;
}
.meal-slot {
  display: flex;
  align-items: center;
  background: rgba(0, 0, 0, 0.6);
  border: 2px solid #fff;
  padding: 8px;
  gap: 10px;
  box-shadow: 3px 3px 0 rgba(0, 0, 0, 0.3);
}
.meal-name {
  font-size: 0.9rem;
  color: #fff;
  font-weight: bold;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.85);
  z-index: 9999;
  display: flex;
  justify-content: center;
  align-items: center;
}
.char-select-modal {
  width: 90%;
  max-width: 400px;
  max-height: 80vh;
  display: flex;
  flex-direction: column;
  background: #2d2d3a !important;
  color: #fff !important;
  border: 4px solid var(--secondary-color) !important;
  padding: 1rem;
  box-sizing: border-box;
}
.char-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  margin: 15px 0;
  overflow-y: auto;
  padding-right: 5px;
  flex: 1;
}
.char-grid::-webkit-scrollbar {
  width: 6px;
}
.char-grid::-webkit-scrollbar-track {
  background: #1a1a1a;
}
.char-grid::-webkit-scrollbar-thumb {
  background: var(--secondary-color);
}
.char-slot {
  position: relative;
  aspect-ratio: 1 / 1;
  border: 2px solid #555;
  background: #1a1a1a;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  overflow: hidden;
}
.char-slot.selected {
  border-color: var(--secondary-color);
  background: rgba(0, 229, 255, 0.1);
  box-shadow: inset 0 0 8px var(--secondary-color);
}
.char-slot.locked {
  filter: grayscale(1);
  cursor: not-allowed;
  opacity: 0.5;
}
.grid-char-img {
  width: 85%;
  height: 85%;
  object-fit: contain;
  image-rendering: pixelated;
}
.lock-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1rem;
  z-index: 2;
}
.char-num {
  position: absolute;
  bottom: 1px;
  right: 2px;
  font-size: 0.5rem;
  color: #777;
}
.mana-bar-container {
  position: relative;
  width: 100%;
  height: 35px;
  background-color: #001133;
  border: 4px solid #fff;
  margin: 15px 0;
  box-shadow: inset 2px 2px 4px rgba(0, 0, 0, 0.8);
  overflow: hidden;
}
.mana-bar-fill {
  height: 100%;
  background: linear-gradient(180deg, #40e0d0 0%, #00e5ff 40%, #0077be 100%);
  transition: width 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  position: relative;
  box-shadow: 0 0 10px #00e5ff;
}
.glare-effect {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 40%;
  background: rgba(255, 255, 255, 0.3);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}
.mana-text-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  color: #fff;
  font-size: 1rem;
  font-weight: bold;
  text-shadow: 2px 2px 0 #000, -1px -1px 0 #003366;
  z-index: 5;
  letter-spacing: 1px;
}
.mana-text-overlay .divider {
  color: #aaddff;
  margin: 0 4px;
}
.blink-text {
  animation: pulse-opacity 2s infinite;
}
@keyframes pulse-opacity {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
  100% {
    opacity: 1;
  }
}
.ai-btn-container {
  width: 100%;
  padding: 15px;
  display: flex;
  justify-content: center;
  background: transparent;
}
.ai-analyze-btn {
  position: relative;
  width: 100%;
  max-width: 360px;
  height: 60px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  overflow: hidden;
  cursor: not-allowed;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
.btn-content {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: black;
  font-family: "Orbitron", sans-serif;
  letter-spacing: 2px;
  font-size: 0.9rem;
  font-weight: 800;
}
.ai-analyze-btn.active {
  cursor: pointer;
  background: rgba(110, 69, 226, 0.1);
  border: 1px solid rgba(0, 229, 255, 0.5);
  box-shadow: 0 0 20px rgba(0, 229, 255, 0.2);
}
.ai-analyze-btn.active .btn-content {
  color: #00e5ff;
  text-shadow: 0 0 8px rgba(0, 229, 255, 0.8);
}
.btn-shimmer {
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(120deg, transparent, rgba(0, 229, 255, 0.2), transparent);
  transition: all 0.6s;
  animation: shimmer 3s infinite;
}
@keyframes shimmer {
  0% {
    left: -100%;
  }
  30% {
    left: 100%;
  }
  100% {
    left: 100%;
  }
}
.corner-line {
  position: absolute;
  width: 10px;
  height: 10px;
  border: 2px solid transparent;
  transition: all 0.3s;
}
.top-left {
  top: 0;
  left: 0;
  border-top-color: #444;
  border-left-color: #444;
}
.bottom-right {
  bottom: 0;
  right: 0;
  border-bottom-color: #444;
  border-right-color: #444;
}
.ai-analyze-btn.active .top-left {
  border-top-color: #00e5ff;
  border-left-color: #00e5ff;
}
.ai-analyze-btn.active .bottom-right {
  border-bottom-color: #00e5ff;
  border-right-color: #00e5ff;
}
.ai-analyze-btn.active:hover {
  transform: translateY(-2px);
  background: rgba(0, 229, 255, 0.15);
  box-shadow: 0 0 30px rgba(0, 229, 255, 0.4);
}
.ai-analyze-btn.active:active {
  transform: scale(0.97);
}
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 20, 0.95);
  z-index: 15000;
  display: flex;
  justify-content: center;
  align-items: center;
  backdrop-filter: blur(5px);
}
.loading-content {
  text-align: center;
}
.loading-icon {
  font-size: 3rem;
  margin-bottom: 20px;
  animation: pulse 1s infinite;
}
.loading-status-bar {
  width: 200px;
  height: 4px;
  background: #333;
  margin: 15px auto;
  position: relative;
  overflow: hidden;
}
.status-fill {
  width: 100%;
  height: 100%;
  background: #00e5ff;
  animation: loading-scan 1.5s infinite;
}
@keyframes loading-scan {
  0% {
    transform: translateX(-100%);
  }
  100% {
    transform: translateX(100%);
  }
}
.ai-result-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100dvh;
  background: rgba(0, 0, 0, 0.85);
  backdrop-filter: blur(5px);
  z-index: 99999;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
}
.ai-result-modal {
  background: #1a1a1a;
  border: 4px solid #fff;
  width: 100%;
  max-width: 420px;
  max-height: 90dvh;
  position: relative;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  box-shadow: 8px 8px 0px 0px rgba(0, 0, 0, 0.5);
}
.modal-body-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 10px 15px;
  scrollbar-width: thin;
  scrollbar-color: #ff0055 #222;
}
.modal-body-scroll::-webkit-scrollbar {
  width: 6px;
}
.modal-body-scroll::-webkit-scrollbar-track {
  background: #222;
}
.modal-body-scroll::-webkit-scrollbar-thumb {
  background: #ff0055;
  border: 1px solid #fff;
}
.modal-header-section {
  padding: 15px 15px 10px;
  border-bottom: 2px dashed #444;
  flex-shrink: 0;
  text-align: center;
}
.retro-confirm-btn {
  margin: 10px 15px 15px;
  flex-shrink: 0;
  padding: 12px;
  font-size: 0.9rem;
  background: #ff0055;
  color: #fff;
  border: none;
  font-family: "NeoDunggeunmo";
  cursor: pointer;
  box-shadow: 0 4px 0 #990033;
  transition: all 0.1s;
}
.retro-confirm-btn:active {
  transform: translateY(2px);
  box-shadow: 0 2px 0 #990033;
}
.retro-pixel-border {
  image-rendering: pixelated;
  position: relative;
}
.modal-main-title {
  font-size: 1.2rem;
  color: #00e5ff;
  text-shadow: 2px 2px 0 #000;
}
.score-card-retro.premium-border {
  display: flex;
  justify-content: space-around;
  align-items: center;
  background: linear-gradient(135deg, #000 0%, #1a1a1a 100%);
  border: 3px solid #ffd700;
  padding: 25px 15px;
  margin-bottom: 25px;
  position: relative;
  overflow: hidden;
  box-shadow: inset 0 0 15px rgba(255, 215, 0, 0.2);
}
.rank-aura-container {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1;
}
.rank-aura {
  position: absolute;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  filter: blur(20px);
  opacity: 0.6;
  animation: rotateAura 4s linear infinite;
}
.aura-S {
  background: conic-gradient(#ff0055, #ffd700, #ff0055);
}
.aura-A {
  background: conic-gradient(#00e5ff, #764ba2, #00e5ff);
}
.aura-B {
  background: conic-gradient(#00ff88, #0085ff, #00ff88);
}
@keyframes rotateAura {
  from {
    transform: rotate(0deg) scale(1);
  }
  50% {
    transform: rotate(180deg) scale(1.2);
  }
  to {
    transform: rotate(360deg) scale(1);
  }
}
.rank-visual-mega {
  font-size: 4.5rem;
  font-weight: 900;
  line-height: 1;
  position: relative;
  z-index: 2;
  font-family: "Arial Black", sans-serif;
  animation: rankPulse 1.5s ease-in-out infinite;
}
.rank-S {
  background: linear-gradient(to bottom, #fff 20%, #ffd700 50%, #b8860b 80%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  filter: drop-shadow(0 0 15px rgba(255, 215, 0, 0.8));
}
.rank-A {
  background: linear-gradient(to bottom, #fff 20%, #ff0055 50%, #8b0000 80%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  filter: drop-shadow(0 0 15px rgba(255, 0, 85, 0.8));
}
.score-container-mega {
  text-align: right;
  z-index: 2;
}
.score-label-neon {
  font-size: 0.7rem;
  color: #ffd700;
  letter-spacing: 2px;
  margin-bottom: 5px;
  text-shadow: 0 0 5px rgba(255, 215, 0, 0.5);
}
.score-number-glitch {
  font-size: 3rem;
  color: #fff;
  font-weight: bold;
  text-shadow: 3px 3px 0px #ff0055, -3px -3px 0px #00e5ff;
  animation: scoreFloat 3s ease-in-out infinite;
}
.xp-bar-mini {
  width: 100%;
  height: 4px;
  background: #333;
  margin-top: 10px;
  border-radius: 2px;
  overflow: hidden;
}
.xp-bar-fill {
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, #ffd700, #fff, #ffd700);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite linear;
}
@keyframes rankPulse {
  0%,
  100% {
    transform: scale(1);
    filter: brightness(1) drop-shadow(0 0 15px currentColor);
  }
  50% {
    transform: scale(1.1);
    filter: brightness(1.3) drop-shadow(0 0 25px currentColor);
  }
}
@keyframes scoreFloat {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
}
.physical-stats-retro {
  margin: 20px 0;
  padding: 0 5px;
}
.energy-dashboard {
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.calorie-gauge {
  background: #0a0a0a;
  border: 2px solid #ffaa00;
  padding: 15px;
  text-align: center;
  position: relative;
  box-shadow: 0 0 10px rgba(255, 170, 0, 0.2);
}
.calorie-gauge::before {
  content: "";
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  border: 2px solid rgba(255, 170, 0, 0.3);
  z-index: -1;
}
.gauge-label {
  display: block;
  font-size: 0.8rem;
  color: #ffaa00;
  letter-spacing: 2px;
  margin-bottom: 5px;
}
.gauge-val {
  font-size: 2rem;
  font-weight: bold;
  color: #fff;
  text-shadow: 2px 2px 0px #ffaa00;
  font-family: "NeoDunggeunmo", monospace;
}
.gauge-val .unit {
  font-size: 1rem;
  color: #ccc;
}
.training-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}
.training-card {
  background: rgba(0, 255, 0, 0.05);
  border: 1px dashed #00ff00;
  padding: 10px 5px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 5px;
  transition: transform 0.2s;
}
.training-card:hover {
  background: rgba(0, 255, 0, 0.15);
  transform: translateY(-2px);
  border-style: solid;
}
.card-icon {
  font-size: 1.8rem;
}
.card-info {
  text-align: center;
}
.card-name {
  font-size: 0.7rem;
  color: #aaa;
  margin-bottom: 2px;
  white-space: nowrap;
}
.card-time {
  font-size: 0.9rem;
  font-weight: bold;
  color: #00ff00;
}
.interactive-card {
  cursor: pointer;
  position: relative;
  transition: all 0.2s ease;
}
.interactive-card:hover {
  transform: translateY(-3px);
  border-color: #ff0000;
  box-shadow: 0 4px 15px rgba(255, 0, 0, 0.3);
  background-color: rgba(255, 255, 255, 0.1);
}
.interactive-card:active {
  transform: scale(0.98);
}
.youtube-hint {
  position: absolute;
  top: 5px;
  right: 5px;
  font-size: 10px;
  color: #666;
  opacity: 0;
  transition: opacity 0.2s;
}
.interactive-card:hover .youtube-hint {
  opacity: 1;
  color: #ff0000;
}
.video-player-section {
  margin: 20px 0;
  animation: slideDown 0.3s ease;
}
.video-box {
  background: #000;
  padding: 10px;
  position: relative;
  border: 2px solid #0f0;
  box-shadow: 0 0 10px #0f0;
}
.video-loading {
  height: 315px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #0f0;
  font-family: "DungGeunMo", sans-serif;
  font-size: 1.2rem;
}
.close-video-btn {
  width: 100%;
  background: #333;
  color: #fff;
  border: none;
  padding: 8px;
  margin-top: 5px;
  cursor: pointer;
  font-family: "DungGeunMo", sans-serif;
}
.close-video-btn:hover {
  background: #f00;
}
@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
.insight-list-retro {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 20px;
}
.insight-card-retro {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid #333;
  padding: 10px;
  position: relative;
}
.insight-card-retro.good {
  border-left: 4px solid #00ff88;
}
.insight-card-retro.warning {
  border-left: 4px solid #ffaa00;
}
.status-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  background: currentColor;
  margin-right: 8px;
  vertical-align: middle;
}
.insight-title {
  font-size: 0.9rem;
  display: inline-block;
  color: #eee;
}
.insight-description {
  font-size: 0.8rem;
  color: #aaa;
  margin-top: 5px;
  line-height: 1.4;
}
.summary-box-retro {
  background: #222;
  border: 2px solid #444;
  padding: 12px;
  display: flex;
  gap: 12px;
  align-items: center;
}
.npc-thumb {
  font-size: 1.5rem;
  background: #333;
  padding: 5px;
  border: 1px solid #555;
}
.summary-text {
  font-size: 0.85rem;
  color: #00ff88;
  line-height: 1.4;
}
.section-title-retro {
  font-size: 0.8rem;
  color: #888;
  margin-bottom: 10px;
}
.retro-modal-enter-active {
  animation: modalSlideIn 0.3s steps(5);
}
.retro-modal-leave-active {
  animation: modalSlideIn 0.2s steps(5) reverse;
}
@keyframes modalSlideIn {
  from {
    transform: translateY(30px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
@keyframes pulse {
  0% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.7;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
@media (max-width: 400px) {
  .ai-result-modal {
    padding: 15px;
  }
  .rank-visual {
    font-size: 2.8rem;
  }
  .score-number-retro {
    font-size: 2rem;
  }
  .rank-visual-mega {
    font-size: 3.5rem;
  }
  .score-number-glitch {
    font-size: 2.2rem;
  }
  .rank-aura {
    width: 90px;
    height: 90px;
  }
}

</style>
