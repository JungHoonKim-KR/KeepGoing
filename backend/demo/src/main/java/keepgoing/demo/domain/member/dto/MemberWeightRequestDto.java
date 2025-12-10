package keepgoing.demo.domain.member.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MemberWeightRequestDto {
    private Long memberId;
    private Double weight;
    private LocalDate date;
    private String memo;
}
