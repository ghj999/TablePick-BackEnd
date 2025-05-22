package com.goorm.tablepick.domain.member.service;

import com.goorm.tablepick.domain.member.dto.GoogleInfo;
import com.goorm.tablepick.domain.member.dto.KakaoInfo;
import com.goorm.tablepick.domain.member.dto.NaverInfo;
import com.goorm.tablepick.domain.member.dto.OAuthInfo;
import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User delegateUser = new DefaultOAuth2UserService().loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId().toLowerCase(); // "google", "kakao", "naver"
        Map<String, Object> attributes = delegateUser.getAttributes();

        OAuthInfo oAuthInfo = createOAuthInfo(registrationId, attributes);
        Member member = memberRepository.findByEmail(oAuthInfo.getEmail())
                .orElseGet(() -> memberRepository.save(oAuthInfo.toEntity()));

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of(
                        "email", member.getEmail(),
                        "id", member.getProviderId()
                ),
                "email"
        );
    }

    private OAuthInfo createOAuthInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "google" -> new GoogleInfo(
                    (String) attributes.get("name"),
                    (String) attributes.get("picture"),
                    (String) attributes.get("email"),
                    (String) attributes.get("sub")
            );

            case "kakao" -> {
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                yield new KakaoInfo(
                        (String) profile.get("nickname"),
                        String.valueOf(attributes.get("id")),
                        (String) profile.get("profile_image_url"),
                        (String) kakaoAccount.get("email")
                );
            }

            case "naver" -> new NaverInfo(attributes);

            default -> throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다: " + registrationId);
        };
    }
}
