package keepgoing.demo.domain.member.dto;

import lombok.Data;
import lombok.Setter;

@Data
public class LevelUpRequest {
    // Getter, Setter가 반드시 있어야 합니다.
    @Setter
    private Long id;
    private Integer score;

}