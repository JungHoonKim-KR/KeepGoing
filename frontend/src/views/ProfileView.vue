<script setup>
import { ref, computed } from "vue";
import Footer from "../components/utils/Footer.vue";
import { useAuthStore } from "@/stores/authStore";
import { useConfigStore } from "@/stores/configStore";
import axios from "axios";

const authStore = useAuthStore();
const config = useConfigStore();

// ----------------------------------------------------
// 1. 상태 관리
// ----------------------------------------------------
// 회원가입 데이터 구조와 동일하게 매핑
const characterImage = computed(() => {
  const charNum = authStore.profileCharacter || 1; // 값이 없으면 기본값 1
  return new URL(
    `../assets/images/gifCharacters/${charNum}.gif`,
    import.meta.url
  ).href;
});

const profile = ref({
  email: authStore.email || "", // 수정 불가 (ID)
  name: authStore.name || "",
  password: "", // 비밀번호 변경은 별도 로직이 일반적이나, 요청대로 필드는 포함
  confirmPassword: "",

  gender: authStore.gender || "M",
  age: authStore.age || 0,
  height: authStore.height || 0,
  weight: authStore.weight || 0,

  // 추가된 항목들
  target_weight: authStore.targetWeight || 0,
  target_water: authStore.targetWater || 2.0,

  activity: authStore.activity || "MODERATE",
  goal: authStore.goal || "DIET",

  health_condition: authStore.healthCondition || "",
  allergies: authStore.allergies || "",
  disliked_food: authStore.dislikedFood || "",
  class: "Adventurer", // 표시용
});

const isEditing = ref(false);

// 옵션 데이터 (회원가입과 동일)
const activityOptions = [
  { value: "LOW", label: "LOW (앉아있는 시간 많음)", icon: "💤" },
  { value: "MODERATE", label: "NORMAL (규칙적인 활동)", icon: "🚶" },
  { value: "HIGH", label: "HIGH (육체 노동/운동 선수)", icon: "🔥" },
];

const goalOptions = [
  { value: "DIET", label: "WEIGHT LOSS (다이어트)", icon: "📉" },
  { value: "MUSCLE", label: "MUSCLE UP (근력 증가)", icon: "💪" },
  { value: "MAINTAIN", label: "MAINTENANCE (유지)", icon: "⚖️" },
];

// ----------------------------------------------------
// 2. 이벤트 핸들러
// ----------------------------------------------------
const playSound = (type) => {
  const AudioContext = window.AudioContext || window.webkitAudioContext;
  if (!AudioContext) return;
  const ctx = new AudioContext();
  const osc = ctx.createOscillator();
  const gain = ctx.createGain();
  osc.connect(gain);
  gain.connect(ctx.destination);

  const now = ctx.currentTime;
  if (type === "click") {
    osc.type = "square";
    osc.frequency.setValueAtTime(600, now);
    gain.gain.setValueAtTime(0.05, now);
    gain.gain.exponentialRampToValueAtTime(0.01, now + 0.1);
    osc.start(now);
    osc.stop(now + 0.1);
  } else if (type === "save") {
    osc.type = "triangle";
    osc.frequency.setValueAtTime(440, now);
    osc.frequency.linearRampToValueAtTime(880, now + 0.2);
    gain.gain.setValueAtTime(0.05, now);
    gain.gain.linearRampToValueAtTime(0, now + 0.3);
    osc.start(now);
    osc.stop(now + 0.3);
  }
};

const startEdit = () => {
  playSound("click");
  isEditing.value = true;
};

const cancelEdit = () => {
  playSound("click");
  isEditing.value = false;
};

const saveProfile = async () => {
  playSound("save");

  // 비밀번호 변경 시도 시 확인
  if (
    profile.value.password &&
    profile.value.password !== profile.value.confirmPassword
  ) {
    alert("비밀번호가 일치하지 않습니다.");
    return;
  }

  try {
    const url = `${config.API_ENDPOINT}/api/member`;
    // 백엔드 DTO 매핑
    const payload = {
      memberId: authStore.memberId,
      name: profile.value.name,
      gender: profile.value.gender,
      age: profile.value.age,
      height: profile.value.height,
      weight: profile.value.weight,

      // [수정] 스네이크 케이스(_글자) -> 카멜 케이스(대문자)
      targetWeight: profile.value.target_weight, // target_weight 값을 targetWeight 키에 담음
      targetWater: profile.value.target_water, // target_water 값을 targetWater 키에 담음

      activity: profile.value.activity,
      goal: profile.value.goal,

      // [수정]
      healthCondition: profile.value.health_condition,

      allergies: profile.value.allergies,

      // [수정] 스토어 state 이름(dislikedFoods)과 통일 권장
      dislikedFoods: profile.value.disliked_food,
    };
    const response = await axios.put(url, payload);

    console.log("Saved:", payload);

    authStore.$patch({
      name: payload.name,
      age: payload.age,
      height: payload.height,
      weight: payload.weight,
      targetWeight: payload.targetWeight,
      targetWater: payload.targetWater,
      activity: payload.activity,
      goal: payload.goal,
      healthCondition: payload.healthCondition,
      allergies: payload.allergies,
      dislikedFoods: payload.dislikedFoods,
    });
    if (response.status === 200) {
      alert("프로필이 업데이트되었습니다.");
      isEditing.value = false;
    }
  } catch (error) {
    console.error("저장 실패:", error);
    alert("오류 발생");
  }
};
</script>

