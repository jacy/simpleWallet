package com.jc.exception;

public class InvalidParameterException extends RuntimeException {
	private static final long serialVersionUID = 2893208744177320332L;
	private String message;

	public InvalidParameterException(String msg) {
		this.message = msg;
	}

	public String getMessage() {
		return message;
	}
}
