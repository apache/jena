package org.apache.jena.shared;

public class AuthenticationRequiredException extends OperationDeniedException {

	public AuthenticationRequiredException() {
		super();
	}

	public AuthenticationRequiredException(String message, Throwable cause) {
		super(message, cause);
	}

	public AuthenticationRequiredException(String message) {
		super(message);
	}

	public AuthenticationRequiredException(Throwable cause) {
		super(cause);
	}

}
