package keepgoing.demo.domain.member.mapper;

import keepgoing.demo.domain.member.entity.Member;
import keepgoing.demo.domain.member.entity.WeightLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface MemberMapper {
    Optional<Member> findById(Long id);
    void update(Member member);
    void updateWeight(Long memberId, double weight);
    void insertWeightLog(WeightLog weightLog);
    List<WeightLog> selectWightListByMemberId(Long memberId);
    Optional<Member> findByEmail(String email);
    void save(Member member);
    void updateRefreshToken(Member member);
}