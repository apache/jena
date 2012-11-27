/**
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

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.logging.Log ;
import org.junit.Test ;

import com.hp.hpl.jena.sparql.expr.nodevalue.NodeValueOps ;

public class TestNodeValueOps extends BaseTest
{
    // ** Addition
    // Numerics
    @Test public void nv_add_1() { testAdd("12", "13", "'25'^^xsd:integer" ) ; }
    @Test public void nv_add_2() { testAdd("'12'^^xsd:decimal", "13", "'25'^^xsd:decimal" ) ; }
    @Test public void nv_add_3() { testAdd("'12.0'^^xsd:decimal", "13", "'25.0'^^xsd:decimal" ) ; }
    @Test public void nv_add_4() { testAdd("12e0", "13", "25.0e0" ) ; }
    
    // Strings
    @Test public void nv_add_10() { testAdd("'12'", "'13'", "'1213'" ) ; }

    //Durations (need to test the wiring, not whether the calculation is right)
    @Test public void nv_add_20() { testAdd("'PT1H'^^xsd:duration", "'PT1H'^^xsd:duration", "'PT2H'^^xsd:duration" ) ; }
  
    @Test public void nv_add_21() { testAdd("'PT1H'^^xsd:dayTimeDuration", "'PT1H'^^xsd:dayTimeDuration", "'PT2H'^^xsd:dayTimeDuration" ) ; }
    
    // Outside the XSD spec.
    //@Test public void nv_add_22() { testAdd("'P1Y'^^xsd:yearMonthDuration", "'PT4H'^^xsd:dayTimeDuration", "'P1YT4H'^^xsd:duration" ) ; }
    @Test public void nv_add_22() {
        try { 
            testAdd("'P1Y'^^xsd:yearMonthDuration", "'PT4H'^^xsd:dayTimeDuration", "'P1YT4H'^^xsd:duration" ) ;
        } catch (NullPointerException ex) {
            if ( isProbablyIBMJVM() )
                // IBM JDK causes NPE on this one.
                // IllegalStateException is acceptable; NullPointerException is not. 
                Log.warn(this, "TestNodeValueOps.nv_add_22 - IBM JVM - reported issue in the support for xsd:xsd:yearMonthDuration/xsd:dayTimeDuration") ;
            else
                throw ex ;
        }
        catch (IllegalStateException ex) {}
    }

    private static boolean isProbablyIBMJVM()
    {
        return System.getProperty("java.vm.name", "").contains("IBM");
    }
    
    // Date/time + duration
    @Test public void nv_add_23() { testAdd("'2000-01-01'^^xsd:date", "'P1Y'^^xsd:duration", "'2001-01-01'^^xsd:date") ; }
    @Test public void nv_add_24() { testAdd("'2000-01-01T00:00:00Z'^^xsd:dateTime", 
                                            "'P1Y1M'^^xsd:yearMonthDuration", 
                                            "'2001-02-01T00:00:00Z'^^xsd:dateTime") ; }
    @Test public void nv_add_25() { testAdd("'2000-01-01T00:00:00Z'^^xsd:dateTime", 
                                            "'P1Y1M1DT1H1M1.1S'^^xsd:duration", 
                                            "'2001-02-02T01:01:01.1Z'^^xsd:dateTime") ; }
    @Test public void nv_add_26() { testAdd("'00:00:00'^^xsd:time", 
                                            "'PT1H2M3.4S'^^xsd:duration", 
                                            "'01:02:03.4'^^xsd:time") ; }
    
    // Bad mixes
    @Test(expected=ExprEvalException.class) 
    public void nv_add_50() { testAdd("'12'", "13") ; }

    @Test(expected=ExprEvalException.class) 
    public void nv_add_51() { testAdd("'12'", "'PT1H'^^xsd:duration") ; }
    
    @Test(expected=ExprEvalException.class) 
    public void nv_add_52() { testAdd("'2012-04-05'^^xsd:date", "'2012-04-05'^^xsd:date") ; }
    
    // ** Subtraction
    // Numerics
    @Test public void nv_sub_1() { testSub("12", "13", "-1" ) ; }
    @Test public void nv_sub_2() { testSub("12", "13.0", "-1.0" ) ; }
    @Test public void nv_sub_3() { testSub("12e0", "13.0", "-1.0e0" ) ; }
    
    // Durations
    //Durations (need to test the wiring, not whether the calculation is right)
    @Test public void nv_sub_20() { testSub("'PT2H'^^xsd:duration", "'PT1H'^^xsd:duration", "'PT1H'^^xsd:duration" ) ; }
    @Test public void nv_sub_21() { testSub("'PT2H'^^xsd:dayTimeDuration", "'PT1H'^^xsd:dayTimeDuration", "'PT1H'^^xsd:dayTimeDuration" ) ; }
    
    //@Test public void nv_sub_22() { testSub("'P2Y'^^xsd:yearMonthDuration", "'P1Y'^^xsd:yearMonthDuration", "'P1Y'^^xsd:yearMonthDuration" ) ; }
    @Test public void nv_sub_22() {
        try { 
            testSub("'P2Y'^^xsd:yearMonthDuration", "'P1Y'^^xsd:yearMonthDuration", "'P1Y'^^xsd:yearMonthDuration" ) ;
        } catch (NullPointerException ex) {
            if ( isProbablyIBMJVM() )
                // IBM JDK causes NPE on this one.
                Log.warn(this, "TestNodeValueOps.nv_sub_22 - IBM JVM - reported issue in the support for xsd:xsd:yearMonthDuration/xsd:dayTimeDuration") ;
            else
                throw ex ;
        }
        catch (IllegalStateException ex) {}
    }
    
    @Test public void nv_sub_23() { testSub("'P3D'^^xsd:dayTimeDuration", "'PT4H'^^xsd:dayTimeDuration", "'P2DT20H'^^xsd:dayTimeDuration" ) ; }
    
    // Date/time - duration
    @Test public void nv_sub_30() { testSub("'2000-01-01'^^xsd:date", "'P1Y'^^xsd:duration", "'1999-01-01'^^xsd:date") ; }
    @Test public void nv_sub_31() { testSub("'2000-01-01T00:00:00Z'^^xsd:dateTime", 
                                            "'P1Y1M'^^xsd:yearMonthDuration", 
                                            "'1998-12-01T00:00:00Z'^^xsd:dateTime") ; }
    @Test public void nv_sub_32() { testSub("'2000-01-01T00:00:00Z'^^xsd:dateTime", 
                                            "'P1Y1M1DT1H1M1.1S'^^xsd:duration", 
                                            "'1998-11-29T22:58:58.9Z'^^xsd:dateTime") ; }
    
    @Test public void nv_sub_33() { testSub("'10:11:12'^^xsd:time", 
                                            "'PT1H2M3.4S'^^xsd:duration", 
                                            "'09:09:08.6'^^xsd:time") ; }
    // Date/time - date/time
    
    // Bad
    @Test(expected=ExprEvalException.class) 
    public void nv_sub_50() { testSub("'12'", "'13'" ) ; }

    // ** Multiplication
    
    @Test public void nv_mult_1() { testMult("12", "13", "156" ) ; }
    @Test public void nv_mult_2() { testMult("-12", "13.0", "-156.0" ) ; }
    @Test public void nv_mult_3() { testMult("'PT1H2M'^^xsd:duration", "2", "'PT2H4M'^^xsd:dayTimeDuration") ; }
    
    // ** Division
    @Test public void nv_div_1() { testDiv("12", "2", "6.0" ) ; }
    @Test public void nv_div_2() { testDiv("12", "2e0", "6.0e0" ) ; }
    
    // == Workers
    
    static void testAdd(String s1, String s2, String s3)
    {
        NodeValue nv3 = NodeValue.parse(s3) ;
        NodeValue nv = testAdd(s1, s2) ;
        assertEquals(nv3, nv) ;
    }
    
    static NodeValue testAdd(String s1, String s2)
    {
        NodeValue nv1 = NodeValue.parse(s1) ;
        NodeValue nv2 = NodeValue.parse(s2) ;
        return NodeValueOps.additionNV(nv1, nv2) ;
    }
    
    static void testSub(String s1, String s2, String s3)
    {
        NodeValue nv3 = NodeValue.parse(s3) ;
        NodeValue nv = testSub(s1, s2) ;
        assertEquals(nv3, nv) ;
    }
    
    static NodeValue testSub(String s1, String s2)
    {
        NodeValue nv1 = NodeValue.parse(s1) ;
        NodeValue nv2 = NodeValue.parse(s2) ;
        return NodeValueOps.subtractionNV(nv1, nv2) ;
    }

    static void testMult(String s1, String s2, String s3)
    {
        NodeValue nv3 = NodeValue.parse(s3) ;
        NodeValue nv = testMult(s1, s2) ;
        assertEquals(nv3, nv) ;
    }
    
    static NodeValue testMult(String s1, String s2)
    {
        NodeValue nv1 = NodeValue.parse(s1) ;
        NodeValue nv2 = NodeValue.parse(s2) ;
        return NodeValueOps.multiplicationNV(nv1, nv2) ;
    }

    static void testDiv(String s1, String s2, String s3)
    {
        NodeValue nv3 = NodeValue.parse(s3) ;
        NodeValue nv = testDiv(s1, s2) ;
        assertEquals(nv3, nv) ;
    }
    
    static NodeValue testDiv(String s1, String s2)
    {
        NodeValue nv1 = NodeValue.parse(s1) ;
        NodeValue nv2 = NodeValue.parse(s2) ;
        return NodeValueOps.divisionNV(nv1, nv2) ;
    }

}

