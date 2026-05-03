package com.liveclass.be_a.domain.enrollment.repository;

import com.liveclass.be_a.domain.enrollment.entity.Enrollment;
import com.liveclass.be_a.domain.enrollment.entity.EnrollmentStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
@RequiredArgsConstructor
public class EnrollmentRepositoryImpl implements EnrollmentRepository{

    private final EntityManager em;

    @Override
    public void save(Enrollment enrollment) {
        em.persist(enrollment);
    }

    @Override
    public List<Enrollment> findByMemberId(Long memberId) {
        return em.createQuery("select e from Enrollment e where e.member.id = :memberId", Enrollment.class)
                .setParameter("memberId", memberId)
                .getResultList();
    }

    @Override
    public int countEnrollments(Long courseId) {
        Long count = em.createQuery("select count(e) from Enrollment e " +
                "where e.course.id = :courseId and e.status = :status", Long.class)
                .setParameter("courseId", courseId)
                .setParameter("status", EnrollmentStatus.CONFIRMED) //수강 확정된 인원만 카운팅
                .getSingleResult();
        return count.intValue();
    }
}
