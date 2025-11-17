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
public class RefreshTokenService {

	private final RefreshTokenRepository refreshTokenRepository;

	/**
	 * 사용자의 기존 Refresh Token을 삭제하고,
	 * 새로운 Refresh Token을 발급하여 저장합니다.
	 *
	 * @param user        사용자 엔티티
	 * @param tokenValue  저장할 리프레시 토큰 값
	 * @param expiresAt   토큰 만료 시간
	 */
	@Transactional
	public void replaceRefreshToken(User user, String tokenValue, LocalDateTime expiresAt) {
		// 1. 기존 Refresh Token 삭제
		log.info("기존 Refresh Token 삭제. userId={}", user.getId());
		refreshTokenRepository.deleteByUserId(user.getId());

		// 2. 새 Refresh Token 저장
		log.info("새 Refresh Token 발급. userId={}", user.getId());
		refreshTokenRepository.save(
			RefreshToken.builder()
				.user(user)
				.tokenValue(tokenValue)
				.expiresAt(expiresAt)
				.build()
		);
	}
}
