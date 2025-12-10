package keepgoing.demo.domain.member.controller;

import keepgoing.demo.domain.member.dto.MemberResponseDto;
import keepgoing.demo.domain.member.dto.MemberUpdateDto;
import keepgoing.demo.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 1. 내 정보 조회 (메인 화면 로딩 시 호출)
    @GetMapping("/{memberId}")
    public ResponseEntity<MemberResponseDto> getMyProfile(@PathVariable Long memberId) {
        return ResponseEntity.ok(memberService.getMemberProfile(memberId));
    }

    // 2. 내 정보 수정 (설정 페이지에서 호출)
    @PutMapping("/{memberId}")
    public ResponseEntity<Void> updateMyProfile(
            @PathVariable Long memberId,
            @RequestBody MemberUpdateDto dto) {

        memberService.updateMemberProfile(memberId, dto);
        return ResponseEntity.ok().build();
    }
}