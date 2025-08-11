package wisoft.nextframe.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class ApiResponse<T> {
	private final String code;
	private final T data;

	public static <T> ApiResponse<T> success(T data) {
		return ApiResponse.of("SUCCESS", data);
	}

	public static <T> ApiResponse<T> failed(T data) {
		return ApiResponse.of("FAILED", data);
	}
}
