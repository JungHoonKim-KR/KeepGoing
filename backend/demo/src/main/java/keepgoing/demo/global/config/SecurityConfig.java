package keepgoing.demo.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 1. 비밀번호 암호화용 Bean (BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. 보안 필터 체인 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // REST API는 CSRF 비활성화
                .cors(AbstractHttpConfigurer::disable) // (일단 끔, 나중에 WebConfig랑 연동)
                .authorizeHttpRequests(auth -> auth
                        // 로그인, 회원가입, 스웨거는 누구나 접근 가능
                        .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // 나머지는 인증 필요 (개발 중엔 귀찮으면 .permitAll()로 두기도 함)
                        .anyRequest().permitAll() // 🚨 테스트 편의를 위해 일단 모두 허용 (나중에 .authenticated()로 변경)
                );

        return http.build();
    }
}
