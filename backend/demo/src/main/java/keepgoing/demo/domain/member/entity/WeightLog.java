package keepgoing.demo.domain.member.entity;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class WeightLog{
    private Long id;            // PK
    private Long memberId;      // FK
    private Double weight;      // 측정 체중
    private Double diff;        // 이전 체중과의 차이
    private LocalDate date;     // 기록 날짜
    private String memo;        // 메모 (치트키)
    private LocalDateTime recordedAt;

    public void updateWeightLog(double newWeight, double newDiff, String newMemo) {
        // 객체의 상태를 직접 변경
        this.weight = newWeight;
        this.diff = newDiff;
        this.memo = newMemo;
        this.recordedAt = LocalDateTime.now(); // 기록 일시 업데이트
    }
}