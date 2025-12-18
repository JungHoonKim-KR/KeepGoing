package keepgoing.demo.domain.diet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import keepgoing.demo.domain.ai.dto.AiRequestDto;
import keepgoing.demo.domain.ai.dto.AiResponseDto;
import keepgoing.demo.domain.ai.service.AiClient;
import keepgoing.demo.domain.diet.dto.*;
import keepgoing.demo.domain.diet.entity.AiReport;
import keepgoing.demo.domain.diet.entity.Diet;
import keepgoing.demo.domain.diet.entity.Food;
import keepgoing.demo.domain.diet.entity.HydrationRecord;
import keepgoing.demo.domain.diet.mapper.DietMapper;
import keepgoing.demo.domain.diet.norm.MealTime;
import keepgoing.demo.domain.member.entity.Member;
import keepgoing.demo.domain.member.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
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

        // 2. 식단 조회
        List<Diet> dietList = dietMapper.findAllByDate(memberId, date);
        if(dietList.isEmpty()) throw new IllegalArgumentException("식단 기록 없음");

        // 3. [변경] AI 전송용 요약 문자열 만들기 (PromptGenerator 사용)
        // (만약 PromptGenerator를 아직 안 만들었다면, 기존 StringBuilder 로직을 유지해도 됩니다)
        // String summary = promptGenerator.createDailyAnalysisPrompt(dietList);

        // -> PromptGenerator가 없다면 기존 로직 유지:
        StringBuilder summary = new StringBuilder();
        for (Diet d : dietList) {
            String foodNames = d.getFoods().stream()
                    .map(Food::getName).collect(Collectors.joining(", "));
            summary.append(String.format("[%s] %s (탄:%.0f, 단:%.0f, 지:%.0f) / ",
                    d.getMealTime(), foodNames, d.getCarbohydrate(), d.getProtein(), d.getFat()));
        }

        // 4. [핵심] AI 요청 객체 생성 (Null 방어 로직 추가 + 필드 10개 맞추기)
        String healthCondition = (member.getHealthCondition() != null && !member.getHealthCondition().isBlank())
                ? member.getHealthCondition() : "없음";
        String allergies = (member.getAllergies() != null && !member.getAllergies().isBlank())
                ? member.getAllergies() : "없음";
        String dislikedFood = (member.getDislikedFood() != null && !member.getDislikedFood().isBlank())
                ? member.getDislikedFood() : "없음";

        AiRequestDto request = new AiRequestDto(
                new AiRequestDto.UserProfile(
                        member.getHeight(),
                        member.getWeight(),
                        member.getAge(),
                        member.getGender(),
                        member.getActivity(),
                        member.getGoal(),
                        // ▼ 추가된 4개 필드 (순서 중요!)
                        healthCondition,
                        allergies,
                        dislikedFood,
                        member.getTargetWeight()
                ),
                new AiRequestDto.DailyLog(
                        date.toString(),
                        summary.toString()
                )
        );

        // 5. AI 호출
        AiResponseDto result = aiClient.requestAnalysis(request);

        // 6. 결과 저장 (구조 변경 반영)
        try {
            // AiResponseDto 구조가 바뀌었으므로 .dailyFeedback() 제거
            // .score(), .oneLineSummary() 바로 접근
            // .recommendations() 리스트를 JSON으로 변환
            String recommendJson = objectMapper.writeValueAsString(result.recommendations());

            dietMapper.saveAiReport(AiReport.builder()
                    .memberId(memberId)
                    .date(date)
                    .score(result.score())               // 수정됨
                    .feedbackText(result.oneLineSummary()) // 수정됨
                    .recommendJson(recommendJson)
                    .build());
        } catch (Exception e) {
            e.printStackTrace(); // (가급적 log.error 사용 권장)
            throw new RuntimeException("결과 저장 실패", e); // 트랜잭션 롤백을 위해 예외 던지기
        }

        return result;
    }

    // 월별 조회
    public List<DailyEvaluationDto> getMonthlyEvaluations(Long memberId, int year, int month) {
        String strYear = String.valueOf(year);
        // 1 -> "01"로 변환
        String strMonth = String.format("%02d", month);
        return dietMapper.selectEvaluationsByMonth(memberId, strYear, strMonth);
    }

    // 평가 저장/수정
    @Transactional
    public void saveEvaluation(DailyEvaluationDto dto) {
        dietMapper.upsertEvaluation(dto);
    }

    // 평가 삭제
    @Transactional
    public void removeEvaluation(Long memberId, LocalDate date, String category) {
        dietMapper.deleteEvaluation(memberId, date, category);
    }

    public Map<String, Diet> selectDailyDiet(Long memberId, LocalDate date) {

        List<Diet> diets = dietMapper.findAllByDate(memberId, date);

        //    (키: "아침", "점심" 등의 Diet 엔티티의 mealTime 값)
        Map<String, Diet> recordedDiets = diets.stream()
                .collect(Collectors.toMap(
                        Diet::getMealTime,
                        diet -> diet,
                        (existing, replacement) -> existing, // 키 충돌 방지
                        () -> new HashMap<>()
                ));

        Map<String, Diet> allDietsWithNulls = new LinkedHashMap<>();

        for(MealTime mealTime : MealTime.values()){
            // MealTime Enum의 이름(예: "아침")을 키로 사용
            String mealKey = mealTime.name();

            // recordedDiets에 해당 키가 있으면 Diet 객체를, 없으면 null을 할당
            Diet diet = recordedDiets.getOrDefault(mealKey, null);

            allDietsWithNulls.put(mealKey, diet);
        }

        return allDietsWithNulls;
    }

    public Double getHydration(Long memberId, LocalDate date) {
        return dietMapper.selectHydration(memberId, date)
                .map(aDouble -> aDouble / 1000.0)
                .orElse(0.0);
    }

    @Transactional
    public void addHydration(WaterInsertRequestDTO dto){
        LocalDate recordDate = LocalDate.now();
        HydrationRecord hydrationRecord = HydrationRecord.builder().memberId(dto.getMemberId()).waterAmount(dto.getWaterAmount()*1000).date(recordDate)
                .build();

        dietMapper.insertHydration(hydrationRecord);

    }

    @Transactional
    public int addDiet(DietInsertRequestDTO dto) {
        Diet newDiet = buildDiet(dto);
        Diet findDiet = dietMapper.selectDiet(dto.getMemberId(), newDiet.getDate(),dto.getMealTime());
        Long dietId;
        if(findDiet == null){
            dietMapper.insertDiet(dto.getMemberId(), newDiet);
            dietId = newDiet.getId();
        }
        else{
            dietMapper.updateDietNutrients(findDiet.getId(), newDiet);
            dietId = findDiet.getId();
        }
        dietMapper.insertFoodMappings(dto.getFoods(), dietId);
        return 1;// 임시
    }


    private Diet buildDiet(DietInsertRequestDTO dto) {
        NutritionTotalsDTO nutritionTotalsDTO = calculateNutritionTotals(dto.getFoods());
        return Diet.builder()
                .memberId(dto.getMemberId())
                .date(dto.getDate())
                .mealTime(dto.getMealTime())
                .energy(nutritionTotalsDTO.getTotalEnergy())
                .water(nutritionTotalsDTO.getTotalWater())
                .protein(nutritionTotalsDTO.getTotalProtein())
                .carbohydrate(nutritionTotalsDTO.getTotalCarbohydrate())
                .sugars(nutritionTotalsDTO.getTotalSugars())
                .sodium(nutritionTotalsDTO.getTotalSodium())
                .build();
    }
    private NutritionTotalsDTO calculateNutritionTotals(List<FoodRecordDTO> foods) {
        double totalEnergy = 0.0, totalWater = 0.0, totalProtein = 0.0, totalFat = 0.0;
        double totalCarbohydrate = 0.0, totalSugars = 0.0, totalSodium = 0.0;

        for (FoodRecordDTO food : foods) {
            double ratio = food.getFoodWeight() / food.getServingSize();
            // Food 객체의 String 필드를 double로 파싱하여 계산
            try {
                totalEnergy += food.getEnergy() * ratio * food.getServings();
                totalWater += food.getWater() * ratio * food.getServings();
                totalProtein += food.getProtein() * ratio * food.getServings();
                totalFat += food.getFat() * ratio * food.getServings();
                totalCarbohydrate += food.getCarbohydrate() * ratio * food.getServings();
                totalSugars += food.getSugars() * ratio * food.getServings();
                totalSodium += food.getSodium() * ratio * food.getServings();
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