package keepgoing.demo.domain.diet.dto;

import keepgoing.demo.domain.diet.entity.Food;
import lombok.Data;

import java.util.List;

@Data
public class DietInsertRequestDTO {
    Long memberId;
    String mealTime;
    List<FoodRecordDTO> foods;
}
