<template>
    <div class="meal-modal-overlay" @click="handleOverlayClick">
        <div class="meal-modal" @click.stop>
            <div class="modal-header">
                <button @click="closeModal" class="close-btn">
                    <span class="close-icon">✕</span>
                </button>
                <h2 class="modal-title">식사 기록</h2>
                <button @click="saveMeal" class="save-btn">완료</button>
            </div>

            <div class="date-section">
                <div class="date-display">{{ formattedDate }}</div>
            </div>

            <div class="meal-type-section">
                <h3 class="section-title">식사 시간</h3>
                <div class="meal-type-buttons">
                    <button
                        v-for="time in mealTimes"
                        :key="time.id"
                        :class="['meal-type-btn', { active: selectedMealTime === time.name }]"
                        @click="selectedMealTime = time.name"
                    >
                        <span class="meal-emoji">{{ time.emoji }}</span>
                        <span class="meal-name">{{ time.name }}</span>
                    </button>
                </div>
            </div>

            <div class="food-input-section">
                <h3 class="section-title">무엇을 드셨나요?</h3>
                
                <div class="food-input-container">
                    <input
                        :value="foodName" @input="handleInput" @keydown="handleKeydown"
                        type="text"
                        class="food-input"
                        placeholder="예: 김치찌개, 삼겹살, 샐러드..."
                        autocomplete="off"
                    />
                    
                    <div v-if="isLoading" class="loading-indicator">
                        <svg class="animate-spin h-5 w-5 text-indigo-500" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                    </div>

                    <button @click="addFood()" class="add-btn">추가</button>
                    
                    <ul 
                        v-if="suggestions.length > 0" 
                        class="suggestions-dropdown"
                    >
                        <li
                            v-for="(suggestion, index) in suggestions"
                            :key="index"
                            @click="selectFood(suggestion)"
                            @mouseover="selectedFoodIndex = index"
                            :class="['suggestion-item', { active: index === selectedFoodIndex }]"
                        >
                            {{ suggestion.name }}
                        </li>
                    </ul>

                </div>

                <div v-if="selectedFoodNameList.length > 0" class="food-list">
                    <div v-for="(foodNameItem, index) in selectedFoodNameList" :key="index" class="food-item">
                        <span class="food-item-name">{{ foodNameItem }}</span>
                        <button @click="removeFood(index)" class="remove-food-btn">
                            ✕
                        </button>
                    </div>
                </div>
            </div>

            <div class="photo-section">
                <h3 class="section-title">사진 (선택)</h3>
                <div class="photo-upload-area" @click="triggerFileInput">
                    <input
                        ref="fileInput"
                        type="file"
                        accept="image/*"
                        style="display: none"
                        @change="handleFileUpload"
                    />
                    <div v-if="!photoPreview" class="photo-placeholder">
                        <span class="camera-icon">📷</span>
                        <span class="photo-text">사진 추가하기</span>
                    </div>
                    <div v-else class="photo-preview">
                        <img :src="photoPreview" alt="식사 사진" />
                        <button @click.stop="removePhoto" class="remove-photo-btn">
                            ✕
                        </button>
                    </div>
                </div>
            </div>

            <div class="memo-section">
                <h3 class="section-title">메모 (선택)</h3>
                <textarea
                    v-model="memo"
                    class="memo-input"
                    placeholder="맛있었던 점, 느낀 점을 자유롭게 적어보세요..."
                    rows="3"
                ></textarea>
            </div>

            <div class="today-meals-section">
                <h3 class="section-title">오늘의 식사</h3>
                <div class="today-meals-grid">
                    <div
                        v-for="meal in todayMeals"
                        :key="meal.type"
                        :class="['meal-card', { completed: meal.completed }]"
                    >
                        <span class="meal-card-emoji">{{ meal.emoji }}</span>
                        <span class="meal-card-name">{{ meal.name }}</span>
                        <span v-if="meal.completed" class="check-icon">✓</span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from "vue";
import dayjs from "dayjs"; 

const emit = defineEmits(['close']);

// ===================================
// 1. 디바운싱 유틸리티 함수
// ===================================
const debounce = (func, delay) => {
    let timeoutId;
    return function(...args) {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(() => {
            func.apply(this, args);
        }, delay);
    };
};

