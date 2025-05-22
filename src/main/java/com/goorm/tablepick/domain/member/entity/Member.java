package com.goorm.tablepick.domain.member.entity;

import com.goorm.tablepick.domain.member.dto.MemberAddtionalInfoRequestDto;
import com.goorm.tablepick.domain.member.dto.MemberUpdateRequestDto;
import com.goorm.tablepick.domain.member.enums.AccountRole;
import com.goorm.tablepick.domain.member.enums.Gender;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class) // ✅ [추가] JPA Auditing 활성화
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nickname;

    @Column(length = 30, nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate birthdate;

    private String phoneNumber;

    private String profileImage;

    @Setter
    private Boolean isMemberDeleted;

    // ✅ [추가] 일반 로그인 비밀번호 필드
    @Column(length = 100)
    private String password;

    @Setter
    @OneToOne
    @JoinColumn(name = "refresh_token_id")
    private RefreshToken refreshToken;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberTag> memberTags;

    @Enumerated(EnumType.STRING)
    private AccountRole roles;

    private String provider;

    private String providerId;

    @Column(length = 255)
    private String fcmToken;

    // ✅ [추가] 생성일, 수정일 자동 관리 필드
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void updateRefreshToken(RefreshToken refreshToken) {
        this.refreshToken = refreshToken;
        refreshToken.setMember(this);
    }

    public Member updateMember(MemberUpdateRequestDto dto) {
        this.nickname = dto.getNickname();
        this.phoneNumber = dto.getPhoneNumber();
        this.gender = dto.getGender();
        this.birthdate = dto.getBirthdate();
        this.profileImage = dto.getProfileImage();
        this.memberTags = dto.getMemberTags();
        return this;
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public void removeFcmToken() {
        this.fcmToken = null;
    }

    public void addMemberInfo(MemberAddtionalInfoRequestDto dto, List<MemberTag> newMemberTags) {
        this.phoneNumber = dto.getPhoneNumber();
        this.gender = dto.getGender();
        this.birthdate = dto.getBirthdate();

        if (this.memberTags == null) {
            this.memberTags = new ArrayList<>();
        } else {
            this.memberTags.clear();
        }

        for (MemberTag tag : newMemberTags) {
            tag.setMember(this);
            this.memberTags.add(tag);
        }
    }
}
