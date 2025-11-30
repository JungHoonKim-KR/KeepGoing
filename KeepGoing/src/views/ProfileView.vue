<script setup>
import { ref } from "vue";

// ----------------------------------------------------
// 1. 상태 관리
// ----------------------------------------------------
const profile = ref({
  name: "홍길동", // 예시 이름 (실제로는 로그인 정보에서 가져와야 함)
  age: 30,
  gender: "male",
  height: 175,
  weight: 70,
  activityLevel: "moderate", // 활동량: 낮음(low), 보통(moderate), 높음(high)
  sleepHours: 7,
  weeklyWorkout: 3,
  bodyType: "balanced", // 체형: 하체(lower), 상체(upper), 골고루(balanced)
});

const isEditing = ref(false);

const activityOptions = [
  { value: "low", label: "낮음 (주로 앉아있음)" },
  { value: "moderate", label: "보통 (규칙적인 활동)" },
  { value: "high", label: "높음 (격렬한 운동)" },
];

const bodyTypeOptions = [
  { value: "lower", label: "하체 발달형" },
  { value: "upper", label: "상체 발달형" },
  { value: "balanced", label: "골고루형" },
];

// ----------------------------------------------------
// 2. 이벤트 핸들러
// ----------------------------------------------------
const startEdit = () => {
  isEditing.value = true;
};

const saveProfile = () => {
  // 💡 여기에 서버 통신 로직을 추가하여 profile.value 데이터를 저장합니다.
  console.log("프로필 저장:", profile.value);
  isEditing.value = false;
  alert("프로필 정보가 저장되었습니다.");
};

const cancelEdit = () => {
  // 실제 앱에서는 저장되지 않은 변경 사항을 되돌리는 로직이 필요합니다.
  // 여기서는 단순히 수정 모드를 닫습니다.
  isEditing.value = false;
};
</script>

<template>
  <div class="profile-view">
    <header class="header">
      <div class="header-content">
        <h1>👤 마이페이지</h1>
        <button v-if="!isEditing" @click="startEdit" class="edit-btn">수정</button>
      </div>
    </header>

    <div class="content">
      <div class="info-card">
        <div class="profile-item">
          <label>이름</label>
          <span class="display-value">{{ profile.name }}</span>
        </div>
        
        <template v-if="!isEditing">
          <div class="profile-item">
            <label>나이</label>
            <span class="display-value">{{ profile.age }}세</span>
          </div>
          <div class="profile-item">
            <label>성별</label>
            <span class="display-value">{{ profile.gender === 'male' ? '남성' : '여성' }}</span>
          </div>
          <div class="profile-item">
            <label>키/몸무게</label>
            <span class="display-value">{{ profile.height }}cm / {{ profile.weight }}kg</span>
          </div>
          <div class="profile-item">
            <label>활동량</label>
            <span class="display-value">{{ activityOptions.find(o => o.value === profile.activityLevel)?.label }}</span>
          </div>
          <div class="profile-item">
            <label>수면 시간</label>
            <span class="display-value">{{ profile.sleepHours }}시간</span>
          </div>
          <div class="profile-item">
            <label>주간 운동 횟수</label>
            <span class="display-value">{{ profile.weeklyWorkout }}회</span>
          </div>
          <div class="profile-item">
            <label>체형</label>
            <span class="display-value body-type">{{ bodyTypeOptions.find(o => o.value === profile.bodyType)?.label }}</span>
          </div>
        </template>
        
        <template v-else>
          <div class="form-group">
            <label for="age">나이 (만)</label>
            <input id="age" type="number" v-model.number="profile.age" min="1" />
          </div>
          
          <div class="form-group">
            <label>성별</label>
            <div class="radio-group">
                <input id="gender-male" type="radio" value="male" v-model="profile.gender" />
                <label for="gender-male">남성</label>
                <input id="gender-female" type="radio" value="female" v-model="profile.gender" />
                <label for="gender-female">여성</label>
            </div>
          </div>

          <div class="form-group-inline">
            <div class="form-group sub-group">
                <label for="height">키 (cm)</label>
                <input id="height" type="number" v-model.number="profile.height" min="100" />
            </div>
            <div class="form-group sub-group">
                <label for="weight">몸무게 (kg)</label>
                <input id="weight" type="number" v-model.number="profile.weight" min="30" />
            </div>
          </div>

          <div class="form-group">
            <label for="activity">활동량</label>
            <select id="activity" v-model="profile.activityLevel">
                <option v-for="option in activityOptions" :key="option.value" :value="option.value">
                    {{ option.label }}
                </option>
            </select>
          </div>
          
          <div class="form-group">
            <label for="sleepHours">평균 수면 시간 (시간)</label>
            <input id="sleepHours" type="number" v-model.number="profile.sleepHours" min="1" max="15" />
          </div>

          <div class="form-group">
            <label for="weeklyWorkout">주간 운동 횟수</label>
            <input id="weeklyWorkout" type="number" v-model.number="profile.weeklyWorkout" min="0" max="7" />
          </div>

          <div class="form-group">
            <label>체형 선택</label>
            <div class="radio-group body-type-select">
                <div v-for="option in bodyTypeOptions" :key="option.value" class="radio-item">
                    <input :id="'body-' + option.value" type="radio" :value="option.value" v-model="profile.bodyType" />
                    <label :for="'body-' + option.value">{{ option.label }}</label>
                </div>
            </div>
          </div>
          
          <div class="action-buttons">
            <button @click="cancelEdit" class="cancel-btn">취소</button>
            <button @click="saveProfile" class="save-btn">저장</button>
          </div>
        </template>
        
      </div>
    </div>

    <nav class="bottom-nav">
      <button @click="$router.push('/')" class="nav-btn">🏠 홈</button>
      <button @click="$router.push('/calendar')" class="nav-btn">📅 캘린더</button>
      <button @click="$router.push('/ai-analysis')" class="nav-btn">🤖 AI 분석</button>
      <button @click="$router.push('/profile')" class="nav-btn active">👤 마이페이지</button>
    </nav>
  </div>
