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

package org.openjena.riot.system;

import org.apache.jena.atlas.lib.Sink ;
import org.openjena.riot.RiotLoader ;
import org.openjena.riot.RiotReader ;
import org.openjena.riot.lang.LangRIOT ;
import org.openjena.riot.tokens.Tokenizer ;

import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.rdf.model.Model ;

/** Jena reader for RIOT N-Triples */
public class JenaReaderNTriples2 extends JenaReaderRIOT
{
    @Override
    protected void readWorker(Model model, Tokenizer tokenizer, String base)
    {
        Sink<Triple> sink = RiotLoader.graphSink(model.getGraph()) ;
        try {
            LangRIOT parser = RiotReader.createParserNTriples(tokenizer, sink) ;
            parser.parse() ;
        } finally {
            sink.close();
            tokenizer.close();
        }
    }
}
