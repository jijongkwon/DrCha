package com.ssafy.drcha.global.error.type;

import com.ssafy.drcha.global.error.ErrorCode;

public class ForbiddenException extends BusinessException {
	public ForbiddenException(ErrorCode errorCode) {
		super(errorCode);
	}

	public ForbiddenException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}

	public ForbiddenException(ErrorCode errorCode, String message, Throwable cause) {
		super(errorCode, message, cause);
	}
}
