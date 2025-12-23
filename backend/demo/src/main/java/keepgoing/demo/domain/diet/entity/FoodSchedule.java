package keepgoing.demo.domain.diet.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
// JSON 타입을 String으로 받거나 Map으로 처리하기 위해 라이브러리 사용 가능하지만,
// MyBatis에서는 일단 String으로 받아서 Service에서 변환하는 게 가장 쉽습니다.

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodSchedule {
    private Long id;
    private Long memberId;
    private LocalDate date;

    // DB의 JSON 컬럼을 매핑 (MyBatis resultMap에서 String으로 받아짐)
    private String recommendJson;

    private String createdAt;
    private String updatedAt;

    // 편의상 Map으로 변환해서 쓸 수도 있지만, DTO 변환은 Service에서 하는 걸 추천합니다.
}