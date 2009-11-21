/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql;


import junit.framework.TestSuite ;

import com.hp.hpl.jena.sparql.core.TestContext ;
import com.hp.hpl.jena.sparql.core.TestEsc ;
import com.hp.hpl.jena.sparql.expr.TS_Expr ;
import com.hp.hpl.jena.sparql.expr.TestExpressions ;
import com.hp.hpl.jena.sparql.path.TestPath ;
import com.hp.hpl.jena.sparql.syntax.TestResultSet ;
import com.hp.hpl.jena.sparql.syntax.TestSerialization ;
import com.hp.hpl.jena.sparql.util.TS_Util ;

public class TS_General extends TestSuite
{
    static final String testSetName         = "General" ;

    static public TestSuite suite() { return new TS_General(); }

    public TS_General()
    {
        super(testSetName) ;

        addTest(TS_Expr.suite()) ;
        addTest(TestExpressions.suite()) ;  // JUnit3 style tests.
        
        addTest(TS_Util.suite()) ;
        
        addTest(TestPath.suite()) ;
        addTest(TestEsc.suite()) ;
        addTest(TestResultSet.suite()) ;
        addTest(TestSerialization.suite()) ;
        
        addTest(TestContext.suite()) ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
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