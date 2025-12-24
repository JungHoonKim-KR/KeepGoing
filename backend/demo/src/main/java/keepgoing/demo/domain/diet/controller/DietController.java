package keepgoing.demo.domain.diet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import keepgoing.demo.domain.ai.dto.AiAnalyzeDto;
import keepgoing.demo.domain.ai.dto.AiRequestDto; // 만능 DTO import
import keepgoing.demo.domain.diet.dto.*;
import keepgoing.demo.domain.diet.entity.AiReport;
import keepgoing.demo.domain.diet.entity.Diet;
import keepgoing.demo.domain.diet.service.DietService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType; // 추가됨
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // 추가됨

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/diets")
@RequiredArgsConstructor
@Tag(name = "식단 관리", description = "식단 분석 및 AI 추천 관련 API")
public class DietController {
    private final DietService dietService;

    // [기존] 일일 식단 분석 (DB 조회 -> AI 분석)
    @PostMapping("/analyze")
    @Operation(summary = "일일 식단 분석 요청", description = "특정 날짜의 식단 기록을 바탕으로 AI 분석 결과를 반환합니다.")
    public ResponseEntity<AiAnalyzeDto> analyze(
            @RequestParam Long memberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        // 기존 로직 유지 (Service 내부에서 AiClient 호출)
        return ResponseEntity.ok(dietService.analyzeDailyDiet(memberId, date));
    }

    @GetMapping("/report/check")
    @Operation(summary = "기존 분석 결과 확인", description = "이미 해당 날짜에 분석한 결과가 있는지 확인합니다.")
    public ResponseEntity<?> checkExistingReport(
            @RequestParam Long memberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        AiReport report = dietService.getExistingReport(memberId, date);

        if (report != null) {
            // 결과가 있으면 데이터를 보냄
            return ResponseEntity.ok(report);
        } else {
            // 결과가 없으면 204(No Content) 혹은 빈 객체 보냄
            return ResponseEntity.noContent().build();
        }
    }

    // -------------------------------------------------------------------------
    // [New] 1. AI 맞춤형 식단 생성
    // -------------------------------------------------------------------------
    @PostMapping("/generate")
    @Operation(summary = "AI 식단 생성 요청", description = "사용자의 설문 데이터(기간, 선호음식 등)를 기반으로 식단표를 생성합니다.")
    public ResponseEntity<?> generateDietPlan(@RequestBody AiRequestDto request) {
        // Service로 요청 전달 -> Service가 AiClient 호출
        return ResponseEntity.ok(dietService.createDietPlan(request));
    }



    // -------------------------------------------------------------------------
    // [New] 2. RPG 바디 스캔 (캐릭터 분석)
    // -------------------------------------------------------------------------
    @PostMapping("/body-scan")
    @Operation(summary = "RPG 바디 스캔", description = "DB에 저장된 회원의 신체 정보를 바탕으로 RPG 스탯을 분석합니다.")

    public ResponseEntity<?> scanBody(@RequestParam Long memberId) {
        return ResponseEntity.ok(dietService.scanBodyStats(memberId));
    }

    // -------------------------------------------------------------------------
    // [New] 3. 음식 사진 스캔 (이미지 업로드)
    // -------------------------------------------------------------------------
    @PostMapping(value = "/scan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "음식 사진 스캔", description = "음식 사진을 업로드하여 칼로리와 운동량을 분석합니다.")
    public ResponseEntity<?> scanFood(@RequestParam("file") MultipartFile file) {
        // Service로 파일 전달
        return ResponseEntity.ok(dietService.scanFoodImage(file));
    }

    @GetMapping("/schedule")
    public ResponseEntity<List<DietScheduleResponseDto>> getSchedules(@RequestParam Long memberId) {
        List<DietScheduleResponseDto> schedules = dietService.getMySchedules(memberId);
        return ResponseEntity.ok(schedules);
    }

    @PostMapping("/schedule")
    public ResponseEntity<?> scheduleDiet(@RequestBody DietApplyRequestDto dto) {
        dietService.insertSchedule(dto);
        return ResponseEntity.ok().build();
    }



    @GetMapping("/meal-daily")
    public ResponseEntity<Map<String, Diet>> getDailyMeal(@RequestParam("memberId") Long memberId,
                                                          @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Map<String, Diet> dailyMeals = dietService.selectDailyDiet(memberId, date);
        return ResponseEntity.ok(dailyMeals);
    }

    @PostMapping("/meal")
    public ResponseEntity<?> meal(@RequestBody DietInsertRequestDTO dto) {
        dietService.addDiet(dto);
        // 응답 형태는 임시로 상태코드만 응답
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/hydration")
    public ResponseEntity<Double> getHydration(@RequestParam("memberId") Long memberId,
                                               @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date){
        Double hydration = dietService.getHydration(memberId, date);
        return ResponseEntity.ok(hydration);
    }

    @PostMapping("/hydration")
    public ResponseEntity<?> addHydration(@RequestBody WaterInsertRequestDTO dto) {
        dietService.addHydration(dto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/evaluations")
    public ResponseEntity<List<DailyEvaluationDto>> getEvaluations(
            @RequestParam Long memberId,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(dietService.getMonthlyEvaluations(memberId, year, month));
    }



}
