package wisoft.nextframe.schedulereservationticketing.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.config.oauth.OAuth2AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final OAuth2UserService oAuth2UserService;
	private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
			// CSRF 보호 기능을 비활성화합니다.
			.csrf(AbstractHttpConfigurer::disable)

			// 세션을 사용하지 않으므로, STATELESS(상태 비저장)으로 설정합니다. -> 토큰 기반 인증을 사용하기 때문입니다.
			.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

			// HTTP 요청에 대한 접근 권한을 설정합니다.
			.authorizeHttpRequests(authorize -> authorize
				// 모든 사용자가 접근 가능한 경로를 지정합니다.
				.requestMatchers("/", "/api/v1/performances").permitAll()
				// 그 외의 모든 요청은 인증된 사용자만 접근할 수 있도록 설정합니다.
				.anyRequest().authenticated()
			)
			// OAuth 2.0 기반 로그인을 설정합니다.
			.oauth2Login(oauth2 -> oauth2
				// 사용자 로그인에 성공한 후 진행할 엔드포인트를 설정합니다.
				.userInfoEndpoint(userInfo ->
					userInfo.userService(oAuth2UserService)
				)
				// 로그인 성공 시, 성공 핸들러가 동작하도록 설정합니다.
				.successHandler(oAuth2AuthenticationSuccessHandler)
			);

		return http.build();
	}
}
