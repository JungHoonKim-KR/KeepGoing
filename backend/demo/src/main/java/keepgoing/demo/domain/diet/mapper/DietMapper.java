package keepgoing.demo.domain.diet.mapper;

import keepgoing.demo.domain.diet.entity.Diet;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DietMapper {
    int insert(Diet diet);
}
