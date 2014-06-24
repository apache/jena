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

package com.hp.hpl.jena.sparql.engine.binding;

import java.io.ByteArrayInputStream ;
import java.io.ByteArrayOutputStream ;
import java.util.ArrayList ;
import java.util.List ;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.riot.RiotException ;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerFactory ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.graph.NodeFactory ;
import com.hp.hpl.jena.rdf.model.AnonId ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.resultset.ResultSetCompare ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.builders.BuilderBinding ;
import com.hp.hpl.jena.sparql.util.NodeUtils ;


public class TestBindingStreams extends BaseTest
{
    @BeforeClass public static void beforeClass()
    { 
        setTestLogging() ;
    }

    @AfterClass public static void afterClass()
    { 
        unsetTestLogging() ;
    }
    
    static Binding b12 = build("(?a 1) (?b 2)") ;
    static Binding b19 = build("(?a 1) (?b 9)") ;
    static Binding b02 = build("(?b 2)") ;
    static Binding b10 = build("(?a 1)") ;
    static Binding b0  = build("") ;
    static Binding bb1 = build("(?a _:XYZ) (?b 1)");
    
    static Binding bb2 = build("(?a 'a\"b\"c') (?b 1)");
    static Binding bb3 = build("(?a 'aΩc') (?b 1)");
    
    static PrefixMap pmap = PrefixMapFactory.create() ;
    static {
        pmap.add(":", "http://example/") ;
    }
    
    static Binding x10 = build("(?x <http://example/abc>)") ;
    
    @Test public void bindingStream_01()        { testRead("VARS ?a ?b . 1 2 .", b12) ; }
    @Test public void bindingStream_02()        { testRead("VARS ?a ?b . - 2 .", b02) ; }
    @Test public void bindingStream_03()        { testRead("VARS ?a ?b . - 2 . 1 - . ", b02, b10) ; }
    @Test public void bindingStream_04()        { testRead("VARS ?a . 1 . VARS ?b . 2 . ", b10, b02) ; }

    @Test(expected=RiotException.class)
    public void bindingStream_05()              { testRead("VARS ?a ?b . 99 . ") ; }
    @Test(expected=RiotException.class)         
    public void bindingStream_06()              { testRead("VARS ?a ?b . 99 11 22 . ") ; }
    
    @Test public void bindingStream_10()        { testRead("VARS ?a ?b . 1 2 . * 9 .", b12, b19) ; }
    @Test public void bindingStream_11()        { testRead("VARS ?a ?b ?c . 1 2 - . * 9 - .", b12, b19) ; }
    
    @Test public void bindingStream_20()        { testRead("PREFIX : <http://example/> . VARS ?x .\n:abc  .\n- .", x10, b0) ; }
    
    @Test public void bindingStream_50()        { testWriteRead(b12) ; }
    @Test public void bindingStream_51()        { testWriteRead(b0) ; }
    @Test public void bindingStream_52()        { testWriteRead(pmap, b12,x10,b19) ; }
    
    @Test public void bindingStream_60()              { testWriteRead(bb1) ; }
    
    @Test
    public void bindingStream_61()
    {
        BindingMap b = BindingFactory.create() ;
        Node bn = NodeFactory.createAnon(new AnonId("unusual")) ;
        b.add(Var.alloc("v"), bn) ;
        testWriteRead(b) ;
    }
    
    @Test public void bindingStream_62()              { testWriteRead(bb2) ; }

    @Test public void bindingStream_63()              { testWriteRead(bb3) ; }

    
    static void testRead(String x, Binding ... bindings)
    {
        Tokenizer t = TokenizerFactory.makeTokenizerString(x) ;
        BindingInputStream inStream = new BindingInputStream(t) ;
        
        if ( bindings.length == 0 )
        {
            for ( ; inStream.hasNext() ; )
                inStream.next() ;
            return ; 
        }
        
        int i ;
        for ( i = 0 ; inStream.hasNext() ; i++ )
        {
            Binding b = inStream.next() ;
            assertTrue("Bindings do not match: expected="+bindings[i]+" got="+b, equalBindings(bindings[i], b)) ;
        }
        
        assertEquals("Wrong length: expect= "+bindings.length+" got="+i,bindings.length, i) ;
    }
    
    static void testWriteRead(Binding ... bindings) { testWriteRead(null, bindings) ; }
    
    static void testWriteRead(PrefixMap prefixMap, Binding ... bindings)
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream() ;
        BindingOutputStream output = new BindingOutputStream(out, prefixMap) ;
        
        for ( Binding b : bindings )
            output.write(b) ;
        output.flush() ;
     
        // When the going gets tough, the tough put in trace statements:
        //System.out.println("T: \n"+out.toString()) ;
        
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray()) ;
        BindingInputStream input = new BindingInputStream(in) ;
        
        List<Binding> results = new ArrayList<>() ;
        for ( ; input.hasNext() ; )
        {
            results.add(input.next()) ;
        }
        assertEquals(bindings.length, results.size()) ;
        for ( int i = 0 ; i < bindings.length ; i++ )
        {
            Binding b1 = bindings[i] ;
            Binding b2 = results.get(i) ;
            assertTrue("Bindings do not match: expected="+b1+" got="+b2, equalBindings(b1, b2)) ;
        }
    }
    

    private static boolean equalBindings(Binding binding1, Binding binding2)
    {
        // Need to have the exact same terms coming back (therefore we can't use BNodeIso to compare values)
        return ResultSetCompare.equal(binding1, binding2, NodeUtils.sameTerm) ;
    }


    private static Binding build(String string)
    {
        Item item = SSE.parse("(binding "+string+")") ;
        return BuilderBinding.build(item) ;
    }
}
