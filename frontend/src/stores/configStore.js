// stores/configStore.js (Pinia 예시)
import { defineStore } from 'pinia';
import dayjs from 'dayjs';

export const useConfigStore = defineStore('config', () => {
    // 🚀 상수 (API 엔드포인트)
    const API_ENDPOINT = 'http://localhost:8080';

    // 👤 사용자 상태 (변화 가능성 있음)
    const MEMBER_ID = 1; // 실제로는 로그인 시 동적으로 설정됨

    // 📅 유틸리티 (Computed는 Store 내에서 Getter로 정의하거나,
    // 간단한 유틸리티 함수는 actions나 일반 함수로 제공)

    const getCurrentDateForAPI = () => dayjs().format('YYYY-MM-DD');
    
    // 현재 날짜는 필요 시 Store 외부에서 computed를 사용할 수 있지만, 
    // Store 내부에서는 Getter로 정의하거나 함수로 제공합니다.
    const currentDate = dayjs().format('YYYY.MM.DD');

    return {
        API_ENDPOINT,
        MEMBER_ID,
        getCurrentDateForAPI,
        currentDate
    };
});