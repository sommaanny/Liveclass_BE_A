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

    //수강신청 인원 카운트
    @Override
    public int countEnrollments(Long courseId) {
        Long count = em.createQuery("select count(e) from Enrollment e " +
                "where e.course.id = :courseId and e.status in (:s1, :s2)", Long.class)
                .setParameter("courseId", courseId)
                .setParameter("s1", EnrollmentStatus.PENDING) //수강 신청한 인원
                .setParameter("s2", EnrollmentStatus.CONFIRMED) //수강 확정된 인원
                .getSingleResult();
        return count.intValue();
    }

    //수강신청 존재 여부 확인
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

    //수강생 목록 조회
    @Override
    public List<Enrollment> findConfirmedStudents(Long courseId) {
        return em.createQuery("select e from Enrollment e join fetch e.member " +
                        "where e.course.id = :courseId and e.status = :status", Enrollment.class)
                .setParameter("courseId", courseId)
                .setParameter("status", EnrollmentStatus.CONFIRMED)
                .getResultList();
    }
}
