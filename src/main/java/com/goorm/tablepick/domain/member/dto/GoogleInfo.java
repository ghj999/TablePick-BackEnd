package com.goorm.tablepick.domain.member.dto;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.enums.AccountRole;
import lombok.Getter;

@Getter
public class GoogleInfo implements OAuthInfo {

    private final String provider = "google";
    private final String providerId;
    private final String email;
    private final String nickname;
    private final String profileImage;

    public GoogleInfo(String name, String picture, String email, String sub) {
        this.nickname = name;
        this.email = email;
        this.profileImage = picture;
        this.providerId = sub;
    }

    @Override
    public Member toEntity() {
        return Member.builder()
                .email(this.email)
                .nickname(this.nickname)
                .profileImage(this.profileImage)
                .roles(AccountRole.USER)
                .isMemberDeleted(false)
                .provider(this.provider)
                .providerId(this.providerId)
                .build();
    }
}