package keepgoing.demo.domain.diet.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NutritionTotalsDTO {
    private final double totalEnergy;
    private final double totalWater;
    private final double totalProtein;
    private final double totalFat;
    private final double totalCarbohydrate;
    private final double totalSugars;
    private final double totalSodium;
}