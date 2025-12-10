package keepgoing.demo.domain.auth.dto;

public record TokenResponseDto(
        String accessToken,
        Long memberId,
        String name
) {}
