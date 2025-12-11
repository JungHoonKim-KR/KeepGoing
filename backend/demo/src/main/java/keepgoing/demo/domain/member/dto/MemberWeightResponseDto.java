package keepgoing.demo.domain.member.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public record MemberWeightResponseDto(
        LocalDate date,
        double weight,
        double diff
) {
}
