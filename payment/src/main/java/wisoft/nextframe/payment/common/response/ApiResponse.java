package wisoft.nextframe.payment.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class ApiResponse<T> {
	private final String code;
	private final T data;
	private final String message;

	public static <T> ApiResponse<T> success(T data) {
		return ApiResponse.of("SUCCESS", data, null);
	}

	public static <T> ApiResponse<T> failed(String message) {
		return ApiResponse.of("FAILED", null, message);
	}
}
