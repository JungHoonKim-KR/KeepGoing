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
        if (memberMapper.findByEmail(dto.email()).isPresent()) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(dto.password());

        Member member = Member.builder()
                .email(dto.email())
                .password(encodedPassword)
                .name(dto.name())
                .role("USER") // 기본 권한 설정
                .gender(dto.gender() != null ? dto.gender() : "M")
                .age(dto.age() != null ? dto.age() : 0)
                .height(dto.height() != null ? dto.height() : 0.0)
                .weight(dto.weight() != null ? dto.weight() : 0.0)

                // ▼▼▼ [수정] 목표 체중 & 목표 물 섭취량 (Null 방지) ▼▼▼
                .targetWeight(dto.targetWeight() != null ? dto.targetWeight() : 0.0)
                .targetWater(dto.targetWater() != null ? dto.targetWater() : 2.0) // 기본 2.0리터
                // ▲▲▲ [여기까지] ▲▲▲

                .activity(dto.activity() != null ? dto.activity() : "moderate")
                .goal(dto.goal() != null ? dto.goal() : "diet")
                .healthCondition(dto.healthCondition() != null ? dto.healthCondition() : "없음")
                .allergies(dto.allergies() != null ? dto.allergies() : "없음")
                .dislikedFood(dto.dislikedFood() != null ? dto.dislikedFood() : "없음")

                // 게이미피케이션 초기값 (빌더에 없으면 0으로 들어가겠지만 명시적으로)
                .level(1)
                .exp(0)
                .currentPoints(0)
                .build();

        memberMapper.save(member);
    }

    // 2. 로그인 (Access + Refresh + Profile Data 발급)
    @Transactional
    public TokenResponseDto login(AuthRequestDto dto) {
        Member member = memberMapper.findByEmail(dto.email())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(dto.password(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 토큰 생성
        String accessToken = jwtTokenProvider.createToken(member.getId(), member.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());

        // 리프레쉬 토큰 DB 저장
        member.updateRefreshToken(refreshToken);
        memberMapper.updateRefreshToken(member);

        // [수정] 모든 정보를 DTO에 담아서 반환한다.
        return new TokenResponseDto(
                accessToken,
                refreshToken,
                member.getId(),
                member.getLevel(),
                member.getExp(),
                member.getName(),
                member.getHeight(),
                member.getWeight(),
                member.getAge(),
                member.getGender(),
                member.getActivity(),
                member.getGoal(),
                member.getTargetWeight(),
                member.getTargetWater(),
                member.getHealthCondition(),
                member.getAllergies(),
                member.getDislikedFood(),
                member.getProfileCharacter()
        );
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