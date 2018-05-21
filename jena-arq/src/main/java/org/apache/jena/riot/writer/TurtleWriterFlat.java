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

import org.apache.jena.atlas.io.IndentedWriter ;
import org.apache.jena.graph.Graph ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.StreamOps ;
import org.apache.jena.riot.system.StreamRDF ;
import org.apache.jena.sparql.util.Context ;

/** Write Turtle with one triple on one line with prefixed names, with short form literals (e.g. integers) */   
public class TurtleWriterFlat extends TurtleWriterBase
{
    @Override
    protected void output(IndentedWriter out, Graph graph, PrefixMap prefixMap, String baseURI, Context context) {
        StreamRDF dest = new WriterStreamRDFFlat(out) ;
        dest.start() ;
        dest.base(baseURI) ;
        StreamOps.sendGraphToStream(graph, dest, prefixMap) ;
        dest.finish() ;
    }
}