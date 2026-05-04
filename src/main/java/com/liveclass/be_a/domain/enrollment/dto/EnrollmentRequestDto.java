package com.liveclass.be_a.domain.enrollment.dto;

public record EnrollmentRequestDto(
        Long courseId,
        Long memberId
) {
}
