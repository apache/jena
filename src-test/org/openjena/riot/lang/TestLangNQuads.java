/*
 * (c) Copyright 2010 Talis Systems Ltd.
 * All rights reserved.
 * [See end of file]
 */

package org.openjena.riot.lang;

import org.junit.Test ;
import org.openjena.atlas.lib.Sink ;
import org.openjena.atlas.lib.SinkCounting ;
import org.openjena.atlas.lib.SinkNull ;
import org.openjena.atlas.lib.StrUtils ;
import org.openjena.riot.RiotLoader ;
import org.openjena.riot.RiotReader ;
import org.openjena.riot.ErrorHandlerTestLib.ErrorHandlerEx ;
import org.openjena.riot.ErrorHandlerTestLib.ExFatal ;
import org.openjena.riot.system.RiotLib ;
import org.openjena.riot.tokens.Tokenizer ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;
import com.hp.hpl.jena.sparql.lib.DatasetLib ;

/** Test of syntax by a quads parser (does not include node validitiy checking) */ 

public class TestLangNQuads extends TestLangNTuples
{
    
    @Test
    public void quad_1()
    {
        parseCount("<x> <p> <s> <g> .") ; 
    }
    
    @Test(expected=ExFatal.class)
    public void quad_2()
    {
        parseCount("<x> <p> <s> <g>") ;        // No trailing DOT
    }
    

    @Test(expected=ExFatal.class) 
    public void nq_only_1()
    {
        parseCount("<x> <p> <s> <g> <c> .") ; 
    }

    @Test(expected=ExFatal.class) 
    public void nq_only_2()
    {
        parseCount("@base <http://example/> . <x> <p> <s> .") ; 
    }

    
    @Test
    public void dataset_1()
    {
        // This must parse to <g> 
        DatasetGraph dsg = parseToDataset("<x> <p> <s> <g> .") ;
        assertEquals(1,dsg.size()) ;
        assertEquals(1, dsg.getGraph(Node.createURI("g")).size()) ;
        assertEquals(0, dsg.getDefaultGraph().size()) ;
    }

    @Override
    protected long parseCount(String... strings)
    {
        SinkCounting<Quad> sink = new SinkCounting<Quad>() ;
        parse(sink, strings) ;
        return sink.getCount() ;
    }
    
    private static DatasetGraph parseToDataset(String string)
    {
        DatasetGraph dsg = DatasetLib.createDatasetGraphMem() ;
        Sink<Quad> sink = RiotLoader.datasetSink(dsg) ;
        try {
            parse(sink, string) ;
        } finally { sink.close() ; }
        return dsg ;
    }
    
    private static void parse(Sink<Quad> sink, String... strings ) 
    {
        String string = StrUtils.strjoin("\n", strings) ;
        Tokenizer tokenizer = tokenizer(string) ;
        LangRIOT parser = RiotReader.createParserNQuads(tokenizer, sink) ;
        parser.getProfile().setHandler(new ErrorHandlerEx()) ;
        parser.parse() ;
        sink.flush();
    }
    
    @Override
    protected void parseCheck(String... strings)
    {
        String string = StrUtils.strjoin("\n", strings) ;
        Tokenizer tokenizer = tokenizer(string) ;
        Sink<Quad> sink = new SinkNull<Quad>() ;
        LangRIOT parser = RiotReader.createParserNQuads(tokenizer, sink) ;
        parser.setProfile(RiotLib.profile(null, false, true, new ErrorHandlerEx())) ;
        parser.parse() ;
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