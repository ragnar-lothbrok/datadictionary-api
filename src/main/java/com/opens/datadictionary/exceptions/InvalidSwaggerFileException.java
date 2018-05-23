package com.opens.datadictionary.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class InvalidSwaggerFileException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidSwaggerFileException(String message) {
		super(message);
	}

	public InvalidSwaggerFileException(String message, Throwable t) {
		super(message, t);
	}
}
