package keepgoing.demo.domain.diet.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DietFoodMapping {
    private Long id;
    private Long memberId;
    private Long dietId;
    private String foodCode;

    // Mybatis에서 객체를 조회하고 담기 위함, 테이블에는 없음
    private Food food;

    private double totalEnergy;
    private double totalWater;
    private double totalCarbohydrate;
    private double totalProtein;
    private double totalFat;
    private double totalSugars;
    private double totalSodium;

}
