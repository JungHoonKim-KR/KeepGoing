<template>
  <div class="record-view">
    <header class="header">
      <button @click="$router.back()" class="back-btn">← 뒤로</button>
      <h1>체중 기록</h1>
    </header>
    
    <div class="content">
      <p>체중 기록 기능 준비 중...</p>
    </div>
  </div>
</template>

<style scoped>
.record-view {
  min-height: 100vh;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 2rem 1.5rem;
  position: relative;
}

.back-btn {
  background: none;
  border: none;
  color: white;
  font-size: 1rem;
  cursor: pointer;
  margin-bottom: 0.5rem;
}

.content {
  padding: 2rem 1.5rem;
}
</style>