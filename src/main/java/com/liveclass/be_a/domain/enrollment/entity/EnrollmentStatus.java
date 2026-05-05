package com.liveclass.be_a.domain.enrollment.entity;

/**
 * 수강신청 상태를 나타내는 enum 클래스
 * PENDING : 신청 완료, 결제 대기
 * CONFIRMED : 결제 완료, 수강 확정
 * CANCELLED : 취소됨
 */
public enum EnrollmentStatus {
    PENDING, CONFIRMED, CANCELLED
}
