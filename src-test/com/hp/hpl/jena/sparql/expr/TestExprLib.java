/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.expr;

import junit.framework.Assert ;
import junit.framework.JUnit4TestAdapter ;
import org.junit.Test ;

import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.expr.ExprLib ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;


public class TestExprLib
{
    public static junit.framework.Test suite()
    {
        return new JUnit4TestAdapter(TestExprLib.class) ;
    }
    
    @Test public void safeEqualityNot_01()      { testSafeEquality("123", false) ;}
    @Test public void safeEqualityNot_02()      { testSafeEquality("?x != <y>", false) ;}
    @Test public void safeEqualityNot_03()      { testSafeEquality("<x> = <y>", false) ;}
    
    @Test public void safeSameTerm_01()         { testSafeEquality("sameTerm(?x, <x>)", true) ;}
    @Test public void safeSameTerm_02()         { testSafeEquality("sameTerm(<x>, ?x)", true) ;}
    
    @Test public void safeSameTerm_03()         { testSafeEquality("sameTerm(?x, 'xyz')", false, true, true) ;}
    @Test public void safeSameTerm_04()         { testSafeEquality("sameTerm(?x, 'xyz')", true, false, false) ;}

    @Test public void safeSameTerm_05()         { testSafeEquality("sameTerm(?x, 'xyz'^^xsd:string)", false, true, true) ;}
    @Test public void safeSameTerm_06()         { testSafeEquality("sameTerm(?x, 'xyz'^^xsd:string)", true, false, false) ;}

    @Test public void safeSameTerm_07()         { testSafeEquality("sameTerm(?x, 'xyz'@en)", true, true, true) ;}
    @Test public void safeSameTerm_08()         { testSafeEquality("sameTerm(?x, 'xyz'@en)", true, false, false) ;}

    @Test public void safeSameTerm_09()         { testSafeEquality("sameTerm(?x, 123)", false, true, true) ;}
    @Test public void safeSameTerm_10()         { testSafeEquality("sameTerm(?x, 123)", true, false, false) ;}

    @Test public void safeSameTerm_11()         { testSafeEquality("sameTerm(?x, 'foo'^^<http://example>)", true, false, false) ;}
    @Test public void safeSameTerm_12()         { testSafeEquality("sameTerm(?x, 'foo'^^<http://example>)", true, true, true) ;}
    
    @Test public void safeEquality_01()         { testSafeEquality("?x = <x>", true) ;}
    @Test public void safeEquality_02()         { testSafeEquality("<x> = ?x", true) ;}
    
    @Test public void safeEquality_03()         { testSafeEquality("?x = 'xyz'", true, true, true) ;}
    @Test public void safeEquality_04()         { testSafeEquality("?x = 'xyz'", false, false, true) ;}

    @Test public void safeEquality_05()         { testSafeEquality("?x = 'xyz'^^xsd:string", true, true, true) ;}
    @Test public void safeEquality_06()         { testSafeEquality("?x = 'xyz'^^xsd:string", false, false, true) ;}

    @Test public void safeEquality_07()         { testSafeEquality("?x = 'xyz'@en", true, true, true) ;}
    @Test public void safeEquality_08()         { testSafeEquality("?x = 'xyz'@en", true, false, false) ;}

    @Test public void safeEquality_09()         { testSafeEquality("?x = 123", true, true, true) ;}
    @Test public void safeEquality_10()         { testSafeEquality("?x = 123", false, true, false) ;}

    @Test public void safeEquality_11()         { testSafeEquality("?x = 'foo'^^<http://example>", true, false, false) ;}
    @Test public void safeEquality_12()         { testSafeEquality("?x = 'foo'^^<http://example>", true, true, true) ;}
    
    private static void testSafeEquality(String string, boolean b)
    {
        Expr expr = ExprUtils.parse(string) ;
        Assert.assertEquals(string, b, ExprLib.isAssignmentSafeEquality(expr)) ;
    }
    
    private static void testSafeEquality(String string, boolean b, boolean graphString, boolean graphNumber)
    {
        Expr expr = ExprUtils.parse(string) ;
        Assert.assertEquals(string, b, ExprLib.isAssignmentSafeEquality(expr, graphString, graphNumber)) ;
    }

}

/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
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