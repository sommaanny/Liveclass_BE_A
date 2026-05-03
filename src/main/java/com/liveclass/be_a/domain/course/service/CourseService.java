package com.liveclass.be_a.domain.course.service;

import com.liveclass.be_a.domain.course.dto.CourseRequestDto;
import com.liveclass.be_a.domain.course.dto.CourseResponseDto;
import com.liveclass.be_a.domain.course.entity.Course;
import com.liveclass.be_a.domain.course.entity.CourseStatus;
import com.liveclass.be_a.domain.course.repository.CourseRepository;
import com.liveclass.be_a.domain.enrollment.repository.EnrollmentRepository;
import com.liveclass.be_a.global.exception.BusinessException;
import com.liveclass.be_a.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 강의 관련 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    //강의 등록, 크리에이터만 가능해야함
    @Transactional
    public Long createCourse(CourseRequestDto courseRequestDto) {
        //간략한 권한 체크(크리에이터인지 확인)
        if (!courseRequestDto.role().equals("CREATOR")) {
            throw new BusinessException(ErrorCode.ROLE_NOT_CREATOR);
        }
        Course course = courseRequestDto.toEntity();
        courseRepository.save(course);

        return course.getId();
    }

    //강의 오픈
    @Transactional
    public Long open(Long courseId, Long creatorId) {
        //id에 해당하는 강의 조회
        Course course = getCourse(courseId);

        //간략한 검증, 강좌를 개설한 강사만 상태를 변결할 수 있음
        if(!creatorId.equals(course.getCreatorId())) {
            throw new BusinessException(ErrorCode.NOT_MATCH_CREATOR);
        }

        //강좌 오픈
        course.openCourse();

        return course.getId();
    }

    //강의 마감
    @Transactional
    public Long close(Long courseId, Long creatorId) {
        //id에 해당하는 강의 조회
        Course course = getCourse(courseId);

        //간략한 검증
        if(!creatorId.equals(course.getCreatorId())) {
            throw new BusinessException(ErrorCode.NOT_MATCH_CREATOR);
        }

        //강좌 마감
        course.closeCourse();

        return course.getId();
    }

    //강의 목록 조회(상태 필터 가능)
    public List<CourseResponseDto> findCourses(CourseStatus status) {
        List<Course> courses = courseRepository.findByStatus(status);
        return courses.stream()
                .map(CourseResponseDto::from)
                .toList();
    }

    //강의 상세 조회
    public CourseResponseDto getCourseDetail(Long courseId) {
        //강의 조회
        Course course = getCourse(courseId);

        //해당 강좌의 수강신청 인원 카운트
        int cnt = enrollmentRepository.countEnrollments(courseId);

        //종합해서 DTO 변환 후 반환
        return CourseResponseDto.from(course, cnt);
    }

    private Course getCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COURSE_NOT_FOUND));
        return course;
    }
}
