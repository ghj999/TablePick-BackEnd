package com.goorm.tablepick.domain.member.dto;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.enums.AccountRole;
import lombok.Getter;

@Getter
public class KakaoInfo implements OAuthInfo {

    private final String provider = "kakao";
    private final String providerId;
    private final String email;
    private final String nickname;
    private final String profileImage;

    public KakaoInfo(String nickname, String providerId, String profileImage, String email) {
        this.providerId = providerId;
        this.email = email;
        this.nickname = nickname;
        this.profileImage = profileImage;
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
