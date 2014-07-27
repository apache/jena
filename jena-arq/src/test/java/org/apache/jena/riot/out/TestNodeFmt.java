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

package org.apache.jena.riot.out;

import org.apache.jena.atlas.io.StringWriterI ;
import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.PrefixMapFactory ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.util.NodeFactoryExtra ;

public class TestNodeFmt extends BaseTest
{
    private static String base = "http://example.org/base" ;
    private static PrefixMap prefixMap = PrefixMapFactory.createForOutput() ;
    static {
        prefixMap.add(":", "http://example/p") ;
        prefixMap.add("ex", "http://example/ex/") ;
    }
    private static NodeFormatter nodeFormatterNTutf8 = new NodeFormatterNT(CharSpace.UTF8) ;
    private static NodeFormatter nodeFormatterNTascii = new NodeFormatterNT(CharSpace.ASCII) ;
    private static NodeFormatter nodeFormatterTTL = new NodeFormatterTTL(base, prefixMap) ;
    
    public static void test(NodeFormatter nodeFormatter, String str)
    {
        test(nodeFormatter, str, str) ;
    }

    public static void test(NodeFormatter nodeFormatter, String nStr , String str)
    {
        Node n = NodeFactoryExtra.parseNode(nStr) ;
        test(nodeFormatter, n, str) ;
    }

    public static void test(NodeFormatter nodeFormatter, Node n , String str)
    {
        StringWriterI sw = new StringWriterI() ;
        nodeFormatter.format(sw, n) ;
        String str2 = sw.toString() ;
        assertEquals(str, str2) ;
    }

    @Test public void nodefmt_nt_01()  { test(nodeFormatterNTutf8, "?x") ; }
    @Test public void nodefmt_nt_02()  { test(nodeFormatterNTutf8, "?xyz") ; }
    @Test public void nodefmt_nt_03()  { test(nodeFormatterNTutf8, Var.alloc(""), "?") ; }
    @Test public void nodefmt_nt_04()  { test(nodeFormatterNTutf8, Var.alloc("?"), "??") ; }

    @Test public void nodefmt_nt_05()  { test(nodeFormatterNTutf8, "'abc'", "\"abc\"") ; }
    @Test public void nodefmt_nt_05a() { test(nodeFormatterNTutf8, "\"abc\"") ; }
    @Test public void nodefmt_nt_06()  { test(nodeFormatterNTutf8, "\"\"") ; }
    @Test public void nodefmt_nt_06a() { test(nodeFormatterNTutf8, "''", "\"\"") ; }
    @Test public void nodefmt_nt_07()  { test(nodeFormatterNTutf8, "'abc'@en", "\"abc\"@en") ; }
    @Test public void nodefmt_nt_07a() { test(nodeFormatterNTutf8, "\"abc\"@en") ; }
    @Test public void nodefmt_nt_08()  { test(nodeFormatterNTutf8, "\"123\"^^<http://www.w3.org/2001/XMLSchema#integer>" ) ; }
    @Test public void nodefmt_nt_09()  { test(nodeFormatterNTutf8, Node.ANY, "ANY") ; }
    
    @Test public void nodefmt_nt_10()  { test(nodeFormatterNTutf8, "'Ω'", "\"Ω\"") ; }
    @Test public void nodefmt_nt_11()  { test(nodeFormatterNTascii, "'Ω'", "\"\\u03A9\"") ; }
    
    @Test public void nodefmt_nt_12()        { test(nodeFormatterNTascii,"<http://example/>") ; }
    @Test public void nodefmt_nt_13()        { test(nodeFormatterNTascii, "\"abc\"^^<http://example/dt>") ; }
    
    @Test public void nodefmt_nt_14()        { test(nodeFormatterNTascii, "'é'", "\"\\u00E9\"") ; }

    @Test public void nodefmt_nt_15()        { test(nodeFormatterNTascii, "'\\n\\t\\f'", "\"\\n\\t\\f\"") ; }
    
    

    
    
    @Test public void nodefmt_ttl_01()  { test(nodeFormatterTTL, "?x") ; }
    @Test public void nodefmt_ttl_02()  { test(nodeFormatterTTL, "?xyz") ; }
    @Test public void nodefmt_ttl_03()  { test(nodeFormatterTTL, Var.alloc(""), "?") ; }
    @Test public void nodefmt_ttl_04()  { test(nodeFormatterTTL, Var.alloc("?"), "??") ; }

    @Test public void nodefmt_ttl_05()  { test(nodeFormatterTTL, "\"abc\"") ; }
    @Test public void nodefmt_ttl_06()  { test(nodeFormatterTTL, "\"\"") ; }
    @Test public void nodefmt_ttl_07()  { test(nodeFormatterTTL, "\"abc\"@en") ; }
    @Test public void nodefmt_ttl_08()  { test(nodeFormatterTTL, Node.ANY, "ANY") ; }
    
