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

import org.apache.jena.atlas.junit.BaseTest ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.query.ARQ ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;

public class TestRegex extends BaseTest
{
    @BeforeClass
    public static void beforeClass() {
        if ( false )
            ARQ.getContext().set(ARQ.regexImpl, ARQ.xercesRegex) ;
    }
    
    @Test public void testRegex1() { regexTest("ABC", "ABC", null, true) ; }
    @Test public void testRegex2() { regexTest("ABC", "abc", null, false) ; }
    @Test public void testRegex3() { regexTest("ABC", "abc", "", false) ; }
    @Test public void testRegex4() { regexTest("ABC", "abc", "i", true) ; }
    @Test public void testRegex5() { regexTest("abc", "B", "i", true) ; }
    @Test public void testRegex6() { regexTest("ABC", "^ABC", null, true) ; }
    @Test public void testRegex7() { regexTest("ABC", "BC", null, true) ; }
    @Test public void testRegex8() { regexTest("ABC", "^BC", null, false) ; }

    public void regexTest(String value, String pattern, String flags, boolean expected)
    {
        Expr s = NodeValue.makeString(value) ;
        
        E_Regex r = new E_Regex(s, pattern, flags) ;
        NodeValue nv = r.eval(BindingFactory.binding(), null) ;
        boolean b = nv.getBoolean() ;
        if ( b != expected )
            fail(fmtTest(value, pattern, flags)+" ==> "+b+" expected "+expected) ;
    }

    private String fmtTest(String value, String pattern, String flags)
    {
        String tmp = "regex(\""+value+"\", \""+pattern+"\"" ;
        if ( flags != null )
            tmp = tmp + "\""+flags+"\"" ;
        tmp = tmp + ")" ;
        return tmp ; 
    }
    
}
