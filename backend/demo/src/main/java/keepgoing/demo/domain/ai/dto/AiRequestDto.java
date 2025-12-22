package keepgoing.demo.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

// null 값은 파이썬으로 보내지 않도록 설정
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AiRequestDto(
        @JsonProperty("userProfile")
        UserProfile profile,

        @JsonProperty("dailyLog")
        DailyLog dailyLog,

        // [New] 식단 생성용 설문 데이터 추가
        @JsonProperty("survey")
        SurveyData survey
) {
    public record UserProfile(
            double height,
            double weight,
            int age,
            String gender,
            String activity,        // 운동량 (Python의 'exercise'와 매핑 필요)
            String goal,
            String healthCondition,
            String allergies,
            String dislikedFood,
            Double targetWeight
    ) {}

    public record DailyLog(
            String date,
            @JsonProperty("dietSummary")
            String summary
    ) {}

    // [New] 식단 생성 시 필요한 추가 정보
    public record SurveyData(
            int duration,       // 기간 (3일, 7일)
            String sleep,       // 수면 시간
            String water,       // 물 섭취량
            int meals,          // 식사 횟수
            String favorite     // 선호 음식
    ) {}
}