package keepgoing.demo.domain.ai.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class AiResponseDto {
    private int score;
    private String rank;
    private String dailyTitle;
    private String oneLineSummary;

    private List<Insight> insights;
    private List<Recommendation> recommendations;

    // [수정] 단순 문자열 리스트로 변경
    private List<String> miningKeywords;

    @Data
    public static class Insight {
        private String type;
        private String iconType;
        private String title;
        private String description;
    }

    @Data
    public static class Recommendation {
        private String menu;
        private String reason;
    }
}