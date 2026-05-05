package com.liveclass.be_a.domain.enrollment.dto;

import com.liveclass.be_a.domain.enrollment.entity.Enrollment;
import com.liveclass.be_a.domain.member.entity.Member;

public record StudentResponseDto(
        Long studentId,
        String name
) {
    public static StudentResponseDto from(Enrollment enrollment) {
        Member member = enrollment.getMember();
        return new StudentResponseDto(member.getId(), member.getName());
    }
}
