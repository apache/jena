/******************************************************************
 * File:        LPRuleSyntaxException.java
 * Created by:  Dave Reynolds
 * Created on:  25-Jul-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: LPRuleSyntaxException.java,v 1.1 2003-08-21 12:04:45 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.impl;

import com.hp.hpl.jena.reasoner.ReasonerException;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * Exception used to indicate syntactic errors the LP version of the
 * backward chaining rule engine.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.1 $ on $Date: 2003-08-21 12:04:45 $
 */
public class LPRuleSyntaxException extends ReasonerException {

    
    /**
     * Constructor.
     * @param msg a free-text message describing the problem
     */
    public LPRuleSyntaxException(String msg, Rule rule) {
        super("Syntax error in backward rule: " + rule.toShortString() 
                +"\n" + msg);
    }
    
    /**
     * Constructor.
     * @param msg a free-text message describing the problem
     * @param cause a nested exception which prompted this error
     */
    public LPRuleSyntaxException(String msg, Rule rule, Throwable cause) {
        super("Syntax error in backward rule: " + rule.toShortString() 
                +"\n" + msg, cause);
    }
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