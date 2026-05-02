package com.liveclass.be_a.domain.course.dto;

import com.liveclass.be_a.domain.course.entity.Course;
import com.liveclass.be_a.domain.course.entity.CourseStatus;

import java.time.LocalDateTime;

public record CourseResponseDto(
        Long id,
        String title,
        String description,
        int price,
        int capacity,
        String status,
        LocalDateTime startDate,
        LocalDateTime endDate,
        boolean isEnrollable //강의 신청이 가능한 상태인지
) {
    //entity -> DTO 변환 메서드
    public static CourseResponseDto from(Course course) {
        return new CourseResponseDto(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getPrice(),
                course.getCapacity(),
                course.getStatus().name(),
                course.getStartDate(),
                course.getEndDate(),
                // 현재 모집 중(OPEN)이면서 종료일이 지나지 않았는지 계산
                course.getStatus() == CourseStatus.OPEN && LocalDateTime.now().isBefore(course.getEndDate())
        );
    }
}
