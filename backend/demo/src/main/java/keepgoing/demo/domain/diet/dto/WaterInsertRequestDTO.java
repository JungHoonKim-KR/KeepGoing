package keepgoing.demo.domain.diet.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class WaterInsertRequestDTO {
    Long memberId;
    double waterAmount;
    LocalDate date;
}
