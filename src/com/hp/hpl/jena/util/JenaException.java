package com.hp.hpl.jena.util;

/**
 * @author jjc
 *
 */
public class JenaException extends RuntimeException {

	/**
	 * Constructor for JenaException.
	 */
	public JenaException() {
		super();
	}

	/**
	 * Constructor for JenaException.
	 * @param message
	 */
	public JenaException(String message) {
		super(message);
	}
    private Throwable cause;
    private boolean initCauseCalled = false;
    /* Java 1.3, 1.4 compatibility.
     * Support getCause() and initCause()
     */
    public Throwable getCause() {
        return cause;
    }
    public Throwable initCause(Throwable c) {
        if ( initCauseCalled )
           throw new IllegalStateException("JenaException.initCause can be called at most once.");
        cause = c;
        initCauseCalled = true;
        return this;
    }
    
	/** Java 1.4 bits ....
	 * Constructor for JenaException.
	 * @param message
	 * @param cause
     * */
	public JenaException(String message, Throwable cause) {
		super(message);
        initCause(cause);
	}

	/**
	 * Constructor for JenaException.
	 * @param cause
	 * */
	public JenaException(Throwable cause) {
		super();
        initCause(cause);
	}
    /* */

}