    @Test public void nodefmt_ttl_11()  { test(nodeFormatterTTL, "<http://example.com/resources>") ; }    // No match
    @Test public void nodefmt_ttl_12()  { test(nodeFormatterTTL, "<http://example/ex/>", "ex:") ; }
    @Test public void nodefmt_ttl_13()  { test(nodeFormatterTTL, "<http://example/ex/abc>", "ex:abc") ; }
    @Test public void nodefmt_ttl_14()  { test(nodeFormatterTTL, "<http://example/ex/ab/c>", "<http://example/ex/ab/c>") ; }
    @Test public void nodefmt_ttl_15()  { test(nodeFormatterTTL, "<http://example/p>", ":") ; }
    @Test public void nodefmt_ttl_16()  { test(nodeFormatterTTL, "<http://example/p#a>", "<http://example/p#a>") ; }
    // Base URI
    @Test public void nodefmt_ttl_17()  { test(nodeFormatterTTL, "<http://example.org/foo>", "<foo>") ; }
    @Test public void nodefmt_ttl_18()  { test(nodeFormatterTTL, "<http://example.org/base#bar>", "<#bar>") ; }

    // Trailing DOT
    @Test public void nodefmt_ttl_19()  { test(nodeFormatterTTL, "<http://example/ex/abc.>", "<http://example/ex/abc.>") ; } 
    @Test public void nodefmt_ttl_20()  { test(nodeFormatterTTL, "<http://example/ex/abc.x>", "ex:abc.x") ; }
    @Test public void nodefmt_ttl_21()  { test(nodeFormatterTTL, "<http://example/ex/abc456.123>", "ex:abc456.123") ; }
    
    @Test public void nodefmt_ttl_29()  { test(nodeFormatterTTL, "'Ω'", "\"Ω\"") ; }
    
    @Test public void prefixedname_01() { testPrefix("", "") ; } 
    
    private void testPrefix(String prefix, String local)
    {
        assertTrue(NodeFormatterTTL.safeForPrefix(prefix)) ;
        assertTrue(NodeFormatterTTL.safeForPrefixLocalname(local)) ;
    }

    // Turtle numbers
    // Integers
    @Test public void nodefmt_ttl_30()  { test(nodeFormatterTTL, "'123'^^<http://www.w3.org/2001/XMLSchema#integer>", "123") ; }
    @Test public void nodefmt_ttl_31()  { test(nodeFormatterTTL, "'123.0'^^<http://www.w3.org/2001/XMLSchema#integer>", "\"123.0\"^^<http://www.w3.org/2001/XMLSchema#integer>") ; }
    @Test public void nodefmt_ttl_32()  { test(nodeFormatterTTL, "''^^<http://www.w3.org/2001/XMLSchema#integer>", "\"\"^^<http://www.w3.org/2001/XMLSchema#integer>") ; }
    @Test public void nodefmt_ttl_33()  { test(nodeFormatterTTL, "'abc'^^<http://www.w3.org/2001/XMLSchema#integer>", "\"abc\"^^<http://www.w3.org/2001/XMLSchema#integer>") ; }
    @Test public void nodefmt_ttl_34()  { test(nodeFormatterTTL, "'+123'^^<http://www.w3.org/2001/XMLSchema#integer>", "+123") ; }
    @Test public void nodefmt_ttl_35()  { test(nodeFormatterTTL, "'-1'^^<http://www.w3.org/2001/XMLSchema#integer>", "-1") ; }

    // Decimals
    @Test public void nodefmt_ttl_40()  { test(nodeFormatterTTL, "'123'^^<http://www.w3.org/2001/XMLSchema#decimal>", "\"123\"^^<http://www.w3.org/2001/XMLSchema#decimal>") ; }
    @Test public void nodefmt_ttl_41()  { test(nodeFormatterTTL, "'123.0'^^<http://www.w3.org/2001/XMLSchema#decimal>", "123.0") ; }
    @Test public void nodefmt_ttl_42()  { test(nodeFormatterTTL, "''^^<http://www.w3.org/2001/XMLSchema#decimal>", "\"\"^^<http://www.w3.org/2001/XMLSchema#decimal>") ; }
    @Test public void nodefmt_ttl_43()  { test(nodeFormatterTTL, "'abc'^^<http://www.w3.org/2001/XMLSchema#decimal>", "\"abc\"^^<http://www.w3.org/2001/XMLSchema#decimal>") ; }
    @Test public void nodefmt_ttl_44()  { test(nodeFormatterTTL, "'+123.0'^^<http://www.w3.org/2001/XMLSchema#decimal>", "+123.0") ; }
    @Test public void nodefmt_ttl_45()  { test(nodeFormatterTTL, "'-1.0'^^<http://www.w3.org/2001/XMLSchema#decimal>", "-1.0") ; }
    @Test public void nodefmt_ttl_46()  { test(nodeFormatterTTL, "'.1'^^<http://www.w3.org/2001/XMLSchema#decimal>", ".1") ; }
    @Test public void nodefmt_ttl_47()  { test(nodeFormatterTTL, "'-.1'^^<http://www.w3.org/2001/XMLSchema#decimal>", "-.1") ; }
    // No trailing digit.  RDF 1.1.
    @Test public void nodefmt_ttl_48()  { test(nodeFormatterTTL, "\"1.\"^^<http://www.w3.org/2001/XMLSchema#decimal>", "\"1.\"^^<http://www.w3.org/2001/XMLSchema#decimal>") ; }
    @Test public void nodefmt_ttl_49()  { test(nodeFormatterTTL, "'.45'^^<http://www.w3.org/2001/XMLSchema#decimal>", ".45") ; }
    
