package keepgoing.demo.domain.diet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import keepgoing.demo.domain.ai.dto.AiResponseDto;
import keepgoing.demo.domain.diet.dto.DietInsertRequestDTO;
import keepgoing.demo.domain.diet.dto.WaterInsertRequestDTO;
import keepgoing.demo.domain.diet.entity.Diet;
import keepgoing.demo.domain.diet.service.DietService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/diets")
@RequiredArgsConstructor
@Tag(name = "식단 관리", description = "식단 분석 및 AI 추천 관련 API")
public class DietController {
    private final DietService dietService;

    @PostMapping("/analyze")
    @Operation(summary = "일일 식단 분석 요청", description = "특정 날짜의 식단 기록을 바탕으로 AI 분석 결과를 반환합니다.") // 메서드 설명
    public ResponseEntity<AiResponseDto> analyze(
            @RequestParam Long memberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(dietService.analyzeDailyDiet(memberId, date));
    }


    @GetMapping("/meal")
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

    @PostMapping("/hydration")
    public ResponseEntity<?> hydration(@RequestBody WaterInsertRequestDTO dto) {
        dietService.addHydration(dto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
