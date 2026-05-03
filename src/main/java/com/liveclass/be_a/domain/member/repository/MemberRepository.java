package com.liveclass.be_a.domain.member.repository;

import com.liveclass.be_a.domain.member.entity.Member;

import java.util.Optional;

public interface MemberRepository {
    public void save(Member member);
    public Optional<Member> findById(Long id);
}