// 실제 Spring Boot API 엔드포인트
const API_ENDPOINT = 'http://localhost:8080';

// ===================================
// 2. Data
// ===================================
const selectedMealTime = ref("breakfast");
const foodName = ref("");

// 최종 목표 변수
const selectedFoodList = ref([]);     
const selectedFoodNameList = ref([]); 

const memo = ref("");
const photoPreview = ref(null);
const fileInput = ref(null);

// Search State
const suggestions = ref([]); 
const isLoading = ref(false);
const selectedFoodIndex = ref(0); 
const isSelectingFood = ref(false); // 💡 추가: 자동 선택 중 플래그

const mealTimes = [
    { id: "breakfast", name: "아침", emoji: "🌅" },
    { id: "lunch", name: "점심", emoji: "🌞" },
    { id: "dinner", name: "저녁", emoji: "🌙" },
    { id: "snack", name: "간식", emoji: "🍪" },
];

const todayMeals = ref([
    { type: "breakfast", name: "아침", emoji: "🌅", completed: false },
    { type: "lunch", name: "점심", emoji: "🌞", completed: true },
    { type: "dinner", name: "저녁", emoji: "🌙", completed: false },
    { type: "snack", name: "간식", emoji: "🍪", completed: false },
]);

// Computed
const formattedDate = computed(() => {
    return dayjs().format("YYYY년 M월 D일");
});

// ===================================
// 3. 음식검색 로직
// ===================================

async function fetchSuggestions(query) {
    
    isLoading.value = true;
    let suggestionsList = []; 

    try {
        const url = `${API_ENDPOINT}/food?foodName=${encodeURIComponent(query)}`;
        
        const response = await fetch(url);

        if (!response.ok) {
            console.error(`HTTP 에러 발생: ${response.status}`);
            throw new Error(`API 호출 실패 (Status: ${response.status})`);
        }

        const data = await response.json();
        
        if (Array.isArray(data)) {
            suggestionsList = data
                .filter(food => food && food.name && food.name.includes(query.trim())) 
                .filter((food, index, self) => food.name && self.findIndex(f => f.name === food.name) === index);
        }

    } catch (error) {
        console.error('검색 API 호출 최종 오류:', error);
        suggestionsList = []; 
    } finally {
        isLoading.value = false;
        return suggestionsList;
    }
}

const debouncedSearch = debounce(async (query) => {
    if (query.trim().length < 1) { 
        suggestions.value = [];
        return;
    }
    
    // 💡 수정: isSelectingFood 플래그가 true면 검색 API 호출을 막습니다.
    if (isSelectingFood.value) {
        return; 
    }
    
    const results = await fetchSuggestions(query.trim());
    suggestions.value = results;
    selectedFoodIndex.value = 0; 
}, 100); 

const handleInput = (event) => {
    foodName.value = event.target.value;
    
    // 💡 수정: isSelectingFood가 true면 검색을 건너뜁니다.
    if (isSelectingFood.value) {
        return;
    }
    
    debouncedSearch(foodName.value);
};

// ===================================
// 4. Methods (핵심 CRUD 로직)
// ===================================

const addFood = (food) => {
    // 1. 자동완성 항목을 선택한 경우 (food 객체가 넘어옴)
    if (food && typeof food === 'object' && food['name']) {
        selectedFoodList.value.push(food);
        selectedFoodNameList.value.push(food['name']);
    } 
    // 2. 직접 입력 후 '추가' 버튼을 누르거나 Enter를 누른 경우
    else if (foodName.value.trim() !== '') {
        const customFoodName = foodName.value.trim();
        
        selectedFoodList.value.push({ name: customFoodName }); 
        selectedFoodNameList.value.push(customFoodName);
    }
    
    // ✨ 드롭다운 닫기 & Input 초기화 (핵심: 이로써 다음 검색을 막고 인풋을 비움)
    foodName.value = ''; 
    suggestions.value = []; 
    selectedFoodIndex.value = 0;
};

const removeFood = (index) => {
    selectedFoodNameList.value.splice(index, 1);
    selectedFoodList.value.splice(index, 1);
};

