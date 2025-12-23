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
        private int day;

        // [수정] String -> MenuDetail 객체로 변경
        private MenuDetail menu;

        private int cal;
        private String difficulty;
        private String quest;
    }

    // [추가] 아침/점심/저녁을 담을 내부 클래스
    @Data
    @NoArgsConstructor
    public static class MenuDetail {
        private String breakfast;
        private String lunch;
        private String dinner;
    }
}