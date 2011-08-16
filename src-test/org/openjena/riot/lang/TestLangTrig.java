/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.lang;

import org.junit.Test ;
import org.openjena.atlas.junit.BaseTest ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.atlas.lib.StrUtils ;
import org.openjena.riot.RiotLoader ;
import org.openjena.riot.RiotReader ;
import org.openjena.riot.ErrorHandlerTestLib.ErrorHandlerEx ;
import org.openjena.riot.ErrorHandlerTestLib.ExWarning ;
import org.openjena.riot.tokens.Tokenizer ;
import org.openjena.riot.tokens.TokenizerFactory ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.lib.DatasetLib ;
import com.hp.hpl.jena.sparql.sse.SSE ;

/** Test the behaviour of the RIOT reader for TriG.  TriG includes checking of terms */
public class TestLangTrig extends BaseTest
{
    @Test public void trig_01()     { parse("{}") ; } 
    @Test public void trig_02()     { parse("{}.") ; }
    @Test public void trig_03()     { parse("<g> {}") ; }
    @Test public void trig_04()     { parse("<g> = {}") ; }
    @Test public void trig_05()     { parse("<g> = {} .") ; }
    
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
        DatasetGraph dsg = DatasetLib.createDatasetGraphMem() ;
        Sink<Quad> sink = RiotLoader.datasetSink(dsg) ;
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(string) ;
        LangTriG parser = RiotReader.createParserTriG(tokenizer, "http://base/", sink) ;
        parser.getProfile().setHandler(new ErrorHandlerEx()) ;
        try {
            parser.parse();
        } finally { sink.close() ; }
        return dsg ;
    }
    
}

/*
 * (c) Copyright 2010 Talis Systems Ltd.
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