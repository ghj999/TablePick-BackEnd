package com.goorm.tablepick.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 웹 관련 설정을 위한 구성 클래스
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")    // 또는 "http://localhost:5173"
                .allowedMethods("GET", "POST", "PATCH", "DELETE", "PUT", "OPTIONS")
                .allowedHeaders("*")
                //.allowedHeaders("Content-Type", "Access-Token")
                //.exposedHeaders("Access-Token")
                //.allowCredentials(true);                  // 주석 친 거는. 이전까지. 아마도 지혜님이 한 부분으로 추정.
                .allowCredentials(false); // Swagger에서는 true ❌
    }

    // Firebase 서비스 워커를 위한 리소스 핸들러 추가
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/firebase-messaging-sw.js")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(0);  // 캐싱 비활성화
    }

    // Firebase 서비스 워커를 위한 필터 빈 추가
//    @Bean
//    public OncePerRequestFilter serviceWorkerHeaderFilter() {
//        return new OncePerRequestFilter() {
//            @Override
//            protected void doFilterInternal(HttpServletRequest request,
//                                            HttpServletResponse response,
//                                            FilterChain filterChain)
//                    throws ServletException, IOException {
//
//                // firebase-messaging-sw.js 요청에 대해서만 헤더 추가
//                if (request.getRequestURI().contains("firebase-messaging-sw.js")) {
//                    response.setHeader("Service-Worker-Allowed", "/");
//                }
//
//                filterChain.doFilter(request, response);
//            }
//        };
//    }
}
