package keepgoing.demo.domain.diet.entity;

import keepgoing.demo.domain.diet.norm.MealTime;
import lombok.*;

import java.time.LocalDate;
import java.util.List;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Diet {
    private Long id;
    private Long memberId;
    private LocalDate date;
    private List<Food> foods;
    private double energy;
    private double water;
    private double protein;
    private double fat;
    private double carbohydrate;
    private double sugars;
    private double sodium;

    private String mealTime;
//    private List<DietFoodMapping> foodMappings;
}