function selectFood(food) {
    // 💡 수정 1: 플래그를 켜서 foodName 변경 없이 addFood를 호출해도 혹시 발생할 수 있는 이벤트를 막음
    isSelectingFood.value = true; 
    
    // 💡 수정 2: foodName.value를 설정하는 코드를 제거하고 바로 addFood 호출
    addFood(food); 

    // 💡 수정 3: 다음 틱(Next Tick)에서 플래그를 해제하여 다음 사용자 입력은 허용
    setTimeout(() => {
        isSelectingFood.value = false;
    }, 100); 
}

const saveMeal = async() => {
    
    // 사진 파일 처리 로직 (Multipart)은 백엔드에 따라 달라질 수 있으므로,
    // 현재는 JSON 데이터만 보내는 것으로 가정합니다.
    const mealData = {
        memberId : 1, // 임시 하드코딩
        mealTime : selectedMealTime.value,
        foods: selectedFoodList.value,
       // photo: ... (Blob 또는 fileId)
       // memo: memo.value,
       // member : ...
    }

    try{
        const response = await fetch(`${API_ENDPOINT}/diet/meal`,{
            method:'POST',
            headers:{
                'Content-Type' : 'application/json'
            },
            body:JSON.stringify(mealData)
        });

        if (!response.ok) {
            console.error(`저장 API 실패: ${response.status}`);
            throw new Error(`저장 API 호출 실패 (Status: ${response.status})`);
        }
    }
    catch(error){
        console.error("식사 기록 저장 중 오류 발생:", error);
    }
    closeModal();
};

function handleKeydown(event) {
    const maxIndex = suggestions.value.length - 1;
    
    if (event.key === 'ArrowDown') {
        event.preventDefault(); 
        selectedFoodIndex.value = Math.min(maxIndex, selectedFoodIndex.value + 1);
    } else if (event.key === 'ArrowUp') {
        event.preventDefault(); 
        selectedFoodIndex.value = Math.max(0, selectedFoodIndex.value - 1);
    } else if (event.key === 'Enter') {
        event.preventDefault();
        if (suggestions.value.length > 0 && selectedFoodIndex.value >= 0) {
            // 자동 완성 목록 중 선택된 항목 추가
            selectFood(suggestions.value[selectedFoodIndex.value]);
        } else if (foodName.value.trim() !== '') {
            // 직접 입력된 텍스트 추가
            addFood(); 
        }
    }
}
    
// --- 모달/사진 로직 ---

const triggerFileInput = () => { fileInput.value?.click(); };
const handleFileUpload = (event) => {
    const file = event.target.files?.[0];
    if (file) {
        const reader = new FileReader();
        reader.onload = (e) => { photoPreview.value = e.target?.result; };
        reader.readAsDataURL(file);
    }
};
const removePhoto = () => {
    photoPreview.value = null;
    if (fileInput.value) { fileInput.value.value = ""; }
};

const closeModal = () => { 
    console.log("모달 닫힘 요청"); 
    emit('close'); 
}; 

const handleOverlayClick = (e) => {
    if (e.target === e.currentTarget) { closeModal(); }
};


// Lifecycle (스크롤 방지)
onMounted(() => { document.body.style.overflow = "hidden"; });
onUnmounted(() => { document.body.style.overflow = ""; });
</script>

<style scoped>
/* Inter 폰트 적용은 index.html이나 App.vue에서 처리되어야 하지만, SFC 내에 유지합니다. */
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@100..900&display=swap');

/* --- 사용자 제공 모달 CSS 시작 --- */

/* 모달 오버레이 */
.meal-modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.6);
    backdrop-filter: blur(8px);
    -webkit-backdrop-filter: blur(8px);
    display: flex;
    align-items: flex-end;
    justify-content: center;
    z-index: 9999;
    animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
    from { opacity: 0; }
    to { opacity: 1; }
}

/* 모달 컨테이너 */
.meal-modal {
    background: white;
    width: 100%;
    max-width: 500px;
    max-height: 90vh;
    border-radius: 1.5rem 1.5rem 0 0;
    overflow-y: auto;
    animation: slideUp 0.3s ease-out;
    box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.15);
}

@keyframes slideUp {
    from { transform: translateY(100%); }
    to { transform: translateY(0); }
}

/* 헤더 */
.modal-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 1.25rem 1.5rem;
    border-bottom: 1px solid #f3f4f6;
    position: sticky;
    top: 0;
    background: white;
    z-index: 10;
}

