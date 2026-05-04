package com.liveclass.be_a.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    COURSE_NOT_DRAFT(HttpStatus.CONFLICT, "COURSE_NOT_DRAFT", "Only draft classes can be changed."),
    COURSE_ALREADY_CLOSED(HttpStatus.CONFLICT, "CLASS_ALREADY_CLOSED", "Class is already closed."),
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE_NOT_FOUND", "Course is not founded"),
    COURSE_CAPACITY_FULL(HttpStatus.CONFLICT, "COURSE_CAPACITY_FULL", "Course capacity is already full"),
    COURSE_NOT_OPEN(HttpStatus.CONFLICT, "COURSE_NOT_OPEN", "Course is not opened"),

    ENROLLMENT_NOT_PENDING(HttpStatus.CONFLICT, "ENROLLMENT_NOT_PENDING", "Only pending enrollments can be confirmed."),
    ENROLLMENT_ALREADY_CANCELLED(HttpStatus.CONFLICT, "ENROLLMENT_ALREADY_CANCELLED", "Enrollment is already cancelled."),
    ENROLLMENT_NOT_CANCELLED(HttpStatus.CONFLICT, "ENROLLMENT_NOT_CANCELLED", "ReEnrollment is only possible in a canceled state."),
    ENROLLMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "ENROLLMENT_NOT_FOUND", "Enrollment is not founded"),
    ALREADY_ENROLLED(HttpStatus.CONFLICT, "ALREADY_ENROLLED", "Member are Already enrolled"),

    ROLE_NOT_CREATOR(HttpStatus.UNAUTHORIZED, "ROLE_NOT_CREATOR", "Only creators can create courses."),
    NOT_MATCH_CREATOR(HttpStatus.UNAUTHORIZED, "NOT_MATCh_CREATOR", "creator is not matched"),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "Member is not founded"),
    LOCK_CONFLICT(HttpStatus.CONFLICT, "LOCK_CONFLICT", "Request is currently locked. Please try again later."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Internal server error."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Invalid request.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

}
