<template>
  <div class="calendar-view">
    <header class="header">
      <h1>캘린더</h1>
    </header>
    
    <div class="content">
      <p>캘린더 기능 준비 중...</p>
    </div>

    <nav class="bottom-nav">
      <button @click="$router.push('/')" class="nav-btn">🏠 홈</button>
      <button @click="$router.push('/calendar')" class="nav-btn active">📅 캘린더</button>
      <button @click="$router.push('/ai-analysis')" class="nav-btn">🤖 AI 분석</button>
      <button @click="$router.push('/profile')" class="nav-btn">👤 마이페이지</button>
    </nav>
  </div>
</template>

<style scoped>
.calendar-view {
  min-height: 100vh;
  padding-bottom: 80px;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 2rem 1.5rem;
}

.content {
  padding: 2rem 1.5rem;
}

.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  display: flex;
  justify-content: space-around;
  padding: 1rem;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
}

.nav-btn {
  background: none;
  border: none;
  padding: 0.5rem;
  font-size: 0.75rem;
  cursor: pointer;
  color: var(--color-text-secondary);
}

.nav-btn.active {
  color: var(--color-primary);
  font-weight: bold;
}
</style>