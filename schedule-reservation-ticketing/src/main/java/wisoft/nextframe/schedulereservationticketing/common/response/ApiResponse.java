package wisoft.nextframe.schedulereservationticketing.common.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

	private final String code;
	private final T data;

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>("SUCCESS", data);
	}

	public static <T> ApiResponse<T> error(ErrorCode errorCode) {
		return new ApiResponse<>(errorCode.getCode(), null);
	}
}
