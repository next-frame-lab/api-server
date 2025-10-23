package wisoft.nextframe.schedulereservationticketing.config.jwt;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;

	private static final List<String> EXCLUDED_PATHS = List.of(
		"/",
		"/api/v1/auth/**",
		"/api/v1/performances",
		"/api/v1/performances/top10"
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

		if (StringUtils.hasText(token)) {
			try {
				if (!jwtTokenProvider.validateToken(token)) {
					throw new DomainException(ErrorCode.INVALID_TOKEN);
				}
				// 토큰 검증 및 사용자 ID 추출
				final UUID userId = jwtTokenProvider.getUserIdFromToken(token);
				log.debug("JWT 토큰이 유효합니다. userId: {}", userId);

				// Spring Security 인증 객체 생성 및 컨텍스트에 저장
				final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId, null,
					java.util.Collections.emptyList());
				SecurityContextHolder.getContext().setAuthentication(authentication);
				log.info("사용자 인증 정보를 SecurityContext에 저장했습니다. userId: {}", userId);

			} catch (SignatureException e) {
				SecurityContextHolder.clearContext();
				log.warn("유효하지 않은 JWT 서명입니다.");
			} catch (MalformedJwtException e) {
				SecurityContextHolder.clearContext();
				log.warn("손상된 JWT 토큰입니다.");
			} catch (ExpiredJwtException e) {
				SecurityContextHolder.clearContext();
				log.warn("만료된 JWT 토큰입니다.");
			} catch (UnsupportedJwtException e) {
				SecurityContextHolder.clearContext();
				log.warn("지원하지 않는 JWT 토큰입니다.");
			} catch (IllegalArgumentException e) {
				SecurityContextHolder.clearContext();
				log.warn("JWT 클레임이 비어있습니다.");
			} catch (Exception e) {
				SecurityContextHolder.clearContext();
				log.error("JWT 필터 처리 중 알 수 없는 오류가 발생했습니다.", e);
			}
		}

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