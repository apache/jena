package org.apache.jena.permissions;

import org.apache.jena.graph.Triple;
import org.apache.jena.shared.AccessDeniedException;

public class UpdateDeniedException extends AccessDeniedException {

	public UpdateDeniedException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public UpdateDeniedException(String message, Throwable cause, Triple triple) {
		super(message, cause, triple);
		// TODO Auto-generated constructor stub
	}

	public UpdateDeniedException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public UpdateDeniedException(String message, Triple triple) {
		super(message, triple);
		// TODO Auto-generated constructor stub
	}

	public UpdateDeniedException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public UpdateDeniedException(Throwable cause, Triple triple) {
		super(cause, triple);
		// TODO Auto-generated constructor stub
	}

	public UpdateDeniedException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
