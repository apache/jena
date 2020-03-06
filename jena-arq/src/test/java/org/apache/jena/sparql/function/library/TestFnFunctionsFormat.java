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

package org.apache.jena.sparql.function.library;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.apache.jena.sparql.expr.*;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.sys.JenaSystem ;
import org.junit.Assert;
import org.junit.Test ;

public class TestFnFunctionsFormat {

    static { JenaSystem.init(); }
    
    /* The French grouping separator changed at Java13 because OpenJDK updated
     * CLDR - Unicode Common Locale Data Repository.
     * https://bugs.openjdk.java.net/browse/JDK-8225247
     * 
     * In the French locale, the grouping separator changes from U+00A0 ==> U+202F
     * Portable tests ...
     */
    private static String GroupSepFR = Character.toString(DecimalFormatSymbols.getInstance(Locale.FRENCH).getGroupingSeparator());
    
    @Test public void formatNumber_01()     { testNumberFormat("fn:format-number(0,'#')", "0") ; }
    @Test public void formatNumber_02()     { testNumberFormat("fn:format-number(1234, '#')", "1234") ; }
    @Test public void formatNumber_03()     { testNumberFormat("fn:format-number(1234, '#,###')", "1,234") ; }
    @Test public void formatNumber_04()     { testNumberFormat("fn:format-number(1e3, '#,###,###.#')", "1,000") ; }
    @Test public void formatNumber_05()     { testNumberFormat("fn:format-number(10.5, '##.#')", "10.5") ; }
    @Test public void formatNumber_06()     { testNumberFormat("fn:format-number(-10.5, '##.##')", "-10.5") ; }
    @Test public void formatNumber_08()     { testNumberFormat("fn:format-number(123, 'NotAPattern')", "NotAPattern123") ; }
    
    @Test public void formatNumber_11()     { testNumberFormat("fn:format-number(0, '#', 'fr')", "0") ; }
    // No-break space
    @Test public void formatNumber_12()     { testNumberFormat("fn:format-number(1234.5,'#,###.#', 'fr')", "1"+GroupSepFR+"234,5") ; }
    @Test public void formatNumber_13()     { testNumberFormat("fn:format-number(1234.5,'#,###.#', 'de')", "1.234,5") ; }
    
    @Test public void formatNumber_14()     { testNumberFormat("fn:format-number(12, '0,000.0', 'en')", "0,012.0") ; }
    @Test public void formatNumber_15()     { testNumberFormat("fn:format-number(0, '00,000', 'fr')", "00"+GroupSepFR+"000") ; }

    @Test(expected=ExprEvalException.class)
    public void formatNumber_20() {
        // String as number
        testNumberFormat("fn:format-number('String', '#')", null) ;
    }
    @Test(expected=ExprEvalException.class)
    public void formatNumber_21() {
        // Pattern is not a string
        testNumberFormat("fn:format-number(123, <uri>)", null) ; 
    }
    @Test(expected=ExprEvalException.class)
    public void formatNumber_22() {
        // Locale is not a string
        testNumberFormat("fn:format-number(123, '###', 123)", null) ; 
    }

    public void formatNumber_23() {
        // Not a locale - default to Locale.ROOT
        testNumberFormat("fn:format-number(123, '###', 'WhereAmI?')", null) ; 
    }
    private static void testNumberFormat(String expression, String expected) {
        Expr expr = ExprUtils.parse(expression) ;
        NodeValue r = expr.eval(null, LibTestExpr.createTest()) ;
        Assert.assertTrue(r.isString());
        Assert.assertEquals(expected, r.getString()) ;
    }

}
