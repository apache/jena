/*
 	(c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: CannotEncodeCharacterException.java,v 1.2 2007-01-02 11:48:38 andy_seaborne Exp $
*/

package com.hp.hpl.jena.shared;

/**
    Exception to throw when a character cannot be encoded into some context
    for some reason.
    
    @author kers
*/
public class CannotEncodeCharacterException extends JenaException
    {
    protected final char badChar;
    protected final String encodingContext;
    
    public CannotEncodeCharacterException( char badChar, String encodingContext )
        {
        super( "cannot encode (char) " + badChar + " in context " + encodingContext );
        this.badChar = badChar; 
        this.encodingContext = encodingContext;
        }

    /**
        Answer the character that could not be encoded.
    */
    public char getBadChar()
        { return badChar; }

    /**
        Answer the name of the context in which the encoding failed.
    */
    public String getEncodingContext()
        { return encodingContext; }
    }

