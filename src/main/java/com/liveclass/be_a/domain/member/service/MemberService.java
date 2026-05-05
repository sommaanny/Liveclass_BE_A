package com.liveclass.be_a.domain.member.service;

import com.liveclass.be_a.domain.member.entity.Member;
import com.liveclass.be_a.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    @Transactional
    public Long saveMember(String name) {
        Member member = new Member(name);
        memberRepository.save(member);
        return member.getId();
    }
}

