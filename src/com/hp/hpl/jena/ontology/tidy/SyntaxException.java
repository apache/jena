package com.hp.hpl.jena.ontology.tidy;

import com.hp.hpl.jena.shared.JenaException;

/**
 * @author jjc
 *
 */
public class SyntaxException extends JenaException {

    /**
     * Constructor for SyntaxException.
     */
    public SyntaxException() {
        super();
    }

    /**
     * Constructor for SyntaxException.
     * @param message
     */
    public SyntaxException(String message) {
        super(message);
    }

    /**
     * Constructor for SyntaxException.
     * @param message
     * @param cause
     */
    public SyntaxException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor for SyntaxException.
     * @param cause
     */
    public SyntaxException(Throwable cause) {
        super(cause);
    }

}
