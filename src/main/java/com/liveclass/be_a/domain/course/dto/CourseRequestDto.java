package com.liveclass.be_a.domain.course.dto;

import com.liveclass.be_a.domain.course.entity.Course;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CourseRequestDto(
        @NotBlank(message = "강의 제목은 필수입니다.")
        @Size(max = 100, message = "강의 제목은 100자 이내로 작성해주세요.")
        String title,

        @NotBlank(message = "강의 설명은 필수입니다.")
        @Size(max = 300, message = "강의 설명은 300자 이내로 작성해주세요.")
        String description,

        @NotNull(message = "강의 가격은 필수입니다.")
        @PositiveOrZero(message = "강의 가격은 0원 이상이어야 합니다.")
        Integer price,

        @NotNull(message = "강의 정원은 필수입니다.")
        @Min(value = 1, message = "강의 정원은 1명 이상이어야 합니다.")
        Integer capacity,

        @NotNull(message = "강의 시작 날짜는 필수입니다.")
        @FutureOrPresent(message = "강의 시작 날짜는 현재 또는 미래여야 합니다.")
        LocalDateTime startDate,

        @NotNull(message = "강의 종료 날짜는 필수입니다.")
        @Future(message = "강의 종료 날짜는 미래여야 합니다.")
        LocalDateTime endDate,

        @NotNull(message = "크리에이터 ID는 필수입니다.")
        Long creatorId,

        String role
) {

    //dto -> entity 변환 메서드
    public Course toEntity() {
        return Course.builder()
                .title(title)
                .description(description)
                .price(price)
                .capacity(capacity)
                .startDate(startDate)
                .endDate(endDate)
                .creatorId(creatorId)
                .build();
    }
}