.modal-title {
    font-size: 1.25rem;
    font-weight: 700;
    color: #111827;
    margin: 0;
}

.close-btn, .save-btn {
    background: none;
    border: none;
    font-size: 1rem;
    cursor: pointer;
    padding: 0.5rem;
    -webkit-tap-highlight-color: transparent;
    touch-action: manipulation;
    transition: transform 0.2s;
}

.close-btn:active, .save-btn:active {
    transform: scale(0.9);
}

.close-icon {
    font-size: 1.5rem;
    color: #6b7280;
}

.save-btn {
    color: #98d8c8;
    font-weight: 600;
}

/* 날짜 섹션 */
.date-section {
    padding: 1rem 1.5rem;
    text-align: center;
}

.date-display {
    font-size: 0.95rem;
    color: #6b7280;
    font-weight: 500;
}

/* 섹션 타이틀 */
.section-title {
    font-size: 1rem;
    font-weight: 600;
    color: #111827;
    margin-bottom: 1rem;
}

/* 식사 시간 선택 */
.meal-type-section {
    padding: 1.5rem;
    border-top: 8px solid #f9fafb;
}

.meal-type-buttons {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 0.75rem;
}

.meal-type-btn {
    background: white;
    border: 2px solid #e5e7eb;
    padding: 1rem 0.5rem;
    border-radius: 1rem;
    cursor: pointer;
    transition: all 0.2s;
    -webkit-tap-highlight-color: transparent;
    touch-action: manipulation;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.5rem;
}

.meal-type-btn:active {
    transform: scale(0.95);
}

.meal-type-btn.active {
    background: linear-gradient(135deg, #98d8c8 0%, #6fafaa 100%);
    border-color: #6fafaa;
}

.meal-type-btn.active .meal-emoji, .meal-type-btn.active .meal-name {
    filter: brightness(1.2);
}

.meal-emoji {
    font-size: 2rem;
}

.meal-name {
    font-size: 0.85rem;
    font-weight: 600;
    color: #374151;
}

.meal-type-btn.active .meal-name {
    color: white;
}

/* 음식 입력 */
.food-input-section {
    padding: 1.5rem;
    border-top: 8px solid #f9fafb;
    position: relative; /* 연관 검색어 드롭다운을 위한 포지션 */
}

/* --- 검색 자동완성 스타일 추가 --- */
.food-input-container {
    display: flex;
    gap: 0.75rem;
    margin-bottom: 1rem;
    position: relative; /* 드롭다운 기준점 */
}

.food-input {
    flex: 1;
    padding: 0.875rem 1rem;
    border: 2px solid #e5e7eb;
    border-radius: 0.875rem;
    font-size: 0.95rem;
    outline: none;
    transition: border-color 0.2s;
    z-index: 100; /* 인풋이 드롭다운 위에 오도록 */
}

.food-input:focus {
    border-color: #98d8c8;
}

.add-btn {
    background: linear-gradient(135deg, #98d8c8 0%, #6fafaa 100%);
    color: white;
    border: none;
    padding: 0.875rem 1.5rem;
    border-radius: 0.875rem;
    font-size: 0.95rem;
    font-weight: 600;
    cursor: pointer;
    -webkit-tap-highlight-color: transparent;
    transition: transform 0.2s;
    z-index: 100;
}

.add-btn:active {
    transform: scale(0.95);
}

/* 연관 검색어 드롭다운 */
.suggestions-dropdown {
    position: absolute;
    top: 100%; /* input 아래에 위치 */
    left: 0;
    width: calc(100% - 70px); /* input width와 비슷하게 조정 (버튼 너비만큼 제외) */
    
    /* 💡 스크롤 가능하도록 높이 증가 */
    max-height: 250px; 
    overflow-y: auto;
    
    background: white;
    border: 1px solid #e5e7eb;
    border-radius: 0 0 0.875rem 0.875rem;
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    z-index: 90;
    list-style: none;
    padding: 0;
    margin-top: -10px; /* input과 겹치도록 */
}

.suggestion-item {
    padding: 0.75rem 1rem;
    cursor: pointer;
    color: #374151;
    transition: background-color 0.1s;
}

.suggestion-item:hover, .suggestion-item.active {
    background: #f0fdfa; /* Tailwind mint-50 */
    font-weight: 600;
}

/* 로딩 인디케이터 */
.loading-indicator {
    position: absolute;
    right: 80px; /* input 필드 근처 */
    top: 50%;
    transform: translateY(-50%);
    z-index: 101;
}
/* --- 검색 자동완성 스타일 종료 --- */


/* 음식 목록 */
.food-list {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
}

.food-item {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    background: #f9fafb;
    padding: 0.5rem 0.75rem;
    border-radius: 2rem;
    font-size: 0.9rem;
}

.food-item-name {
    color: #374151;
}

.remove-food-btn {
    background: none;
    border: none;
    color: #ef4444;
    font-size: 1rem;
    cursor: pointer;
    padding: 0;
    line-height: 1;
    -webkit-tap-highlight-color: transparent;
}

/* 사진 업로드 */
.photo-section {
    padding: 1.5rem;
    border-top: 8px solid #f9fafb;
}

.photo-upload-area {
    border: 2px dashed #d1d5db;
    border-radius: 1rem;
    padding: 2rem;
    text-align: center;
    cursor: pointer;
    transition: all 0.2s;
    -webkit-tap-highlight-color: transparent;
}

.photo-upload-area:active {
    transform: scale(0.98);
    background: #f9fafb;
}

.photo-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.5rem;
}

.camera-icon {
    font-size: 3rem;
}

.photo-text {
    font-size: 0.9rem;
    color: #6b7280;
}

.photo-preview {
    position: relative;
}

.photo-preview img {
    width: 100%;
    max-height: 200px;
    object-fit: cover;
    border-radius: 0.875rem;
}

.remove-photo-btn {
    position: absolute;
    top: 0.5rem;
    right: 0.5rem;
    background: rgba(0, 0, 0, 0.6);
    color: white;
    border: none;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    font-size: 1.25rem;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    -webkit-tap-highlight-color: transparent;
}

/* 메모 */
.memo-section {
    padding: 1.5rem;
    border-top: 8px solid #f9fafb;
}

.memo-input {
    width: 100%;
    padding: 1rem;
    border: 2px solid #e5e7eb;
    border-radius: 0.875rem;
    font-size: 0.9rem;
    color: #111827;
    resize: none;
    font-family: inherit;
    outline: none;
    transition: border-color 0.2s;
}

.memo-input:focus {
    border-color: #98d8c8;
}

.memo-input::placeholder {
    color: #9ca3af;
}

/* 오늘의 식사 */
.today-meals-section {
    padding: 1.5rem;
    border-top: 8px solid #f9fafb;
}

.today-meals-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 0.75rem;
}

