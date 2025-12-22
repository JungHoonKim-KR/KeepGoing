package keepgoing.demo.domain.ai.dto;

import java.util.List;

public record AiRecommendDto {
    public List<Recommendation> recommendations;


    public record Recommendation(
            String menu,
            String reason
    ) {
    }
}
