package com.example.apirules.exception;

public class NotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final String code;

	public NotFoundException(String code, String message) {
		super(message);
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}