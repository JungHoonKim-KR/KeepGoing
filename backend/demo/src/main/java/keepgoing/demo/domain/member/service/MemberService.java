package keepgoing.demo.domain.member.service;

import keepgoing.demo.domain.member.dto.MemberResponseDto;
import keepgoing.demo.domain.member.dto.MemberUpdateDto;
import keepgoing.demo.domain.member.dto.MemberWeightRequestDto;
import keepgoing.demo.domain.member.dto.MemberWeightResponseDto;
import keepgoing.demo.domain.member.entity.Member;
import keepgoing.demo.domain.member.entity.WeightLog;
import keepgoing.demo.domain.member.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberMapper memberMapper;

    // 회원 정보 조회
    @Transactional(readOnly = true)
    public MemberResponseDto getMemberProfile(Long memberId) {
        Member member = memberMapper.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        // 레벨업 필요 경험치 계산 (예: 레벨 * 100)
        int maxExp = member.getLevel() * 100;

        // 랭크 계산 (간단 예시)
        String rank = (member.getLevel() >= 10) ? "S" : "A";

        return new MemberResponseDto(
                member.getId(),
                member.getName(),
                member.getHeight(),
                member.getWeight(),
                member.getGoal(),
                member.getLevel(),
                member.getExp(),
                maxExp,
                rank
        );
    }
    @Transactional(readOnly = true)
    public MemberWeightResponseDto getWeight(Long memberId, LocalDate date) {
        WeightLog weightLogByMemberId = memberMapper.findWeightLogByMemberId(memberId, date);
        System.out.println(weightLogByMemberId);
        return new MemberWeightResponseDto(weightLogByMemberId.getDate(),  weightLogByMemberId.getWeight(),
                weightLogByMemberId.getDiff(), weightLogByMemberId.getDiff() >= 0);
    }

//    @Transactional(readOnly = true)
//    public List<MemberResponseDto> getWeightLogs(Long memberId, LocalDate date) {
//
//    }
    public void updateWeight(MemberWeightRequestDto dto){
//        memberMapper.
//        memberMapper.updateWeight(dto.getMemberId(), dto.getWeight());
//
        Optional<Member> findMember = memberMapper.findById(dto.getMemberId());
        if(findMember.isEmpty()){
            new NoSuchElementException("멤버 ID " + dto.getMemberId() + "에 해당하는 사용자를 찾을 수 없습니다.");
        }
        Member member = findMember.get();
        double diff = member.getWeight() - dto.getWeight();
        WeightLog.builder()
                .memberId(dto.getMemberId())
                .weight(dto.getWeight())
                .date(dto.getDate())
                .memo(dto.getMemo())
                .build();
//        memberMapper.insertWeightLog();

    }
    @Transactional(readOnly = true)
    public List<WeightLog> getWeightLogs(Long memberId) {
        return memberMapper.selectWightListByMemberId(memberId);
    }

    // 회원 정보 수정
    public void updateMemberProfile(Long memberId, MemberUpdateDto dto) {
        Member member = memberMapper.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 없음"));

        // 엔티티 수정
        member.updateProfile(
                dto.height(),
                dto.weight(),
                dto.activity(),
                dto.goal(),
                dto.targetWeight()
        );

        // DB 업데이트
        memberMapper.update(member);
    }
}