<template>
  <div class="profile-view retro-theme">
    <div class="scanlines"></div>

    <Teleport to="#header-extra-actions">
      <button
        v-if="!isEditing"
        @click="startEdit"
        class="retro-header-btn edit"
      >
        EDIT
      </button>
      <button
        v-if="isEditing"
        @click="cancelEdit"
        class="retro-header-btn cancel"
      >
        CANCEL
      </button>
    </Teleport>

    <div class="content">
      <div class="character-showcase">
        <div class="holo-pad"></div>
        <img :src="characterImage" alt="Avatar" class="character-sprite" />
        <div class="class-label">{{ profile.class }}</div>
        <div class="player-name-display">{{ profile.name }}</div>
      </div>

      <div class="stats-container">
        <template v-if="!isEditing">
          <div class="section-title pixel-font">회원 정보</div>

          <div class="stats-grid">
            <div class="stat-box">
              <span class="label">나이</span>
              <span class="val">{{ profile.age }}</span>
            </div>
            <div class="stat-box">
              <span class="label">성별</span>
              <span class="val">{{
                profile.gender === "M" ? "남" : "여"
              }}</span>
            </div>
            <div class="stat-box">
              <span class="label">키</span>
              <span class="val">{{ profile.height }}<small>cm</small></span>
            </div>
            <div class="stat-box">
              <span class="label">체중</span>
              <span class="val">{{ profile.weight }}<small>kg</small></span>
            </div>
            <div class="stat-box">
              <span class="label">목표 체중</span>
              <span class="val"
                >{{ profile.target_weight }}<small>kg</small></span
              >
            </div>
            <div class="stat-box">
              <span class="label">목표 수분량</span>
              <span class="val"
                >{{ profile.target_water }}<small>L</small></span
              >
            </div>
          </div>

          <div class="traits-list">
            <div class="trait-row">
              <div class="trait-icon">⚡</div>
              <div class="trait-info">
                <div class="trait-label">활동습관</div>
                <div class="trait-val">
                  {{
                    activityOptions.find((o) => o.value === profile.activity)
                      ?.label
                  }}
                </div>
              </div>
            </div>
            <div class="trait-row">
              <div class="trait-icon">🏆</div>
              <div class="trait-info">
                <div class="trait-label">식단목표</div>
                <div class="trait-val">
                  {{ goalOptions.find((o) => o.value === profile.goal)?.label }}
                </div>
              </div>
            </div>

            <div class="trait-text-group">
              <div class="text-block">
                <span class="sub-label">기저질환</span>
                <p>{{ profile.health_condition || "" }}</p>
              </div>
              <div class="text-block">
                <span class="sub-label">알레르기</span>
                <p>{{ profile.allergies || "" }}</p>
              </div>
              <div class="text-block">
                <span class="sub-label">기피 음식</span>
                <p>{{ profile.disliked_food || "" }}</p>
              </div>
            </div>
          </div>
        </template>

        <template v-else>
          <div class="section-title pixel-font blink">UPDATE PROFILE...</div>

          <div class="edit-form">
            <div class="form-group">
              <label>이메일</label>
              <input
                type="text"
                v-model="profile.email"
                class="retro-input disabled"
                disabled
              />
            </div>
            <div class="form-group">
              <label>이름</label>
              <input type="text" v-model="profile.name" class="retro-input" />
            </div>

            <div class="separator"></div>

            <div class="form-row">
              <div class="form-group">
                <label>나이</label>
                <input
                  type="number"
                  v-model.number="profile.age"
                  class="retro-input"
                />
              </div>
              <div class="form-group">
                <label>성별</label>
                <div class="radio-pill-group">
                  <label
                    class="pill"
                    :class="{ active: profile.gender === 'M' }"
                  >
                    <input
                      type="radio"
                      value="M"
                      v-model="profile.gender"
                      hidden
                    />
                    남
                  </label>
                  <label
                    class="pill"
                    :class="{ active: profile.gender === 'F' }"
                  >
                    <input
                      type="radio"
                      value="F"
                      v-model="profile.gender"
                      hidden
                    />
                    여
                  </label>
                </div>
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label>키 (cm)</label>
                <input
                  type="number"
                  v-model.number="profile.height"
                  class="retro-input"
                />
              </div>
              <div class="form-group">
                <label>현재 체중 (kg)</label>
                <input
                  type="number"
                  v-model.number="profile.weight"
                  class="retro-input"
                />
              </div>
            </div>

            <div class="form-row">
              <div class="form-group">
                <label class="highlight">목표 체중 (kg)</label>
                <input
                  type="number"
                  v-model.number="profile.target_weight"
                  class="retro-input highlight-input"
                />
              </div>
              <div class="form-group">
                <label class="highlight">목표 수분량 (L)</label>
                <input
                  type="number"
                  step="0.1"
                  v-model.number="profile.target_water"
                  class="retro-input highlight-input"
                />
              </div>
            </div>

            <div class="separator"></div>

            <div class="form-group">
              <label>활동 습관</label>
              <select v-model="profile.activity" class="retro-select">
                <option
                  v-for="opt in activityOptions"
                  :key="opt.value"
                  :value="opt.value"
                >
                  {{ opt.icon }} {{ opt.label }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label>식단 목표</label>
              <select v-model="profile.goal" class="retro-select">
                <option
                  v-for="opt in goalOptions"
                  :key="opt.value"
                  :value="opt.value"
                >
                  {{ opt.icon }} {{ opt.label }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label>기저질환</label>
              <textarea
                v-model="profile.health_condition"
                class="retro-textarea"
              ></textarea>
            </div>
            <div class="form-group">
              <label>알레르기</label>
              <textarea
                v-model="profile.allergies"
                class="retro-textarea"
              ></textarea>
            </div>
            <div class="form-group">
              <label>기피음식</label>
              <textarea
                v-model="profile.disliked_food"
                class="retro-textarea"
              ></textarea>
            </div>

            <button @click="saveProfile" class="retro-btn full-btn">
              변경하기
            </button>
          </div>
        </template>
      </div>
    </div>

    <Footer></Footer>
  </div>
</template>

<style scoped>
@import url("https://cdn.jsdelivr.net/gh/neodgm/neodgm-webfont@latest/neodgm/style.css");

:root {
  --neon-blue: #00e5ff;
  --neon-pink: #ff0055;
  --neon-green: #00ff00;
  --bg-dark: #101018;
  --card-bg: #1a1a24;
}

/* === 레이아웃 === */
.profile-view.retro-theme {
  min-height: 100vh;
  background-color: #101018 !important;
  font-family: "NeoDunggeunmo", monospace;
  color: #e0e0e0;
  padding-bottom: 120px;
  position: relative;
  overflow-x: hidden;
  box-sizing: border-box;
}

.scanlines {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  background: linear-gradient(rgba(18, 16, 16, 0) 50%, rgba(0, 0, 0, 0.1) 50%),
    linear-gradient(
      90deg,
      rgba(255, 0, 0, 0.03),
      rgba(0, 255, 0, 0.01),
      rgba(0, 0, 255, 0.03)
    );
  background-size: 100% 4px, 6px 100%;
  z-index: 1;
}

.content {
  padding: 1rem;
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 500px;
  margin: 0 auto;
  box-sizing: border-box;
}

/* === 캐릭터 === */
.character-showcase {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  height: 180px; /* 이름 추가로 높이 약간 증가 */
}
.character-sprite {
  width: 100px;
  height: 100px;
  object-fit: contain;
  image-rendering: pixelated;
  z-index: 2;
  animation: float 3s infinite ease-in-out;
}
@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}
.holo-pad {
  position: absolute;
  bottom: 40px;
  width: 120px;
  height: 30px;
  border: 2px solid var(--neon-blue);
  border-radius: 50%;
  background: rgba(0, 229, 255, 0.1);
  transform: rotateX(60deg);
  box-shadow: 0 0 20px var(--neon-blue);
  z-index: 1;
  animation: pulsePad 2s infinite;
}
@keyframes pulsePad {
  0%,
  100% {
    opacity: 0.5;
  }
  50% {
    opacity: 1;
  }
}
.class-label {
  margin-top: 5px;
  font-size: 0.8rem;
  color: var(--neon-pink);
  letter-spacing: 2px;
}
.player-name-display {
  font-size: 1.2rem;
  color: #fff;
  margin-top: 5px;
  text-shadow: 0 0 5px var(--neon-blue);
}

/* === STATS 박스 === */
.stats-container {
  background: #1a1a24;
  border: 2px solid #333;
  padding: 1.25rem;
  position: relative;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
  border-radius: 4px;
}
.section-title {
  font-size: 0.9rem;
  color: var(--neon-green);
  margin-bottom: 1rem;
  margin-top: 1.5rem;
  border-left: 4px solid var(--neon-green);
  padding-left: 10px;
}
.section-title:first-child {
  margin-top: 0;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  margin-bottom: 1rem;
}
.stat-box {
  background: #222;
  padding: 12px;
  border: 1px solid #444;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.stat-box.highlight-box {
  border-color: var(--neon-pink);
}
.stat-box.highlight-box .label {
  color: var(--neon-pink);
}

.stat-box .label {
  color: #888;
  font-size: 0.7rem;
}
.stat-box .val {
  color: #fff;
  font-size: 1rem;
  font-weight: bold;
}
.stat-box .val small {
  font-size: 0.7rem;
  color: #aaa;
  margin-left: 2px;
  font-weight: normal;
}

/* === TRAITS 리스트 (뷰 모드) === */
.traits-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.trait-row {
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 1px dashed #333;
  padding-bottom: 8px;
}
.trait-icon {
  font-size: 1.4rem;
  width: 30px;
  text-align: center;
}
.trait-info {
  flex: 1;
}
.trait-label {
  font-size: 0.65rem;
  color: #888;
  margin-bottom: 3px;
  display: block;
}
.trait-val {
  font-size: 0.9rem;
  color: var(--neon-blue);
  line-height: 1.2;
}

/* 텍스트 그룹 (기저질환 등) */
.trait-text-group {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.text-block {
  background: #222;
  padding: 10px;
  border-left: 2px solid #555;
}
.sub-label {
  font-size: 0.7rem;
  color: #aaa;
  display: block;
  margin-bottom: 4px;
}
.text-block p {
  font-size: 0.9rem;
  color: #fff;
  margin: 0;
  white-space: pre-wrap;
}

/* === 수정 폼 === */
.edit-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.form-row {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
  flex: 1;
  min-width: 140px;
}
.form-group label {
  font-size: 0.75rem;
  color: #aaa;
}
.form-group label.highlight {
  color: var(--neon-pink);
}

/* Input 스타일 */
.retro-input,
.retro-select,
.retro-textarea {
  background-color: #000 !important;
  border: 2px solid #555;
  color: #fff !important;
  padding: 12px;
  font-family: inherit;
  font-size: 1rem;
  outline: none;
  width: 100%;
  box-sizing: border-box;
  border-radius: 0;
}
.retro-input:focus,
.retro-select:focus,
.retro-textarea:focus {
  border-color: var(--neon-blue);
}
.highlight-input:focus {
  border-color: var(--neon-pink);
}
.retro-input.disabled {
  background: #222 !important;
  color: #777 !important;
  border-style: dashed;
}
.retro-textarea {
  height: 80px;
  resize: none;
}

/* Radio Pill */
.radio-pill-group {
  display: flex;
  gap: 8px;
  height: 48px;
}
.pill {
  flex: 1;
  border: 2px solid #555;
  color: #888;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-weight: bold;
  background: #222;
}
.pill.active {
  border-color: var(--neon-blue);
  background: rgba(0, 229, 255, 0.2);
  color: #fff;
}

.separator {
  height: 1px;
  background: #333;
  margin: 5px 0;
  border-bottom: 1px dashed #444;
}

.full-btn {
  width: 100%;
  background: var(--neon-green);
  color: white;
  font-weight: bold;
  border: none;
  padding: 15px;
  font-size: 1.1rem;
  margin-top: 1rem;
  cursor: pointer;
  box-shadow: 4px 4px 0 #000;
  transition: all 0.1s;
}
.full-btn:active {
  transform: translate(2px, 2px);
  box-shadow: 2px 2px 0 #000;
}

.blink {
  animation: blink 1s infinite;
}
@keyframes blink {
  50% {
    opacity: 0;
  }
}

/* 헤더 버튼 */
.retro-header-btn {
  background: transparent;
  padding: 5px 12px;
  font-family: "NeoDunggeunmo", monospace;
  cursor: pointer;
  font-size: 0.8rem;
  border: 1px solid;
  margin-left: 10px;
  transition: all 0.2s;
}
.retro-header-btn.edit {
  border-color: #00e5ff;
  color: #00e5ff;
}
.retro-header-btn.edit:hover {
  background: #00e5ff;
  color: #000;
  box-shadow: 0 0 10px #00e5ff;
}
.retro-header-btn.cancel {
  border-color: #ff0055;
  color: #ff0055;
}
.retro-header-btn.cancel:hover {
  background: #ff0055;
  color: #fff;
  box-shadow: 0 0 10px #ff0055;
}
</style>
