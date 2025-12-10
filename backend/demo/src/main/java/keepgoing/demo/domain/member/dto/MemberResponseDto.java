package keepgoing.demo.domain.member.dto;

public record MemberResponseDto(
        Long id,
        String name,

        // 신체 정보
        Double height,
        Double weight,
        String goal,

        // 게임 정보 (화면 상단에 표시될 레벨/경험치)
        int level,
        int exp,
        int maxExp, // 다음 레벨까지 필요한 경험치 (계산해서 내려줌)
        String rank // "S", "A" 등 (로직으로 계산)
) {}
