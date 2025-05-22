package com.goorm.tablepick.domain.member.dto;

import com.goorm.tablepick.domain.member.entity.Member;

public interface OAuthInfo {
    String getProvider();
    String getProviderId();
    String getEmail();
    String getNickname();
    String getProfileImage();

    Member toEntity(); // provider 정보 기반으로 Member 생성
}
