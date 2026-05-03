package com.liveclass.be_a.domain.course.repository;

import com.liveclass.be_a.domain.course.entity.Course;
import com.liveclass.be_a.domain.course.entity.CourseStatus;

import java.util.List;
import java.util.Optional;

/**
 * 강의 레포지토리
 */
public interface CourseRepository {
    public void save(Course course); //강의 등록
    public List<Course> findByStatus(CourseStatus status); //강의 목록 조회 (상태 필터 가능)
    public Optional<Course> findById(Long id); //강의 id로 조회
    public Optional<Course> findByIdWithLock(Long id); //동시성 제어를 위해 락을 이용한 조회
}
