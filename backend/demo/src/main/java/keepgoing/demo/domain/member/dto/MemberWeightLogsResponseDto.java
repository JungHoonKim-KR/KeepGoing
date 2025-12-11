package keepgoing.demo.domain.member.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

public record MemberWeightLogsResponseDto (
    List<MemberWeightResponseDto> memberWeightResponseDtos,
    String memo
){}


