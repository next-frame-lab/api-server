package wisoft.nextframe.schedulereservationticketing.controller.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.common.response.ApiResponse;
import wisoft.nextframe.schedulereservationticketing.dto.auth.KaKaoSigninRequest;
import wisoft.nextframe.schedulereservationticketing.dto.auth.TokenResponse;
import wisoft.nextframe.schedulereservationticketing.service.auth.OAuthService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final OAuthService oAuthService;

	@PostMapping("/signin")
	public ResponseEntity<ApiResponse<?>> signin(@RequestBody KaKaoSigninRequest request) {
		final TokenResponse tokenResponse = oAuthService.kakaoSignin(request.authCode());

		final ApiResponse<TokenResponse> response = ApiResponse.success(tokenResponse);

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