.meal-card {
    background: #f9fafb;
    padding: 1rem 0.5rem;
    border-radius: 1rem;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.5rem;
    position: relative;
    border: 2px solid transparent;
}

.meal-card.completed {
    background: linear-gradient(135deg, #e0f2f1 0%, #b2dfdb 100%);
    border-color: #4db6ac;
}

.meal-card-emoji {
    font-size: 1.75rem;
}

.meal-card-name {
    font-size: 0.8rem;
    color: #6b7280;
    font-weight: 500;
}

.meal-card.completed .meal-card-name {
    color: #00796b;
    font-weight: 600;
}

.check-icon {
    position: absolute;
    top: 0.25rem;
    right: 0.25rem;
    background: #4db6ac;
    color: white;
    width: 20px;
    height: 20px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 0.75rem;
    font-weight: bold;
}

/* 모바일 최적화 */
@media (max-width: 390px) {
    .meal-type-buttons {
        grid-template-columns: repeat(2, 1fr);
    }

    .meal-emoji {
        font-size: 1.75rem;
    }

    .today-meals-grid {
        grid-template-columns: repeat(2, 1fr);
    }
}

/* 스크롤바 스타일링 */
.meal-modal::-webkit-scrollbar {
    width: 6px;
}

.meal-modal::-webkit-scrollbar-track {
    background: transparent;
}

.meal-modal::-webkit-scrollbar-thumb {
    background: #d1d5db;
    border-radius: 3px;
}

.meal-modal::-webkit-scrollbar-thumb:hover {
    background: #9ca3af;
}

/* --- 사용자 제공 모달 CSS 종료 --- */
</style>