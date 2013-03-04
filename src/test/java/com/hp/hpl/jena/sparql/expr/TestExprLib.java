/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.sparql.expr;

import org.junit.Assert ;
import org.junit.Test ;

import com.hp.hpl.jena.sparql.util.ExprUtils ;


/** Break expression testing suite into parts
 * @see TestExpressions
 * @see TestExprLib
 * @see TestNodeValue
 */
public class TestExprLib
{
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
