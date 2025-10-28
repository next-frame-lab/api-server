package wisoft.nextframe.schedulereservationticketing.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.config.jwt.JwtAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("!loadtest")
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
	private final CustomAccessDeniedHandler customAccessDeniedHandler;

	private static final String[] PERMIT_URLS = {
		"/",
		"/api/v1/performances/**",
		"/api/v1/tickets",
		"/api/v1/auth/**",
		"/v3/api-docs/**",
		"/swagger-ui/**",
		"/swagger-ui.html",
		"/swagger-resources/**"
	};

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
			// CSRF 보호 기능을 비활성화
			.csrf(AbstractHttpConfigurer::disable)

			// CORS 설정 비활성화
			.cors(AbstractHttpConfigurer::disable)

			// 세션을 사용하지 않으므로, STATELESS(상태 비저장)으로 설정(세션 대신 토큰을 사용함).
			.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

			// HTTP 요청에 대한 접근 권한 설정
			.authorizeHttpRequests(authorize -> authorize
				// 모든 사용자가 접근 가능한 경로를 지정(인증이 필요없는 접근 경로)
				.requestMatchers(PERMIT_URLS).permitAll()
				// CORS preflight 요청 허용
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				// 그 외의 모든 요청은 인증된 사용자만 접근할 수 있도록 설정
				.anyRequest().authenticated())

			// 예외 처리 설정
			.exceptionHandling(exceptions -> exceptions
				.authenticationEntryPoint(customAuthenticationEntryPoint)
				.accessDeniedHandler(customAccessDeniedHandler))

			// 인증 필터 적용
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.build();
	}
}
