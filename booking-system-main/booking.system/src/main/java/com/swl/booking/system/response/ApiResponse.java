package com.swl.booking.system.response;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class ApiResponse<T> implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7713056373920733302L;
	private String status;
	private String message;
	private T data;
	private Map<String, String> errors;

	public ApiResponse() {
		super();
	}

	public ApiResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }

    // Success response with data
    public ApiResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // Error response with errors map
    public ApiResponse(String status, String message, Map<String, String> errors) {
        this.status = status;
        this.message = message;
        this.errors = errors;
    }

    // Full constructor
    public ApiResponse(String status, String message, T data, Map<String, String> errors) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.errors = errors;
    }
}