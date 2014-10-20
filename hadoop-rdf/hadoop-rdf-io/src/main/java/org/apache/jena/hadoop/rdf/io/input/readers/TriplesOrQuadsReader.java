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

package org.apache.jena.hadoop.rdf.io.input.readers;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.jena.hadoop.rdf.types.QuadWritable;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * A record reader that reads RDF from any triples/quads format. Triples are
 * converted into quads in the default graph. This behaviour can be changed by
 * deriving from this class and overriding the {@link #getGraphNode()} method
 * 
 * 
 * 
 */
@SuppressWarnings("javadoc")
public class TriplesOrQuadsReader extends AbstractRdfReader<Quad, QuadWritable> {

    @Override
    protected RecordReader<LongWritable, QuadWritable> selectRecordReader(Lang lang) throws IOException {
        if (!RDFLanguages.isQuads(lang) && !RDFLanguages.isTriples(lang))
            throw new IOException(lang.getLabel() + " is not a RDF triples/quads format");

        if (lang.equals(Lang.NQ) || lang.equals(Lang.NQUADS)) {
            return new WholeFileNQuadsReader();
        } else if (lang.equals(Lang.TRIG)) {
            return new TriGReader();
        } else if (lang.equals(Lang.NTRIPLES) || lang.equals(Lang.NT)) {
            return new TriplesToQuadsReader(new WholeFileNTriplesReader());
        } else if (lang.equals(Lang.TTL) || lang.equals(Lang.TURTLE) || lang.equals(Lang.N3)) {
            return new TriplesToQuadsReader(new TurtleReader());
        } else if (lang.equals(Lang.RDFXML)) {
            return new TriplesToQuadsReader(new RdfXmlReader());
        } else if (lang.equals(Lang.RDFJSON)) {
            return new TriplesToQuadsReader(new RdfJsonReader());
        }
        throw new IOException(lang.getLabel() + " has no associated RecordReader implementation");
    }

    /**
     * Gets the graph node which represents the graph into which triples will be
     * indicated to belong to when they are converting into quads.
     * <p>
     * Defaults to {@link Quad#defaultGraphNodeGenerated} which represents the
     * default graph
     * </p>
     * 
     * @return Graph node
     */
    protected Node getGraphNode() {
        return Quad.defaultGraphNodeGenerated;
    }
}
