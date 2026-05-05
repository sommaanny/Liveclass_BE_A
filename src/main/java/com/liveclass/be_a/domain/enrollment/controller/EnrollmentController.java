package com.liveclass.be_a.domain.enrollment.controller;

import com.liveclass.be_a.domain.enrollment.dto.EnrollmentRequestDto;
import com.liveclass.be_a.domain.enrollment.dto.EnrollmentResponseDto;
import com.liveclass.be_a.domain.enrollment.dto.StudentResponseDto;
import com.liveclass.be_a.domain.enrollment.entity.EnrollmentStatus;
import com.liveclass.be_a.domain.enrollment.service.EnrollmentService;
import com.liveclass.be_a.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 수강 신청 관련 컨트톨러
 */
@Tag(name = "Enrollment-Controller", description = "수강신청 관련 API 엔드포인트")
@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {
    private final EnrollmentService enrollmentService;

    //수강신청
    @PostMapping
    public ApiResponse<Long> enrollment(@RequestBody EnrollmentRequestDto requestDto) {
        Long enrollmentId = enrollmentService.enrollment(requestDto.courseId(), requestDto.memberId());
        return ApiResponse.success(enrollmentId);
    }

    //결제완료
    @PatchMapping("/{enrollmentId}/payment")
    public ApiResponse<Long> payment(@PathVariable Long enrollmentId) {
        Long paymentId = enrollmentService.payment(enrollmentId);
        return ApiResponse.success(paymentId);
    }

    //수강취소
    @PatchMapping("/{enrollmentId}/cancel")
    public ApiResponse<Long> cancel(@PathVariable Long enrollmentId) {
        Long cancelId = enrollmentService.cancel(enrollmentId);
        return ApiResponse.success(cancelId);
    }

    //재신청
    @PatchMapping("/{enrollmentId}/re-enroll")
    public ApiResponse<Long> reEnroll(@PathVariable Long enrollmentId) {
        Long reEnrollId = enrollmentService.reEnroll(enrollmentId);
        return ApiResponse.success(reEnrollId);
    }

    //내 수강신청 목록 조회(수강신청 상태 필터링 가능)
    @GetMapping("/me")
    public ApiResponse<List<EnrollmentResponseDto>> getMyEnrollments(@RequestParam Long memberId
            , @RequestParam(required = false)EnrollmentStatus status) {

        List<EnrollmentResponseDto> memberEnrollments = enrollmentService.findMemberEnrollments(memberId, status);
        return ApiResponse.success(memberEnrollments);
    }

    @GetMapping("/students")
    public ApiResponse<List<StudentResponseDto>> getConfirmedStudents(@RequestParam Long courseId
            , @RequestParam Long creatorId) {

        List<StudentResponseDto> confirmedStudents = enrollmentService.getConfirmedStudents(courseId, creatorId);
        return ApiResponse.success(confirmedStudents);
    }
}
