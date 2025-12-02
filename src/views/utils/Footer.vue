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
  name: 'Footer', // 컴포넌트 이름 지정
  // Vue Router를 사용하고 있으므로 별도의 데이터나 로직은 필요 없습니다.
  // 이 컴포넌트는 App.vue나 레이아웃 컴포넌트에 import 되어 사용됩니다.
};
</script>

<style scoped>
/*
  이곳에 .bottom-nav, .nav-btn, .nav-icon 등에 대한 CSS 스타일을 작성합니다.
  'scoped' 속성을 사용하여 스타일이 이 컴포넌트에만 적용되도록 합니다.
*/
.bottom-nav {
  position: fixed; /* 하단 고정 */
  bottom: 0;
  left: 0;
  height: 60px; /* 원하는 높이 설정 */
  display: flex;
  justify-content: space-around;
  align-items: center;
  background-color: #ffffff; /* 배경색 */
  box-shadow: 0 -2px 5px rgba(0, 0, 0, 0.1); /* 상단 그림자 */
  z-index: 1000;
}

.nav-btn {
  flex-grow: 1; /* 버튼이 공간을 균등하게 차지 */
  height: 100%;
  border: none;
  background: none;
  cursor: pointer;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 0;
}

.nav-icon {
  font-size: 24px; /* 아이콘 크기 */
  opacity: 0.5; /* 비활성 상태 */
  transition: opacity 0.3s;
}

.nav-btn.active .nav-icon {
  opacity: 1; /* 활성 상태 */
  transform: scale(1.1); /* 활성 상태일 때 약간 확대 */
}
</style>