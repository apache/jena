/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package dev;

import static org.junit.Assert.* ;

import com.hp.hpl.jena.sparql.expr.Expr ;
import com.hp.hpl.jena.sparql.util.ExprUtils ;

import org.junit.Test ;

public class TestExprLib
{
    @Test public void safeEqualityNot_01()     { testSafeEquality("123", false) ;}
    @Test public void safeEqualityNot_02()     { testSafeEquality("?x != <y>", false) ;}
    @Test public void safeEqualityNot_03()     { testSafeEquality("<x> = <y>", false) ;}
    
    @Test public void safeSameTerm_01()     { testSafeEquality("sameTerm(?x, <x>)", true) ;}
    @Test public void safeSameTerm_02()     { testSafeEquality("sameTerm(<x>, ?x)", true) ;}

    
    @Test public void safeEquality_01()     { testSafeEquality("?x = <x>", true) ;}
    @Test public void safeEquality_02()     { testSafeEquality("<x> = ?x", true) ;}
    
    // 
    @Test public void safeEquality_03()     { testSafeEquality("<x> = 'xyz'", true, true, true) ;}
    @Test public void safeEquality_04()     { testSafeEquality("<x> = 'xyz'", false, false, true) ;}

    @Test public void safeEquality_05()     { testSafeEquality("<x> = 123", false, false, true) ;}
    @Test public void safeEquality_06()     { testSafeEquality("<x> = 123", false, true, false) ;}

    
    
    //@Test public void safeEquality_03()     { testSafeEquality("", false) ;}
//    @Test public void safeEquality_04()     { testSafeEquality("123", false) ;}
//    @Test public void safeEquality_05()     { testSafeEquality("123", false) ;}
//    @Test public void safeEquality_06()     { testSafeEquality("123", false) ;}
//    @Test public void safeEquality_07()     { testSafeEquality("123", false) ;}
//    @Test public void safeEquality_08()     { testSafeEquality("123", false) ;}
//    @Test public void safeEquality_09()     { testSafeEquality("123", false) ;}
//    @Test public void safeEquality_10()     { testSafeEquality("123", false) ;}
//    @Test public void safeEquality_11()     { testSafeEquality("123", false) ;}

    
    private static void testSafeEquality(String string, boolean b)
    {
        Expr expr = ExprUtils.parse(string) ;
        assertEquals(string, b, ExprLib.isAssignmentSafeEquality(expr)) ;
    }
    
    private static void testSafeEquality(String string, boolean b, boolean graphString, boolean graphNumber)
    {
        Expr expr = ExprUtils.parse(string) ;
        assertEquals(string, b, ExprLib.isAssignmentSafeEquality(expr, graphString, graphNumber)) ;
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