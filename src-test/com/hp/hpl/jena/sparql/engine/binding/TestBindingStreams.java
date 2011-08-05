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

package com.hp.hpl.jena.sparql.engine.binding;

import junit.framework.JUnit4TestAdapter ;
import org.junit.AfterClass ;
import org.junit.BeforeClass ;
import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.riot.ErrorHandlerFactory ;
import org.openjena.riot.RiotException ;
import org.openjena.riot.tokens.Tokenizer ;
import org.openjena.riot.tokens.TokenizerFactory ;

import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingInputStream ;
import com.hp.hpl.jena.sparql.resultset.ResultSetCompare ;
import com.hp.hpl.jena.sparql.resultset.ResultSetCompare.BNodeIso ;
import com.hp.hpl.jena.sparql.sse.Item ;
import com.hp.hpl.jena.sparql.sse.SSE ;
import com.hp.hpl.jena.sparql.sse.builders.BuilderBinding ;
import com.hp.hpl.jena.sparql.util.NodeUtils ;

public class TestBindingStreams extends BaseTest
{
    public static junit.framework.Test suite()
    {
        return new JUnit4TestAdapter(TestBindingStreams.class) ;
    }
    
    @BeforeClass public static void beforeClass()
    { 
        ErrorHandlerFactory.setTestLogging(false) ;
    }

    @AfterClass public static void afterClass()
    { 
        ErrorHandlerFactory.setTestLogging(true) ;
    }
    
    static Binding b12 = build("(?a 1) (?b 2)") ;
    static Binding b19 = build("(?a 1) (?b 9)") ;
    static Binding b02 = build("(?b 2)") ;
    static Binding b10 = build("(?a 1)") ;
    static Binding b0  = build("") ;
    
    static Binding x10 = build("(?x <http://example/abc>)") ;
    
    @Test public void bindingStream_01()        { test("VARS ?a ?b . 1 2 .", b12) ; }
    @Test public void bindingStream_02()        { test("VARS ?a ?b . - 2 .", b02) ; }
    @Test public void bindingStream_03()        { test("VARS ?a ?b . - 2 . 1 - . ", b02, b10) ; }
    @Test public void bindingStream_04()        { test("VARS ?a . 1 . VARS ?b . 2 . ", b10, b02) ; }

    @Test(expected=RiotException.class)
    public void bindingStream_05()              { test("VARS ?a ?b . 99 . ") ; }
    @Test(expected=RiotException.class)         
    public void bindingStream_06()              { test("VARS ?a ?b . 99 11 22 . ") ; }
    
    @Test public void bindingStream_10()        { test("VARS ?a ?b . 1 2 . * 9 .", b12, b19) ; }
    @Test public void bindingStream_11()        { test("VARS ?a ?b ?c . 1 2 - . * 9 - .", b12, b19) ; }
    
    @Test public void bindingStream_20()        { test("PREFIX : <http://example/> . VARS ?x .\n:abc  .\n- .", x10, b0) ; }
    
    static void test(String x, Binding ... bindings)
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

    private static boolean equalBindings(Binding binding1, Binding binding2)
    {
        return ResultSetCompare.equal(binding1, binding2, new BNodeIso(NodeUtils.sameTerm)) ;
    }


    private static Binding build(String string)
    {
        Item item = SSE.parse("(binding "+string+")") ;
        return BuilderBinding.build(item) ;
    }
}

