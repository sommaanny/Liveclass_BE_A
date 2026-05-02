package com.liveclass.be_a.domain.course.entity;

import com.liveclass.be_a.global.exception.BusinessException;
import com.liveclass.be_a.global.exception.ErrorCode;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import static com.liveclass.be_a.domain.course.entity.CourseStatus.*;

/*
 *  강의 도메인 엔티티
 */
@Getter
@Entity
@Table(name = "courses")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long id;

    //권한 검증을 간략하게 확인하기 위한 강사ID 필드
    @Column(nullable = false, updatable = false)
    private Long creatorId;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String title; //강의 제목

    @NotBlank

    @Column(nullable = false, length = 300)
    private String description; //강의 설명

    @PositiveOrZero
    @Column(nullable = false)
    private int price; //강의 가격

    @Min(1)
    @Column(nullable = false)
    private int capacity; //강의 정원

    @NotNull
    @FutureOrPresent
    @Column(nullable = false)
    private LocalDateTime startDate; //강의 시작 날짜

    @NotNull
    @Future
    @Column(nullable = false)
    private LocalDateTime endDate; //강의 종료 날짜

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private CourseStatus status; //강의 상태

    @Builder
    public Course(String title, String description, int price, int capacity,
                  LocalDateTime startDate, LocalDateTime endDate) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.capacity = capacity;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = DRAFT;
    }

    //모집 시작
    public void openCourse() {
        //DRAFT 상태인지 체크
        if (status != DRAFT) {
            throw new BusinessException(ErrorCode.COURSE_NOT_DRAFT);
        }
        this.status = OPEN;
    }

    //모집 마감
    public void closeCourse() {
        //이미 마감된 강의인지 확인
        if (status == CLOSED) {
            throw new BusinessException(ErrorCode.COURSE_ALREADY_CLOSED);
        }

        this.status = CLOSED;
    }
}
