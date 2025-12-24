package keepgoing.demo.domain.diet.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiReport {
    private Long id;
    private Long memberId;
    private LocalDate date;
    private int score;
    private String rank;
    private String feedbackText;
    private String dailyTitle;   // 컬럼 추가 혹은 가공
    private String insightsJson; // [추가] insights 배열을 통째로 저장할 필드
    private int totalCalories;
    private String exerciseJson;
}