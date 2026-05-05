package com.liveclass.be_a.domain.enrollment.repository;

import com.liveclass.be_a.domain.enrollment.entity.Enrollment;
import com.liveclass.be_a.domain.enrollment.entity.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 수강신청 레포지토리
 */
public interface EnrollmentRepository {
    public void save(Enrollment enrollment); //수강신청 저장
    public Optional<Enrollment> findById(Long id); //enrollment id로 조회
    public Page<Enrollment> findByMemberId(Long memberId, EnrollmentStatus status, Pageable pageable); //수강신청 찾기 (내 수강 신청 목록 조회를 위해)
    public int countEnrollments(Long courseId); //특정 강좌의 수강신청 카운트(현재 신청 인원 조회를 위해)
    public boolean existsByCourseIdAndMemberId(Long courseId, Long memberId); //중복 확인을 위해 수강신청 존재 유무 체크

    public List<Enrollment> findConfirmedStudents(Long courseId); //수강생 조회를 위한 join 조회
}
