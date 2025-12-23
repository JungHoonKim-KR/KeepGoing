package keepgoing.demo.domain.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BodyScanResponse {
    // 1. 기본 정보
    private double bmi;
    private String title;       // 예: "수면 부족 좀비" (재미있는 별명)

    // 2. [New] 업그레이드된 분석 정보
    private String healthTier;       // 건강 등급 (입문자, 아마추어, 프로...)
    private List<String> vulnerableParts; // 취약 부위 리스트 (예: ["HEAD", "STOMACH"]) -> 프론트에서 히트맵 표시용
    private String prediction;       // 30일 후 미래 예측

    // 3. 상세 스코어 및 팁
    private HealthScore healthScore; // 5각형 그래프 데이터
    private List<String> tags;       // 태그 (#야식금지 등)
    private String actionTip;        // 한 줄 조언

    // 내부 클래스: 5각형 스탯 점수
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HealthScore {
        private int muscle;      // 근력
        private int endurance;   // 지구력
        private int recovery;    // 회복력
        private int nutrition;   // 영양
        private int metabolism;  // 대사
    }
}