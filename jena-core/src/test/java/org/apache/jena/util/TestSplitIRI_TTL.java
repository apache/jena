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

package org.apache.jena.util;

import static org.apache.jena.util.SplitIRI.* ;

import java.util.Objects ;

import junit.framework.JUnit4TestAdapter ;
import org.junit.Assert ;
import org.junit.Test ;

/** Test splitting IRI strings using Turtle rules.
 */
public class TestSplitIRI_TTL {
    public static junit.framework.Test suite() {
        return new JUnit4TestAdapter(TestSplitIRI_TTL.class) ;
    }
    
    // Basics
    @Test public void split_basic_00() { testSplit("http://example/foo", "http://example/".length()) ; }
    @Test public void split_basic_01() { testPrefixLocalname("http://example/foo",            "http://example/",      "foo"       ) ; }
    @Test public void split_basic_02() { testPrefixLocalname("http://example/foo#bar",        "http://example/foo#",  "bar"       ) ; }
    @Test public void split_basic_03() { testPrefixLocalname("http://example/foo#",           "http://example/foo#",  ""          ) ; }
    @Test public void split_basic_04() { testPrefixLocalname("http://example/",               "http://example/",      ""          ) ; }
    @Test public void split_basic_05() { testPrefixLocalname("http://example/1abc",           "http://example/",      "1abc"      ) ; }
    @Test public void split_basic_06() { testPrefixLocalname("http://example/1.2.3.4",        "http://example/",      "1.2.3.4"   ) ; }
    @Test public void split_basic_07() { testPrefixLocalname("http://example/xyz#1.2.3.4",    "http://example/xyz#",  "1.2.3.4"   ) ; }
    @Test public void split_basic_08() { testPrefixLocalname("http://example/xyz#_abc",       "http://example/xyz#",  "_abc"      ) ; }
    @Test public void split_basic_09() { testPrefixLocalname("http://example/xyz/_1.2.3.4",   "http://example/xyz/", "_1.2.3.4"   ) ; }
    
    // Relative URIs 
    @Test public void split_rel_1() { testPrefixLocalname("xyz/_1.2.3.4",  "xyz/", "_1.2.3.4" ) ; }
    @Test public void split_rel_2() { testPrefixLocalname("xyz",           "",     "xyz" ) ; }
    @Test public void split_rel_3() { testPrefixLocalname("",              "",     "" ) ; }
    
    // Bizarre but legal URIs
    @Test public void split_weird_1() { testPrefixLocalname("abc:def",       "abc:", "def" ) ; }
    

    // Turtle details.
    // "." leading dot is not legal.
    @Test public void split_ttl_01() { testPrefixLocalname("http://example/foo#bar:baz",    "http://example/foo#",  "bar:baz"   ) ; }
    @Test public void split_ttl_02() { testPrefixLocalname("http://example/a:b:c",          "http://example/",      "a:b:c"     ) ; }
    @Test public void split_ttl_03() { testPrefixLocalname("http://example/.2.3.4",         "http://example/.",     "2.3.4"     ) ; }

    // "." leading dot is not legal.
    @Test public void split_ttl_04() { testPrefixLocalname("abc:xyz/.def",       "abc:xyz/.", "def" ) ; }
    // "-" leading dash is not legal.
    @Test public void split_ttl_05() { testPrefixLocalname("abc:xyz/-def",       "abc:xyz/-", "def" ) ; }
    @Test public void split_ttl_06() { testPrefixLocalname("abc:xyz/-.-.-def",       "abc:xyz/-.-.-", "def" ) ; }
    @Test public void split_ttl_07() { testPrefixLocalname("http://example/id=89",          "http://example/",      "id=89"   ) ; }
    // Trailing '.'
    @Test public void split_ttl_08() { testPrefixLocalname("http://example/2.3.",           "http://example/",      "2.3."     ) ; }
    
    // Turtle details, including escaping.
    // Test for PrefixLocalnameEsc
    @Test public void split_ttl_esc_01() { testPrefixLocalnameEsc("http://example/id=89",  "id\\=89"   ) ; }
    @Test public void split_ttl_esc_02() { testPrefixLocalnameEsc("http://example/a,b",  "a\\,b"   ) ; }
    // Trailing '.'
    @Test public void split_ttl_esc_033() { testPrefixLocalnameEsc("http://example/2.3.", "2.3\\."     ) ; }
    
    // URNs split differently.
    @Test public void split_urn_01() { testPrefixLocalname("urn:foo:bar",                   "urn:foo:",              "bar"       ) ; }

    // Anti-tests
    @Test public void split_51() { testPrefixLocalnameNot("http://example/foo#bar:baz", "http://example/foo#bar", "baz"     ) ; }

    
    
    private void testSplit(String string, int expected) {
        int i = splitpoint(string) ;
        Assert.assertEquals(expected, i) ;
    }

    // ??
    private void testTurtle(String string, String expectedPrefix, String expectedLocalname) {
        int i = splitpoint(string) ;
        String ns = string ;
        String ln = "" ;
        if ( i > 0 ) {
            ns = string.substring(0, i) ;
            ln = string.substring(i) ;
        }
        
        if ( expectedPrefix != null )
            Assert.assertEquals(expectedPrefix, ns);
        if ( expectedLocalname != null )
            Assert.assertEquals(expectedLocalname, ln);
        if (  expectedPrefix != null && expectedLocalname != null ) {
            String x = ns+ln ;
            Assert.assertEquals(string, x) ;
        }
    }

    private void testPrefixLocalnameNoSplit(String string) {
        int i = splitpoint(string) ;
        String msg = string ;
        if ( i != -1 ) {
            // Better error message.
            String ns = namespace(string) ;
            String ln = localname(string) ;
            msg = "Unexpected split of '"+string+"' into ("+ns+", "+ln+") [index="+i+"]" ;
        }
        Assert.assertEquals(msg, -1, i) ;
    }
    
    private void testPrefixLocalname(String string, String expectedPrefix, String expectedLocalname) {
        String ns = namespace(string) ;
        String ln = localname(string) ;

        if ( expectedPrefix != null )
            Assert.assertEquals(expectedPrefix, ns);
        if ( expectedLocalname != null )
            Assert.assertEquals(expectedLocalname, ln);
        if (  expectedPrefix != null && expectedLocalname != null ) {
            String x = ns+ln ;
            Assert.assertEquals(string, x) ;
        }
    }

    private void testPrefixLocalnameEsc(String string, String expectedLocalname) {
      String ln = localnameTTL(string) ;
      Assert.assertEquals(expectedLocalname, ln);
  }

    private void testPrefixLocalnameNot(String string, String expectedPrefix, String expectedLocalname) {
//      Node n = NodeFactory.createURI(string) ;
//      String ns = n.getNameSpace() ;
//      String ln = n.getLocalName() ;
        String ns = namespace(string) ;
        String ln = localname(string) ;

        boolean b1 = Objects.equals(expectedPrefix, ns) ;
        boolean b2 = Objects.equals(expectedLocalname, ln) ;

        // Test not both true.
        Assert.assertFalse("Wrong: ("+ns+","+ln+")", b1&&b2);
        // But it still combines.
        String x = ns+ln ;
        Assert.assertEquals(string, x) ;
    }
}

