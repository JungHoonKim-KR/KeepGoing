package keepgoing.demo.domain.auth.dto;

public record TokenResponseDto(
        String accessToken,
        String refreshToken, // [추가]
        Long memberId,
        String name
) {}
