package wisoft.nextframe.schedulereservationticketing.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

	// Common
	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "입력 값이 올바르지 않습니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버에 오류가 발생했습니다."),
	INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "입력 값의 타입이 올바르지 않습니다."),

	// Auth(User)
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "해당 사용자를 찾을 수 없습니다."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "유효하지 않은 토큰입니다."),
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),
	AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "유효한 인증 정보가 없습니다."),
	LOGGED_OUT_USER(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "로그아웃된 사용자입니다. 다시 로그인해주세요."),
	TOKEN_MISMATCH(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "토큰이 일치하지 않습니다. 비정상적인 접근입니다."),
	EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "만료된 Refresh Token입니다."),


	// Performance and Stadium
	PERFORMANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "해당 공연을 찾을 수 없습니다."),
	SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "해당 공연 일정을 찾을 수 없습니다."),
	STADIUM_NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "해당 공연장을 찾을 수 없습니다."),

	// Reservation
	SEAT_ALREADY_LOCKED(HttpStatus.CONFLICT, "CONFLICT", "이미 예약되었거나 선택할 수 없는 좌석입니다."),
	INVALID_SEAT_SELECTION_COUNT(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "좌석은 1개 이상, 4개 이하로 선택해야 합니다."),
	TOTAL_PRICE_MISMATCH(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "요청한 금액과 계산된 금액이 일치하지 않습니다."),
	SEAT_NOT_DEFINED(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "요청한 좌석 중 일부를 찾을 수 없습니다."),
	PERFORMANCE_SCHEDULE_MISMATCH(HttpStatus.BAD_REQUEST, "BAD_REQUEST", "공연과 공연 일정 정보가 일치하지 않습니다."),
	RESERVATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "해당 예약 정보를 찾을 수 없습니다."),

	// Review
	REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "해당 리뷰을 찾을 수 없습니다."),
	REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "CONFLICT", "이미 리뷰를 작성했습니다."),

	// Ticketing
	TICKET_ALREADY_ISSUED(HttpStatus.CONFLICT, "CONFLICT", "이미 발급된 티켓입니다."),
	TICKET_NOT_FOUND(HttpStatus.NOT_FOUND, "NOT_FOUND", "해당 티켓을 찾을 수 없습니다."),
	PAYMENT_NOT_COMPLETED(HttpStatus.CONFLICT, "CONFLICT", "결제 완료된 상태가 아닐 경우, 티켓을 발급할 수 없습니다."),

	// OAuth
	FAILED_TO_RECEIVE_KAKAO_TOKEN(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
		"카카오 서버로부터 토큰 응답을 받지 못했습니다."),
	MISSING_KAKAO_ACCESS_TOKEN(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
		"카카오 토큰 응답에 access token이 없습니다."),
	FAILED_TO_GET_KAKAO_USER_INFO(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
		"카카오 사용자 정보를 받아오는데 실패했습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
