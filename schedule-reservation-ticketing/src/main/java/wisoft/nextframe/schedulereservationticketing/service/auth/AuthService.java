package wisoft.nextframe.schedulereservationticketing.service.auth;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.config.jwt.JwtTokenProvider;
import wisoft.nextframe.schedulereservationticketing.dto.auth.tokenrefresh.TokenRefreshResponse;
import wisoft.nextframe.schedulereservationticketing.entity.user.RefreshToken;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.repository.user.RefreshTokenRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtTokenProvider jwtTokenProvider;

	@Transactional
	public TokenRefreshResponse reissueToken(String refreshTokenValue) {
		UUID userId;
		try {
			userId = jwtTokenProvider.getUserIdFromToken(refreshTokenValue);
		} catch (ExpiredJwtException e) {
			userId = UUID.fromString(e.getClaims().getSubject());
			log.warn("만료된 Refresh Token으로 재발급 시도. userId: {}", userId);
			throw new DomainException(ErrorCode.EXPIRED_REFRESH_TOKEN);
		} catch (JwtException | IllegalArgumentException e) {
			log.warn("유효하지 않은 Refresh Token으로 재발급 시도. Error: {}", e.getMessage());
			throw new DomainException(ErrorCode.INVALID_TOKEN);
		}
		log.debug("토큰 재발급 요청. userId: {}", userId);

		final UUID finalUserId = userId;

		final RefreshToken refreshToken = refreshTokenRepository.findByUserIdWithUser(userId)
			.orElseThrow(() -> {
				log.warn("로그아웃 처리되었거나 DB에 존재하지 않는 토큰으로 재발급 시도. userId: {}", finalUserId);
				// 이 경우, 토큰은 유효하지만 DB에 없으므로 로그아웃된 사용자로 간주하는 것이 더 정확합니다.
				return new DomainException(ErrorCode.LOGGED_OUT_USER);
			});

		// DB에 저장된 토큰과 일치하는지 최종 확인 (탈취 방지)
		if (!refreshToken.getTokenValue().equals(refreshTokenValue)) {
			log.error("DB의 토큰과 불일치. 탈취 가능성 의심. userId: {}", userId);
			refreshTokenRepository.delete(refreshToken); // 탈취 의심 토큰 즉시 삭제
			throw new DomainException(ErrorCode.TOKEN_MISMATCH);
		}

		// 새로운 Access Token 생성
		final String newAccessToken = jwtTokenProvider.generateAccessToken(refreshToken.getUser().getId());
		log.info("Access Token 재발급 성공. userId: {}", userId);

		return new TokenRefreshResponse(newAccessToken);
	}

	/**
	 * Refresh Token을 DB에 저장하거나 이미 존재하면 업데이트합니다.
	 * (OAuthService로부터 이동)
	 * @param user 사용자 엔티티
	 * @param tokenValue 저장할 리프레시 토큰 값
	 * @param expiresAt 토큰 만료 시간
	 */
	@Transactional
	public void saveOrUpdateRefreshToken(User user, String tokenValue, LocalDateTime expiresAt) {
		refreshTokenRepository.findByUser(user)
			.ifPresentOrElse(
				refreshToken -> {
					log.debug("Refresh Token 업데이트. userId: {}", user.getId());
					refreshToken.updateTokenValue(tokenValue, expiresAt);
				},
				() -> {
					log.debug("신규 Refresh Token 저장. userId: {}", user.getId());
					refreshTokenRepository.save(RefreshToken.builder()
						.user(user)                 // 어떤 사용자의 토큰인지
						.tokenValue(tokenValue)     // 실제 토큰 값
						.expiresAt(expiresAt)       // 토큰 만료 시간
						.build());
				}
			);
	}
}


