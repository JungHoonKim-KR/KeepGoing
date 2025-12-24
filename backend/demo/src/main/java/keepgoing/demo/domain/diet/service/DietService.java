package keepgoing.demo.domain.diet.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import keepgoing.demo.domain.ai.dto.AiAnalyzeDto;
import keepgoing.demo.domain.ai.dto.AiRecommendDto;
import keepgoing.demo.domain.ai.dto.AiRequestDto;
import keepgoing.demo.domain.ai.dto.BodyScanResponse;
import keepgoing.demo.domain.ai.service.AiClient;
import keepgoing.demo.domain.diet.dto.*;
import keepgoing.demo.domain.diet.entity.*;
import keepgoing.demo.domain.diet.mapper.DietMapper;
import keepgoing.demo.domain.diet.norm.MealTime;
import keepgoing.demo.domain.member.dto.LevelUpResponseDto;
import keepgoing.demo.domain.member.entity.Member;
import keepgoing.demo.domain.member.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DietService {

    private final DietMapper dietMapper;
    private final MemberMapper memberMapper;
    private final AiClient aiClient;
    private final ObjectMapper objectMapper;
    @Transactional
    public AiAnalyzeDto analyzeDailyDiet(Long memberId, LocalDate date) {
        // 1. 회원 조회
        Member member = memberMapper.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        // 2. 식단 조회
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

        // 4. AI 요청 객체 생성 (Null 방어)
        String healthCondition = (member.getHealthCondition() != null && !member.getHealthCondition().isBlank()) ? member.getHealthCondition() : "없음";
        String allergies = (member.getAllergies() != null && !member.getAllergies().isBlank()) ? member.getAllergies() : "없음";
        String dislikedFood = (member.getDislikedFood() != null && !member.getDislikedFood().isBlank()) ? member.getDislikedFood() : "없음";

        AiRequestDto request = new AiRequestDto(
                new AiRequestDto.UserProfile(
                        member.getHeight(), member.getWeight(), member.getAge(),
                        member.getGender(), member.getActivity(), member.getGoal(),
                        healthCondition, allergies, dislikedFood, member.getTargetWeight()
                ),
                new AiRequestDto.DailyLog(date.toString(), summary.toString()),
                null
        );

        // 5. AI 호출
        AiAnalyzeDto result = aiClient.requestAnalysis(request);

        // 6. 결과 저장 (방법 2 적용: 전체 결과를 JSON화 하여 feedbackText에 저장)
        try {
            // [수정 포인트] AiAnalyzeDto(result) 전체를 JSON 문자열로 변환
            String fullAnalysisJson = objectMapper.writeValueAsString(result);

            String exerciseJson = "[]";
            if (result.recommendedExercises() != null) {
                exerciseJson = objectMapper.writeValueAsString(result.recommendedExercises());
            }

            dietMapper.saveAiReport(AiReport.builder()
                    .memberId(memberId)
                    .date(date)
                    .score(result.score())
                    .rank(result.rank())
                    // [핵심] feedbackText 컬럼에 상세 데이터가 포함된 JSON 전체를 넣음
                    .feedbackText(fullAnalysisJson)
                    .totalCalories(result.totalCalories())
                    .exerciseJson(exerciseJson)
                    .build());

            dietMapper.upsertEvaluation(memberId, date, result.rank());
            updateExp(memberId, member.getExp());

        } catch (Exception e) {
            throw new RuntimeException("AI 분석 결과 저장 중 오류 발생", e);
        }

        return result;
    }

    public AiReport getExistingReport(Long memberId, LocalDate date) {
        return dietMapper.findByMemberIdAndDate(memberId, date);
    }

    // -------------------------------------------------------------------------
    // [New] 1. AI 식단 생성 (Controller -> Service -> AiClient)
    // -------------------------------------------------------------------------
    public List createDietPlan(AiRequestDto requestDto) {
        // 필요하다면 여기서 회원의 기본 정보(키, 몸무게 등)를 DB에서 조회하여
        // requestDto에 없는 정보를 채워 넣을 수도 있습니다.
        // 예: requestDto = requestDto.withProfile(member.getHeight(), ...);

        // 여기서는 Controller가 보내준 DTO를 그대로 AI 서버로 토스합니다.
        return aiClient.requestDietGeneration(requestDto);
    }

    public BodyScanResponse scanBodyStats(Long memberId) {

        // 1. 회원 정보 조회
        Member member = memberMapper.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보가 없습니다. ID=" + memberId));

        // 2. AI 요청용 DTO 생성
        // 설문(Survey) 데이터는 없으므로 null을 보냅니다.
        AiRequestDto request = new AiRequestDto(
                new AiRequestDto.UserProfile(
                        member.getHeight(),
                        member.getWeight(),
                        member.getAge(),
                        member.getGender(),
                        member.getActivity(), // 활동량은 필수!
                        member.getGoal(),     // 목표도 있으면 좋음
                        null, null, null, null // 기타 상세 정보는 null
                ),
                null, // dailyLog (불필요)
                null  // survey (설문 안 하므로 null) 👈 핵심
        );

        // 3. AI 서버로 요청
        return aiClient.requestBodyScan(request);
    }
    // -------------------------------------------------------------------------
    // [New] 3. 음식 사진 스캔
    // -------------------------------------------------------------------------
    public Map scanFoodImage(org.springframework.web.multipart.MultipartFile file) {
        // 파일 저장 로직이 필요하다면 여기에 추가 (S3 업로드 등)
        // 지금은 AI 분석 결과만 바로 리턴
        return aiClient.requestFoodScan(file);
    }

    @Transactional
    public void insertSchedule(DietApplyRequestDto requestDto) {
        // 기준일: 오늘
        LocalDate startDate = LocalDate.now();

        // 리스트 순회
        for (DietApplyRequestDto.DietPlanItem item : requestDto.getPlans()) {
            try {
                // 1. 날짜 계산 (Day 1 = 오늘, Day 2 = 내일 ...)
                // day는 1부터 시작하므로 (day - 1)을 더함
                LocalDate targetDate = startDate.plusDays(item.getDay());

                // 2. JSON 컬럼에 넣을 데이터 Map으로 구성
                // (day는 날짜로 변환되었으니 JSON 내용에서는 뺍니다)
                Map<String, Object> jsonMap = new HashMap<>();
                jsonMap.put("menu", item.getMenu());
                jsonMap.put("cal", item.getCal());
                jsonMap.put("difficulty", item.getDifficulty());
                jsonMap.put("quest", item.getQuest());

                // 3. Map -> JSON String 변환
                String jsonString = objectMapper.writeValueAsString(jsonMap);

                // 4. Mapper 호출 (Upsert)
                dietMapper.upsertFoodSchedule(requestDto.getMemberId(), targetDate, jsonString);

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("식단 스케쥴 저장 중 오류 발생");
            }
        }
    }

    @Transactional(readOnly = true)
    public List<DietScheduleResponseDto> getMySchedules(Long memberId) {
        // 1. DB 조회
        List<FoodSchedule> schedules = dietMapper.findSchedulesAfterToday(memberId);

        // 2. Entity -> DTO 변환
        return schedules.stream().map(s -> {
            Map<String, Object> json = new HashMap<>();
            try {
                if (s.getRecommendJson() != null) {
                    json = objectMapper.readValue(s.getRecommendJson(), new TypeReference<Map<String, Object>>() {});
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // [수정 포인트] "menu"는 이제 String이 아니라 Map(객체)입니다.
            // objectMapper.convertValue를 사용하면 Map을 클래스(MenuDetail)로 안전하게 변환해줍니다.
            DietScheduleResponseDto.MenuDetail menuDetail = null;

            try {
                if (json.containsKey("menu")) {
                    menuDetail = objectMapper.convertValue(json.get("menu"), DietScheduleResponseDto.MenuDetail.class);
                }
            } catch (IllegalArgumentException e) {
                // 호환성 처리: 옛날 데이터가 String일 경우 대비 (선택사항)
                menuDetail = DietScheduleResponseDto.MenuDetail.builder()
                        .breakfast("데이터 형식 오류")
                        .build();
            }

            // 퀘스트 및 칼로리 추출
            String quest = (String) json.getOrDefault("quest", "미션 없음");
            int calories = 0;
            if (json.get("cal") instanceof Number) {
                calories = ((Number) json.get("cal")).intValue();
            }

            // Builder 패턴으로 반환
            return DietScheduleResponseDto.builder()
                    .date(s.getDate().toString())
                    .menu(menuDetail) // 객체 그대로 넣기
                    .totalCal(calories)
                    .quest(quest)
                    .build();

        }).collect(Collectors.toList());
    }
    // 월별 조회
    public List<DailyEvaluationDto> getMonthlyEvaluations(Long memberId, int year, int month) {
        String strYear = String.valueOf(year);
        // 1 -> "01"로 변환
        String strMonth = String.format("%02d", month);
        return dietMapper.selectEvaluationsByMonth(memberId, strYear, strMonth);
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
        // 1. 프론트에서 온 최종 음식 리스트로 영양소 총합 계산 (newDiet에 담김)
        Diet newDiet = buildDiet(dto);

        // 2. 해당 날짜/식사시간에 이미 기록된 식단이 있는지 확인
        Diet findDiet = dietMapper.selectDiet(dto.getMemberId(), newDiet.getDate(), dto.getMealTime());

        Long dietId;

        if (findDiet == null) {
            // [신규] 식단 테이블 생성
            dietMapper.insertDiet(dto.getMemberId(), newDiet);
            dietId = newDiet.getId();
        } else {
            // [수정] 기존 식단 ID 확보
            dietId = findDiet.getId();

            // (중요 1) 영양소 정보를 '누적(+)'하지 말고 '최신값(=)'으로 덮어씌움
            dietMapper.updateDietNutrients(dietId, newDiet);

            // (중요 2) 기존에 연결된 음식 매핑 정보를 모두 삭제 (초기화)
            dietMapper.deleteFoodMappings(dietId);
        }

        // 3. (중요 3) 프론트에서 받은 최종 리스트를 새로 저장
        // 신규일 땐 그냥 저장되고, 수정일 땐 삭제 후 저장되므로 결과적으로 '교체'됨
        if (dto.getFoods() != null && !dto.getFoods().isEmpty()) {
            dietMapper.insertFoodMappings(dto.getFoods(), dietId);
        }

        return 1;
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
                .fat(nutritionTotalsDTO.getTotalFat())
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

    private LevelUpResponseDto updateExp(Long memberId, Integer exp){
        Member member = memberMapper.findById(memberId).get();

        int memberExp = member.getExp() + exp;
        int memberLevel = member.getLevel();
        if(memberExp >= 100){
            memberExp %= 100;
            memberLevel += 1;
        }
        memberMapper.updateExp(memberId, memberLevel, memberExp);
        return new LevelUpResponseDto(memberId, memberLevel, memberExp);
    }

}