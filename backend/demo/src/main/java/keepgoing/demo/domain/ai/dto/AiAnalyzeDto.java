package keepgoing.demo.domain.ai.dto;

import java.util.List;

// Class 대신 Record 사용 (Lombok @Data, @NoArgsConstructor 불필요)
public record AiAnalyzeDto(
        int score,
        String rank,
        String dailyTitle,
        String oneLineSummary,
        List<Insight> insights,
        List<Recommendation> recommendations,
        List<String> miningKeywords
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