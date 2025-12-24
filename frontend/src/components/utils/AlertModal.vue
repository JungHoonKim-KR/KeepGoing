<template>
  <div class="modal-overlay" @click="$emit('close')">
    <div class="modal-win alert-modal pop-in" @click.stop>
      <div class="modal-header">
        <span>SYSTEM MESSAGE</span>
      </div>
      <div class="modal-body">
        <div class="alert-icon">✅</div>
        <p class="alert-message" v-html="message"></p>
        <div class="button-group">
          <button class="ok-btn" @click="$emit('close')">확인 (OK)</button>
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
defineEmits(['close']);
</script>

<style scoped>
@import url("https://cdn.jsdelivr.net/gh/neodgm/neodgm-webfont@latest/neodgm/style.css");

.modal-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.85);
  z-index: 1000000000; /* Higher than confirm modal */
  display: flex; align-items: center; justify-content: center;
  backdrop-filter: blur(5px);
  font-family: "NeoDunggeunmo", monospace;
}

.modal-win.alert-modal {
  width: 90%; max-width: 400px;
  background: #050510;
  border: 2px solid #00ff00; /* Green for success */
  box-shadow: 0 0 20px rgba(0, 255, 0, 0.2);
  display: flex; flex-direction: column;
}

.pop-in {
  animation: popIn 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
}
@keyframes popIn {
  from { transform: scale(0.8); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

.modal-header {
  background: #00ff00;
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

.alert-icon {
  font-size: 3rem;
  margin-bottom: 15px;
  animation: float 2s infinite ease-in-out;
}
@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.alert-message {
  font-size: 1.1rem;
  line-height: 1.6;
  margin-bottom: 25px;
  white-space: pre-wrap;
}

.button-group {
  display: flex;
  justify-content: center;
}

.ok-btn {
  width: 100%;
  padding: 12px;
  font-weight: bold;
  border: 2px solid;
  cursor: pointer;
  font-family: inherit;
  text-transform: uppercase;
  transition: all 0.2s;
  border-radius: 5px;
  background: #238636;
  color: #fff;
  border-color: #fff;
}
.ok-btn:hover {
  background: #fff;
  color: #238636;
}
</style>
