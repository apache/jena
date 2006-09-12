/*
 	(c) Copyright 2006 Hewlett-Packard Development Company, LP
 	All rights reserved.
 	$Id: CannotEncodeCharacterException.java,v 1.1 2006-09-12 10:40:52 chris-dollin Exp $
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

