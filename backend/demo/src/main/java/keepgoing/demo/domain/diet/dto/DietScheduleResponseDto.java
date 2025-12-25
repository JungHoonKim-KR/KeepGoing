package keepgoing.demo.domain.diet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
public class DietScheduleResponseDto {
    private String date;        // "yyyy-MM-dd" (Map의 Key로 사용될 예정)
    private MenuDetail menu; // 프론트엔드 v-for 대응용 리스트
    private String quest;
    private int totalCal;
    private Boolean isIncludeFavorite;
    // 아침/점심/저녁 구조체
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuDetail {
        private String breakfast;
        private String lunch;
        private String dinner;
    }
}