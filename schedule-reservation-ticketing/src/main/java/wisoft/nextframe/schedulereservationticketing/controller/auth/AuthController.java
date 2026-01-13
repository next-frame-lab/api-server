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
import wisoft.nextframe.schedulereservationticketing.dto.auth.TokenRefreshRequest;
import wisoft.nextframe.schedulereservationticketing.dto.auth.TokenRefreshResponse;
import wisoft.nextframe.schedulereservationticketing.service.auth.AuthService;
import wisoft.nextframe.schedulereservationticketing.service.auth.OAuthFacade;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final OAuthFacade oAuthFacade;
	private final AuthService authService;

	@PostMapping("/signin")
	public ResponseEntity<ApiResponse<?>> signin(@RequestBody KaKaoSigninRequest request) {
		final SigninResponse signinResponse = oAuthFacade.signin(request.provider() ,request.authCode());

		final ApiResponse<SigninResponse> response = ApiResponse.success(signinResponse);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/refresh")
	public ResponseEntity<ApiResponse<?>> refreshToken(@RequestBody TokenRefreshRequest request) {
		final TokenRefreshResponse tokenRefreshResponse = authService.reissueToken(request.getRefreshToken());

		final ApiResponse<TokenRefreshResponse> response = ApiResponse.success(tokenRefreshResponse);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
