package com.liveclass.be_a.domain.member.controller;

import com.liveclass.be_a.domain.member.service.MemberService;
import com.liveclass.be_a.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping
    public ApiResponse<Long> saveMember(@RequestParam String name) {
        Long memberId = memberService.saveMember(name);
        return ApiResponse.success(memberId);
    }
}
