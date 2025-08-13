package wisoft.nextframe.schedulereservationticketing.dto.common;

import lombok.Getter;

@Getter
public class ApiResponse<T> {

	private final String code;
	private final T data;

	public ApiResponse(String code, T data) {
		this.code = code;
		this.data = data;
	}

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>("SUCCESS", data);
	}
}
