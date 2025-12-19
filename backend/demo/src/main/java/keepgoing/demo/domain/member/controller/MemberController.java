package keepgoing.demo.domain.member.controller;

import keepgoing.demo.domain.member.dto.*;
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

    @GetMapping("/weight/logs")
    public ResponseEntity<MemberWeightLogsResponseDto> getMemberWeightLogs(@RequestParam("memberId") Long memberId,
                                                                           @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(memberService.getWeightLogs(memberId, date));
    }

    @PostMapping("/weight")
    public ResponseEntity<Void> updateMyWeight(@RequestBody MemberWeightRequestDto dto) {
        memberService.saveWeight(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/level")
    public ResponseEntity<Void> updateLevel(@RequestBody LevelUpRequest request) {
        // 이제 request.getId()와 request.getScore()로 안전하게 접근 가능합니다.
        memberService.updateExp(request.getId(), request.getScore());
        return ResponseEntity.ok().build();
    }
}