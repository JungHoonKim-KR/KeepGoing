package keepgoing.demo.domain.diet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import keepgoing.demo.domain.ai.dto.AiRequestDto;
import keepgoing.demo.domain.ai.dto.AiResponseDto;
import keepgoing.demo.domain.ai.service.AiClient;
import keepgoing.demo.domain.diet.dto.DietInsertRequestDTO;
import keepgoing.demo.domain.diet.dto.NutritionTotalsDTO;
import keepgoing.demo.domain.diet.entity.AiReport;
import keepgoing.demo.domain.diet.entity.Diet;
import keepgoing.demo.domain.diet.entity.Food;
import keepgoing.demo.domain.diet.mapper.DietMapper;
import keepgoing.demo.domain.diet.norm.MealTime;
import keepgoing.demo.domain.member.entity.Member;
import keepgoing.demo.domain.member.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DietService {

    private final DietMapper dietMapper;
    private final MemberMapper memberMapper;
    private final AiClient aiClient;
    private final ObjectMapper objectMapper;

    @Transactional
    public AiResponseDto analyzeDailyDiet(Long memberId, LocalDate date) {
        // 1. 회원 조회
        Member member = memberMapper.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        // 2. 식단 조회 (DB에 저장된 거 가져오기)
        List<Diet> dietList = dietMapper.findAllByDate(memberId, date);
        if(dietList.isEmpty()) throw new IllegalArgumentException("식단 기록 없음");

        // 3. AI 전송용 요약 문자열 만들기
        StringBuilder summary = new StringBuilder();
        for (Diet d : dietList) {
            String foodNames = d.getFoods().stream()
                    .map(Food::getName).collect(Collectors.joining(", "));

            summary.append(String.format("[%s] %s (탄:%.0f, 단:%.0f, 지:%.0f) / ",
                    d.getMealTime(), foodNames, d.getCarbohydrate(), d.getProtein(), d.getFat()));
        }

        // 4. AI 요청
        AiRequestDto request = new AiRequestDto(
                new AiRequestDto.UserProfile(
                        member.getHeight(), member.getWeight(), member.getAge(),
                        member.getGender(), member.getActivity(), member.getGoal()),
                new AiRequestDto.DailyLog(date.toString(), summary.toString())
        );

        AiResponseDto result = aiClient.requestAnalysis(request);

        // 5. 결과 저장
        try {
            dietMapper.saveAiReport(AiReport.builder()
                    .memberId(memberId)
                    .date(date)
                    .score(result.dailyFeedback().score())
                    .feedbackText(result.dailyFeedback().oneLineSummary())
                    .recommendJson(objectMapper.writeValueAsString(result.tomorrowRecommendation()))
                    .build());
        } catch (Exception e) { e.printStackTrace(); }

        return result;
    }

    @Transactional
    public int addDiet(DietInsertRequestDTO dto) {
        Diet diet = buildDiet(dto);
        dietMapper.insert(dto.getMemberId(), diet);
        Long dietId = diet.getId();
        dietMapper.insertFoodMappings(dto.getFoods(), dietId);
        System.out.println("실행");
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


}