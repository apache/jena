/******************************************************************
 * File:        BackchainFRule.java
 * Created by:  Dave Reynolds
 * Created on:  26-Jan-03
 * 
 * (c) Copyright 2003, 2004 Hewlett-Packard Development Company, LP
 * [See end of file]
 * $Id: BackchainFRule.java,v 1.6 2004-12-07 09:56:19 andy_seaborne Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rdfsReasoner1;

import com.hp.hpl.jena.reasoner.*;

/**
 * Variant on a forward rule which instantiates a backward rule
 * and registers it with the reasoner.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.6 $ on $Date: 2004-12-07 09:56:19 $
 */
public class BackchainFRule extends BaseFRule {

    /**
     * Constructor
     */
    public BackchainFRule(String spec) {
        super(spec);
    }
    
    /**
     * Assert each of the instantiated Triples into the reasoner
     * triple cache.
     */
    void fire(TriplePattern[] body, RDFSInfGraph reasoner) {
        reasoner.addBRule(new BRWRule(body[0], body[1]));
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
