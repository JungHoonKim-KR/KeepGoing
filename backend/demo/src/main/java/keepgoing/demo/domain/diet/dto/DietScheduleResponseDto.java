package keepgoing.demo.domain.diet.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class DietScheduleResponseDto {
    private String date;        // "yyyy-MM-dd" (Map의 Key로 사용될 예정)
    private List<MenuDto> menus; // 프론트엔드 v-for 대응용 리스트
    private String quest;
    private int totalCal;

    @Data
    @Builder
    public static class MenuDto {
        private String name;
        private int cal;
    }
}