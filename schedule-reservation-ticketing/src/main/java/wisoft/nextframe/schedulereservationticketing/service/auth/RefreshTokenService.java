package wisoft.nextframe.schedulereservationticketing.service.auth;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.entity.user.RefreshToken;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.repository.user.RefreshTokenRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

	private final RefreshTokenRepository refreshTokenRepository;

	/**
	 * 특정 사용자의 Refresh Token을 저장하거나 이미 존재하면 새 값으로 업데이트합니다.
	 * 모든 작업은 트랜잭션 내에서 안전하게 처리됩니다.
	 *
	 * @param user        사용자 엔티티
	 * @param tokenValue  저장할 리프레시 토큰 값
	 * @param expiresAt   토큰 만료 시간
	 */
	public void saveOrUpdateRefreshToken(User user, String tokenValue, LocalDateTime expiresAt) {
		refreshTokenRepository.findByUser(user)
			.ifPresentOrElse(
				refreshToken -> {
					log.debug("Refresh Token 업데이트. userId: {}", user.getId());
					// 기존 토큰의 값과 만료 시간을 업데이트합니다.
					refreshToken.updateTokenValue(tokenValue, expiresAt);
				},
				() -> {
					log.debug("신규 Refresh Token 저장. userId: {}", user.getId());
					// 새 Refresh Token 엔티티를 생성하여 저장합니다.
					refreshTokenRepository.save(RefreshToken.builder()
						.user(user)
						.tokenValue(tokenValue)
						.expiresAt(expiresAt)
						.build());
				}
			);
	}
}
