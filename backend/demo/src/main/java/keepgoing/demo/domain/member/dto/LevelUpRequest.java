package keepgoing.demo.domain.member.dto;

import lombok.Data;

@Data
public class LevelUpRequest {
    // Getter, Setter가 반드시 있어야 합니다.
    private Long id;
    private Integer score;

    public void setId(Long id) { this.id = id; }

    public void setScore(Integer score) { this.score = score; }
}