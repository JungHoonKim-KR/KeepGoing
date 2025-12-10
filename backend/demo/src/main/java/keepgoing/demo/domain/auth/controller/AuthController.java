package keepgoing.demo.domain.auth.controller;


import keepgoing.demo.domain.auth.dto.AuthRequestDto;
import keepgoing.demo.domain.auth.dto.TokenResponseDto;
import keepgoing.demo.domain.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody AuthRequestDto dto) {
        authService.signup(dto);
        return ResponseEntity.ok("회원가입 성공! 로그인해주세요.");
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody AuthRequestDto dto) {
        return ResponseEntity.ok(authService.login(dto));
    }
    //토큰 재발급
    @PostMapping("/reissue")
    public ResponseEntity<TokenResponseDto> reissue(@RequestBody String refreshToken) {
        // 원래는 DTO로 받는 게 좋지만, 간단하게 String Body로 받음
        // (Postman에서 보낼 때 따옴표 없이 텍스트로 보내거나, JSON {"refreshToken": "..."} 쓰려면 DTO 필요)

        // 여기서는 JSON으로 받기 위해 DTO를 즉석에서 만드는 게 낫습니다.
        // 하지만 편의상 Service가 String만 뱉도록 했으므로,
        // 프론트엔드 편의를 위해 AccessToken만 줘도 되고, 형식을 맞춰줘도 됩니다.

        String newAccessToken = authService.reissue(refreshToken);

        // memberId나 name은 토큰에서 꺼내거나 DB조회해야 하는데,
        // 재발급 땐 보통 accessToken만 주면 됩니다.
        // 일단 TokenResponseDto 형식을 맞추기 위해 null 채워서 보냅니다.
        return ResponseEntity.ok(new TokenResponseDto(newAccessToken, refreshToken, null, null));
    }

    // [추가] 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam Long memberId) {
        authService.logout(memberId);
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }
}