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

package org.apache.jena.riot.writer;

import java.io.OutputStream ;
import java.io.Writer ;

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.datatypes.xsd.impl.XMLLiteralType ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.riot.Lang ;
import org.apache.jena.riot.WriterDatasetRIOT ;
import org.apache.jena.riot.WriterGraphRIOT ;
import org.apache.jena.riot.lang.ReaderTriX ;
import org.apache.jena.riot.lang.TriX ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.RiotLib ;
import org.apache.jena.riot.system.StreamRDFOps ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.util.Context ;

/** Write TriX.
 * See {@link TriX} for details.
 * The writer defers to {@link StreamWriterTriX}. 
 * @see TriX
 * @see ReaderTriX
 * @see StreamWriterTriX
 */
public class WriterTriX implements WriterDatasetRIOT, WriterGraphRIOT {
    private static String rdfXMLLiteral = XMLLiteralType.theXMLLiteralType.getURI() ;

    // Common pattern.
    @Override
    public Lang getLang() {
        return Lang.TRIX ;
    }
    
    // Dataset
    @Override
    public void write(OutputStream out, DatasetGraph datasetGraph, PrefixMap prefixMap, String baseURI, Context context) {
        IndentedWriter iOut = new IndentedWriter(out) ;
        write(iOut, datasetGraph, prefixMap, baseURI, null) ;
    }

    @Override
    public void write(Writer out, DatasetGraph datasetGraph, PrefixMap prefixMap, String baseURI, Context context) {
        IndentedWriter iOut = RiotLib.create(out) ;
        write(iOut, datasetGraph, prefixMap, baseURI, null) ;
    }

    private void write(IndentedWriter out, DatasetGraph datasetGraph, PrefixMap prefixMap, String baseURI, Context context) {
        StreamWriterTriX w = new StreamWriterTriX(out) ;
        StreamRDFOps.datasetToStream(datasetGraph, w) ;
    }

    // Graph
    @Override
    public void write(OutputStream out, Graph graph, PrefixMap prefixMap, String baseURI, Context context) {
        IndentedWriter iOut = new IndentedWriter(out) ;
        write(iOut, graph, prefixMap, baseURI, null) ;
    }

    @Override
    public void write(Writer out, Graph graph, PrefixMap prefixMap, String baseURI, Context context) {
        IndentedWriter iOut = RiotLib.create(out) ;
        write(iOut, graph, prefixMap, baseURI, null) ;
    }
    
    private static void write(IndentedWriter out, Graph graph, PrefixMap prefixMap, String baseURI, Object context) {
        StreamWriterTriX w = new StreamWriterTriX(out) ;
        StreamRDFOps.graphToStream(graph, w) ;
    }
}

