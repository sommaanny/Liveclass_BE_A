package com.liveclass.be_a.domain.enrollment.dto;

import com.liveclass.be_a.domain.enrollment.entity.Enrollment;

public record EnrollmentResponseDto(
        Long enrollmentId,
        Long courseId,
        Long memberId,
        String status
) {
    public static EnrollmentResponseDto from(Enrollment enrollment) {
        return new EnrollmentResponseDto(
                enrollment.getId(),
                enrollment.getCourse().getId(),
                enrollment.getMember().getId(),
                enrollment.getStatus().name()
        );
    }
}
