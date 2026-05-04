package com.liveclass.be_a.domain.enrollment.repository;

import com.liveclass.be_a.domain.enrollment.entity.Enrollment;
import com.liveclass.be_a.domain.enrollment.entity.EnrollmentStatus;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class EnrollmentRepositoryImpl implements EnrollmentRepository{

    private final EntityManager em;

    @Override
    public void save(Enrollment enrollment) {
        em.persist(enrollment);
    }

    @Override
    public Optional<Enrollment> findById(Long id) {
        return Optional.ofNullable(em.find(Enrollment.class, id));
    }

    @Override
    public List<Enrollment> findByMemberId(Long memberId, EnrollmentStatus status) {
        if (status == null) {
            return em.createQuery("select e from Enrollment e where e.member.id = :memberId", Enrollment.class)
                    .setParameter("memberId", memberId)
                    .getResultList();
        }

        return em.createQuery("select e from Enrollment e where e.member.id = :memberId and e.status = :status", Enrollment.class)
                .setParameter("memberId", memberId)
                .setParameter("status", status)
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

    @Override
    public boolean existsByCourseIdAndMemberId(Long courseId, Long memberId) {
        List<Long> ids = em.createQuery("select e.id from Enrollment e " +
                        "where e.course.id = :courseId " +
                        "and e.member.id = :memberId", Long.class)
                .setParameter("courseId", courseId)
                .setParameter("memberId", memberId)
                .setFirstResult(0)
                .setMaxResults(1)
                .getResultList();

        return !ids.isEmpty();
    }
}
