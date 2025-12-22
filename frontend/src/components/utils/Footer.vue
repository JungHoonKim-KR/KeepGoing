<template>
  <div class="footer-wrapper">
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
            <span class="nav-icon">🏰</span><span class="nav-label">BASE</span>
          </div>
          <div class="led-light"></div>
        </button>

        <button
          @click="navigate('/calendar')"
          :class="['retro-btn', { active: currentPath === '/calendar' }]"
        >
          <div class="btn-face">
            <span class="nav-icon">📜</span><span class="nav-label">LOGS</span>
          </div>
          <div class="led-light"></div>
        </button>

        <!-- <button @click="$emit('open-radio')" class="retro-btn">
          <div class="btn-face">
            <span class="nav-icon">📻</span>
            <span class="nav-label">COMM</span>
          </div>
          <div class="led-light"></div>
        </button> -->

        <button
          @click="navigate('/ai-analysis')"
          :class="[
            'retro-btn',
            { active: currentPath.includes('/ai-analysis') },
          ]"
        >
          <div class="btn-face">
            <span class="nav-icon">👾</span><span class="nav-label">NPC</span>
          </div>
          <div class="led-light"></div>
        </button>

        <button
          @click="navigate('/profile')"
          :class="['retro-btn', { active: currentPath === '/profile' }]"
        >
          <div class="btn-face">
            <span class="nav-icon">🧙‍♂️</span><span class="nav-label">HERO</span>
          </div>
          <div class="led-light"></div>
        </button>
      </div>
    </nav>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useRouter, useRoute } from "vue-router";

const router = useRouter();
const route = useRoute();
const currentPath = computed(() => route.path);

// ✨ 중요: 이벤트를 정의해야 부모가 받을 수 있습니다.
const emit = defineEmits(["open-radio"]);

const navigate = (path) => {
  router.push(path);
};
</script>

<style scoped>
/* ... 기존 스타일 그대로 유지 (special-face 클래스 삭제됨) ... */
@import url("https://cdn.jsdelivr.net/gh/neodgm/neodgm-webfont@latest/neodgm/style.css");

.footer-wrapper {
  position: fixed;
  bottom: 20px;
  left: 0;
  width: 100%;
  display: flex;
  justify-content: center;
  z-index: 1000;
  pointer-events: none;
}
.retro-nav-container {
  pointer-events: auto;
  background-color: #2d2d3a;
  border: 4px solid #000;
  border-radius: 16px;
  padding: 10px 12px;
  width: 95%;
  max-width: 420px;
  box-shadow: inset 2px 2px 0 rgba(255, 255, 255, 0.1),
    inset -2px -2px 0 rgba(0, 0, 0, 0.3), 0 10px 20px rgba(0, 0, 0, 0.5);
  position: relative;
  box-sizing: border-box;
}
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
  justify-content: space-between;
  gap: 8px;
}
.retro-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  flex: 1;
  display: flex;
  justify-content: center;
  min-width: 48px;
}

/* 은색 통일 */
.btn-face {
  width: 100%;
  height: 50px;
  background: #c0c0c0;
  border: 2px solid #000;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  box-shadow: inset 2px 2px 0 #fff, inset -2px -2px 0 #888, 0 4px 0 #000;
  transform: translateY(0);
  transition: all 0.1s;
}
.retro-btn:active .btn-face,
.retro-btn.active .btn-face {
  background: #e0e0e0;
  transform: translateY(4px);
  box-shadow: inset 2px 2px 0 #888, inset -2px -2px 0 #fff, 0 0 0 #000;
}

.nav-icon {
  font-size: 1.4rem;
  filter: grayscale(100%);
  transition: filter 0.2s;
}
.nav-label {
  font-size: 0.6rem;
  font-weight: bold;
  color: #555;
  letter-spacing: -0.5px;
}
.retro-btn.active .nav-icon,
.retro-btn:active .nav-icon {
  filter: grayscale(0%);
  transform: scale(1.1);
}
.retro-btn.active .nav-label,
.retro-btn:active .nav-label {
  color: #000;
}

.led-light {
  position: absolute;
  bottom: -8px;
  left: 50%;
  transform: translateX(-50%);
  width: 6px;
  height: 3px;
  background: #333;
  border-radius: 2px;
  transition: all 0.3s;
}
.retro-btn:nth-child(1).active .led-light {
  background: #ff0055;
  box-shadow: 0 0 5px #ff0055;
}
.retro-btn:nth-child(2).active .led-light {
  background: #ffcc00;
  box-shadow: 0 0 5px #ffcc00;
}
.retro-btn:nth-child(3):active .led-light {
  background: #00ff00;
  box-shadow: 0 0 8px #00ff00;
}
.retro-btn:nth-child(4).active .led-light {
  background: #00e5ff;
  box-shadow: 0 0 5px #00e5ff;
}
.retro-btn:nth-child(5).active .led-light {
  background: #9d00ff;
  box-shadow: 0 0 5px #9d00ff;
}

@media (max-width: 380px) {
  .footer-wrapper {
    bottom: 10px;
  }
  .retro-nav-container {
    padding: 8px;
  }
  .btn-face {
    height: 45px;
  }
  .nav-icon {
    font-size: 1.2rem;
  }
}
</style>
