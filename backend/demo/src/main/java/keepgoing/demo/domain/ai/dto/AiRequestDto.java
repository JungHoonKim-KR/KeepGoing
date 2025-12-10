package keepgoing.demo.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiRequestDto(
        @JsonProperty("userProfile")
        UserProfile profile,

        @JsonProperty("dailyLog")
        DailyLog dailyLog
) {
    public record UserProfile(
            double height,
            double weight,
            int age,
            String gender,
            String activity,
            String goal,
            String healthCondition,
            String allergies,
            String dislikedFood,
            Double targetWeight
    ) {}

    public record DailyLog(
            String date,

            // [핵심 수정]
            // Java 변수명이 무엇이든 상관없이, JSON으로 나갈 때는 무조건 "dietSummary"로 나가게 함
            @JsonProperty("dietSummary")
            String summary
    ) {}
}