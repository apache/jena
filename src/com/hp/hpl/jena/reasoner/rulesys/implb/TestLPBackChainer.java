/******************************************************************
 * File:        TestLPBackChainer.java
 * Created by:  Dave Reynolds
 * Created on:  25-Jul-2003
 * 
 * (c) Copyright 2003, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 * $Id: TestLPBackChainer.java,v 1.2 2003-08-11 22:08:31 der Exp $
 *****************************************************************/
package com.hp.hpl.jena.reasoner.rulesys.implb;

import com.hp.hpl.jena.reasoner.rulesys.test.TestBackchainer;
import com.hp.hpl.jena.reasoner.*;

import java.util.*;

import junit.framework.TestSuite;

/**
 * Run the original backchainer tests on an LP reasoner.
 * 
 * @author <a href="mailto:der@hplb.hpl.hp.com">Dave Reynolds</a>
 * @version $Revision: 1.2 $ on $Date: 2003-08-11 22:08:31 $
 */
public class TestLPBackChainer extends TestBackchainer {
    
    /**
     * Boilerplate for junit
     */ 
    public TestLPBackChainer( String name ) {
        super( name ); 
    }
    
    /**
     * Boilerplate for junit.
     * This is its own test suite
     */
    public static TestSuite suite() {
        return new TestSuite( TestLPBackChainer.class ); 
    }  

    /**
     * Override in subclasses to test other reasoners.
     */
    public Reasoner createReasoner(List rules) {
        LPBackwardRuleReasoner reasoner = new LPBackwardRuleReasoner(rules);
        reasoner.tablePredicate(sP);
        reasoner.tablePredicate(sC);
        reasoner.tablePredicate(ty);
        reasoner.tablePredicate(p);
        reasoner.tablePredicate(a);
        reasoner.tablePredicate(b);
        return reasoner;
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