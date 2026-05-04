package com.liveclass.be_a.domain.enrollment.service;

import com.liveclass.be_a.domain.course.entity.Course;
import com.liveclass.be_a.domain.course.entity.CourseStatus;
import com.liveclass.be_a.domain.course.repository.CourseRepository;
import com.liveclass.be_a.domain.enrollment.dto.EnrollmentResponseDto;
import com.liveclass.be_a.domain.enrollment.entity.Enrollment;
import com.liveclass.be_a.domain.enrollment.entity.EnrollmentStatus;
import com.liveclass.be_a.domain.enrollment.repository.EnrollmentRepository;
import com.liveclass.be_a.domain.member.entity.Member;
import com.liveclass.be_a.domain.member.repository.MemberRepository;
import com.liveclass.be_a.global.exception.BusinessException;
import com.liveclass.be_a.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class EnrollmentServiceTest {

    @Autowired
    EnrollmentService enrollmentService;
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    EnrollmentRepository enrollmentRepository;
    @Autowired
    EntityManager em;

    private Long courseId;
    private Long memberId;

    @BeforeEach
    void setUp() {
        // 테스트용 유저 생성
        Member member = new Member("홍길동");
        memberRepository.save(member);
        memberId = member.getId();

        // 테스트용 강의 생성 (정원 1명)
        Course course = Course.builder()
                .creatorId(100L)
                .title("테스트 강의")
                .description("테스트 강의입니다.")
                .price(10_000)
                .capacity(1)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .build();

        courseRepository.save(course);
        courseId = course.getId();
    }

    /**
     * 1. 수강 신청 (enrollment) 테스트
     */
    @Test
    @DisplayName("수강 신청 성공")
    void enrollment_success() {
        // Given
        openCourse();

        // When
        Long id = enrollmentService.enrollment(courseId, memberId);
        em.flush();

        // Then
        Enrollment e = enrollmentRepository.findById(id).get();
        assertThat(e.getCourse().getId()).isEqualTo(courseId);
        assertThat(e.getMember().getId()).isEqualTo(memberId);
    }

    @Test
    @DisplayName("수강 신청 실패 - 강의가 OPEN 상태가 아님")
    void enrollment_fail_not_open() {
        // Given: 강의가 DRAFT 상태 (openCourse 미호출)

        // When & Then
        BusinessException e = assertThrows(BusinessException.class,
                () -> enrollmentService.enrollment(courseId, memberId));
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.COURSE_NOT_OPEN);
    }

    @Test
    @DisplayName("수강 신청 실패 - 같은 강의 중복 신청")
    void enrollment_fail_duplicate() {
        // Given
        openCourse();
        enrollmentService.enrollment(courseId, memberId); //수강 신청

        // When & Then
        BusinessException e = assertThrows(BusinessException.class,
                () -> enrollmentService.enrollment(courseId, memberId)); //중복 신청
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.ALREADY_ENROLLED);
    }

    @Test
    @DisplayName("수강 신청 실패 - 정원 초과")
    void enrollment_fail_capacity_full() {
        // Given
        openCourse();
        enrollmentService.enrollment(courseId, memberId);// 이미 1명 신청 (정원 1명)

        // 다른 유저 생성
        Member anotherMember = new Member("둘리");
        memberRepository.save(anotherMember);

        // When & Then
        BusinessException e = assertThrows(BusinessException.class,
                () -> enrollmentService.enrollment(courseId, anotherMember.getId()));
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.COURSE_CAPACITY_FULL);
    }

    /**
     * 2. 결제 완료 (payment) 테스트
     */
    @Test
    @DisplayName("결제 확인 성공 - 상태가 CONFIRMED로 변경됨")
    void payment_success() {
        // Given
        openCourse();
        Long enrollmentId = enrollmentService.enrollment(courseId, memberId);

        // When
        enrollmentService.payment(enrollmentId);
        em.flush();
        em.clear();

        // Then
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).get();
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.CONFIRMED);
    }

    @Test
    @DisplayName("결제 확인 실패 - PENDING 상태가 아닌 상황")
    void payment_fail() {
        // Given
        openCourse();
        Long enrollmentId = enrollmentService.enrollment(courseId, memberId);

        // When
        enrollmentService.payment(enrollmentId);
        em.flush();
        em.clear();

        // Then
        BusinessException e = assertThrows(BusinessException.class,
                () -> enrollmentService.payment(enrollmentId));
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.ENROLLMENT_NOT_PENDING);
    }

    /**
     * 3. 수강 취소 (cancel) 테스트
     */
    @Test
    @DisplayName("수강 취소 성공")
    void cancel_success() {
        // Given
        openCourse();
        Long enrollmentId = enrollmentService.enrollment(courseId, memberId);

        // When
        enrollmentService.cancel(enrollmentId);
        em.flush();
        em.clear();

        // Then
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).get();
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.CANCELLED);
    }

    @Test
    @DisplayName("수강 취소 실패 - 이미 취소한 수강신청")
    void cancel_fail() {
        // Given
        openCourse();
        Long enrollmentId = enrollmentService.enrollment(courseId, memberId);

        // When
        enrollmentService.cancel(enrollmentId);
        em.flush();
        em.clear();

        // Then
        BusinessException e = assertThrows(BusinessException.class,
                () -> enrollmentService.cancel(enrollmentId));
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.ENROLLMENT_ALREADY_CANCELLED);
    }

    /**
     * 4. 재신청 (reEnroll) 테스트
     */
    @Test
    @DisplayName("재신청 성공")
    void reEnroll_success() {
        // Given: 신청 후 취소된 상태
        openCourse();
        Long enrollmentId = enrollmentService.enrollment(courseId, memberId);
        enrollmentService.cancel(enrollmentId);

        // When
        enrollmentService.reEnroll(enrollmentId);
        em.flush();
        em.clear();

        // Then
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).get();
        assertThat(enrollment.getStatus()).isNotEqualTo(EnrollmentStatus.CANCELLED);
    }

    @Test
    @DisplayName("재신청 실패 - 취소 상태가 아닐 때")
    void reEnroll_fail() {
        // Given: 신청 후 취소된 상태
        openCourse();
        Long enrollmentId = enrollmentService.enrollment(courseId, memberId);

        // When & Then
        BusinessException e = assertThrows(BusinessException.class,
                () -> enrollmentService.reEnroll(enrollmentId));
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.ENROLLMENT_NOT_CANCELLED);
    }

    /**
     * 5. 목록 조회 테스트
     */
    @Test
    @DisplayName("내 수강 신청 목록 조회 성공")
    void findMemberEnrollments_success() {
        //given
        openCourse();
        enrollmentService.enrollment(courseId, memberId);

        // When & Then
        List<EnrollmentResponseDto> memberEnrollments = enrollmentService.findMemberEnrollments(memberId, null);

        assertThat(memberEnrollments.size()).isEqualTo(1);
        assertThat(memberEnrollments.get(0).memberId()).isEqualTo(memberId);
        assertThat(memberEnrollments.get(0).courseId()).isEqualTo(courseId);
    }

    @Test
    @DisplayName("내 수강 신청 목록 조회 실패 - 존재하지 않는 회원")
    void findMemberEnrollments_fail_member_not_found() {
        // When & Then
        BusinessException e = assertThrows(BusinessException.class,
                () -> enrollmentService.findMemberEnrollments(9999L, null));
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
    }

    // 편의 메서드: 강의 상태 OPEN으로 변경
    private void openCourse() {
        Course course = courseRepository.findById(courseId).get();
        course.openCourse(); // DRAFT -> OPEN
        em.flush();
    }
}