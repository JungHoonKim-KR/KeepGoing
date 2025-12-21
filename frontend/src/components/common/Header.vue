<script setup>
import { useAuthStore } from "@/stores/authStore";
import { useRouter } from "vue-router";

const authStore = useAuthStore();
const router = useRouter();

const handleLogout = () => {
  if (confirm("시스템 접속을 종료하시겠습니까?")) {
    authStore.logout();
  }
};

const goHome = () => {
  router.push("/");
};
</script>
<template>
  <header class="global-header retro-theme">
    <div class="header-content">
      <div class="header-right" v-if="authStore.isAuthenticated">
        <span class="user-name">PLAYER: {{ authStore.name }}</span>

        <div class="btn-container">
          <div id="header-extra-actions"></div>
        <button class="logout-btn" @click="handleLogout">LOGOUT</button>
        </div>
        
      </div>
    </div>
    <div class="header-line"></div>
  </header>
</template>

<style scoped>
  .btn-container{
    display: flex;
    gap: 0.2rem;
  }
.global-header {
  width: 100%;
  background-color: #000; /* 헤더 배경은 검정 */
  padding-top: 15px;
  position: sticky; /* 스크롤 내려도 상단 고정 */
  top: 0;
  z-index: 1000;
  font-family: "NeoDunggeunmo", monospace;
}

.header-content {
  /* display: flex; */
  justify-content: space-between;
  align-items: center;
  padding: 0 20px 15px 20px;
  max-width: 1200px; /* 너무 넓게 퍼지지 않도록 */
  margin: 0 auto;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
  cursor: pointer;
}

.system-status {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 0.8rem;
  color: #00ff00;
}

.status-light {
  width: 8px;
  height: 8px;
  background-color: #00ff00;
  border-radius: 50%;
  box-shadow: 0 0 5px #00ff00;
}

.brand-logo {
  font-size: 1.5rem;
  margin: 0;
  color: #00e5ff;
  text-shadow: 2px 2px #003300;
  letter-spacing: 2px;
}

/* 기존 style scoped 내에서 수정 */
.header-right {
  display: flex;
  align-items: center;
  justify-content: space-between;  
  
  gap: 20px; /* 요소 사이 간격 */
}

/* 만약 #header-extra-actions 안에 내용이 들어오면 간격을 줍니다 */
#header-extra-actions:not(:empty) {
    margin-right: 10px; /* EXIT 버튼과의 간격 */
}

.user-name {
  color: #ffd700;
  font-size: 0.9rem;
}

.logout-btn {
  background: transparent;
  border: 1px solid #ff0055;
  color: #ff0055;
  padding: 5px 12px;
  font-family: inherit;
  cursor: pointer;
  font-size: 0.8rem;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: #ff0055;
  color: #fff;
  box-shadow: 0 0 10px #ff0055;
}

.header-line {
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, transparent, #00ff00, transparent);
  box-shadow: 0 0 5px #00ff00;
}

/* 애니메이션 */
.blink {
  animation: blink-animation 2s infinite;
}

@keyframes blink-animation {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.3;
  }
}

/* 모바일 대응 */
@media (max-width: 600px) {
  .brand-logo {
    display: none;
  } /* 모바일에서 로고 숨기고 상태만 표시 */
  .user-name {
    font-size: 0.7rem;
  }
}
</style>
