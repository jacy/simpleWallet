package com.jc.exception;

public class BadRequestException extends RuntimeException {
	private static final long serialVersionUID = -3035910664767453512L;
	private Errors error;

	public BadRequestException(Errors error) {
		this.error = error;
	}

	public Errors getError() {
		return error;
	}

	@Override
	public String toString() {
		return "BadRequestException [error=" + error + "]";
	}

}
