package keepgoing.demo.domain.diet.entity;

import lombok.*;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Food {
    private String code;         // 식품코드
    private String name;         // 식품명
    private String dataTypeName;     // 데이터구분명
    private String middleCategoryName; // 식품중분류명

    private double foodWeight;       // 식품중량
    private double servingSize;   // 영양성분함량기준량
    private double energy;           // 에너지(kcal)
    private double water;            // 수분(g)
    private double protein;          // 단백질(g)
    private double fat;              // 지방(g)
    private double carbohydrate;     // 탄수화물(g)
    private double sugars;           // 당류(g)
    private double sodium;           // 나트륨(g)



}