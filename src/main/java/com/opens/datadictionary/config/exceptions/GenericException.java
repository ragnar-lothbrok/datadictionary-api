package com.opens.datadictionary.config.exceptions;

public class GenericException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public GenericException(String message) {
		super(message);
	}

	public GenericException(String message, Throwable t) {
		super(message, t);
	}
}
