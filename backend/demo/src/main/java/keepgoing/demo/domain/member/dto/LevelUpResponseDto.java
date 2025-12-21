package keepgoing.demo.domain.member.dto;

import lombok.Data;

@Data
public class LevelUpResponseDto {
    private Long id;
    private int level;
    private int exp;

    public LevelUpResponseDto(Long memberId, int memberLevel, int memberExp) {
        this.id = memberId;
        this.level = memberLevel;
        this.exp = memberExp;
    }
}
