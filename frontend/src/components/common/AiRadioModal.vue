<script setup>
import { ref, watch, onUnmounted } from "vue";

const props = defineProps({
  isOpen: Boolean,
});
const emit = defineEmits(["close"]);

// === 상태 관리 ===
const userPrompt = ref("");
const aiResponse = ref("채널 연결 대기 중...");
const isSpeaking = ref(false); // AI가 말하는 중인가?
const isProcessing = ref(false); // 통신 연결 중인가?

// === 🔊 TTS (음성 합성) ===
const synth = window.speechSynthesis;
let utterance = null;

const speak = (text) => {
  if (synth.speaking) synth.cancel();

  utterance = new SpeechSynthesisUtterance(text);

  // 한국어 음성 찾기
  const voices = synth.getVoices();
  const korVoice = voices.find((v) => v.lang.includes("ko"));
  if (korVoice) utterance.voice = korVoice;

  // 무전기 느낌: 약간 빠르게, 피치는 평범하게
  utterance.pitch = 1.0;
  utterance.rate = 1.1;

  utterance.onstart = () => {
    isSpeaking.value = true;
  };
  utterance.onend = () => {
    isSpeaking.value = false;
  };

  synth.speak(utterance);
};

// === 📡 메시지 전송 (시뮬레이션) ===
const sendMessage = () => {
  if (!userPrompt.value.trim()) return;

  const input = userPrompt.value;
  userPrompt.value = ""; // 입력창 비우기
  isProcessing.value = true;
  aiResponse.value = "데이터 수신 중...";

  // 실제로는 여기서 API를 호출합니다. (지금은 시뮬레이션)
  setTimeout(() => {
    isProcessing.value = false;

    // 예시 응답 로직
    let responseText = "";
    if (input.includes("힘들어") || input.includes("배고파")) {
      responseText =
        "경고. 멘탈 수치가 떨어지고 있습니다. 지금 포기하면 초기화됩니다. 물 한 잔 마시고 버티십시오. 오버.";
    } else if (input.includes("운동")) {
      responseText =
        "좋은 자세입니다. 오늘 목표치까지 30% 남았습니다. 계속 진행하십시오. 오버.";
    } else {
      responseText = `수신된 내용: "${input}". 해당 내용에 대한 분석 결과를 전송합니다. 식단을 유지하십시오. 이상.`;
    }

    aiResponse.value = ""; // 타이핑 효과를 위해 비움
    typeWriter(responseText);
    speak(responseText);
  }, 1000);
};

// 타자기 효과
const typeWriter = (text) => {
  let i = 0;
  const speed = 50;
  const type = () => {
    if (i < text.length) {
      aiResponse.value += text.charAt(i);
      i++;
      setTimeout(type, speed);
    }
  };
  type();
};

// === 닫기 및 초기화 ===
const close = () => {
  synth.cancel();
  isSpeaking.value = false;
  emit("close");
};

// 모달이 열릴 때 초기 메시지
watch(
  () => props.isOpen,
  (newVal) => {
    if (newVal) {
      aiResponse.value = "";
      setTimeout(() => {
        const intro = "통신 채널 개방. 명령을 대기 중입니다.";
        typeWriter(intro);
        speak(intro);
      }, 500);
    } else {
      synth.cancel();
    }
  }
);

onUnmounted(() => synth.cancel());
</script>

<template>
  <div v-if="isOpen" class="radio-overlay" @click.self="close">
    <div class="radio-device">
      <div class="device-header">
        <span class="freq">FREQ: 140.96</span>
        <div class="signal-light" :class="{ 'blink-red': isSpeaking }"></div>
        <button class="close-btn" @click="close">OFF</button>
      </div>

      <div class="screen-container">
        <div class="visualizer">
          <div v-if="isSpeaking" class="wave-group">
            <div
              class="bar"
              v-for="n in 15"
              :key="n"
              :style="{ animationDuration: `${Math.random() * 0.5 + 0.2}s` }"
            ></div>
          </div>
          <div v-else class="flat-line"></div>
        </div>

        <div class="text-display">
          <p class="ai-text">
            <span class="prefix">HQ:</span>
            {{ aiResponse }}<span class="cursor">_</span>
          </p>
        </div>
      </div>

      <div class="input-area">
        <input
          v-model="userPrompt"
          @keyup.enter="sendMessage"
          type="text"
          class="comm-input"
          placeholder="메시지 입력..."
          autocomplete="off"
        />
        <button class="transmit-btn" @click="sendMessage">SEND</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url("https://cdn.jsdelivr.net/gh/neodgm/neodgm-webfont@latest/neodgm/style.css");

