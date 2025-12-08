package keepgoing.demo.domain.diet.controller;

import keepgoing.demo.domain.diet.dto.DietInsertRequestDTO;
import keepgoing.demo.domain.diet.entity.Food;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/diet")
@RequiredArgsConstructor
public class DietController {
    /*
    식단 저장 호출
    우선은 회원이 설계되지 않아 음식만 저장(실제로 저장하진 않았음)
     */
    @PostMapping("/meal")
    public ResponseEntity<?> meal(@RequestBody DietInsertRequestDTO dto) {

        // 응답 형태는 임시로 상태코드만 응답
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/water")
    public ResponseEntity<?> water(@RequestBody DietInsertRequestDTO dto) {
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
