package com.liveclass.be_a.domain.enrollment.entity;

import com.liveclass.be_a.domain.course.entity.Course;
import com.liveclass.be_a.domain.member.entity.Member;
import com.liveclass.be_a.global.exception.BusinessException;
import com.liveclass.be_a.global.exception.ErrorCode;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import static com.liveclass.be_a.domain.enrollment.entity.EnrollmentStatus.CANCELLED;
import static com.liveclass.be_a.domain.enrollment.entity.EnrollmentStatus.PENDING;

/**
 * 수강신청 도메인 엔티티
 * 회원이 같은 강좌를 중복 수강신청하지 못하도록 유니크 제약조건 설정
 */
@Getter
@Entity
@Table(name = "enrollments",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_enrollment_course_member", columnNames = {"course_id", "member_id"})
        },
        indexes = {
            @Index(name = "idx_course_status", columnList = "course_id, status")
        })
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course; //강의

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; //유저

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private EnrollmentStatus status; //수강신청 상태

    @Column
    private LocalDateTime confirmedAt;

    //강의 생성
    public static Enrollment createEnrollment(Course course, Member member) {
        //수강신청 객체 생성
        Enrollment enrollment = new Enrollment();

        //초기 세팅
        enrollment.course = course;
        enrollment.member = member;
        enrollment.status = EnrollmentStatus.PENDING;

        return enrollment;
    }

    public void confirm() {
        //결제 완료, 수강 확정
        //PENDING 상태인지 체크
        if (status != PENDING) {
            throw new BusinessException(ErrorCode.ENROLLMENT_NOT_PENDING);
        }

        //결제 완료 시각 기록
        this.confirmedAt = LocalDateTime.now();
        this.status = EnrollmentStatus.CONFIRMED;
    }

    //수강 신청 취소
    public void cancel() {
        //이미 취소된 신청인지 확인
        if(status == CANCELLED) {
            throw new BusinessException(ErrorCode.ENROLLMENT_ALREADY_CANCELLED);
        }

        //결제 확정 후 7일이 지났는지 확인
        if (confirmedAt != null && confirmedAt.plusDays(7).isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.ENROLLMENT_CANCEL_PERIOD_EXPIRED);
        }

        this.status = CANCELLED;
    }

    //취소 후 재신청
    public void reEnroll() {
        // 캔슬 상태가 아니면 재신청 불가
        if (this.status != CANCELLED) {
            throw new BusinessException(ErrorCode.ENROLLMENT_NOT_CANCELLED);
        }

        //결제 완료 시간 갱신
        this.confirmedAt = null;

        this.status = PENDING;
    }
}
