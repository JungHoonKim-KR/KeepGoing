package keepgoing.demo.domain.member.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
public class WeightLog{
    private Long id;            // PK
    private Long memberId;      // FK
    private Double weight;      // 측정 체중
    private Double diff;        // 이전 체중과의 차이
    private LocalDate date;     // 기록 날짜
    private String memo;        // 메모 (치트키)
}