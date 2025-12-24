package keepgoing.demo.domain.diet.service;

import keepgoing.demo.domain.ai.service.AiClient;
import keepgoing.demo.domain.diet.dto.FoodAnalysisResponseDto;
import keepgoing.demo.domain.diet.entity.Food;
import keepgoing.demo.domain.diet.mapper.FoodMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FoodService {

    private final AiClient aiClient;
    private final FoodMapper foodMapper;

    public List<Food> selectByName(String partOfName) {
        return foodMapper.selectByName(partOfName);
    }

    /**
     * 음식을 분석하고, 음식일 경우에만 DB에 저장합니다.
     * 결과는 항상 프론트엔드로 반환합니다.
     */
    @Transactional
    public FoodAnalysisResponseDto analyzeAndSaveFood(String inputName) {

        // 1. AI 서버 분석 요청
        FoodAnalysisResponseDto result = aiClient.requestFoodAnalysis(inputName);

        // 2. 음식이 맞는 경우에만 저장 로직 실행
        if (result.getIsFood() == 1) {
            Food food = foodMapper.selectByExactName(inputName);

            if (food == null) {
                result.setIsFood(2);
                // (1) PK 생성 (USER_ + UUID 8자리)
                String generatedCode = "USER_" + UUID.randomUUID().toString().substring(0, 8);
                result.setCode(generatedCode); // DTO에도 코드를 심어줌 (프론트 필요 시)

                // (2) DTO -> Entity 변환
                Food foodEntity = Food.builder()
                        .code(generatedCode)
                        .name(result.getName())
                        .dataTypeName(result.getDataTypeName())
                        .middleCategoryName(result.getMiddleCategoryName())
                        .foodWeight(safeDouble(result.getFoodWeight()))
                        // String("100") -> double(100.0) 변환
                        .servingSize(parseStringDouble(result.getServingSize()))
                        .energy(safeDouble(result.getEnergy()))
                        .water(safeDouble(result.getWater()))
                        .protein(safeDouble(result.getProtein()))
                        .fat(safeDouble(result.getFat()))
                        .carbohydrate(safeDouble(result.getCarbohydrate()))
                        .sugars(safeDouble(result.getSugars()))
                        .sodium(safeDouble(result.getSodium()))
                        .build();

                // (3) DB 저장
                foodMapper.save(foodEntity);
            }
        } else {
            log.info("🚫 음식이 아님: {}, 저장을 건너뜁니다.", inputName);
        }

        // 3. 결과 반환 (음식이든 아니든 프론트엔드로 데이터를 보냄)
        return result;
    }

    // [Helper] Null 방지용
    private double safeDouble(Double value) {
        return value != null ? value : 0.0;
    }

    // [Helper] "100", "100g" 같은 문자열을 double로 변환
    private double parseStringDouble(String value) {
        if (value == null || value.isEmpty()) return 0.0;
        try {
            return Double.parseDouble(value.replaceAll("[^0-9.]", ""));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}