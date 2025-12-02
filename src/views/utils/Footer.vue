<template>
  <nav class="bottom-nav">
    <button @click="$router.push('/')" :class="['nav-btn', { 'active': $route.path === '/' }]">
      <span class="nav-icon">🏠</span>
    </button>
    <button @click="$router.push('/calendar')" :class="['nav-btn', { 'active': $route.path === '/calendar' }]">
      <span class="nav-icon">📅</span>
    </button>
    <button @click="$router.push('/ai-analysis')" :class="['nav-btn', { 'active': $route.path === '/ai-analysis' }]">
      <span class="nav-icon">🤖</span>
    </button>
    <button @click="$router.push('/profile')" :class="['nav-btn', { 'active': $route.path === '/profile' }]">
      <span class="nav-icon">👤</span>
    </button>
  </nav>
</template>

<script>
export default {
  name: 'Footer', 
};
</script>

<style scoped>

/* 하단 네비게이션 */
.bottom-nav {
 position: fixed;
    bottom: 24px; /* 바닥에서 띄우기 */
    left: 20px;   /* 왼쪽 여백 */
    right: 20px;  /* 오른쪽 여백 */
    
    /* 둥근 모서리 */
    border-radius: 24px; 
    
    /* 유리 같은 반투명 배경 효과 */
    background: rgba(255, 255, 255, 0.85); 
    backdrop-filter: blur(15px);
    -webkit-backdrop-filter: blur(15px);
    
    /* 레이아웃 */
    display: flex;
    justify-content: space-around;
    align-items: center; /* 아이콘 수직 중앙 정렬 */
    padding: 0.5rem;
    
    /* 부드러운 그림자 */
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
    z-index: 1000;
    
    /* 테두리는 제거하거나 아주 얇은 흰색 선으로 빛 반사 효과 */
    border: 1px solid rgba(255, 255, 255, 0.4);
}

.nav-btn {
  background: none;
  border: none;
  padding: 0.4rem 0.5rem;
  cursor: pointer;
  color: var(--color-text-secondary);
  transition: all 0.2s ease-out;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.2rem;
  flex: 1;
  max-width: 70px;
  -webkit-tap-highlight-color: transparent;
  touch-action: manipulation;
  user-select: none;
}

.nav-icon {
  font-size: 1.35rem;
  transition: transform 0.2s ease-out;
  display: block;
}

.nav-label {
  font-size: 0.625rem;
  font-weight: 500;
  letter-spacing: -0.01em;
}

.nav-btn.active {
  color: var(--color-primary);
}

.nav-btn.active .nav-icon {
  transform: scale(1.1);
}

.nav-btn:active {
  transform: scale(0.92);
  opacity: 0.7;
}
</style>