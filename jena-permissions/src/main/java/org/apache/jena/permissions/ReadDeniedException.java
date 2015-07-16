package org.apache.jena.permissions;

import org.apache.jena.graph.Triple;
import org.apache.jena.shared.AccessDeniedException;

public class ReadDeniedException extends AccessDeniedException {

	public ReadDeniedException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ReadDeniedException(String message, Throwable cause, Triple triple) {
		super(message, cause, triple);
		// TODO Auto-generated constructor stub
	}

	public ReadDeniedException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public ReadDeniedException(String message, Triple triple) {
		super(message, triple);
		// TODO Auto-generated constructor stub
	}

	public ReadDeniedException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public ReadDeniedException(Throwable cause, Triple triple) {
		super(cause, triple);
		// TODO Auto-generated constructor stub
	}

	public ReadDeniedException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
