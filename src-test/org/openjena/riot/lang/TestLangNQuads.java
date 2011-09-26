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
