package com.liveclass.be_a.domain.enrollment.service;

import com.liveclass.be_a.domain.course.entity.Course;
import com.liveclass.be_a.domain.course.entity.CourseStatus;
import com.liveclass.be_a.domain.course.repository.CourseRepository;
import com.liveclass.be_a.domain.enrollment.dto.EnrollmentResponseDto;
import com.liveclass.be_a.domain.enrollment.dto.StudentResponseDto;
import com.liveclass.be_a.domain.enrollment.entity.Enrollment;
import com.liveclass.be_a.domain.enrollment.entity.EnrollmentStatus;
import com.liveclass.be_a.domain.enrollment.repository.EnrollmentRepository;
import com.liveclass.be_a.domain.member.entity.Member;
import com.liveclass.be_a.domain.member.repository.MemberRepository;
import com.liveclass.be_a.global.exception.BusinessException;
import com.liveclass.be_a.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 수강신청 관리 서비스
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final MemberRepository memberRepository;
    private final CourseRepository courseRepository;

    //수강 신청, 강의별 최대 정원을 초과한 신청은 거부(동시성 고려)
    @Transactional
    public Long enrollment(Long courseId, Long memberId) {
        //신청 회원 조회
        Member member = getMember(memberId);

        //강의 조회 with 비관적 락 -> 강의 조회를 순차적으로 진행
        Course course = courseRepository.findByIdWithLock(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        //1. 강의 상태 체크(OPEN일 때만 수강신청 가능)
        if (!course.getStatus().equals(CourseStatus.OPEN)) {
            throw new BusinessException(ErrorCode.COURSE_NOT_OPEN);
        }

        //2. 중복 신청 체크
        if (enrollmentRepository.existsByCourseIdAndMemberId(courseId, memberId)) {
            throw new BusinessException(ErrorCode.ALREADY_ENROLLED);
        }

        //3. 강의의 정원이 초과되지 않았는지 체크
        int currentCount = enrollmentRepository.countEnrollments(courseId); //현재 수강 신청 인원
        if (currentCount >= course.getCapacity()) {
            throw new BusinessException(ErrorCode.COURSE_CAPACITY_FULL); //만석이면 예외 throw
        }

        //수강 신청 엔티티 생성 및 저장
        Enrollment enrollment = Enrollment.createEnrollment(course, member);
        enrollmentRepository.save(enrollment);

        return enrollment.getId();
    }

    //결제 완료
    @Transactional
    public Long payment(Long enrollmentId) {
        //수강신청 조회
        Enrollment enrollment = getEnrollment(enrollmentId);

        //결제 완료, 수강 확정 -> 단순 상태 변경
        enrollment.confirm();

        return enrollment.getId();
    }

    //수강 취소
    @Transactional
    public Long cancel(Long enrollmentId) {
        //수강신청 조회
        Enrollment enrollment = getEnrollment(enrollmentId);

        enrollment.cancel(); //수강 취소

        return enrollment.getId();
    }

    //재신청
    @Transactional
    public Long reEnroll(Long enrollmentId) {
        Enrollment enrollment = getEnrollment(enrollmentId);

        enrollment.reEnroll(); //재신청

        return enrollment.getId();
    }

    //내 수강 신청 목록 조회(수강신청 상태 필터링 가능)
    public Page<EnrollmentResponseDto> findMemberEnrollments(Long memberId, EnrollmentStatus status, Pageable pageable) {
        //회원이 존재하는지 체크
        if (!memberRepository.existsById(memberId)) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        //memberId로 수강신청 조회
        return enrollmentRepository.findByMemberId(memberId, status, pageable)
                .map(EnrollmentResponseDto::from);
    }

    //수강생 목록 조회
    public List<StudentResponseDto> getConfirmedStudents(Long courseId, Long creatorId) {
        //강의 정보 조회
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));

        //강좌 생성자(크리에이터) 확인
        if (!course.getCreatorId().equals(creatorId)) {
            throw new BusinessException(ErrorCode.NOT_MATCH_CREATOR);
        }

        return enrollmentRepository.findConfirmedStudents(courseId)
                .stream()
                .map(StudentResponseDto::from)
                .toList();
    }

    private Enrollment getEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENROLLMENT_NOT_FOUND));
        return enrollment;
    }

    private Member getMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        return member;
    }
}
