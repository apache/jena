/******************************************************************
 * File:        DatatypeFormatException.java
 * Created by:  Dave Reynolds
 * Created on:  07-Dec-02
 * 
 * (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: DatatypeFormatException.java,v 1.1.1.1 2002-12-19 19:13:38 bwm Exp $
 *****************************************************************/
package com.hp.hpl.jena.graph.dt;

import com.hp.hpl.jena.rdf.model.RDFException;

/**
 * Exception thrown when a lexical form does not match the stated
 * datatype.
 * 
 * @TODO could hold the lexical form and datatype in local fields
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1.1.1 $ on $Date: 2002-12-19 19:13:38 $
 */
public class DatatypeFormatException extends RDFException 
{
    /**
     * Preferred constructor.
     * @param lexicalForm the illegal string discovered
     * @param dtype the datatype that found the problem
     * @param msg additional context for the error
     */
    public DatatypeFormatException(String lexicalForm, RDFDatatype dtype, String msg) {
        super("Lexical form '" + lexicalForm +
               "' is not a legal instance of " + dtype + " " + msg);
    }
                  
    /**
     * Creates a new instance of <code>DatatypeFormatException</code> 
     * without detail message.
     */
    public DatatypeFormatException() {
    }
    
    /**
     * Constructs an instance of <code>DatatypeFormatException</code> 
     * with the specified detail message.
     * @param msg the detail message.
     */
    public DatatypeFormatException(String msg) {
        super(msg);
    }

}

/*
    (c) Copyright Hewlett-Packard Company 2002
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
