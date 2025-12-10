package keepgoing.demo.domain.member.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MemberWeightResponseDto {
    private LocalDate date;
    private double weight;
    private double change;
}
