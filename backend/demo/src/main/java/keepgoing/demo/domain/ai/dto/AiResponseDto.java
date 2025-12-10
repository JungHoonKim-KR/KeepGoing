package keepgoing.demo.domain.ai.dto;
import java.util.List;
public record AiResponseDto(
        int score,                  // 점수
        String rank,                // [RPG] 랭크 (S/A/B...)
        String dailyTitle,          // [RPG] 오늘의 칭호
        String oneLineSummary,      // 한줄 총평

        // [RPG] 인사이트 (아이콘, 설명 등)
        List<RpgInsight> insights,

        // [핵심] 여기가 없어서 에러가 났던 겁니다! ▼
        List<Recommendation> recommendations
) {
    // 내부 레코드: RPG 인사이트 구조
    public record RpgInsight(
            String type,        // positive, warning 등
            String iconType,    // sword, skull 등
            String title,       // 제목
            String description  // 내용
    ) {}

    // 내부 레코드: 추천 메뉴 구조
    public record Recommendation(
            String menu,
            String reason
    ) {}
}