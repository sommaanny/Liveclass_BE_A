package com.liveclass.be_a.domain.member.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 회원 도메인 엔티티
 */
@Getter
@Entity
@Table(name = "members")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    public Member(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 30)
    private String name; //유저 이름

    //간략한 권함 검증을 위한 역할 필드
    @Column
    private String role;
}
