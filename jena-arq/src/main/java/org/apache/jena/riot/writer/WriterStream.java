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

package org.apache.jena.riot.writer;

import java.util.Iterator ;
import java.util.Map.Entry ;

import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.riot.system.StreamRDFLib ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;

public class WriterStream
{
    private WriterStream() {}
    
    protected static void finish(StreamRDF dest)
    { dest.finish() ; }

    protected static void start(StreamRDF dest)
    { dest.start() ; }

    protected static void writePrefixes(StreamRDF dest, PrefixMap prefixMap)
    {
        if ( prefixMap != null )
        {
            for ( Entry<String, String> e : prefixMap.getMappingCopyStr().entrySet())
                dest.prefix(e.getKey(), e.getValue()) ;
        }
    }

    protected static void write(StreamRDF dest, DatasetGraph datasetGraph, PrefixMap prefixes, String baseURI) 
    {
        start(dest) ;
        dest.base(baseURI) ;
        writePrefixes(dest, prefixes) ;
        Iterator<Quad> iter = datasetGraph.find(null, null, null, null) ;
        StreamRDFLib.quadsToStream(dest, iter) ;
        finish(dest) ;
    }

    protected static void write(StreamRDF dest, Graph graph, PrefixMap prefixes, String baseURI) 
    {
        start(dest) ;
        dest.base(baseURI) ;
        writePrefixes(dest, prefixes) ;
        Iterator<Triple> iter = graph.find(null, null, null) ;
        StreamRDFLib.triplesToStream(dest, iter) ;
        finish(dest) ;
    }
}

