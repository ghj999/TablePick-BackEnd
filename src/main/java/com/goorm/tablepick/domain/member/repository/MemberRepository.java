package com.goorm.tablepick.domain.member.repository;

import com.goorm.tablepick.domain.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    // ✅ [추가] 이메일 존재 여부 확인용
    boolean existsByEmail(String email);
}
