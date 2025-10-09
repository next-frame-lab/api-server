package wisoft.nextframe.schedulereservationticketing.controller.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.common.response.ApiResponse;
import wisoft.nextframe.schedulereservationticketing.dto.auth.KaKaoSigninRequest;
import wisoft.nextframe.schedulereservationticketing.dto.auth.SigninResponse;
import wisoft.nextframe.schedulereservationticketing.dto.auth.tokenrefresh.TokenRefreshRequest;
import wisoft.nextframe.schedulereservationticketing.dto.auth.tokenrefresh.TokenRefreshResponse;
import wisoft.nextframe.schedulereservationticketing.service.auth.AuthService;
import wisoft.nextframe.schedulereservationticketing.service.auth.OAuthService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final OAuthService oAuthService;
	private final AuthService authService;

	@PostMapping("/signin")
	public ResponseEntity<ApiResponse<?>> signin(@RequestBody KaKaoSigninRequest request) {
		log.info("카카오 로그인 시도. provider: {}", request.provider());

		final SigninResponse signinResponse = oAuthService.kakaoSignin(request.provider() ,request.authCode());
		log.info("카카오 로그인 성공. email: {}", signinResponse.email());

		final ApiResponse<SigninResponse> response = ApiResponse.success(signinResponse);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/refresh")
	public ResponseEntity<ApiResponse<?>> refreshToken(@RequestBody TokenRefreshRequest request) {
		log.info("Access Token 재발급 요청.");

		final TokenRefreshResponse tokenRefreshResponse = authService.reissueToken(request.getRefreshToken());
		log.info("Access Token 재발급 성공.");

		final ApiResponse<TokenRefreshResponse> response = ApiResponse.success(tokenRefreshResponse);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
