package com.liveclass.be_a.domain.course.controller;

import com.liveclass.be_a.domain.course.dto.CourseRequestDto;
import com.liveclass.be_a.domain.course.dto.CourseResponseDto;
import com.liveclass.be_a.domain.course.entity.CourseStatus;
import com.liveclass.be_a.domain.course.service.CourseService;
import com.liveclass.be_a.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 강의 관련 컨트롤러
 */
@Tag(name = "Course-Controller", description = "강의 관련 API 엔드포인트")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/courses")
public class CourseController {
    private final CourseService courseService;

    //강의 등록
    @PostMapping
    public ApiResponse<Long> createCourse(@RequestBody @Valid CourseRequestDto courseRequestDto) {
        Long courseId = courseService.createCourse(courseRequestDto);
        return ApiResponse.success(courseId);
    }

    //강의 오픈
    @PatchMapping("/{courseId}/open")
    public ApiResponse<Long> openCourse(@PathVariable Long courseId, @RequestParam Long creatorId) {
        Long openId = courseService.open(courseId, creatorId);
        return ApiResponse.success(openId);
    }

    //강의 마감
    @PatchMapping("/{courseId}/close")
    public ApiResponse<Long> closeCourse(@PathVariable Long courseId, @RequestParam Long creatorId) {
        Long closeId = courseService.close(courseId, creatorId);
        return ApiResponse.success(closeId);
    }

    //강의 목록 조회 (상태 필터 가능)
    @GetMapping
    public ApiResponse<List<CourseResponseDto>> findCoursesList(@RequestParam(required = false) CourseStatus status) {
        List<CourseResponseDto> courses = courseService.findCourses(status);
        return ApiResponse.success(courses);
    }

    //강의 상세 조회
    @GetMapping("/{courseId}")
    public ApiResponse<CourseResponseDto> getCourseDetail(@PathVariable Long courseId) {
        CourseResponseDto courseDetail = courseService.getCourseDetail(courseId);
        return ApiResponse.success(courseDetail);
    }
}
