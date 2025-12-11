package keepgoing.demo.domain.member.mapper;

import keepgoing.demo.domain.member.entity.Member;
import keepgoing.demo.domain.member.entity.WeightLog;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Mapper
public interface MemberMapper {
    Optional<Member> findById(Long id);
    void update(Member member);

    // 2. 삽입: 새로운 기록 추가
    void insertWeightLog(WeightLog log);
    // 3. 수정: 기존 기록의 weight, diff, memo 업데이트
    void updateWeightLog(WeightLog log);

    WeightLog findWeightByMemberId(Long memberId, LocalDate date);
    // 입력 날짜 이전의 가장 최신 기록 1개 조회 (Diff 계산용)
    WeightLog findLatestWeightLogBeforeDate(Long memberId, LocalDate date);
    List<WeightLog> findWeightLogsByMemberId(Long memberId, LocalDate date);
    Optional<Member> findByEmail(String email);
    void save(Member member);
    void updateRefreshToken(Member member);
}