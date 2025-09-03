package wisoft.nextframe.schedulereservationticketing.config.oauth;

import java.io.IOException;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.config.jwt.JwtTokenProvider;
import wisoft.nextframe.schedulereservationticketing.dto.auth.TokenResponse;
import wisoft.nextframe.schedulereservationticketing.entity.user.User;
import wisoft.nextframe.schedulereservationticketing.repository.user.UserRepository;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;
	private final ObjectMapper objectMapper;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {

		// 1. 인증 정보에서 OAuth2User 객체를 추출합니다.
		final OAuth2User oAuth2User = (OAuth2User)authentication.getPrincipal();

		// 2. OAuth2User에서 사용자 이메일을 추출합니다.
		//    카카오 응답 기준으로 'kakao_account' 객체 내부의 'email' 필드를 가져옵니다.
		final Map<String, Object> kakaoAccount = (Map<String, Object>)oAuth2User.getAttributes().get("kakao_account");
		final String email = (String)kakaoAccount.get("email");

		// 3. 이메일을 사용하여 데이터베이스에서 사용자를 조회합니다.
		final User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new EntityNotFoundException("존재하지 않는 사용자입니다."));

		// 4. JwtTokenProvider를 사용해 Access Token과 Refresh Token을 생성합니다.
		//    토큰에는 사용자의 고유 ID를 담습니다.
		final String accessToken = jwtTokenProvider.generateAccessToken(user.getId());
		final String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

		response.setContentType("application/json;charset=UTF-8");
		response.setStatus(HttpServletResponse.SC_OK);

		final TokenResponse tokenResponse = TokenResponse.of(accessToken, refreshToken);

		// ObjectMapper를 사용해 DTO 객체를 JSON 문자열로 변환합니다.
		response.getWriter().write(objectMapper.writeValueAsString(tokenResponse));
	}
}
