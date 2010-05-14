/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.riot.lang;

import org.junit.Test ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.atlas.lib.SinkCounting ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.riot.RiotException ;
import com.hp.hpl.jena.riot.RiotReader ;
import com.hp.hpl.jena.riot.tokens.Tokenizer ;
import com.hp.hpl.jena.riot.tokens.TokenizerFactory ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.tdb.lib.DatasetLib ;

public class TestLangNQuads extends TestLangNTuples
{
    
    @Test
    public void quad_1()
    {
        parseToSink("<x> <p> <s> <g> .") ; 
    }
    
    @Test(expected=RiotException.class)
    public void quad_2()
    {
        parseToSink("<x> <p> <s> <g>") ;        // No trailing DOT
    }
    
    @Test
    public void dataset_1()
    {
        DatasetGraph dsg = parseToDataset("<x> <p> <s> <g> .") ;
        assertEquals(1,dsg.size()) ;
        assertEquals(1, dsg.getGraph(Node.createURI("g")).size()) ;
        assertEquals(0, dsg.getDefaultGraph().size()) ;
    }

    @Override
    protected long parseToSink(String string)
    {
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(string) ;
        SinkCounting<Quad> sink = new SinkCounting<Quad>() ;
        
        LangNQuads x = RiotReader.createParserNQuads(tokenizer, sink) ;
        x.parse() ;
        return sink.getCount() ;
    }
    
    private static DatasetGraph parseToDataset(String string)
    {
        DatasetGraph dsg = DatasetLib.createDatasetGraphMem() ;
        Sink<Quad> sink = DatasetLib.datasetSink(dsg) ;
        
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(string) ;
        LangRIOT parser = RiotReader.createParserNQuads(tokenizer, sink) ;
        parser.parse() ;
        sink.flush();
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