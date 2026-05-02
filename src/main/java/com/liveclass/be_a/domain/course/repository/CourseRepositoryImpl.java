package com.liveclass.be_a.domain.course.repository;

import com.liveclass.be_a.domain.course.entity.Course;
import com.liveclass.be_a.domain.course.entity.CourseStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CourseRepositoryImpl implements CourseRepository{

    private final EntityManager em;

    //강의 등록
    @Override
    public void save(Course course) {
        em.persist(course);
    }

    //강의 아이디로 조회
    @Override
    public Optional<Course> findById(Long id) {
        return Optional.ofNullable(em.find(Course.class, id));
    }

    //강의 목록 조회 (상태에 따라 필터 가능)
    @Override
    public List<Course> findByStatus(CourseStatus status) {
        if (status == null) {
            return em.createQuery("select c from Course c", Course.class)
                    .getResultList();
        }

        return em.createQuery("select c from Course c where c.status = :status", Course.class)
                .setParameter("status", status)
                .getResultList();
    }
}
