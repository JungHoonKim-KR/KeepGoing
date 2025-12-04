package keepgoing.demo.domain.diet.mapper;

import keepgoing.demo.domain.diet.entity.Food;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FoodMapper {
    List<Food>selectAll();
}