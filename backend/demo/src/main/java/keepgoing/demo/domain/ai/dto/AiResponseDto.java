package keepgoing.demo.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public record AiResponseDto(
        @JsonProperty("daily_feedback") DailyFeedback dailyFeedback,
        @JsonProperty("tomorrow_recommendation") TomorrowRecommendation tomorrowRecommendation
) {
    public record DailyFeedback(
            int score,
            @JsonProperty("one_line_summary") String oneLineSummary,
            List<String> pros,
            List<String> cons
    ) {}

    public record TomorrowRecommendation(
            String message,
            Map<String, String> meals // breakfast, lunch...
    ) {}
}