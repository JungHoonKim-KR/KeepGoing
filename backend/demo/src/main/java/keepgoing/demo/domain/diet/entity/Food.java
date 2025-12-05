package keepgoing.demo.domain.diet.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class Food {

    private String code;         // 식품코드
    private String name;         // 식품명
    private String dataTypeName;     // 데이터구분명
    private String middleCategoryName; // 식품중분류명
    private String foodWeight;       // 식품중량

    private String servingSize;   // 영양성분함량기준량
    private String energy;           // 에너지(kcal)
    private String water;            // 수분(g)
    private String protein;          // 단백질(g)
    private String fat;              // 지방(g)
    private String ash;              // 회분(g)
    private String carbohydrate;     // 탄수화물(g)
    private String sugars;           // 당류(g)
}