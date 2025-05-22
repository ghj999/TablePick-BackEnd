package com.goorm.tablepick.global.jwt;

import com.goorm.tablepick.domain.member.entity.Member;
import com.goorm.tablepick.domain.member.entity.RefreshToken;
import com.goorm.tablepick.domain.member.repository.MemberRepository;
import com.goorm.tablepick.domain.member.repository.RefreshTokenRepository;
import com.goorm.tablepick.global.security.CustomUserDetailsService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;
    private final CustomUserDetailsService customUserDetailsService;

    private static final String REDIRECT_URL = "http://localhost:5173/oauth2/success"; // 🔧 [추가]

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        DefaultOAuth2User oAuth2User = (DefaultOAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = extractEmail(attributes); // 🔧 [수정] 여러 플랫폼 대응
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("인증 후 사용자 정보가 없습니다."));

        authenticateUser(member);

        String accessToken = getAccessTokenFromCookie(request);
        String refreshToken = getRefreshTokenFromCookie(request);

        String storedRefreshToken = member.getRefreshToken() != null ? member.getRefreshToken().getToken() : null;

        // 🔧 [변경] 토큰 유효성 검사 및 발급
        if (accessToken == null || !jwtProvider.validateToken(accessToken)) {
            accessToken = jwtProvider.createAccessToken(member.getId(), member.getEmail());
            if (storedRefreshToken == null || !jwtProvider.validateToken(storedRefreshToken)) {
                refreshToken = issueAndSaveRefreshToken(member).getToken();
            }
        }

        // 🔧 [추가] 토큰 쿠키 설정 메서드로 분리
        setTokenCookies(response, accessToken, refreshToken);

        // 🔧 [상수 사용] 리다이렉트
        response.sendRedirect(REDIRECT_URL);
    }

    private String extractEmail(Map<String, Object> attributes) {
        if (attributes.containsKey("kakao_account")) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            return (String) kakaoAccount.get("email");
        } else if (attributes.containsKey("response")) { // 🔧 [추가] for NAVER
            Map<String, Object> naverResponse = (Map<String, Object>) attributes.get("response");
            return (String) naverResponse.get("email");
        } else {
            return (String) attributes.get("email"); // Google 등 기본
        }
    }

    private void authenticateUser(Member member) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(member.getEmail());
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }

    private RefreshToken issueAndSaveRefreshToken(Member member) {
        if (member.getRefreshToken() != null) {
            refreshTokenRepository.delete(member.getRefreshToken());
        }

        String newRefreshToken = jwtProvider.createRefreshToken(member.getId(), member.getEmail());
        RefreshToken refreshToken = RefreshToken.builder()
                .token(newRefreshToken)
                .expiredAt(LocalDateTime.now().plusDays(7))
                .member(member)
                .build();

        member.setRefreshToken(refreshToken);
        return refreshTokenRepository.save(refreshToken);
    }

    private void setTokenCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        Cookie accessCookie = new Cookie("access_token", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(60 * 60); // 1시간

        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7일

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);
    }

    private String getAccessTokenFromCookie(HttpServletRequest request) {
        return getCookieValue(request, "access_token");
    }

    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        return getCookieValue(request, "refresh_token");
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (name.equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }
}
