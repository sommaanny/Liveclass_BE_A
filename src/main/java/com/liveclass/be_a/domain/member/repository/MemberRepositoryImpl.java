package com.liveclass.be_a.domain.member.repository;

import com.liveclass.be_a.domain.member.entity.Member;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {
    private final EntityManager em;

    @Override
    public void save(Member member) {
        em.persist(member);
    }

    @Override
    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(em.find(Member.class, id));
    }

    @Override
    public boolean existsById(Long memberId) {
        List<Long> ids = em.createQuery("select m.id from Member m where m.id = :memberId", Long.class)
                .setParameter("memberId", memberId)
                .setFirstResult(0)
                .setMaxResults(1)
                .getResultList();

        return !ids.isEmpty();
    }
}
