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

import org.apache.jena.riot.lang.LangRIOT ;
import org.apache.jena.riot.lang.RDFParserOutput ;
import org.apache.jena.riot.lang.RDFParserOutputCounting ;
import org.apache.jena.riot.lang.RDFParserOutputLib ;
import org.apache.jena.riot.tokens.Tokenizer ;
import org.junit.Test ;
import org.openjena.riot.ErrorHandlerTestLib.ExFatal ;
import org.openjena.riot.RiotReader ;

import com.hp.hpl.jena.graph.Node ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
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
        RDFParserOutputCounting sink = RDFParserOutputLib.count() ;
        parse(sink, strings) ;
        return sink.count() ;
    }
    
    private DatasetGraph parseToDataset(String string)
    {
        DatasetGraph dsg = DatasetLib.createDatasetGraphMem() ;
        RDFParserOutput dest = RDFParserOutputLib.dataset(dsg) ;
        parse(dest, string) ;
        return dsg ;
    }

    @Override
    protected LangRIOT createParser(Tokenizer tokenizer, RDFParserOutput sink)
    {
        return RiotReader.createParserNQuads(tokenizer, sink) ;
    }
    
}
