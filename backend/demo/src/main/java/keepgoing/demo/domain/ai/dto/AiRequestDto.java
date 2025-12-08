package keepgoing.demo.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// record를 쓰면 코드가 간결해집니다. (Java 17 이상 권장)
public record AiRequestDto(
        @JsonProperty("profile") UserProfile profile,
        @JsonProperty("daily_log") DailyLog dailyLog
) {
    public record UserProfile(
            double height, double weight, int age, String gender, String activity, String goal
    ) {}

    public record DailyLog(
            String date,
            // 아침, 점심, 저녁 상세 내용을 하나의 문자열로 요약해서 보낼 예정
            @JsonProperty("total_summary") String totalSummary
    ) {}
}