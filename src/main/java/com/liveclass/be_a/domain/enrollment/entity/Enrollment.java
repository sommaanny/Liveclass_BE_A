package com.liveclass.be_a.domain.enrollment.entity;

import com.liveclass.be_a.domain.course.entity.Course;
import com.liveclass.be_a.domain.member.entity.Member;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 수강신청 도메인 엔티티
 */
@Getter
@Entity
@Table(name = "enrollments")
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
}
