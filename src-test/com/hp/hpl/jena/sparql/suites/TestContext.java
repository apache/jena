/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.suites;

import com.hp.hpl.jena.sparql.util.Context;
import com.hp.hpl.jena.sparql.util.Symbol;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/** com.hp.hpl.jena.query.util.test.TestContext
 * 
 * @author Andy Seaborne
 */

public class TestContext extends TestCase
{
    public static TestSuite suite()
    {
        TestSuite ts = new TestSuite(TestContext.class) ;
        ts.setName("TestContext") ;
        return ts ;
    }
    

    
    Symbol p1 = Symbol.create("p1") ;
    Symbol p2 = Symbol.create("p2") ;
    
    public void testCxt1() { Context cxt = new Context(); }
    
    public void testCxt2()
    { 
        Context cxt = new Context();
        assertTrue("Defined in empty context", !cxt.isDefined(p1)) ;
        cxt.set(p1, "v") ;
        assertTrue("Not defined after .set", cxt.isDefined(p1)) ;
        Object v = cxt.get(p1) ;
        assertSame("Not the same", "v", v) ;
    }

    public void testCxt3()
    { 
        Context cxt = new Context();
        cxt.set(p1, "v") ;
        cxt.setIfUndef(p1, "w") ;
        Object v = cxt.get(p1) ;
        assertSame("Not as first set", "v", v) ;
    }

    public void testCxt4()
    { 
        Context cxt = new Context();
        cxt.set(p1, "true") ;
        assertTrue("Not true", cxt.isTrue(p1)) ;
        assertTrue("Not true or undef", cxt.isTrueOrUndef(p1)) ;
        
        assertTrue("Not false or undef", cxt.isFalseOrUndef(p2)) ;
        assertTrue("False when undef", !cxt.isFalse(p2)) ;
    }

    public void testCxt5()
    { 
        Context cxt = new Context();
        cxt.set(p1, "false") ;
        assertTrue("Not false", cxt.isFalse(p1)) ;
        assertTrue("Not false or undef", cxt.isFalseOrUndef(p1)) ;
    }
    
    public void testCxt6()
    { 
        Context cxt = new Context();
        cxt.setTrue(p1) ;
        assertTrue("Not true", cxt.isTrue(p1)) ;
        String x = cxt.getAsString(p1) ;
        assertEquals("Not string 'true'", "true", x) ;
    }

}

/*
 * (c) Copyright 2004, 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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