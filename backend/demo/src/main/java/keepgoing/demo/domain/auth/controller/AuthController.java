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
        // 서비스에서 새로운 AccessToken 발급
        String newAccessToken = authService.reissue(refreshToken);

        // Record의 빌더를 사용하여 필요한 정보만 세팅
        // 나머지 memberId, level, ..., profileCharacter는 자동으로 null 처리됨
        TokenResponseDto response = TokenResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();

        return ResponseEntity.ok(response);
    }

    // [추가] 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam Long memberId) {
        authService.logout(memberId);
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }
}