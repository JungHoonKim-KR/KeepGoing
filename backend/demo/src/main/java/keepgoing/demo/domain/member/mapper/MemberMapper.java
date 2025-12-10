package keepgoing.demo.domain.member.mapper;

import keepgoing.demo.domain.member.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import java.util.Optional;

@Mapper
public interface MemberMapper {
    Optional<Member> findById(Long id);
    void update(Member member);
}