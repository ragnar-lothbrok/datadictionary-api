package com.opens.datadictionary.exceptions;

import org.springframework.hateoas.VndErrors;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ControllerAdvice
public class DictionaryControllerAdvice {

	private ObjectMapper mapper = new ObjectMapper();

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public String onException(Exception e) throws JsonProcessingException {
		String logref = e.getClass().getSimpleName();
		String msg = getExceptionMessage(e);
		return mapper.writeValueAsString(new VndErrors(logref, msg));
	}

	private String getExceptionMessage(Exception e) {
		return StringUtils.hasText(e.getMessage()) ? e.getMessage() : e.getClass().getSimpleName();
	}

	@ExceptionHandler({ InvalidSwaggerFileException.class, JsonProcessingException.class, GenericException.class })
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ResponseBody
	public String onNotFoundException(Exception e) throws JsonProcessingException {
		String logref = e.getClass().getSimpleName();
		String msg = getExceptionMessage(e);
		return mapper.writeValueAsString(new VndErrors(logref, msg));
	}

}
