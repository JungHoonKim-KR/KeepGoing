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
public class Controller {
    private final FoodService foodService;
    @GetMapping("")
    public ResponseEntity<?> test(){
        List<Food> foods = foodService.selectAll();
        return new ResponseEntity<>(foods, HttpStatus.OK);
    }

    @GetMapping("/name")
    public ResponseEntity<?> name(@RequestParam("name") String name){
        List<Food> foods = foodService.selectByName(name);
        return new ResponseEntity<>(foods, HttpStatus.OK);
    }
}
