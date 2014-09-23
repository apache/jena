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

package org.apache.jena.riot.lang;

import org.apache.jena.atlas.junit.BaseTest ;
import org.apache.jena.atlas.lib.StrUtils ;
import org.apache.jena.riot.ErrorHandlerTestLib ;
import org.apache.jena.riot.ErrorHandlerTestLib.ErrorHandlerEx ;
import org.apache.jena.riot.ErrorHandlerTestLib.ExWarning ;
import org.apache.jena.riot.RiotReader ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.apache.jena.riot.tokens.TokenizerFactory ;
import org.junit.Test ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory ;
import com.hp.hpl.jena.sparql.sse.SSE ;

/** Test the behaviour of the RIOT reader for TriG.  TriG includes checking of terms */
public class TestLangTrig extends BaseTest
{
    @Test public void trig_01()     { parse("{}") ; } 
    @Test public void trig_02()     { parse("{}.") ; }
    @Test public void trig_03()     { parse("<g> {}") ; }
    
    @Test(expected=ErrorHandlerTestLib.ExFatal.class) 
    public void trig_04()     { parse("<g> = {}") ; }
    @Test(expected=ErrorHandlerTestLib.ExFatal.class)
    public void trig_05()     { parse("<g> = {} .") ; }
    
    // Need to check we get resolved URIs.
    @Test public void trig_10()     //{ parse("{ <x> <p> <q> }") ; }
    {
        DatasetGraph dsg = parse("{ <x> <p> <q> }") ;
        assertEquals(1, dsg.getDefaultGraph().size()) ;
        Triple t = dsg.getDefaultGraph().find(null,null,null).next();
        Triple t2 = SSE.parseTriple("(<http://base/x> <http://base/p> <http://base/q>)") ;
        assertEquals(t2, t) ;
    }
    
    @Test public void trig_11()
    {
        DatasetGraph dsg = parse("@prefix ex:  <http://example/> .",
                                 "{ ex:s ex:p 123 }") ;
        assertEquals(1, dsg.getDefaultGraph().size()) ;
        Triple t = dsg.getDefaultGraph().find(null,null,null).next();
        Triple t2 = SSE.parseTriple("(<http://example/s> <http://example/p> 123)") ;
    }
    
    
    @Test public void trig_12()     { parse("@prefix xsd:  <http://www.w3.org/2001/XMLSchema#> .",
                                            "{ <x> <p> '1'^^xsd:byte }") ; }
    
    // Also need to check that the RiotExpection is called in normal use. 
    
    // Bad terms.
    @Test (expected=ExWarning.class)
    public void trig_20()     { parse("@prefix ex:  <bad iri> .", "{ ex:s ex:p 123 }") ; }
    
    @Test (expected=ExWarning.class)
    public void trig_21()     { parse("@prefix ex:  <http://example/> .", "{ ex:s <http://example/broken p> 123 }") ; }
    
    @Test (expected=ExWarning.class)
    public void trig_22()     { parse("{ <x> <p> 'number'^^<bad uri> }") ; }

    @Test (expected=ExWarning.class)
    public void trig_23()     { parse("@prefix xsd:  <http://www.w3.org/2001/XMLSchema#> .", "{ <x> <p> 'number'^^xsd:byte }") ; }

    //Check reading into a dataset.
    
    private static DatasetGraph parse(String... strings)
    {
        String string = StrUtils.strjoin("\n", strings) ;
        DatasetGraph dsg = DatasetGraphFactory.createMem() ;
        StreamRDF sink = StreamRDFLib.dataset(dsg) ;
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(string) ;
        LangTriG parser = RiotReader.createParserTriG(tokenizer, "http://base/", sink) ;
        parser.getProfile().setHandler(new ErrorHandlerEx()) ;
        parser.parse();
        return dsg ;
    }
    
}
