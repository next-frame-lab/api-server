package wisoft.nextframe.schedulereservationticketing.common.response;

import lombok.Getter;

@Getter
public class ErrorResponseDto {

	private final String code;
	private final Object data = null;

	public ErrorResponseDto(String code) {
		this.code = code;
	}
}
