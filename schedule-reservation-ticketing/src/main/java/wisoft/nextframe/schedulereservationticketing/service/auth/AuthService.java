package wisoft.nextframe.schedulereservationticketing.service.auth;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.config.jwt.JwtTokenProvider;
import wisoft.nextframe.schedulereservationticketing.dto.auth.tokenrefresh.TokenRefreshResponse;
import wisoft.nextframe.schedulereservationticketing.entity.user.RefreshToken;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.repository.user.RefreshTokenRepository;
import wisoft.nextframe.schedulereservationticketing.repository.user.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

	private final RefreshTokenRepository refreshTokenRepository;
	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;

	@Transactional
	public TokenRefreshResponse reissueToken(String refreshTokenValue) {
		// 1. Refresh Token 유효성 검증
		if (!jwtTokenProvider.validateToken(refreshTokenValue)) {
			log.warn("유효하지 않은 Refresh Token으로 재발급 시도.");
			throw new DomainException(ErrorCode.INVALID_TOKEN);
		}

		// 2. Refresh Token에서 사용자 ID 추출
		final UUID userId = jwtTokenProvider.getUserIdFromToken(refreshTokenValue);
		log.debug("토큰 재발급 요청. userId: {}", userId);

		final User user = userRepository.findById(userId)
			.orElseThrow(() -> {
				log.warn("토큰은 유효하지만 DB에 존재하지 않는 사용자. userId: {}", userId);
				return new DomainException(ErrorCode.USER_NOT_FOUND);
			});

		// 3. DB에 저장된 Refresh Token과 일치하는지 확인
		final RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
			.orElseThrow(() -> {
				log.warn("로그아웃 처리된 사용자의 토큰으로 재발급 시도. userId: {}", userId);
				return new DomainException(ErrorCode.LOGGED_OUT_USER);
			});
		if (!refreshToken.getTokenValue().equals(refreshTokenValue)) {
			log.error("DB의 토큰과 불일치. 탈취 가능성 의심. userId: {}", userId);
			throw new DomainException(ErrorCode.TOKEN_MISMATCH);
		}

		// 4. 새로운 Access Token 생성
		final String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId());
		log.debug("새 Access Token 생성 완료. userId: {}", userId);

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


