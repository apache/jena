/*
 * (c) Copyright 2003, Hewlett-Packard Development Company, LP
 * [See end of file]
 */
 
package com.hp.hpl.jena.shared;
 
import java.io.*;

/**
 * This should be a superclass of most exceptions
 * arising from Jena code.
 * @author jjc
 *
 */
public class JenaException extends RuntimeException {

	/**
	 * Constructor for JenaException.
	 * @param message
	 */
	public JenaException( String message ) {
		super(message);
	}
    
    public JenaException()
        { super(); }
        
    private Throwable cause;
    /* Java 1.3, 1.4 compatibility.
     * Support getCause() and initCause()
     */
    public Throwable getCause() {
        return cause;
    }

	/**
	 * Java 1.4 bits ....
     * Constructor for JenaException.
	 * @param cause
	 * */
	public JenaException( Throwable cause )
		{ this( "rethrew: " + cause.toString(), cause ); }
    
    public JenaException( String message, Throwable cause )
        { super( message ); this.cause = cause; }
    
    /*
        These are so that the printout of a nested exception reveals where
        the originating exception came from ... essential when debugging. 
    */
    
    public void printStackTrace( PrintStream s )
        {
        if (cause != null) cause.printStackTrace( s );
        super.printStackTrace( s );
        }
        
    public void printStackTrace( PrintWriter w )
        {
        if (cause != null) cause.printStackTrace( w );
        super.printStackTrace( w );
        }

}
/*
 *  (c) Copyright 2003 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */