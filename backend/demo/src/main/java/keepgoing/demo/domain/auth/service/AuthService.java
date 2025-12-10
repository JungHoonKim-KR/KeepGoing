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
    private final PasswordEncoder passwordEncoder;
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

        // Member 엔티티 생성 (Null Safety 적용)
        Member member = Member.builder()
                .email(dto.email())
                .password(encodedPassword)
                .name(dto.name())
                .role("ROLE_USER")
                // ▼▼▼ [여기 수정] 값이 없으면(null) 기본값(0 또는 "M")을 넣도록 설정 ▼▼▼
                .gender(dto.gender() != null ? dto.gender() : "M")
                .age(dto.age() != null ? dto.age() : 0)          // null이면 0세
                .height(dto.height() != null ? dto.height() : 0.0) // null이면 0.0cm
                .weight(dto.weight() != null ? dto.weight() : 0.0) // null이면 0.0kg
                .targetWeight(dto.targetWeight() != null ? dto.targetWeight() : 0.0)
                .activity(dto.activity() != null ? dto.activity() : "moderate")
                .goal(dto.goal() != null ? dto.goal() : "diet")
                // 문자열은 DTO에서 이미 처리했지만 안전하게 한 번 더
                .healthCondition(dto.healthCondition() != null ? dto.healthCondition() : "없음")
                .allergies(dto.allergies() != null ? dto.allergies() : "없음")
                .dislikedFood(dto.dislikedFood() != null ? dto.dislikedFood() : "없음")
                .build();

        // DB 저장
        memberMapper.save(member);
    }

    // 2. 로그인 (Access + Refresh 발급)
    @Transactional
    public TokenResponseDto login(AuthRequestDto dto) {
        Member member = memberMapper.findByEmail(dto.email())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(dto.password(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 토큰 2개 생성
        String accessToken = jwtTokenProvider.createToken(member.getId(), member.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());

        // 리프레쉬 토큰 DB 저장
        member.updateRefreshToken(refreshToken);
        memberMapper.updateRefreshToken(member);

        return new TokenResponseDto(accessToken, refreshToken, member.getId(), member.getName());
    }

    // 3. 토큰 재발급
    @Transactional(readOnly = true)
    public String reissue(String refreshToken) {
        // 토큰 유효성 체크
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레쉬 토큰입니다.");
        }

        // 유저 조회
        Long memberId = jwtTokenProvider.getMemberIdFromToken(refreshToken);
        Member member = memberMapper.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        // DB 토큰과 일치하는지 확인
        if (member.getRefreshToken() == null || !member.getRefreshToken().equals(refreshToken)) {
            throw new IllegalArgumentException("토큰 정보가 일치하지 않습니다.");
        }

        // 새 Access Token 발급
        return jwtTokenProvider.createToken(member.getId(), member.getEmail());
    }

    //로그아웃 (DB에서 리프레쉬 토큰 삭제)
    @Transactional
    public void logout(Long memberId) {
        Member member = memberMapper.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        // 토큰을 null로 변경
        member.updateRefreshToken(null);
        memberMapper.updateRefreshToken(member);
    }
}