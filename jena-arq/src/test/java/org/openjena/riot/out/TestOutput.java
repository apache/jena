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

package org.openjena.riot.out;

import java.io.ByteArrayOutputStream ;
import java.io.IOException ;
import java.io.OutputStreamWriter ;
import java.io.UnsupportedEncodingException ;
import java.io.Writer ;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.riot.system.Prologue ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.util.NodeFactory ;

// Check the basisc of output 
//  Nodes, escapes, prefix mapping.
public class TestOutput extends BaseTest
{
    public TestOutput() {}
    static Prologue prologue = new Prologue() ;
    static {
        prologue.getPrefixMap().add("", "http://example/") ;
        prologue.getPrefixMap().add("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#") ;
        prologue.getPrefixMap().add("rdfs", "http://www.w3.org/2000/01/rdf-schema#") ;
        prologue.getPrefixMap().add("owl", "http://www.w3.org/2002/07/owl#") ;
        prologue.getPrefixMap().add("xsd", "http://www.w3.org/2001/XMLSchema#") ;
        prologue.getPrefixMap().add("x", "http://example/ns/a") ;
    }
    
    
    @Test public void output_01()        { testStringForNode("<http://example/>") ; }

    @Test public void output_02()        { testStringForNode("''") ; }
    
    @Test public void output_03()        { testStringForNode("'abc'@en") ; }
    
    @Test public void output_04()        { testStringForNode("'abc'^^<http://example/dt>") ; }
    
    @Test public void output_05()        { testStringForNode("'é'", "\"\\u00E9\"") ; }

    @Test public void output_06()        { testStringForNode("'\\n\\t\\f'", "\"\\n\\t\\f\"") ; }
    
    @Test public void output_10()        { testStringForNode("<http://example/>", ":", prologue) ; }
    
    @Test public void output_11()        { testStringForNode("<http://example/ns/abc>", "x:bc", prologue) ; }

    @Test public void output_12()        { testStringForNode("123", "\"123\"^^xsd:integer", prologue) ; }
    
    @Test public void output_13()        { test(Node.ANY, "ANY", prologue) ; } 
    
    private void test(Node node, String string, Prologue prologue2)
    {
        try
        {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream() ;
            Writer w = new OutputStreamWriter(bytes, "ASCII") ;
            OutputLangUtils.output(w, node, prologue) ;
            w.flush();
            String str = bytes.toString("ASCII") ;
            assertEquals(string, str) ;
        }
        catch (UnsupportedEncodingException ex) { ex.printStackTrace(); }
        catch (IOException ex) { ex.printStackTrace(); }
    }

    private static void testStringForNode(String nodeStr)
    {
        String expected = nodeStr.replace("'", "\"") ;
        testStringForNode(nodeStr, expected) ;
    }
    
    private static void testStringForNode(String nodeStr, String expected)
    {
        testStringForNode(nodeStr, expected, null) ;
    }
    
    private static void testStringForNode(String nodeStr, String expected, Prologue prologue)
    {
        String x =  stringForNode(nodeStr, prologue, "ASCII") ;
        assertEquals(expected, x) ;
    }

    
    private static String stringForNode(String nodeStr)
    {
        return stringForNode(nodeStr, null, "ASCII") ;
    }
    
    private static String stringForNode(String nodeStr, Prologue prologue, String encoding)
    {
        Node node = NodeFactory.parseNode(nodeStr) ;
        try
        {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream() ;
            Writer w = new OutputStreamWriter(bytes, encoding) ;
            OutputLangUtils.output(w, node, prologue) ;
            w.flush();
            return bytes.toString(encoding) ;
        } 
        catch (UnsupportedEncodingException ex) { ex.printStackTrace(); }
        catch (IOException ex) { ex.printStackTrace(); }
        return null ;
    }
}
