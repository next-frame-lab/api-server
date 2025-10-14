package wisoft.nextframe.schedulereservationticketing.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.config.jwt.JwtAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@Profile("!loadtest")
@RequiredArgsConstructor
public class SecurityConfig {

	private final CorsProperties corsProperties;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			// CSRF 보호 기능을 비활성화합니다.
			.csrf(AbstractHttpConfigurer::disable)

			// 세션을 사용하지 않으므로, STATELESS(상태 비저장)으로 설정합니다(세션 대신 토큰을 사용함).
			.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

			// HTTP 요청에 대한 접근 권한을 설정합니다.
			.authorizeHttpRequests(authorize -> authorize
				// 모든 사용자가 접근 가능한 경로를 지정합니다.(인증이 필요없는 접근 경로)
				.requestMatchers("/",
					"/api/v1/performances/**",
					"api/v1/tickets",
					"/api/v1/auth/**",
					"/v3/api-docs/**",
					"/swagger-ui/**",
					"/swagger-ui.html",
					"/service1/**",
					"/service2/**").permitAll()
				// CORS preflight 요청 허용
				.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				// 그 외의 모든 요청은 인증된 사용자만 접근할 수 있도록 설정합니다.
				.anyRequest().authenticated()
			);

		http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public CorsFilter corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();

		List<String> origins = corsProperties.allowedOrigins();

		log.info("CORS 허용 출처: {}", origins);
		config.setAllowedOrigins(origins);

		config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
		config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
		config.setAllowCredentials(true);
		config.setMaxAge(3600L);

		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}
}
