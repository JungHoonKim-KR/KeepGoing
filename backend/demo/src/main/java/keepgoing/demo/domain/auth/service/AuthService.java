package keepgoing.demo.domain.auth.service;

import keepgoing.demo.domain.auth.dto.AuthRequestDto;
import keepgoing.demo.domain.auth.dto.TokenResponseDto;
import keepgoing.demo.domain.member.entity.Member;
import keepgoing.demo.domain.member.mapper.MemberMapper;
import keepgoing.demo.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder; // SecurityConfig에서 등록한 BCrypt
    private final JwtTokenProvider jwtTokenProvider;

    // 1. 회원가입
    @Transactional
    public void signup(AuthRequestDto dto) {
        // 이메일 중복 체크
        if (memberMapper.findByEmail(dto.email()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(dto.password());

        // 저장
        Member member = Member.builder()
                .email(dto.email())
                .password(encodedPassword)
                .name(dto.name())
                .role("ROLE_USER")
                .build();

        memberMapper.save(member);
    }

    // 2. 로그인
    public TokenResponseDto login(AuthRequestDto dto) {
        // 회원 조회
        Member member = memberMapper.findByEmail(dto.email())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 비밀번호 검증 (입력비번 vs DB암호화비번)
        if (!passwordEncoder.matches(dto.password(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 토큰 발급
        String accessToken = jwtTokenProvider.createToken(member.getId(), member.getEmail());

        return new TokenResponseDto(accessToken, member.getId(), member.getName());
    }
}
