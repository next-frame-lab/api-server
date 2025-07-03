package wisoft.nextframe.schedulereservationticketing.common.response;

import lombok.Getter;

@Getter
public class ApiErrorResponse {

	private final String code;
	private final Object data = null;

	public ApiErrorResponse(String code) {
		this.code = code;
	}
}