.radio-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.85);
  backdrop-filter: blur(4px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000;
  font-family: "NeoDunggeunmo", monospace;
}

/* 무전기 본체 디자인 */
.radio-device {
  width: 90%;
  max-width: 380px;
  background-color: #0f1510; /* 아주 어두운 녹색빛 검정 */
  border: 4px solid #334433;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.8), inset 0 0 40px rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

/* 헤더 */
.device-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 2px solid #334433;
  padding-bottom: 0.5rem;
}
.freq {
  color: #00ff41;
  font-size: 0.9rem;
  text-shadow: 0 0 5px #00ff41;
}
.signal-light {
  width: 10px;
  height: 10px;
  background: #111;
  border-radius: 50%;
  border: 1px solid #555;
}
.signal-light.blink-red {
  background: #ff3333;
  box-shadow: 0 0 8px #ff3333;
}
.close-btn {
  color: #555;
  font-size: 0.8rem;
  background: none;
  border: 1px solid #333;
  padding: 2px 6px;
  cursor: pointer;
}

/* 스크린 (녹색 모니터 느낌) */
.screen-container {
  background: #001100;
  border: 2px solid #004400;
  border-radius: 4px;
  padding: 1rem;
  min-height: 180px;
  display: flex;
  flex-direction: column;
  position: relative;
  overflow: hidden;
  box-shadow: inset 0 0 20px rgba(0, 255, 0, 0.1);
}
/* 스캔라인 오버레이 */
.screen-container::after {
  content: " ";
  display: block;
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  right: 0;
  background: linear-gradient(
    rgba(18, 16, 16, 0) 50%,
    rgba(0, 255, 0, 0.05) 50%
  );
  background-size: 100% 4px;
  pointer-events: none;
}

/* 비주얼라이저 (음성 파형) */
.visualizer {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px dashed #004400;
  margin-bottom: 10px;
}
.flat-line {
  width: 90%;
  height: 2px;
  background: #004400;
}
.wave-group {
  display: flex;
  align-items: center;
  gap: 3px;
  height: 100%;
}
.bar {
  width: 6px;
  background: #00ff41;
  animation: equalize 0.5s infinite ease-in-out;
}
@keyframes equalize {
  0% {
    height: 10%;
  }
  50% {
    height: 80%;
  }
  100% {
    height: 10%;
  }
}

/* 텍스트 영역 */
.text-display {
  flex: 1;
  overflow-y: auto;
}
.ai-text {
  color: #00ff41;
  font-size: 0.95rem;
  line-height: 1.5;
  text-shadow: 0 0 2px #00ff41;
}
.prefix {
  font-weight: bold;
  margin-right: 5px;
  opacity: 0.7;
}
.cursor {
  animation: blink 1s infinite;
}

/* 입력 영역 */
.input-area {
  display: flex;
  gap: 8px;
}
.comm-input {
  flex: 1;
  background: #000;
  border: 1px solid #334433;
  color: #00ff41;
  padding: 10px;
  font-family: inherit;
  font-size: 0.9rem;
  outline: none;
}
.comm-input::placeholder {
  color: #004400;
}
.comm-input:focus {
  border-color: #00ff41;
}

.transmit-btn {
  background: #004400;
  color: #00ff41;
  border: 1px solid #00ff41;
  padding: 0 15px;
  font-weight: bold;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.1s;
}
.transmit-btn:active {
  background: #00ff41;
  color: #000;
}

@keyframes blink {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0;
  }
}
</style>
