package com.ssafy.drcha.global.error.type;

import com.ssafy.drcha.global.error.ErrorCode;

public class DataNotFoundException extends BusinessException {
	public DataNotFoundException(ErrorCode errorCode) {
		super(errorCode);
	}

	public DataNotFoundException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}

	public DataNotFoundException(ErrorCode errorCode, String message, Throwable cause) {
		super(errorCode, message, cause);
	}
}
