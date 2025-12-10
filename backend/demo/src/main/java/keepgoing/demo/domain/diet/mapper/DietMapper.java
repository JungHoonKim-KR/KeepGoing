package keepgoing.demo.domain.diet.mapper;

import keepgoing.demo.domain.diet.dto.FoodRecordDTO;
import keepgoing.demo.domain.diet.entity.AiReport;
import keepgoing.demo.domain.diet.entity.Diet;
import keepgoing.demo.domain.diet.entity.Food;
import keepgoing.demo.domain.diet.entity.HydrationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Mapper
public interface DietMapper {
    void insertDiet(@Param("memberId")Long memberId, @Param("diet") Diet diet);
    void insertHydration(@Param("hydrationRecord") HydrationRecord hydrationRecord);
    void insertFoodMappings(@Param("foods") List<FoodRecordDTO> foods, @Param("dietId") Long dietId);

    void updateDietNutrients(@Param("dietId") Long dietId, @Param("diet") Diet diet);

    Diet selectDiet(@Param("memberId") Long memberId,
                              @Param("date") LocalDate date,
                              @Param("mealTime") String mealTime);

    // [추가] 1. 특정 날짜의 식단 기록 조회 (Join 쿼리 사용 예정)
    List<Diet> findAllByDate(@Param("memberId") Long memberId, @Param("date") LocalDate date);
    Optional<Double> selectHydration(@Param("memberId")Long memberId, @Param("date")LocalDate date);
    // [유지] 2. AI 결과 저장 (이건 내 담당)
    void saveAiReport(AiReport aiReport);
}
