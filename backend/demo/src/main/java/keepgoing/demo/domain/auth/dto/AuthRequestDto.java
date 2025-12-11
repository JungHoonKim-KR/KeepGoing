package keepgoing.demo.domain.auth.dto;


public record AuthRequestDto(
        String email,
        String password,
        String name,
        String gender,
        Integer age,
        Double height,
        Double weight,
        Double targetWeight, // 프론트: target_weight -> 백엔드: targetWeight (카멜케이스)
        Double targetWater,
        String activity,
        String goal,
        String healthCondition,
        String allergies,
        String dislikedFood
) {}