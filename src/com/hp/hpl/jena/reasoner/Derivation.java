/******************************************************************
 * File:        Derivation.java
 * Created by:  Dave Reynolds
 * Created on:  06-Apr-03
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: Derivation.java,v 1.4 2003-06-04 08:09:49 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner;

import java.io.PrintWriter;

/**
 * Derivation records are used to determine how an inferred triple
 * was derived from a set of source triples and a reasoner. SubClasses
 * provide more specific information.
 * 
 * <p>A future option might be to generate an RDF description of
 * the derivation trace. </p>
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.4 $ on $Date: 2003-06-04 08:09:49 $
 */
public interface Derivation {

    /**
     * Return a short-form description of this derivation.
     */
    public String toString();
    
    /**
     * Print a deep traceback of this derivation back to axioms and 
     * source assertions.
     * @param out the stream to print the trace out to
     * @param bindings set to true to print intermediate variable bindings for
     * each stage in the derivation
     */
    public void printTrace(PrintWriter out, boolean bindings);
}

/*
    (c) Copyright Hewlett-Packard Company 2003
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
