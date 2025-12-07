package keepgoing.demo.domain.diet.controller;

import keepgoing.demo.domain.diet.dto.DietInsertRequestDTO;
import keepgoing.demo.domain.diet.entity.Diet;
import keepgoing.demo.domain.diet.entity.Food;
import keepgoing.demo.domain.diet.service.FoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/food")
@RequiredArgsConstructor
public class Controller {
    private final FoodService foodService;
    /*
     음식 검색 시 호출
     현재는 300ms 마다 호출
     */
    @GetMapping("/name")
    public ResponseEntity<?> name(@RequestParam("name") String name){
        List<Food> foods = foodService.selectByName(name);
        return new ResponseEntity<>(foods, HttpStatus.OK);
    }

    /*
    식단 저장 호출
    우선은 회원이 설계되지 않아 음식만 저장(실제로 저장하진 않았음)
     */
    @PostMapping("/diet")
    public ResponseEntity<?> diet(@RequestBody DietInsertRequestDTO dto) {
        System.out.println("식사 시간: " + dto.getMealTime());
        System.out.println("음식 목록: " + dto.getFoods().size() + "개");

        for (Food food : dto.getFoods()) {
            System.out.println(food);
        }
        return new ResponseEntity<>(HttpStatus.CREATED);
    }
}
