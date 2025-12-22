package keepgoing.demo.domain.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty; // JSON 처리를 위해 추가 권장
import java.util.List;

// [수정] 클래스 이름 뒤에 ( )를 열고 필드를 넣어야 합니다.
public record AiRecommendDto(
        @JsonProperty("recommendations") // JSON 키값 매핑 (선택사항이지만 권장)
        List<Recommendation> recommendations
) {

    // 내부 Record는 문법이 맞습니다. 그대로 두시면 됩니다.
    public record Recommendation(
            @JsonProperty("menu")
            String menu,

            @JsonProperty("reason")
            String reason
    ) {}
}