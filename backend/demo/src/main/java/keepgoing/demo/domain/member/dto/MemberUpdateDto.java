package keepgoing.demo.domain.member.dto;

public record MemberUpdateDto(
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
        String healthCondition,
        String allergies,
        String dislikedFood
) {}