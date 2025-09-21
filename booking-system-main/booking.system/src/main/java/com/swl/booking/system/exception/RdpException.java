package com.swl.booking.system.exception;

import java.io.Serial;

public class RdpException extends Exception {

	@Serial
	private static final long serialVersionUID = 2336548558388641255L;

	public RdpException(String message) {
		super(message);
	}

	public RdpException(String message, Throwable cause) {
		super(message, cause);
	}
}
