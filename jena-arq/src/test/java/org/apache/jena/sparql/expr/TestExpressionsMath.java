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

package org.apache.jena.sparql.expr;

import static org.apache.jena.sparql.expr.LibTestExpr.* ;

import org.apache.jena.sparql.ARQException ;
import org.junit.Test ;

/** Expression evaluation involving math: */
public class TestExpressionsMath
{
    @Test public void pi_01()           { testDouble("math:pi()", "3.1415926", 0.00001) ; }
    @Test(expected=ARQException.class)
    public void pi_02()           { testDouble("math:pi(1)", "3.1415926", 0.00001) ; }

    @Test public void exp_01()          { testDouble("math:exp(2)", Math.exp(2.0e0), 0.00001) ; }
    @Test public void exp_02()          { testDouble("math:exp(2.0)", Math.exp(2.0e0), 0.00001) ; }
    @Test public void exp_03()          { testDouble("math:exp(-2e1)", Math.exp(-2e1), 0.00001) ; }
    @Test public void exp_04()          { test("math:exp(1e0/0)", "'INF'^^xsd:double") ; }
    @Test public void exp_05()          { test("math:exp('INF'^^xsd:double)", "'INF'^^xsd:double") ; }
    @Test public void exp_06()          { test("math:exp('-INF'^^xsd:double)", "'0.0e0'^^xsd:double") ; }
    @Test public void exp_07()          { test("math:exp('NaN'^^xsd:double)", "'NaN'^^xsd:double") ; }

    @Test public void exp10_01()        { test("math:exp10(2)", "100") ; }
    @Test public void exp10_02()        { testDouble("math:exp10(-1)", 0.1, 0.00001) ; }
    @Test public void exp10_03()        { testDouble("math:exp10(2.0)", 100, 0.00001) ; }
    @Test public void exp10_04()        { test("math:exp10(0)", "1") ; }
    @Test public void exp10_05()        { testDouble("math:exp10('NaN'^^xsd:double)", Double.NaN, 0.0000001 ) ; }
    
    @Test public void log_01()          { testDouble("math:log(1)", Math.log(1), 0.0000001 ) ; }
    @Test public void log_02()          { testDouble("math:log('NaN'^^xsd:double)", Double.NaN, 0.0000001 ) ; }
    @Test public void log_03()          { test("math:log('INF'^^xsd:double)", "'INF'^^xsd:double") ; }
    @Test public void log_04()          { test("math:log(0)", "'-INF'^^xsd:double") ; }
    @Test public void log_05()          { test("math:exp('INF'^^xsd:double)", "'INF'^^xsd:double") ; }
    @Test public void log_06()          { test("math:exp('-INF'^^xsd:double)", "'0.0e0'^^xsd:double") ; }
    @Test public void log_07()          { test("math:exp('NaN'^^xsd:double)", "'NaN'^^xsd:double") ; }
    
    @Test public void pow_01()          { test("math:pow(2,2)", "4") ; } 
    @Test public void pow_02()          { testDouble("math:pow(2,-2)", 0.25, 0.00001) ; }
    @Test public void pow_03()          { test("math:pow(2,0)", "1") ; }
    
    @Test public void pow_10()          { test("math:pow('INF'^^xsd:double, 1)", "'INF'^^xsd:double") ; }
    @Test public void pow_11()          { test("math:pow(1, 'INF'^^xsd:double)", "1") ; }
    @Test public void pow_12()          { test("math:pow(1e0, 'INF'^^xsd:double)", "'1.0e0'^^xsd:double") ; }
    
    @Test public void pow_13()          { test("math:pow('INF'^^xsd:double,0)", "'1.0e0'^^xsd:double") ; }
    @Test public void pow_14()          { test("math:pow('-INF'^^xsd:double, 0)", "'1.0e0'^^xsd:double") ; }
    @Test public void pow_15()          { test("math:pow('NaN'^^xsd:double, 1)", "'NaN'^^xsd:double") ; }
    @Test public void pow_16()          { test("math:pow(1, 'NaN'^^xsd:double)", "'NaN'^^xsd:double") ; }
    
    @Test public void sqrt_01()         { test("math:sqrt(1)", "'1.0e0'^^xsd:double") ; }
    @Test public void sqrt_02()         { testDouble("math:sqrt(2)", Math.sqrt(2), 0.000001) ; }
    @Test public void sqrt_03()         { test("math:sqrt(-2)", "'NaN'^^xsd:double") ; }
    
    @Test(expected=ARQException.class)
    public void sqrt_04()               { test("math:sqrt('TWO')", "'dummy'") ; }
    
    @Test public void sqrt_10()         { test("math:sqrt('INF'^^xsd:double)", "'INF'^^xsd:double") ; }
    @Test public void sqrt_11()         { test("math:sqrt('-INF'^^xsd:double)", "'NaN'^^xsd:double") ; }
    @Test public void sqrt_12()         { test("math:sqrt('NaN'^^xsd:double)", "'NaN'^^xsd:double") ; }

    //  4.8.7 math:sqrt
//  4.8.8 math:sin
//  4.8.9 math:cos
//  4.8.10 math:tan
//  4.8.11 math:asin
//  4.8.12 math:acos
//  4.8.13 math:atan
//  4.8.14 math:atan2
    
    
    // Yes - atan uses (Y,X)
    @Test public void atan2_01()        { testDouble("math:atan2(0,1)", "0.0e0", 0.00001) ; }
    @Test public void atan2_02()        { testDouble("math:atan2(1,0)", Math.PI/2, 0.00001) ; }
    @Test public void atan2_03()        { testDouble("math:atan2(2.0,0.0)", Math.PI/2, 0.00001) ; }
    @Test public void atan2_04()        { testDouble("math:atan2(-2.0e1, 0.0)", - Math.PI/2, 0.00001) ; } 
}
