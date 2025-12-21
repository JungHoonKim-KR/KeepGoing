package keepgoing.demo.domain.auth.dto;

import lombok.Builder;

@Builder
public record TokenResponseDto(
        String accessToken,
        String refreshToken,
        Long memberId,
        Integer level,
        Integer exp,
        String name,
        Double height,
        Double weight,
        Integer age,
        String gender,
        String activity,
        String goal,
        Double targetWeight,
        Double targetWater,
        Integer profileCharacter
) {}