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

package org.openjena.riot;

import java.io.OutputStream ;
import java.util.Iterator ;

import com.hp.hpl.jena.graph.Graph ;
import com.hp.hpl.jena.graph.Triple ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Quad ;

/** Output RDF in various formats (unfinished)
 * @deprecated Use {@link org.apache.jena.riot.RiotWriter}
 */
@Deprecated
public class RiotWriter
{
    // Work in progress
    // A class of all the ways to write things - just jumps to right place in code. 
    
    /** @deprecated Use {@link org.apache.jena.riot.RiotWriter} */
    @Deprecated
    public static void writeNQuads(OutputStream out, DatasetGraph dsg)
    {
        org.apache.jena.riot.RiotWriter.writeNQuads(out, dsg) ;
    }
    /** @deprecated Use {@link org.apache.jena.riot.RiotWriter} */
    @Deprecated
    public static void writeNQuads(OutputStream out, Iterator<Quad> it)
    {
        org.apache.jena.riot.RiotWriter.writeNQuads(out, it) ;
    }
    /** @deprecated Use {@link org.apache.jena.riot.RiotWriter} */
    @Deprecated
    public static void writeTriples(OutputStream out, Graph graph)
    {
        org.apache.jena.riot.RiotWriter.writeTriples(out, graph) ;
    }
    /** @deprecated Use {@link org.apache.jena.riot.RiotWriter} */
    @Deprecated
    public static void writeTriples(OutputStream out, Iterator<Triple> it)
    {
        org.apache.jena.riot.RiotWriter.writeTriples(out, it) ;
    }
    /** @deprecated Use {@link org.apache.jena.riot.RiotWriter} */
    @Deprecated
    public static void writeRDFJSON(OutputStream out, Graph graph)
    {
        org.apache.jena.riot.RiotWriter.writeRDFJSON(out, graph) ;
    }
}
