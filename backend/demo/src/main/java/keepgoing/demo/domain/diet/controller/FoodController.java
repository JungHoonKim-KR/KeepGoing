package keepgoing.demo.domain.diet.controller;

import keepgoing.demo.domain.diet.entity.Food;
import keepgoing.demo.domain.diet.service.FoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/food")
@RequiredArgsConstructor
public class FoodController {
    private final FoodService foodService;
    /*
     음식 검색 시 호출
     현재는 300ms 마다 호출
     */
    @GetMapping("")
    public ResponseEntity<?> name(@RequestParam("foodName") String foodName){
        List<Food> foods = foodService.selectByName(foodName);
        return new ResponseEntity<>(foods, HttpStatus.OK);
    }
}
