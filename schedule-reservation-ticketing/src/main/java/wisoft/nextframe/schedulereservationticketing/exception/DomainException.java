package wisoft.nextframe.schedulereservationticketing.exception;

import lombok.Getter;
import wisoft.nextframe.schedulereservationticketing.common.exception.ErrorCode;

@Getter
public class DomainException extends RuntimeException {

	private final ErrorCode errorCode;

	public DomainException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}
}
