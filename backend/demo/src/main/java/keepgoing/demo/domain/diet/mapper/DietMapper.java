package keepgoing.demo.domain.diet.mapper;

import keepgoing.demo.domain.diet.entity.AiReport;
import keepgoing.demo.domain.diet.entity.Diet;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DietMapper {
    int insert(int memberId, Diet diet);

    // 1. 식단 리스트 저장
    void saveDietList(List<Diet> dietList);

    // 2. AI 리포트 저장
    void saveAiReport(AiReport aiReport);
}
