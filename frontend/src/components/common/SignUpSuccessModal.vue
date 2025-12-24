<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="handleClose">
    <div class="modal-content">
      <div class="modal-header">
        <h3 class="modal-title">{{ title }}</h3>
        <button class="close-button" @click="handleClose">X</button>
      </div>
      <div class="modal-body">
        <img v-if="imageSrc" :src="imageSrc" alt="Character" class="modal-image" />
        <p>{{ message }}</p>
      </div>
      <div class="modal-footer">
        <button class="confirm-button" @click="handleClose">확인</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
  isOpen: {
    type: Boolean,
    required: true
  },
  title: {
    type: String,
    default: "알림"
  },
  message: {
    type: String,
    required: true
  },
  imageSrc: {
    type: String,
    default: null // New prop for image source
  }
});

const emit = defineEmits(['close']);

const handleClose = () => {
  emit('close');
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999; /* Ensure it's above other content */
}

.modal-content {
  background: #101010; /* Retro dark background */
  border: 2px solid var(--neon-blue); /* Neon blue border */
  box-shadow: 0 0 15px var(--neon-blue); /* Neon glow */
  border-radius: 8px;
  width: 90%;
  max-width: 400px;
  color: #e0e0e0; /* Light text */
  font-family: "NeoDunggeunmo", monospace;
  animation: fadeInPop 0.3s ease-out;
  display: flex;
  flex-direction: column;
}

.modal-header {
  padding: 15px 20px;
  border-bottom: 1px solid var(--neon-blue);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #2a2a34; /* Slightly lighter header */
}

.modal-title {
  margin: 0;
  font-size: 1.2em;
  color: var(--neon-green); /* Neon green title */
}

.close-button {
  background: none;
  border: none;
  color: #e0e0e0;
  font-size: 1.2em;
  cursor: pointer;
  padding: 5px;
  transition: color 0.2s;
}

.close-button:hover {
  color: var(--neon-pink); /* Neon pink on hover */
}

.modal-body {
  padding: 20px;
  text-align: center;
  flex-grow: 1; /* Allow body to grow */
  display: flex; /* Added for image centering */
  flex-direction: column; /* Added for image and text stacking */
  align-items: center; /* Center horizontally */
  justify-content: center; /* Center vertically */
}

.modal-image {
  max-width: 100px; /* Adjust size as needed */
  max-height: 100px;
  margin-bottom: 15px; /* Space between image and text */
  image-rendering: pixelated; /* For pixel art style */
}

.modal-footer {
  padding: 15px 20px;
  border-top: 1px solid var(--neon-blue);
  text-align: center;
  background-color: #2a2a34;
}

.confirm-button {
  background: var(--neon-blue); /* Neon blue button */
  color: white;
  border: none;
  padding: 10px 20px;
  font-size: 1em;
  font-weight: bold;
  border-radius: 4px;
  cursor: pointer;
  transition: background 0.2s, box-shadow 0.2s;
}

.confirm-button:hover {
  background: #00e5ff; /* Lighter neon blue */
  box-shadow: 0 0 10px #00e5ff; /* Stronger glow */
}

/* Keyframe for fade-in and pop effect */
@keyframes fadeInPop {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(-20px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
  }
}

/* Neon color variables (assuming they are defined globally or in a parent) */
/* If not, define them here or ensure they are imported */
:root {
  --neon-blue: #00e5ff;
  --neon-green: #00ff00;
  --neon-pink: #ff0055;
  --neon-yellow: #ffd700;
}
</style>
