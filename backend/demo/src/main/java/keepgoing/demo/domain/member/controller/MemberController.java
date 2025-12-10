package keepgoing.demo.domain.member.controller;

import keepgoing.demo.domain.member.dto.MemberResponseDto;
import keepgoing.demo.domain.member.dto.MemberUpdateDto;
import keepgoing.demo.domain.member.dto.MemberWeightRequestDto;
import keepgoing.demo.domain.member.dto.MemberWeightResponseDto;
import keepgoing.demo.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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

    @GetMapping("/weight")
    public ResponseEntity<MemberWeightResponseDto> getMemberWeight(@RequestParam("memberId") Long memberId,
                                         @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(memberService.getWeight(memberId, date));
    }

    @PutMapping("/weight")
    public ResponseEntity<Void> updateMyWeight(@RequestBody MemberWeightRequestDto dto){
        memberService.updateWeight(dto);
        return ResponseEntity.ok().build();
    }


}