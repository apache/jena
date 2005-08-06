/*
 *  (c) Copyright 2001, 2002, 2003, 2004, 2005 Hewlett-Packard Development Company, LP
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
 
 * * $Id: WrappedException.java,v 1.2 2005-08-06 06:14:50 jeremy_carroll Exp $
   
   AUTHOR:  Jeremy J. Carroll
*/
/*
 * WrappedException.java
 *
 * Created on July 10, 2001, 11:44 AM
 */

package com.hp.hpl.jena.rdf.arp.impl;

import java.io.IOException;

import org.xml.sax.SAXException;
/**
 * Wrap some other exception - being wise to SAXExceptions which
 * wrap something else.
 * @author  jjc
 
 */
class WrappedException extends java.lang.RuntimeException {
    /** Creates new WrappedException */
    WrappedException(SAXException e) {
        Exception in0 = e.getException();
        if ( in0 == null ) {
            initCause(e);
            return;
        }
        if ( (in0 instanceof RuntimeException) 
             || (in0 instanceof SAXException )
             || (in0 instanceof IOException ) )
            {
            initCause(in0);
            return;
        }
        initCause(e);
    }
    WrappedException(IOException e) {
        initCause(e);
    }
    /** Throw the exception,  falling back to be a wrapped SAXParseException.
     */
    void throwMe() throws IOException, SAXException {
        Throwable inner = this.getCause();
        if ( inner instanceof SAXException ) {
            throw (SAXException)inner;
        }  
        if ( inner instanceof IOException ) {
            throw (IOException)inner;
        }
        if ( inner instanceof RuntimeException ) {
            throw (RuntimeException)inner;
        }
        // I don't think this line is reachable:
        throw new RuntimeException("Supposedly unreacahble code.");
    }
    

}
