package keepgoing.demo.domain.diet.mapper;

import keepgoing.demo.domain.diet.entity.AiReport;
import keepgoing.demo.domain.diet.entity.Diet;
import keepgoing.demo.domain.diet.entity.Food;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface DietMapper {
    void insert(Long memberId, Diet diet);
    void insertFoodMappings(@Param("foods") List<Food> foods, @Param("dietId") Long dietId);
    // [추가] 1. 특정 날짜의 식단 기록 조회 (Join 쿼리 사용 예정)
    List<Diet> findAllByDate(@Param("memberId") Long memberId, @Param("date") LocalDate date);

    // [유지] 2. AI 결과 저장 (이건 내 담당)
    void saveAiReport(AiReport aiReport);
}
