package com.liveclass.be_a.domain.course.entity;

/**
 * 강의 상태를 나타내는 enum 클래스
 * DRAFT : 초안(신청 불가)
 * OPEN : 모집 중(신청 가능)
 * CLOSED : 모집 마감(신청 불가)
 */
public enum CourseStatus {
    DRAFT, OPEN, CLOSED
}
