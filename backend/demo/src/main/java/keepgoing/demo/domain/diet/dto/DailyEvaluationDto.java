package keepgoing.demo.domain.diet.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DailyEvaluationDto {
    private Long memberId;
    private LocalDate date;
    private String rank;
}