package org.apache.jena.sparql.core;

import org.apache.jena.sparql.JenaTransactionException;

/**
 * Thrown when a transaction attempts to work outside its controlled region of data.
 */
public class JenaTransactionRegionException extends JenaTransactionException {

    public JenaTransactionRegionException(String message) {
        super(message);
    }

}
