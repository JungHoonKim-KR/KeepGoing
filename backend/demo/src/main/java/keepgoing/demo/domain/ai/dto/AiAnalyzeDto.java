package keepgoing.demo.domain.ai.dto;

import java.util.List;
import java.util.Map;

// Class 대신 Record 사용 (Lombok @Data, @NoArgsConstructor 불필요)
public record AiAnalyzeDto(
        int score,
        String rank,
        String dailyTitle,
        String oneLineSummary,
        List<Insight> insights,
        List<String> miningKeywords,
        Integer totalCalories,
        List<Map<String, Object>> recommendedExercises

) {

    // 내부 클래스도 Record로 정의
    public record Insight(
            String type,
            String iconType,
            String title,
            String description
    ) {}

    public record Recommendation(
            String menu,
            String reason
    ) {}
}