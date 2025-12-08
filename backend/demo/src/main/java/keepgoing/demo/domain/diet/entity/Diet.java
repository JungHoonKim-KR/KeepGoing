package keepgoing.demo.domain.diet.entity;

import keepgoing.demo.domain.diet.norm.MealTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
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

    private MealTime mealTime;
    private List<DietFoodMapping> foodMappings;
}
