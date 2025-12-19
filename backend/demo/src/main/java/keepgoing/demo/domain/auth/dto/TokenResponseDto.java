package keepgoing.demo.domain.auth.dto;

public record TokenResponseDto(
        String accessToken,
        String refreshToken, // [추가]
        Long memberId,
        Integer level,
        Integer exp,
        String name
) {}
