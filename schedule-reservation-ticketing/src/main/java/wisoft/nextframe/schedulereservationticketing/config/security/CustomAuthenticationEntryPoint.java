package wisoft.nextframe.schedulereservationticketing.config.security;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;
import wisoft.nextframe.schedulereservationticketing.common.response.ApiResponse;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException authException) throws IOException {
		// 응답 내용 생성
		final ApiResponse<?> apiErrorResponse = ApiResponse.error(ErrorCode.INVALID_TOKEN);

		// HTTP 상태 코드 설정
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Status Code 401

		// 응답의 Content-Type과 인코딩 설정
		response.setContentType("application/json");
		response.setCharacterEncoding("utf-8");

		// ObjectMapper를 사용해 apiErrorResponse 객체를 Json 문자열로 변환하여 응답 본문에 작성
		final String jsonResponse = objectMapper.writeValueAsString(apiErrorResponse);
		response.getWriter().write(jsonResponse);
	}
}
