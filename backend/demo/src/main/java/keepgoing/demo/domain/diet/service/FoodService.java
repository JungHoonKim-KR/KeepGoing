package keepgoing.demo.domain.diet.service;

import keepgoing.demo.domain.diet.entity.Food;
import keepgoing.demo.domain.diet.mapper.FoodMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FoodService  {
    private final FoodMapper foodMapper;

    public List<Food> selectByName(String partOfName){
        return foodMapper.selectByName(partOfName);
    }

}