package keepgoing.demo.domain.diet.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
// 혹시 Python에서 "error" 같은 정의되지 않은 필드를 보내도 에러 안 나게 무시 설정
@JsonIgnoreProperties(ignoreUnknown = true)
public class FoodAnalysisResponseDto {
    @JsonProperty("isFood")
    private Integer isFood;

    // DB 컬럼명과 일치시킨 필드들
    private String code;
    private String name;                // 기존 foodName -> name
    private String dataTypeName;        // 추가됨
    private String middleCategoryName;  // 추가됨

    private Double foodWeight;      // servingSize(숫자) -> foodWeight
    private String servingSize;         // servingSize(문자열)로 변경됨 (예: "1그릇")

    private Double energy;          // kcal -> energy
    private Double water;           // 추가됨
    private Double protein;
    private Double fat;
    private Double carbohydrate;    // carbo -> carbohydrate
    private Double sugars;          // 추가됨
    private Double sodium;          // 추가됨
}