package keepgoing.demo.domain.member.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
public class MemberWeightResponseDto {
    public MemberWeightResponseDto(LocalDate date, double weight, double diff, boolean isPlus) {
        this.date = date;
        this.weight = weight;
        this.diff = diff;
        this.isPlus = isPlus;
    }

    private LocalDate date;
    private double weight;
    private double diff;
    private boolean isPlus;
}
