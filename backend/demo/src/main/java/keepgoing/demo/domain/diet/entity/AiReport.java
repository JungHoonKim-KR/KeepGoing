package keepgoing.demo.domain.diet.entity;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class AiReport {
    private Long id;
    private Long memberId;
    private LocalDate date;
    private int score;
    private String feedbackText;
    private String recommendJson; // 추천 식단 JSON 문자열
}