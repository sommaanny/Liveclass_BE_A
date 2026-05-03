package com.liveclass.be_a.domain.course.service;

import com.liveclass.be_a.domain.course.dto.CourseRequestDto;
import com.liveclass.be_a.domain.course.dto.CourseResponseDto;
import com.liveclass.be_a.domain.course.entity.Course;
import com.liveclass.be_a.domain.course.entity.CourseStatus;
import com.liveclass.be_a.domain.course.repository.CourseRepository;
import com.liveclass.be_a.global.exception.BusinessException;
import com.liveclass.be_a.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional //테스트 완료 후 자동 롤백
@SpringBootTest
class CourseServiceTest {

    private static final Long CREATOR_ID = 1L;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("강의 생성 성공 - 크리에이터일 경우")
    void createCourse() {
        //given
        CourseRequestDto request = createCourseRequest("스프링 입문", CREATOR_ID, "CREATOR");

        //when
        courseService.createCourse(request);
        em.flush();

        //then
        List<CourseResponseDto> courses = courseService.findCourses(null);
        assertThat(courses).hasSize(1);
        assertThat(courses.get(0).title()).isEqualTo("스프링 입문");
        assertThat(courses.get(0).status()).isEqualTo(CourseStatus.DRAFT.name());
    }

    @Test
    @DisplayName("강의 생성 실패 - 크리에이터가 아닐 경우")
    void createCourseFail() {
        //given
        CourseRequestDto request = createCourseRequest("스프링 입문", CREATOR_ID, "USER");

        //when
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            courseService.createCourse(request);
        });

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ROLE_NOT_CREATOR);
    }

    @Test
    @DisplayName("강의 OPEN(모집중) 성공 - 해당 강좌의 크리에이터일 경우")
    void open() {
        Course course = saveCourse("JPA 기본편", CREATOR_ID);

        courseService.open(course.getId(), CREATOR_ID);

        assertThat(course.getStatus()).isEqualTo(CourseStatus.OPEN);
    }

    @Test
    @DisplayName("강의 OPEN 실패 - 강의를 찾을 수 없을 경우")
    void openFailWhenCourseNotFound() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            courseService.open(999L, CREATOR_ID);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COURSE_NOT_FOUND);
    }

    @Test
    @DisplayName("강의 OPEN 실패 - 크리에이터가 일치하지 않을 경우")
    void openFailWhenCreatorNotMatched() {
        Course course = saveCourse("JPA 기본편", CREATOR_ID);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            courseService.open(course.getId(), 2L);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_MATCH_CREATOR);
    }

    @Test
    @DisplayName("강의 OPEN 실패 - DRAFT 상태가 아닐 경우")
    void openFailWhenCourseIsNotDraft() {
        Course course = saveCourse("JPA 기본편", CREATOR_ID);
        course.openCourse();

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            courseService.open(course.getId(), CREATOR_ID);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COURSE_NOT_DRAFT);
    }

    @Test
    @DisplayName("강의 CLOSED(모집 마감) 성공 - 해당 강좌의 크리에이터일 경우")
    void close() {
        Course course = saveCourse("HTTP 완벽 가이드", CREATOR_ID);

        courseService.close(course.getId(), CREATOR_ID);

        assertThat(course.getStatus()).isEqualTo(CourseStatus.CLOSED);
    }

    @Test
    @DisplayName("강의 CLOSE 실패 - 강의를 찾을 수 없을 경우")
    void closeFailWhenCourseNotFound() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            courseService.close(999L, CREATOR_ID);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COURSE_NOT_FOUND);
    }

    @Test
    @DisplayName("강의 CLOSE 실패 - 크리에이터가 일치하지 않을 경우")
    void closeFailWhenCreatorNotMatched() {
        Course course = saveCourse("HTTP 완벽 가이드", CREATOR_ID);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            courseService.close(course.getId(), 2L);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NOT_MATCH_CREATOR);
    }

    @Test
    @DisplayName("강의 CLOSE 실패 - 이미 마감된 강의일 경우")
    void closeFailWhenAlreadyClosed() {
        Course course = saveCourse("HTTP 완벽 가이드", CREATOR_ID);
        course.closeCourse();

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            courseService.close(course.getId(), CREATOR_ID);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COURSE_ALREADY_CLOSED);
    }

    @Test
    @DisplayName("강의 목록 조회 성공 - 상태 필터 가능")
    void findCourses() {
        saveCourse("초안 강의", CREATOR_ID);
        Course openCourse = saveCourse("모집중 강의", CREATOR_ID);
        openCourse.openCourse();
        Course closedCourse = saveCourse("마감 강의", CREATOR_ID);
        closedCourse.closeCourse();

        List<CourseResponseDto> allCourses = courseService.findCourses(null);
        List<CourseResponseDto> openCourses = courseService.findCourses(CourseStatus.OPEN);
        List<CourseResponseDto> closedCourses = courseService.findCourses(CourseStatus.CLOSED);

        assertThat(allCourses).hasSize(3);
        assertThat(openCourses).hasSize(1);
        assertThat(closedCourses).hasSize(1);
    }

    @Test
    @DisplayName("강의 상세 조회 성공 - 현재 신청 인원 포함")
    void getCourseDetail() {
        Course course = saveCourse("상세 조회 강의", CREATOR_ID);
        course.openCourse();

        //JPA 캐시 삭제
        em.flush();
        em.clear();

        CourseResponseDto response = courseService.getCourseDetail(course.getId());

        assertThat(response.id()).isEqualTo(course.getId());
        assertThat(response.title()).isEqualTo("상세 조회 강의");
        assertThat(response.currentCount()).isZero();
        assertThat(response.isEnrollable()).isTrue();
    }

    @Test
    @DisplayName("강의 상세 조회 실패 - 강의를 찾을 수 없을 경우")
    void getCourseDetailFailWhenCourseNotFound() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            courseService.getCourseDetail(999L);
        });

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.COURSE_NOT_FOUND);
    }

    private Course saveCourse(String title, Long creatorId) {
        Course course = Course.builder()
                .title(title)
                .description("강의 설명")
                .price(10_000)
                .capacity(30)
                .creatorId(creatorId)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(30))
                .build();
        courseRepository.save(course);
        return course;
    }

    private CourseRequestDto createCourseRequest(String title, Long creatorId, String role) {
        return new CourseRequestDto(
                title,
                "강의 설명",
                10_000,
                30,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(30),
                creatorId,
                role
        );
    }
}
