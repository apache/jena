/******************************************************************
 * File:        ReasonerException.java
 * Created by:  Dave Reynolds
 * Created on:  09-Jan-2003
 * 
 * (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: ReasonerException.java,v 1.7 2004-12-07 09:56:29 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner;

import com.hp.hpl.jena.shared.JenaException;

/**
 * Exception signalling some generic problem with the reasoning subsystem.
 * Subclasses of this exception may be used to report more specific problems.
 * <p>In the future there may be a top level JenaException which this exception
 * should extend.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.7 $ on $Date: 2004-12-07 09:56:29 $
 */
public class ReasonerException extends JenaException {
    
    /**
     * Constructor.
     * @param msg a free-text message describing the problem
     */
    public ReasonerException(String msg) {
        super(msg);
    }
    
    /**
     * Constructor.
     * @param msg a free-text message describing the problem
     * @param cause a nested exception which prompted this error
     */
    public ReasonerException(String msg, Throwable cause) {
        super(msg, cause);
    }

}

/*
    (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
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

