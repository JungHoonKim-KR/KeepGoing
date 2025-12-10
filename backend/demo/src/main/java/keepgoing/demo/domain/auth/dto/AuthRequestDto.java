package keepgoing.demo.domain.auth.dto;

public record AuthRequestDto(
        String email,
        String password,
        String name // 회원가입 때만 씀
) {}