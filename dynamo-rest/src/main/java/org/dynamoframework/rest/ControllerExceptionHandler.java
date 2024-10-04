package org.dynamoframework.rest;

/*-
 * #%L
 * Dynamo Framework
 * %%
 * Copyright (C) 2014 - 2024 Open Circle Solutions
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.dynamoframework.exception.*;
import org.dynamoframework.rest.model.ErrorMessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class ControllerExceptionHandler {

	@ExceptionHandler({ConstraintViolationException.class})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ApiResponse(responseCode = "400", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE)})
	public ResponseEntity<ErrorMessageResponse> constraintViolation(ConstraintViolationException ex) {
		log.error(ex.getMessage(), ex);
		String collect = ex.getConstraintViolations().stream().map(err -> mapError(err))
			.collect(Collectors.joining(", "));

		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(new ErrorMessageResponse(HttpStatus.BAD_REQUEST.value(), collect));
	}

	@ExceptionHandler({JsonProcessingException.class})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ApiResponse(responseCode = "400", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE)})
	public ResponseEntity<ErrorMessageResponse> jsonProcessingException(JsonProcessingException ex) {
		log.error(ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(new ErrorMessageResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
	}

	/**
	 * Called when the REST endpoint cannot be found
	 *
	 * @param ex the exception that occurred
	 * @return the error response message
	 */
	@ExceptionHandler(NoResourceFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ApiResponse(responseCode = "404", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE)})
	public ResponseEntity<ErrorMessageResponse> noResourceFound(NoResourceFoundException ex) {
		log.error(ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(new ErrorMessageResponse(HttpStatus.NOT_FOUND.value(), "Endpoint not found"));
	}

	@ExceptionHandler(OcsNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	@ApiResponse(responseCode = "404", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE)})
	public ResponseEntity<ErrorMessageResponse> noResourceFound(OcsNotFoundException ex) {
		log.error(ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(new ErrorMessageResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
	}

	@ExceptionHandler(OCSValidationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ApiResponse(responseCode = "400", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE)})
	public ResponseEntity<ErrorMessageResponse> validationError(OCSValidationException ex) {
		log.error(ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(new ErrorMessageResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
	}

	@ExceptionHandler(OCSNonUniqueException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ApiResponse(responseCode = "400", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE)})
	public ResponseEntity<ErrorMessageResponse> nonUniqueException(OCSNonUniqueException ex) {
		log.error(ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
			.body(new ErrorMessageResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()));
	}

	@ExceptionHandler(OCSSecurityException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	@ApiResponse(responseCode = "401", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE)})
	public ResponseEntity<ErrorMessageResponse> securityException(OCSSecurityException ex) {
		log.error(ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
			.body(new ErrorMessageResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ApiResponse(responseCode = "500", content = {@Content(mediaType = MediaType.APPLICATION_JSON_VALUE)})
	public ResponseEntity<ErrorMessageResponse> fallback(Exception ex) {
		log.error(ex.getMessage(), ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(new ErrorMessageResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage()));
	}

	private String mapError(ConstraintViolation<?> error) {
		return error.getPropertyPath() + " " +
			error.getMessage() == null ? "Unknown error" : error.getMessage();
	}
}
