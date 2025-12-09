<script setup>
import { ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();

// 상태 관리
const email = ref("");
const password = ref("");
const isLoading = ref(false);

// 🔊 효과음 (기존 유지)
const playSound = (type) => {
  const AudioContext = window.AudioContext || window.webkitAudioContext;
  if (!AudioContext) return;
  const ctx = new AudioContext();
  const osc = ctx.createOscillator();
  const gain = ctx.createGain();
  osc.connect(gain);
  gain.connect(ctx.destination);

  const now = ctx.currentTime;
  if (type === "type") {
    osc.type = "square";
    osc.frequency.setValueAtTime(800, now);
    gain.gain.setValueAtTime(0.02, now);
    gain.gain.exponentialRampToValueAtTime(0.001, now + 0.05);
    osc.start(now);
    osc.stop(now + 0.05);
  } else if (type === "start") {
    osc.type = "sawtooth";
    osc.frequency.setValueAtTime(440, now);
    osc.frequency.setValueAtTime(554, now + 0.1);
    osc.frequency.setValueAtTime(659, now + 0.2);
    gain.gain.setValueAtTime(0.1, now);
    gain.gain.linearRampToValueAtTime(0, now + 0.6);
    osc.start(now);
    osc.stop(now + 0.6);
  }
};

const handleInput = () => playSound("type");

const handleLogin = async () => {
  if (!email.value || !password.value) return;

  isLoading.value = true;
  playSound("start");

  // 로그인 시뮬레이션 후 홈으로 이동
  setTimeout(() => {
    localStorage.setItem("userToken", "access-granted-token-123");
    isLoading.value = false;
    router.push("/");
  }, 1500);
};

const goToSignup = () => {
  console.log("Go to New Game (Signup)");
};
</script>

<template>
  <div class="login-view retro-theme">
    <div class="scanlines"></div>
    <div class="vignette"></div>

    <div class="login-container">
      <div class="title-section">
        <h1 class="game-title">HEALTH RPG</h1>
        <p class="subtitle">INSERT COIN TO START</p>
      </div>

      <div class="form-box">
        <div class="input-group">
          <label>PLAYER ID</label>
          <input
            v-model="email"
            type="email"
            class="retro-input"
            placeholder="아이디(이메일) 입력"
            @input="handleInput"
          />
        </div>

        <div class="input-group">
          <label>PASSWORD</label>
          <input
            v-model="password"
            type="password"
            class="retro-input"
            placeholder="비밀번호 입력"
            @input="handleInput"
            @keyup.enter="handleLogin"
          />
        </div>

        <button
          class="start-btn"
          :class="{ loading: isLoading }"
          @click="handleLogin"
          :disabled="isLoading"
        >
          <span v-if="!isLoading" class="btn-text">PRESS START ▶</span>
          <span v-else class="btn-text blink">CONNECTING...</span>
        </button>
      </div>

      <div class="footer-links">
        <button @click="goToSignup" class="link-btn">회원가입</button>
        <span class="divider">|</span>
        <button class="link-btn">비밀번호 찾기</button>
      </div>

      <div class="copyright">© 2025 HEALTH QUEST CORP.</div>
    </div>
  </div>
</template>

<style scoped>
@import url("https://cdn.jsdelivr.net/gh/neodgm/neodgm-webfont@latest/neodgm/style.css");

:root {
  --neon-blue: #00e5ff;
  --neon-pink: #ff0055;
  --neon-yellow: #ffd700;
  --bg-color: #101018;
  --input-bg: #1a1a24;
  --text-white: #ffffff;
  --text-gray: #aaaaaa;
}

.login-view {
  min-height: 100vh;
  background-color: var(--bg-color);
  font-family: "NeoDunggeunmo", monospace;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  color: var(--text-white);
  padding: 20px;
  box-sizing: border-box;
}

/* 배경 효과 */
.scanlines {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  background: linear-gradient(rgba(18, 16, 16, 0) 50%, rgba(0, 0, 0, 0.05) 50%),
    linear-gradient(
      90deg,
      rgba(255, 0, 0, 0.03),
      rgba(0, 255, 0, 0.01),
      rgba(0, 0, 255, 0.03)
    );
  background-size: 100% 3px, 3px 100%;
  z-index: 1;
}
.vignette {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  background: radial-gradient(
    circle,
    rgba(0, 0, 0, 0) 50%,
    rgba(0, 0, 0, 0.6) 100%
  );
  z-index: 2;
}

.login-container {
  position: relative;
  z-index: 10;
  width: 100%;
  max-width: 420px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* --- [수정됨] 타이틀 섹션: 부드러운 애니메이션 --- */
.title-section {
  margin-bottom: 2.5rem;
  text-align: center;
  width: 100%;
}

.game-title {
  font-size: 3rem;
  color: var(--neon-blue);
  /* 흔들림 대신 빛나는 효과와 둥둥 뜨는 효과 적용 */
  text-shadow: 0 0 10px var(--neon-pink), 0 0 20px var(--neon-pink);
  margin: 0;
  position: relative;
  letter-spacing: 2px;
  line-height: 1.2;
  word-break: keep-all;
  animation: float 4s ease-in-out infinite; /* 천천히 위아래로 움직임 */
}

.subtitle {
  color: var(--neon-yellow);
  font-size: 0.9rem;
  margin-top: 0.8rem;
  letter-spacing: 1px;
  animation: pulse-glow 2s infinite alternate; /* 부드럽게 깜빡임 */
}

/* --- [수정됨] 입력 폼 디자인 개선 --- */
.form-box {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
  background: rgba(255, 255, 255, 0.03);
  padding: 1.5rem;
  border: 1px solid #333;
  border-radius: 8px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.5);
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.input-group label {
  font-size: 0.8rem;
  color: var(--neon-blue);
  margin-left: 2px;
}

.retro-input {
  background-color: var(--input-bg);
  border: 2px solid #444;
  color: #444; /* [중요] 입력 글씨 흰색 */
  padding: 14px;
  font-family: inherit;
  font-size: 1rem;
  outline: none;
  width: 100%;
  box-sizing: border-box;
  border-radius: 4px;
  transition: all 0.2s;
}

.retro-input:focus {
  border-color: var(--neon-blue);
  box-shadow: 0 0 8px rgba(0, 229, 255, 0.3);
  background-color: #20202a;
}

.retro-input::placeholder {
  color: #666;
}

/* [중요] 브라우저 자동완성 시 배경/글씨색 강제 조정 */
.retro-input:-webkit-autofill,
.retro-input:-webkit-autofill:hover,
.retro-input:-webkit-autofill:focus,
.retro-input:-webkit-autofill:active {
  -webkit-text-fill-color: #ffffff;
  -webkit-box-shadow: 0 0 0px 1000px #1a1a24 inset;
  transition: background-color 5000s ease-in-out 0s;
}

/* 버튼 스타일 */
.start-btn {
  margin-top: 1rem;
  background: var(--neon-pink);
  color: #fff;
  border: 2px solid #fff;
  padding: 1rem;
  font-size: 1.2rem;
  font-family: inherit;
  font-weight: bold;
  cursor: pointer;
  box-shadow: 4px 4px 0 #000;
  transition: transform 0.1s, box-shadow 0.1s;
  width: 100%;
}
.start-btn:active {
  transform: translate(2px, 2px);
  box-shadow: 2px 2px 0 #000;
}
.start-btn:disabled {
  background: #555;
  border-color: #888;
  cursor: wait;
}

/* 하단 링크 */
.footer-links {
  margin-top: 2rem;
  display: flex;
  gap: 1rem;
  align-items: center;
  font-size: 0.9rem;
}
.link-btn {
  background: none;
  border: none;
  color: #888;
  font-family: inherit;
  font-size: inherit;
  cursor: pointer;
  padding: 5px;
}
.link-btn:hover {
  color: var(--text-white);
  text-decoration: underline;
}
.divider {
  color: #444;
}
.copyright {
  margin-top: 3rem;
  font-size: 0.7rem;
  color: #444;
}

/* --- [수정됨] 편안한 애니메이션 --- */
@keyframes float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-8px);
  }
}

@keyframes pulse-glow {
  0% {
    opacity: 0.7;
    text-shadow: 0 0 5px var(--neon-yellow);
  }
  100% {
    opacity: 1;
    text-shadow: 0 0 15px var(--neon-yellow);
  }
}

@keyframes blink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0;
  }
}

/* 모바일 화면 조정 */
@media (max-width: 400px) {
  .game-title {
    font-size: 2.2rem;
  }
  .subtitle {
    font-size: 0.8rem;
  }
  .form-box {
    padding: 1.2rem;
  }
  .start-btn {
    font-size: 1.1rem;
  }
}
</style>