</template>

<style scoped>
/* --- 기본 변수 정의 (캘린더 뷰와 통일) --- */
:root {
  --color-primary: #667eea;
  --color-secondary: #764ba2;
  --color-text-default: #333;
  --color-text-secondary: #555;
  --color-red: #ff3b30;
  --color-green: #4CAF50;
  --color-accent: #FF9800; /* 강조 색상 */
}

/* --- 레이아웃 및 헤더 --- */
.profile-view {
  min-height: 100vh;
  background-color: #f7f7f7;
  padding-bottom: 80px; /* 네비게이션 공간 확보 */
}

.header {
  background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-secondary) 100%);
  color: white;
  padding: 2rem 1.5rem 1rem;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h1 {
  font-size: 1.5rem;
  font-weight: bold;
  margin: 0;
}

.edit-btn {
  background-color: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 20px;
  padding: 0.5rem 1rem;
  font-size: 0.8rem;
  font-weight: 500;
  cursor: pointer;
}

/* --- 컨텐츠 카드 --- */
.content {
  padding: 1rem 1.5rem;
}

.info-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
}

/* --- 정보 표시 모드 스타일 --- */
.profile-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 0;
  border-bottom: 1px solid #eee;
}

.profile-item:last-child {
  border-bottom: none;
}

.profile-item label {
  font-weight: 600;
  color: var(--color-text-secondary);
}

.display-value {
  font-weight: bold;
  color: var(--color-text-default);
}

.body-type {
    color: var(--color-primary);
    background-color: #e6e9ff;
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 0.9rem;
}

/* --- 폼 수정 모드 스타일 --- */
.form-group {
    margin-bottom: 1rem;
}

.form-group label {
    display: block;
    font-weight: 600;
    margin-bottom: 0.4rem;
    color: var(--color-text-default);
}

.form-group input[type="number"],
.form-group select {
    width: 100%;
    padding: 0.75rem 1rem;
    border: 1px solid #ddd;
    border-radius: 8px;
    font-size: 1rem;
    box-sizing: border-box;
    transition: border-color 0.2s;
}

.form-group input[type="number"]:focus,
.form-group select:focus {
    border-color: var(--color-primary);
    outline: none;
}

.form-group-inline {
    display: flex;
    gap: 1rem;
    margin-bottom: 1rem;
}

.form-group-inline .sub-group {
    flex: 1;
    margin-bottom: 0;
}

.radio-group {
    display: flex;
    gap: 1.5rem;
    align-items: center;
}

.radio-group input[type="radio"] {
    display: none;
}

.radio-group label {
    padding: 0.5rem 1rem;
    border: 1px solid #ddd;
    border-radius: 20px;
    cursor: pointer;
    font-weight: normal;
    transition: all 0.2s;
}

.radio-group input[type="radio"]:checked + label {
    background-color: var(--color-primary);
    color: white;
    border-color: var(--color-primary);
    font-weight: 600;
}

.body-type-select {
    flex-wrap: wrap;
    gap: 0.75rem;
}

/* --- 액션 버튼 --- */
.action-buttons {
    display: flex;
    justify-content: space-between;
    margin-top: 2rem;
    gap: 1rem;
}

.save-btn, .cancel-btn {
    flex: 1;
    padding: 1rem;
    border: none;
    border-radius: 8px;
    font-size: 1rem;
    font-weight: bold;
    cursor: pointer;
    transition: background-color 0.2s;
}

.save-btn {
    background-color: var(--color-primary);
    color: white;
}

.cancel-btn {
    background-color: #ccc;
    color: var(--color-text-default);
}


/* --- 하단 네비게이션 (캘린더 뷰와 동일) --- */
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
  z-index: 10;
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