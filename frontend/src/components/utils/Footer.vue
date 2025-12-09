<template>
  <nav class="retro-nav-container">
    <div class="screw top-left"></div>
    <div class="screw top-right"></div>
    <div class="screw bottom-left"></div>
    <div class="screw bottom-right"></div>

    <div class="nav-buttons">
      <button
        @click="navigate('/')"
        :class="['retro-btn', { active: currentPath === '/' }]"
      >
        <div class="btn-face">
          <span class="nav-icon">🏰</span>
          <span class="nav-label">BASE</span>
        </div>
        <div class="led-light"></div>
      </button>

      <button
        @click="navigate('/calendar')"
        :class="['retro-btn', { active: currentPath === '/calendar' }]"
      >
        <div class="btn-face">
          <span class="nav-icon">📜</span>
          <span class="nav-label">LOGS</span>
        </div>
        <div class="led-light"></div>
      </button>

      <button
        @click="navigate('/ai-analysis')"
        :class="['retro-btn', { active: currentPath === '/ai-analysis' }]"
      >
        <div class="btn-face">
          <span class="nav-icon">👾</span>
          <span class="nav-label">NPC</span>
        </div>
        <div class="led-light"></div>
      </button>

      <button
        @click="navigate('/profile')"
        :class="['retro-btn', { active: currentPath === '/profile' }]"
      >
        <div class="btn-face">
          <span class="nav-icon">🧙‍♂️</span>
          <span class="nav-label">HERO</span>
        </div>
        <div class="led-light"></div>
      </button>
    </div>
  </nav>
</template>

<script setup>
import { computed } from "vue";
import { useRouter, useRoute } from "vue-router";

const router = useRouter();
const route = useRoute();

const currentPath = computed(() => route.path);

// 간단한 클릭 효과음 (앞서 만든 로직과 동일)
const playClickSound = () => {
  const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
  const osc = audioCtx.createOscillator();
  const gainNode = audioCtx.createGain();

  osc.connect(gainNode);
  gainNode.connect(audioCtx.destination);

  // '틱' 하는 가벼운 스위치 소리
  osc.type = "square";
  osc.frequency.setValueAtTime(200, audioCtx.currentTime);
  osc.frequency.exponentialRampToValueAtTime(50, audioCtx.currentTime + 0.1);

  gainNode.gain.setValueAtTime(0.05, audioCtx.currentTime);
  gainNode.gain.exponentialRampToValueAtTime(0.01, audioCtx.currentTime + 0.1);

  osc.start();
  osc.stop(audioCtx.currentTime + 0.1);
};

const navigate = (path) => {
  playClickSound();
  router.push(path);
};
</script>

<style scoped>
/* 폰트 (필요 시 상위 컴포넌트에서 로드된 것 사용) */
@import url("https://cdn.jsdelivr.net/gh/neodgm/neodgm-webfont@latest/neodgm/style.css");

.retro-nav-container {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  width: 90%;
  max-width: 400px;

  /* 게임 컨트롤러 본체 스타일 */
  background-color: #2d2d3a;
  border: 4px solid #000;
  border-radius: 12px; /* 약간 둥글지만 각진 느낌 */
  padding: 12px 8px;

  display: flex;
  justify-content: center;
  align-items: center;

  /* 입체적인 그림자 */
  box-shadow: inset 2px 2px 0px rgba(255, 255, 255, 0.1),
    inset -2px -2px 0px rgba(0, 0, 0, 0.3), 8px 8px 0px rgba(0, 0, 0, 0.5);

  z-index: 1000;
  font-family: "NeoDunggeunmo", monospace;
}

/* 장식용 나사 구멍 */
.screw {
  position: absolute;
  width: 6px;
  height: 6px;
  background: #111;
  border-radius: 50%;
  box-shadow: inset 1px 1px 2px rgba(0, 0, 0, 0.8);
}
.top-left {
  top: 6px;
  left: 6px;
}
.top-right {
  top: 6px;
  right: 6px;
}
.bottom-left {
  bottom: 6px;
  left: 6px;
}
.bottom-right {
  bottom: 6px;
  right: 6px;
}

.nav-buttons {
  display: flex;
  justify-content: space-around;
  width: 100%;
  gap: 4px;
}

/* 레트로 버튼 스타일 */
.retro-btn {
  background: none;
  border: none;
  cursor: pointer;
  position: relative;
  width: 64px;
  height: 64px;
  padding: 0;
  -webkit-tap-highlight-color: transparent;
}

/* 버튼의 물리적 모양 (Face) */
.btn-face {
  width: 100%;
  height: 100%;
  background: #c0c0c0; /* 회색 플라스틱 */
  border: 3px solid #000;
  border-radius: 8px; /* 둥근 사각형 버튼 */
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;

  /* 입체감: 튀어나와 보임 */
  box-shadow: inset 3px 3px 0 #fff, inset -3px -3px 0 #888, 4px 4px 0 #000; /* 그림자 */

  transform: translateY(0);
  transition: all 0.1s;
}

/* 아이콘 & 텍스트 */
.nav-icon {
  font-size: 1.5rem;
  filter: grayscale(100%); /* 비활성 상태는 흑백 */
  transition: filter 0.2s;
}

.nav-label {
  font-size: 0.6rem;
  font-weight: bold;
  color: #444;
  letter-spacing: -0.5px;
}

/* LED 표시등 */
.led-light {
  position: absolute;
  bottom: -6px; /* 버튼 아래쪽 */
  left: 50%;
  transform: translateX(-50%);
  width: 8px;
  height: 4px;
  background: #333;
  border-radius: 2px;
  transition: background 0.2s, box-shadow 0.2s;
}

/* === 활성 상태 (Active) === */
.retro-btn.active .btn-face {
  background: #e0e0e0;
  transform: translate(2px, 2px); /* 눌린 상태 */
  box-shadow: inset 2px 2px 0 #888,
    /* 눌려서 안쪽 그림자 반전 */ inset -2px -2px 0 #fff, 2px 2px 0 #000; /* 바깥 그림자 줄어듦 */
}

/* 활성 상태 색상 테마 */
.retro-btn.active .nav-icon {
  filter: grayscale(0%);
  transform: scale(1.1);
}

.retro-btn.active .nav-label {
  color: #000;
}

/* 버튼별 LED 색상 */
.retro-btn:nth-child(1).active .led-light {
  background: #ff0055; /* Hot Pink */
  box-shadow: 0 0 5px #ff0055;
}
.retro-btn:nth-child(2).active .led-light {
  background: #ffcc00; /* Yellow */
  box-shadow: 0 0 5px #ffcc00;
}
.retro-btn:nth-child(3).active .led-light {
  background: #00e5ff; /* Cyan */
  box-shadow: 0 0 5px #00e5ff;
}
.retro-btn:nth-child(4).active .led-light {
  background: #9d00ff; /* Purple */
  box-shadow: 0 0 5px #9d00ff;
}

/* === 클릭(Active) 시각 효과 === */
.retro-btn:active .btn-face {
  background: #a0a0a0;
  transform: translate(4px, 4px); /* 완전히 눌림 */
  box-shadow: none; /* 그림자 사라짐 */
}

/* 미디어 쿼리 (작은 화면) */
@media (max-width: 360px) {
  .retro-nav-container {
    width: 95%;
    bottom: 10px;
  }
  .retro-btn {
    width: 56px;
    height: 56px;
  }
}
</style>
