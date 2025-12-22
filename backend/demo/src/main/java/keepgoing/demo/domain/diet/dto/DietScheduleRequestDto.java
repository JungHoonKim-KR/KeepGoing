package keepgoing.demo.domain.diet.dto;

import lombok.Data;

@Data
public class DietScheduleRequestDto {
    private Long memberId;
    private int period;
}
