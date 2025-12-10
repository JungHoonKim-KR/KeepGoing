package keepgoing.demo.domain.member.dto;

public record MemberUpdateDto(
        Double height,
        Double weight,
        String activity,
        String goal,
        Double targetWeight
) {}