package com.hp.hpl.jena.ontology.tidy;

import com.hp.hpl.jena.shared.JenaException;

/**
 * @author jjc
 *
 */
public class SyntaxException extends JenaException {

    /**
     * Constructor for SyntaxException.
     * @param message
     */
    public SyntaxException(String message) {
        super(message);
    }

    /**
     * Constructor for SyntaxException.
     * @param cause
     */
    public SyntaxException(Exception cause) {
        super(cause);
    }

}
