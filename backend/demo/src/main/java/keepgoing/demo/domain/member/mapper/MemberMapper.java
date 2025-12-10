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
    void updateWeight(Long memberId, double weight);
    void insertWeightLog(WeightLog weightLog);

    WeightLog findWeightLogByMemberId(Long memberId, LocalDate date);
    List<WeightLog> findWeightLogsByMemberId(Long memberId);
    List<WeightLog> selectWightListByMemberId(Long memberId);
    Optional<Member> findByEmail(String email);
    void save(Member member);
    void updateRefreshToken(Member member);
}