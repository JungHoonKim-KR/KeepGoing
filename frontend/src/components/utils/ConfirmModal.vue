<template>
  <div class="modal-overlay" @click="$emit('cancel')">
    <div class="modal-win confirm-modal pop-in" @click.stop>
      <div class="modal-header">
        <span>SYSTEM NOTICE</span>
      </div>
      <div class="modal-body">
        <div class="confirm-icon">⚠️</div>
        <p class="confirm-message" v-html="message"></p>
        <div class="button-group">
          <button class="confirm-btn" @click="$emit('confirm')">동기화 (CONFIRM)</button>
          <button class="cancel-btn" @click="$emit('cancel')">취소 (CANCEL)</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  message: {
    type: String,
    required: true,
  },
});
defineEmits(['confirm', 'cancel']);
</script>

<style scoped>
@import url("https://cdn.jsdelivr.net/gh/neodgm/neodgm-webfont@latest/neodgm/style.css");

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.85);
  z-index: 999999999;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(5px);
  font-family: "NeoDunggeunmo", monospace;
}

.modal-win.confirm-modal {
  width: 90%;
  max-width: 400px;
  background: #050510;
  border: 2px solid #ffd700; /* 경고/알림에 어울리는 노란색 */
  box-shadow: 0 0 20px rgba(255, 215, 0, 0.2);
  display: flex;
  flex-direction: column;
  max-height: 85vh;
}

.pop-in {
  animation: popIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
@keyframes popIn {
  from { transform: scale(0.8); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

.modal-header {
  background: #ffd700;
  color: #000;
  padding: 12px;
  text-align: center;
  font-weight: bold;
  letter-spacing: 1px;
}

.modal-body {
  padding: 25px 20px;
  text-align: center;
  color: #fff;
}

.confirm-icon {
  font-size: 3rem;
  margin-bottom: 15px;
  animation: float 2s infinite ease-in-out;
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.confirm-message {
  font-size: 1.1rem;
  line-height: 1.6;
  margin-bottom: 25px;
  white-space: pre-wrap; /* \n을 줄바꿈으로 인식 */
}

.button-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.confirm-btn, .cancel-btn {
  width: 100%;
  padding: 12px;
  font-weight: bold;
  border: 2px solid;
  cursor: pointer;
  font-family: inherit;
  text-transform: uppercase;
  transition: all 0.2s;
  border-radius: 5px;
}

.confirm-btn {
  background: #238636; /* Green */
  color: #fff;
  border-color: #fff;
}
.confirm-btn:hover {
  background: #fff;
  color: #238636;
}

.cancel-btn {
  background: #8b949e; /* Gray */
  color: #000;
  border-color: #000;
}
.cancel-btn:hover {
  background: #000;
  color: #8b949e;
}
</style>
