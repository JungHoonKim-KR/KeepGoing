package keepgoing.demo.domain.member.dto;

import lombok.Data;

@Data
public class CharacterUpdateRequestDto {
    private Long memberId;
    private Integer characterNumber;
}
