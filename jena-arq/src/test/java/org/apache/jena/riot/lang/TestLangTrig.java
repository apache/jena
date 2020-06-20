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

import static org.junit.Assert.assertEquals;

import org.apache.jena.graph.Triple ;
import org.apache.jena.riot.ErrorHandlerTestLib ;
import org.apache.jena.riot.ErrorHandlerTestLib.ExFatal ;
import org.apache.jena.riot.ErrorHandlerTestLib.ExWarning ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.sse.SSE ;
import org.junit.Test ;

/** Test the behaviour of the RIOT reader for TriG.  TriG includes checking of terms */
public class TestLangTrig
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
    @Test (expected=ExFatal.class)
    public void trig_20()     { parse("@prefix ex:  <bad iri> .", "{ ex:s ex:p 123 }") ; }
    
    @Test (expected=ExFatal.class)
    public void trig_21()     { parse("@prefix ex:  <http://example/> .", "{ ex:s <http://example/broken p> 123 }") ; }
    
    @Test (expected=ExFatal.class)
    public void trig_22()     { parse("{ <x> <p> 'number'^^<bad uri> }") ; }

    @Test (expected=ExWarning.class)
    public void trig_23()     { parse("@prefix xsd:  <http://www.w3.org/2001/XMLSchema#> .", "{ <x> <p> 'number'^^xsd:byte }") ; }

    private static DatasetGraph parse(String... strings) {
        return ParserTestBaseLib.parseDataset(Lang.TRIG, strings) ;
    }
    
}
