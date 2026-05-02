package com.liveclass.be_a.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    COURSE_NOT_DRAFT(HttpStatus.CONFLICT, "COURSE_NOT_DRAFT", "Only draft classes can be changed."),
    COURSE_ALREADY_CLOSED(HttpStatus.CONFLICT, "CLASS_ALREADY_CLOSED", "Class is already closed."),
    ENROLLMENT_NOT_PENDING(HttpStatus.CONFLICT, "ENROLLMENT_NOT_PENDING", "Only pending enrollments can be confirmed."),
    ENROLLMENT_ALREADY_CANCELLED(HttpStatus.CONFLICT, "ENROLLMENT_ALREADY_CANCELLED", "Enrollment is already cancelled."),
    ENROLLMENT_NOT_CANCELLED(HttpStatus.CONFLICT, "ENROLLMENT_NOT_CANCELLED", "ReEnrollment is only possible in a canceled state."),
    ROLE_NOT_CREATOR(HttpStatus.UNAUTHORIZED, "ROLE_NOT_CREATOR", "Only creators can create courses."),
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE_NOT_FOUND", "Course is not founded"),
    NOT_MATCH_CREATOR(HttpStatus.UNAUTHORIZED, "NOT_MATCh_CREATOR", "creator is not matched");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

}
