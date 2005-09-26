/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.iri;

import java.net.URISyntaxException;

import org.xml.sax.Locator;

import com.hp.hpl.jena.rdf.arp.MalformedURIException;

/**
 * Unstable code.
 * @author Jeremy J. Carroll
 *
 */
public class IRIException extends RuntimeException {

    final int conformanceLevel;
    
//    public IRIException() {
//        super();
//    }

    /**
     * Not part of API.
     */
    public IRIException(int lvl,IRIFactory f,String message) {
        super(message);
        conformanceLevel = lvl;
        Locator l = f.locator;
        file =l.getSystemId();
        line = l.getLineNumber();
        column = l.getColumnNumber();
    }

//    public IRIException(int lvl, String message, Throwable cause) {
//        super(message, cause);
//        conformanceLevel = lvl;
//    }
    /**
     * Not part of API.
     */
    public IRIException(int lvl, IRIFactory f, URISyntaxException cause) {
        this(lvl,f,(Exception)cause);
   }
    /**
     * Not part of API.
     */
    public IRIException(int lvl, IRIFactory f, MalformedURIException cause) {
        this(lvl,f,(Exception)cause);
    }
    final int line;
    final int column;
    final String file;
    private IRIException(int lvl, IRIFactory fact, Exception e) {
        super(e);
        conformanceLevel = lvl;
        Locator l = fact.locator;
        file =l.getSystemId();
        line = l.getLineNumber();
        column = l.getColumnNumber();
        
    }
    /**
     * Indicates the specification that has been broken.
     * 
     */
    public int getConformance() {
        return conformanceLevel;
    }

}


/*
 *  (c) Copyright 2005 Hewlett-Packard Development Company, LP
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
 
