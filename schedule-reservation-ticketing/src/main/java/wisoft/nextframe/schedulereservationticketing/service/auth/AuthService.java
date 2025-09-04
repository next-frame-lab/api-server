package wisoft.nextframe.schedulereservationticketing.service.auth;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.config.jwt.JwtTokenProvider;
import wisoft.nextframe.schedulereservationticketing.dto.auth.tokenrefresh.TokenRefreshResponse;
import wisoft.nextframe.schedulereservationticketing.entity.user.RefreshToken;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.exception.auth.InvalidTokenException;
import wisoft.nextframe.schedulereservationticketing.repository.user.RefreshTokenRepository;
import wisoft.nextframe.schedulereservationticketing.repository.user.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final RefreshTokenRepository refreshTokenRepository;
	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;

	@Transactional
	public TokenRefreshResponse reissueToken(String refreshTokenValue) {
		// 1. Refresh Token을 유효성 검증합니다.
		if (!jwtTokenProvider.validateToken(refreshTokenValue)) {
			throw new InvalidTokenException("유효하지 않은 Refresh Token입니다.");
		}

		// 2. Refresh Token에서 사용자 ID 추출합니다.
		final UUID userId = jwtTokenProvider.getUserIdFromToken(refreshTokenValue);
		final User user = userRepository.findById(userId)
			.orElseThrow(() -> new EntityNotFoundException("토큰에 해당하는 사용자를 찾을 수 없습니다."));

		// 3. DB에 저장된 Refresh Token과 일치하는지 확인합니다.
		final RefreshToken refreshToken = refreshTokenRepository.findByUser(user)
			.orElseThrow(() -> new RuntimeException("로그아웃된 사용자입니다. 다시 로그인해주세요."));
		if (!refreshToken.getTokenValue().equals(refreshTokenValue)) {
			throw new RuntimeException("토큰이 일치하지 않습니다. 비정상적인 접근입니다.");
		}

		// 4. 모든 검증이 통과되었기 때문에, 새로운 Access Token을 생성합니다.
		final String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId());

		// 5. 응답 DTO에 담아 반환합니다.
		return new TokenRefreshResponse(newAccessToken);
	}
}