    // Doubles.
    @Test public void nodefmt_ttl_50()  { test(nodeFormatterTTL, "'123'^^<http://www.w3.org/2001/XMLSchema#double>", "\"123\"^^<http://www.w3.org/2001/XMLSchema#double>") ; }
    @Test public void nodefmt_ttl_51()  { test(nodeFormatterTTL, "'123.0'^^<http://www.w3.org/2001/XMLSchema#double>", "\"123.0\"^^<http://www.w3.org/2001/XMLSchema#double>") ; }
    @Test public void nodefmt_ttl_52()  { test(nodeFormatterTTL, "'123.0e0'^^<http://www.w3.org/2001/XMLSchema#double>", "123.0e0") ; }
    @Test public void nodefmt_ttl_53()  { test(nodeFormatterTTL, "'123e0'^^<http://www.w3.org/2001/XMLSchema#double>", "123e0") ; }
    @Test public void nodefmt_ttl_54()  { test(nodeFormatterTTL, "'.1e0'^^<http://www.w3.org/2001/XMLSchema#double>", ".1e0") ; }

    @Test public void nodefmt_ttl_55()  { test(nodeFormatterTTL, "'123.0e+10'^^<http://www.w3.org/2001/XMLSchema#double>", "123.0e+10") ; }
    @Test public void nodefmt_ttl_56()  { test(nodeFormatterTTL, "'123.0e-10'^^<http://www.w3.org/2001/XMLSchema#double>", "123.0e-10") ; }

    @Test public void nodefmt_ttl_57()  { test(nodeFormatterTTL, "''^^<http://www.w3.org/2001/XMLSchema#double>", "\"\"^^<http://www.w3.org/2001/XMLSchema#double>") ; }
    @Test public void nodefmt_ttl_58()  { test(nodeFormatterTTL, "'+123.0e-10'^^<http://www.w3.org/2001/XMLSchema#double>", "+123.0e-10") ; }
    @Test public void nodefmt_ttl_59()  { test(nodeFormatterTTL, "'-123.0e-10'^^<http://www.w3.org/2001/XMLSchema#double>", "-123.0e-10") ; }

    @Test public void nodefmt_ttl_60()  { test(nodeFormatterTTL, "'-123.e-10'^^<http://www.w3.org/2001/XMLSchema#double>", "-123.e-10") ; }
    @Test public void nodefmt_ttl_61()  { test(nodeFormatterTTL, "'.1e-10'^^<http://www.w3.org/2001/XMLSchema#double>", ".1e-10") ; }
    @Test public void nodefmt_ttl_62()  { test(nodeFormatterTTL, "'.e9'^^<http://www.w3.org/2001/XMLSchema#double>", "\".e9\"^^<http://www.w3.org/2001/XMLSchema#double>") ; }
    
    // Booleans
    @Test public void nodefmt_ttl_70()  { test(nodeFormatterTTL, "'true'^^<http://www.w3.org/2001/XMLSchema#boolean>", "true") ; }
    @Test public void nodefmt_ttl_71()  { test(nodeFormatterTTL, "'1'^^<http://www.w3.org/2001/XMLSchema#boolean>", "\"1\"^^<http://www.w3.org/2001/XMLSchema#boolean>") ; }

    @Test public void nodefmt_ttl_72()  { test(nodeFormatterTTL, "'false'^^<http://www.w3.org/2001/XMLSchema#boolean>", "false") ; }
    @Test public void nodefmt_ttl_73()  { test(nodeFormatterTTL, "'0'^^<http://www.w3.org/2001/XMLSchema#boolean>", "\"0\"^^<http://www.w3.org/2001/XMLSchema#boolean>") ; }

    // Illegal lexical form.
    @Test public void nodefmt_ttl_74()  { test(nodeFormatterTTL, "'False'^^<http://www.w3.org/2001/XMLSchema#boolean>", "\"False\"^^<http://www.w3.org/2001/XMLSchema#boolean>") ; }
    @Test public void nodefmt_ttl_75()  { test(nodeFormatterTTL, "'True'^^<http://www.w3.org/2001/XMLSchema#boolean>", "\"True\"^^<http://www.w3.org/2001/XMLSchema#boolean>") ; }
    
}
