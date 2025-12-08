package keepgoing.demo.domain.diet.controller;

import keepgoing.demo.domain.ai.dto.AiResponseDto;
import keepgoing.demo.domain.diet.service.DietService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/diet")
@RequiredArgsConstructor
public class DietController {
    private final DietService dietService;

    @PostMapping("/analyze")
    public ResponseEntity<AiResponseDto> analyze(
            @RequestParam Long memberId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return ResponseEntity.ok(dietService.analyzeDailyDiet(memberId, date));
    }
}