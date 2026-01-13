package wisoft.nextframe.schedulereservationticketing.controller.auth;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import wisoft.nextframe.schedulereservationticketing.common.exception.DomainException;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.config.jwt.JwtAuthenticationFilter;
import wisoft.nextframe.schedulereservationticketing.config.security.SecurityConfig;
import wisoft.nextframe.schedulereservationticketing.dto.auth.KaKaoSigninRequest;
import wisoft.nextframe.schedulereservationticketing.dto.auth.SigninResponse;
import wisoft.nextframe.schedulereservationticketing.dto.auth.TokenRefreshRequest;
import wisoft.nextframe.schedulereservationticketing.dto.auth.TokenRefreshResponse;
import wisoft.nextframe.schedulereservationticketing.service.auth.AuthService;
import wisoft.nextframe.schedulereservationticketing.service.auth.OAuthFacade;

@WebMvcTest(value = AuthController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {SecurityConfig.class, JwtAuthenticationFilter.class}
    )
)
@WithMockUser
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private OAuthFacade oAuthFacade;

	@MockitoBean
	private AuthService authService;

	@Nested
	@DisplayName("카카오 로그인 테스트")
	class signinTest {
		@Test
		@DisplayName("카카오 로그인 성공: 201 Created와 엑세스/리프레시 토큰 및 유저 정보를 반환한다")
		void signin_success() throws Exception {
			// given
			KaKaoSigninRequest request = new KaKaoSigninRequest("kakao", "auth_code_123");
			SigninResponse response = new SigninResponse(
				"access-token-example",
				"refresh-token-example",
				"http://image.url/profile.jpg",
				"홍길동",
				25,
				"test@example.com"
			);

			given(oAuthFacade.signin(anyString(), anyString()))
				.willReturn(response);

			// when and then
			mockMvc.perform(post("/api/v1/auth/signin")
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.code").value("SUCCESS"))
				.andExpect(jsonPath("$.data.accessToken").value("access-token-example"))
				.andExpect(jsonPath("$.data.refreshToken").value("refresh-token-example"))
				.andExpect(jsonPath("$.data.name").value("홍길동"));

		}

		@Test
		@DisplayName("카카오 로그인 실패: 인증 코드가 만료되었거나 잘못된 경우 400 Bad Request를 반환한다")
		void signin_fail_invalid_code() throws Exception {
			// given
			KaKaoSigninRequest request = new KaKaoSigninRequest("kakao", "invalid_code");

			given(oAuthFacade.signin(anyString(), anyString()))
				.willThrow(new DomainException(ErrorCode.INVALID_KAKAO_AUTH_CODE));

			// when and then
			mockMvc.perform(post("/api/v1/auth/signin")
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("BAD_REQUEST"));
		}
	}


		@Nested
		@DisplayName("토큰 재발급 테스트")
		class refreshTokenTest {
			@Test
			@DisplayName("토큰 재발급 성공: 201 Created와 새로운 액세스 토큰을 반환한다")
			void refreshToken_success() throws Exception {
				// given
				TokenRefreshRequest request = new TokenRefreshRequest("valid-refresh-token");
				TokenRefreshResponse response = new TokenRefreshResponse("new-access-token");

				given(authService.reissueToken(anyString()))
					.willReturn(response);

				// when and then
				mockMvc.perform(post("/api/v1/auth/refresh")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isCreated())
					.andExpect(jsonPath("$.code").value("SUCCESS"))
					.andExpect(jsonPath("$.data.accessToken").value("new-access-token"));
			}

			@Test
			@DisplayName("토큰 재발급 실패: 리프레시 토큰이 만료되었거나 유효하지 않으면 401 Unauthorized를 반환한다")
			void refreshToken_fail_invalid_token() throws Exception {
				// given
				TokenRefreshRequest request = new TokenRefreshRequest("expired-refresh-token");

				given(authService.reissueToken(anyString()))
					.willThrow(new DomainException(ErrorCode.INVALID_TOKEN)); // 예시 에러코드

				// when and then
				mockMvc.perform(post("/api/v1/auth/refresh")
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isUnauthorized())
					.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
			}
		}
}