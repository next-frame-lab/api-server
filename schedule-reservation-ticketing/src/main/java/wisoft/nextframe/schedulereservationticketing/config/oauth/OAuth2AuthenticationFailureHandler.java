package wisoft.nextframe.schedulereservationticketing.config.oauth;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import wisoft.nextframe.schedulereservationticketing.common.response.ApiErrorResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	private final ObjectMapper objectMapper;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException exception) throws IOException, ServletException {
		// 1. 실패 로그를 기록합니다.
		log.error("OAuth2 Login Failed: {}", exception.getMessage());
		log.error("Exception StackTrace: ", exception); // 스택 트레이스 전체를 보려면 이 로그를 활성화하세요.

		// 2. HTTP 응답 상태를 설정합니다.
		// 클라이언트에게 인증이 실패했음을 알리는 401 Unauthorized 상태 코드를 설정합니다.
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json;charset=UTF-8");

		// 3. 에러 응답 DTO 생성
		ApiErrorResponse errorResponse = new ApiErrorResponse("401");

		// 4. ObjectMapper를 사용하여 DTO를 JSON 문자열로 변환하고 응답 본문에 작성
		response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
	}

}
