package keepgoing.demo.domain.diet.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Builder
public class HydrationRecord {
    private Long id;
    private Long memberId;
    private double waterAmount;
    private LocalDate date;
}
