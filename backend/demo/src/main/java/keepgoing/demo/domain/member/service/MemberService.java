package keepgoing.demo.domain.member.service;

import keepgoing.demo.domain.member.dto.*;
import keepgoing.demo.domain.member.entity.Member;
import keepgoing.demo.domain.member.entity.WeightLog;
import keepgoing.demo.domain.member.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

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

        WeightLog weightLogByMemberId = memberMapper.findWeightByMemberId(memberId, date);

        if (weightLogByMemberId == null) {

            return new MemberWeightResponseDto(date, 0.0, 0.0);
        }
        return new MemberWeightResponseDto(
                weightLogByMemberId.getDate(),
                weightLogByMemberId.getWeight(),
                weightLogByMemberId.getDiff()
        );
    }

    @Transactional(readOnly = true)
    public MemberWeightLogsResponseDto getWeightLogs(Long memberId, LocalDate date) {
        List<WeightLog> weightLogsByMemberId = memberMapper.findWeightLogsByMemberId(memberId, date);

        if (weightLogsByMemberId.isEmpty()) {
            return new MemberWeightLogsResponseDto(new ArrayList<>(), "");
        }

        // 2. 입력 date와 일치하는 로그를 찾습니다.
        Optional<WeightLog> targetLogOptional = weightLogsByMemberId.stream()
                .filter(log -> log.getDate().isEqual(date))
                .findFirst();

        // 3. targetLog의 memo를 추출하고, 목록에서 targetLog를 제외합니다.
        String targetMemo = "";
        List<MemberWeightResponseDto> historyList = new ArrayList<>();

        if (targetLogOptional.isPresent()) {
            WeightLog targetLog = targetLogOptional.get();
            targetMemo = targetLog.getMemo();

            // targetLog를 제외한 나머지 로그들을 historyList에 추가 (스트림 사용)
            historyList = weightLogsByMemberId.stream()
//                    .filter(log -> !log.getDate().iszEqual(date)) // 현재 날짜 기록 제외
                    .map(log -> new MemberWeightResponseDto(log.getDate(), log.getWeight(), log.getDiff()))
                    .collect(Collectors.toList());
        } else {
            // 가져온 5개 로그 전체를 이전 기록 목록으로 사용하고, 메모는 비워둡니다.
            historyList = weightLogsByMemberId.stream()
                    .map(log -> new MemberWeightResponseDto(log.getDate(), log.getWeight(), log.getDiff()))
                    .collect(Collectors.toList());
        }

        return new MemberWeightLogsResponseDto(historyList, targetMemo);
    }

    public void saveWeight(MemberWeightRequestDto dto){
        WeightLog targetLog = memberMapper.findWeightByMemberId(dto.getMemberId(), dto.getDate());

        WeightLog previousLog = memberMapper.findLatestWeightLogBeforeDate(dto.getMemberId(), dto.getDate());

        double diff = 0.0;
        if (previousLog != null) {
            // 이전 기록이 있다면, 새로운 체중과 이전 체중의 차이를 계산
            // DECIMAL 타입이므로 Double로 변환하여 계산
            diff = dto.getWeight() - previousLog.getWeight().doubleValue();
        }

        if(targetLog != null){
            targetLog.updateWeightLog(dto.getWeight(), diff, dto.getMemo());
            memberMapper.updateWeightLog(targetLog);
        }
        else{
            WeightLog newLog = WeightLog.builder()
                    .memberId(dto.getMemberId())
                    .weight(dto.getWeight())
                    .diff(diff)
                    .date(dto.getDate())
                    .memo(dto.getMemo())
                    .build();
            memberMapper.insertWeightLog(newLog);
        }

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