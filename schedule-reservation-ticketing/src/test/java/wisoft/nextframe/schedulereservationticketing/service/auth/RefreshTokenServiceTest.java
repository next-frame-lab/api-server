package wisoft.nextframe.schedulereservationticketing.service.auth;

import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import wisoft.nextframe.schedulereservationticketing.entity.user.RefreshToken;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.repository.user.RefreshTokenRepository;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

	@Mock
	private RefreshTokenRepository refreshTokenRepository;

	@InjectMocks
	private RefreshTokenService refreshTokenService;

	@Test
	@DisplayName("기존 리프레시 토큰을 삭제하고 새로운 토큰을 저장하는 테스트")
	void replaceRefreshTokenTest() {
		// given
		User user = User.builder().id(UUID.randomUUID()).build();
		String tokenValue = "new-refresh-token";
		LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

		// when
		refreshTokenService.replaceRefreshToken(user, tokenValue, expiresAt);

		// then
		verify(refreshTokenRepository, times(1)).deleteByUserId(user.getId());
		verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
	}
}
