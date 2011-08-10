/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
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
    static Prologue prologue = new Prologue() ;
    static {
        prologue.getPrefixMap().add("", "http://example/") ;
        prologue.getPrefixMap().add("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#") ;
        prologue.getPrefixMap().add("rdfs", "http://www.w3.org/2000/01/rdf-schema#") ;
        prologue.getPrefixMap().add("owl", "http://www.w3.org/2002/07/owl#") ;
        prologue.getPrefixMap().add("xsd", "http://www.w3.org/2001/XMLSchema#") ;
        prologue.getPrefixMap().add("x", "http://example/a") ;
    }
    
    
    @Test public void output_01()        { testStringForNode("<http://example/>") ; }

    @Test public void output_02()        { testStringForNode("''") ; }
    
    @Test public void output_03()        { testStringForNode("'abc'@en") ; }
    
    @Test public void output_04()        { testStringForNode("'abc'^^<http://exmaple/dt>") ; }
    
    @Test public void output_05()        { testStringForNode("'é'", "\"\\u00E9\"") ; }

    @Test public void output_06()        { testStringForNode("'\\n\\t\\f'", "\"\\n\\t\\f\"") ; }
    
    @Test public void output_10()        { testStringForNode("<http://example/>", ":", prologue) ; }
    
    @Test public void output_11()        { testStringForNode("<http://example/abc>", "x:bc", prologue) ; }

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