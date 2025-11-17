package wisoft.nextframe.schedulereservationticketing.config.jwt;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.common.exception.JwtAuthenticationException;
import wisoft.nextframe.schedulereservationticketing.config.security.CustomAuthenticationEntryPoint;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;
	private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		// Authorization 헤더에서 JWT 토큰 추출
		final String token = resolveToken(request);

		if (StringUtils.hasText(token)) {
			try {
				// 토큰 검증 및 사용자 ID 추출
				final UUID userId = jwtTokenProvider.getUserIdFromToken(token);
				log.debug("JWT 토큰이 유효합니다. userId: {}", userId);

				// Spring Security 인증 객체 생성 및 컨텍스트에 저장
				final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
					userId,
					null,
					Collections.emptyList()
				);

				SecurityContextHolder.getContext().setAuthentication(authentication);
				log.info("사용자 인증 정보를 SecurityContext에 저장했습니다. userId: {}", userId);

			} catch (JwtException | IllegalArgumentException e) {
				SecurityContextHolder.clearContext();
				log.warn("JWT 인증 실패: {}", e.getMessage());

				customAuthenticationEntryPoint.commence(
					request,
					response,
					new JwtAuthenticationException("유효하지 않은 토큰입니다.", e));

				return;
			} catch (Exception e) {
				SecurityContextHolder.clearContext();
				log.error("JWT 필터 처리 중 알 수 없는 오류 발생", e);

				customAuthenticationEntryPoint.commence(
					request,
					response,
					new AuthenticationServiceException("필터 처리 중 오류 발생", e)
				);

				return;
			}
		}

		filterChain.doFilter(request, response);
	}

	/**
	 * HttpServletRequest에서 "Authorization" 헤더를 파싱하여 JWT 토큰을 추출하는 메서드
	 */
	private String resolveToken(HttpServletRequest request) {
		final String bearerToken = request.getHeader("Authorization");

		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}

		return null;
	}
}