package keepgoing.demo.domain.diet.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class DietApplyRequestDto {
    private Long memberId;
    private List<DietPlanItem> plans;

    @Data
    @NoArgsConstructor
    public static class DietPlanItem {
        private int day;        // 날짜 계산용 (DB 저장 X)
        private String menu;    // JSON 저장 O
        private int cal;        // JSON 저장 O
        private String difficulty; // JSON 저장 O
        private String quest;   // JSON 저장 O
    }
}