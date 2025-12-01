package wisoft.nextframe.schedulereservationticketing.service.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.config.jwt.JwtTokenProvider;
import wisoft.nextframe.schedulereservationticketing.dto.auth.TokenRefreshResponse;
import wisoft.nextframe.schedulereservationticketing.entity.user.RefreshToken;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.repository.user.RefreshTokenRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@InjectMocks
	private AuthService authService;

	@Mock
	private RefreshTokenRepository refreshTokenRepository;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@Test
	@DisplayName("토큰 재발급 성공: DB 토큰과 일치하면 새로운 Access Token을 반환한다")
	void reissueToken_success() {
		// given
		String inputRefreshToken = "valid_refresh_token";
		String newAccessToken = "new_access_token";
		UUID userId = UUID.randomUUID();

		given(jwtTokenProvider.getUserIdFromToken(inputRefreshToken))
			.willReturn(userId);

		User mockUser = mock(User.class);
		given(mockUser.getId()).willReturn(userId);

		RefreshToken mockRefreshToken = mock(RefreshToken.class);
		given(mockRefreshToken.getTokenValue()).willReturn(inputRefreshToken);
		given(mockRefreshToken.getUser()).willReturn(mockUser);

		given(refreshTokenRepository.findByUserIdWithUser(userId))
			.willReturn(Optional.of(mockRefreshToken));

		// 새 토큰 생성
		given(jwtTokenProvider.generateAccessToken(userId))
			.willReturn(newAccessToken);

		// when
		TokenRefreshResponse response = authService.reissueToken(inputRefreshToken);

		// then
		assertThat(response.getAccessToken()).isEqualTo(newAccessToken);

		// 검증
		verify(refreshTokenRepository).findByUserIdWithUser(userId);
		verify(refreshTokenRepository, never()).delete(any());
	}

	@Test
	@DisplayName("토큰 탈취 감지: 요청 토큰과 DB 토큰이 다르면 DB 토큰을 삭제하고 예외를 던진다")
	void reissueToken_fail_tokenMismatch_hijacking() {
		// given
		String inputRefreshToken = "stolen_token_value";
		String dbRefreshTokenValue = "original_token_value";
		UUID userId = UUID.randomUUID();

		given(jwtTokenProvider.getUserIdFromToken(inputRefreshToken))
			.willReturn(userId);

		// DB에 저장된 토큰 정보
		RefreshToken dbRefreshToken = mock(RefreshToken.class);
		given(dbRefreshToken.getTokenValue()).willReturn(dbRefreshTokenValue);

		given(refreshTokenRepository.findByUserIdWithUser(userId))
			.willReturn(Optional.of(dbRefreshToken));

		// when and then
		assertThatThrownBy(() -> authService.reissueToken(inputRefreshToken))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.TOKEN_MISMATCH);

		// 검증: 탈취 의심 시 DB의 토큰을 삭제했는지 확인
		verify(refreshTokenRepository).delete(dbRefreshToken);
	}

	@Test
	@DisplayName("로그아웃 유저: 토큰은 유효하지만 DB에 없으면 LOGGED_OUT_USER 예외 발생")
	void reissueToken_fail_loggedOut() {
		// given
		String inputRefreshToken = "valid_token";
		UUID userId = UUID.randomUUID();

		given(jwtTokenProvider.getUserIdFromToken(inputRefreshToken))
			.willReturn(userId);

		// DB 조회 결과 없음
		given(refreshTokenRepository.findByUserIdWithUser(userId))
			.willReturn(Optional.empty());

		// when and then
		assertThatThrownBy(() -> authService.reissueToken(inputRefreshToken))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.LOGGED_OUT_USER);
	}

	@Test
	@DisplayName("만료된 토큰: ExpiredJwtException 발생 시 EXPIRED_REFRESH_TOKEN 예외로 변환")
	void reissueToken_fail_expired() {
		// given
		String expiredToken = "expired_token";
		UUID userId = UUID.randomUUID();

		Claims mockClaims = mock(Claims.class);
		given(mockClaims.getSubject()).willReturn(userId.toString());

		// ExpiredJwtException 생성 (Claims에 Subject가 설정된 Mock 주입)
		ExpiredJwtException expiredException = new ExpiredJwtException(null, mockClaims, "Expired");

		// Provider가 만료 예외를 던짐
		given(jwtTokenProvider.getUserIdFromToken(expiredToken))
			.willThrow(expiredException);

		// when and then
		assertThatThrownBy(() -> authService.reissueToken(expiredToken))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.EXPIRED_REFRESH_TOKEN);
	}

	@Test
	@DisplayName("유효하지 않은 토큰: JwtException 발생 시 INVALID_TOKEN 예외로 변환")
	void reissueToken_fail_invalid() {
		// given
		String invalidToken = "invalid_token";

		// Provider가 일반 JWT 예외를 던짐
		given(jwtTokenProvider.getUserIdFromToken(invalidToken))
			.willThrow(new JwtException("Invalid signature"));

		// when and then
		assertThatThrownBy(() -> authService.reissueToken(invalidToken))
			.isInstanceOf(DomainException.class)
			.hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_TOKEN);
	}
}