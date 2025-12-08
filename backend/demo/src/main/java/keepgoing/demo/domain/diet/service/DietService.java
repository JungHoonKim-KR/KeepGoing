package keepgoing.demo.domain.diet.service;

import keepgoing.demo.domain.diet.dto.DietInsertRequestDTO;
import keepgoing.demo.domain.diet.dto.NutritionTotalsDTO;
import keepgoing.demo.domain.diet.entity.Diet;
import keepgoing.demo.domain.diet.entity.DietFoodMapping;
import keepgoing.demo.domain.diet.entity.Food;
import keepgoing.demo.domain.diet.mapper.DietMapper;
import keepgoing.demo.domain.diet.norm.MealTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DietService {
    private final DietMapper dietMapper;

    public int addDiet(DietInsertRequestDTO dto) {

        return 1;// 임시

    }


    private Diet buildDiet(DietInsertRequestDTO dto) {
        NutritionTotalsDTO nutritionTotalsDTO = calculateNutritionTotals(dto.getFoods());
        LocalDate recordDate = LocalDate.now();
        MealTime mealTime = MealTime.valueOf(dto.getMealTime());
        return Diet.builder()
                .memberId(dto.getMemberId())
                .date(recordDate)
                .mealTime(mealTime)
                .energy(nutritionTotalsDTO.getTotalEnergy())
                .water(nutritionTotalsDTO.getTotalWater())
                .protein(nutritionTotalsDTO.getTotalProtein())
                .carbohydrate(nutritionTotalsDTO.getTotalCarbohydrate())
                .sugars(nutritionTotalsDTO.getTotalSugars())
                .sodium(nutritionTotalsDTO.getTotalSodium())
                .build();


    }
    private NutritionTotalsDTO calculateNutritionTotals(List<Food> foods) {
        double totalEnergy = 0.0, totalWater = 0.0, totalProtein = 0.0, totalFat = 0.0;
        double totalCarbohydrate = 0.0, totalSugars = 0.0, totalSodium = 0.0;

        for (Food food : foods) {

            double ratio = Double.parseDouble(food.getFoodWeight()) / Double.parseDouble(food.getServingSize());
            // Food 객체의 String 필드를 double로 파싱하여 계산
            try {
                totalEnergy += Double.parseDouble(food.getEnergy()) * ratio;
                totalWater += Double.parseDouble(food.getWater()) * ratio;
                totalProtein += Double.parseDouble(food.getProtein()) * ratio;
                totalFat += Double.parseDouble(food.getFat()) * ratio;
                totalCarbohydrate += Double.parseDouble(food.getCarbohydrate()) * ratio;
                totalSugars += Double.parseDouble(food.getSugars()) * ratio;
                totalSodium += Double.parseDouble(food.getSodium()) * ratio;
            } catch (NumberFormatException e) {
                // 계산 실패 시 로깅 및 예외 처리
                throw new RuntimeException("영양 성분 데이터 변환 오류 발생", e);
            }
        }
        return NutritionTotalsDTO.builder().totalEnergy(totalEnergy).totalCarbohydrate(totalCarbohydrate)
                .totalFat(totalFat).totalProtein(totalProtein).totalSodium(totalSodium)
                .totalSugars(totalSugars).totalWater(totalWater)
                .build();
    }

//    private List<DietFoodMapping> createMappings()
}
