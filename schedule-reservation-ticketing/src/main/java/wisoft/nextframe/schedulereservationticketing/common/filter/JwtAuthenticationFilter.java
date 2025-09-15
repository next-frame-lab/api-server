package wisoft.nextframe.schedulereservationticketing.common.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.config.jwt.JwtTokenProvider;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;

	private static final List<String> EXCLUDED_PATHS = List.of(
		"/",
		"/api/v1/auth/**",
		"/api/v1/performances/**"
	);
	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		final String path = request.getRequestURI();
		return EXCLUDED_PATHS.stream().anyMatch(p -> pathMatcher.match(p, path));
	}

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		// 1. 요청 헤더에서 "Authorization" 헤더를 통해 JWT 토큰을 추출합니다.
		final String token = resolveToken(request);

		// 2. 토큰이 존재하고, 유효성 검증(validateToken)을 통과한 경우에만 인증 처리를 합니다.
		if (token != null && jwtTokenProvider.validateToken(token)) {
			// 3. 토큰에서 사용자 ID를 추출합니다.
			final UUID userId = jwtTokenProvider.getUserIdFromToken(token);

			// 4. 추출한 사용자 ID를 사용하여 Spring Security의 인증 객체(Authentication)를 생성합니다.
			//    - principal: 사용자 식별자 (여기서는 UUID)
			//    - credentials: 자격 증명 (보통 비밀번호, JWT에서는 사용하지 않으므로 null)
			//    - authorities: 사용자 권한 목록 (여기서는 권한 없음을 의미하는 빈 리스트)
			final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
				userId,
				null,
				Collections.emptyList()
			);

			// 5. 생성된 인증 객체를 SecurityContextHolder에 저장합니다.
			//    이렇게 하면, 현재 요청을 처리하는 동안 이 사용자는 '인증된 사용자'로 간주됩니다.
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		// 6. 다음 필터로 요청과 응답을 전달합니다.
		//    여기서 인증 처리가 완료되었거나, 토큰이 없어 인증이 필요 없는 요청은 다음 단계로 넘어갑니다.
		filterChain.doFilter(request, response);
	}


	/**
	 * HttpServletRequest에서 "Authorization" 헤더를 파싱하여 JWT 토큰을 추출하는 메서드
	 */
	private String resolveToken(HttpServletRequest request) {
		final String bearerToken = request.getHeader("Authorization");
		// 헤더 값이 존재하고 "Bearer"로 시작하는지 검증합니다.
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			// "Bearer " 접두사(7자리)를 자르고 실제 토큰 부분만 반환합니다.
			return bearerToken.substring(7);
		}
		return null;
	}
}