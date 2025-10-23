package wisoft.nextframe.schedulereservationticketing.common.exception;

import lombok.Getter;

@Getter
public class DomainException extends RuntimeException {

	private final ErrorCode errorCode;

	public DomainException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
