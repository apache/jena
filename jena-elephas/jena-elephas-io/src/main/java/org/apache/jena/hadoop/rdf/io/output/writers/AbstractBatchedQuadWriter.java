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

package org.apache.jena.hadoop.rdf.io.output.writers;

import java.io.Writer;
import java.util.List;

import org.apache.commons.collections.IteratorUtils;
import org.apache.jena.graph.Node ;
import org.apache.jena.hadoop.rdf.types.QuadWritable;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFWriterRegistry;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.core.DatasetGraphFactory ;
import org.apache.jena.sparql.core.Quad ;

/**
 * Abstract batched record writer for quad formats
 * 
 * 
 * 
 * @param <TKey>
 */
public abstract class AbstractBatchedQuadWriter<TKey> extends AbstractBatchedNodeTupleWriter<TKey, Quad, QuadWritable> {

    private DatasetGraph g = DatasetGraphFactory.createGeneral();

    protected AbstractBatchedQuadWriter(Writer writer, long batchSize) {
        super(writer, batchSize);
    }

    @Override
    protected final long add(QuadWritable value) {
        g.add(value.get());
        return g.size();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected final long writeOutput(Writer writer) {
        if (this.g.size() == 0)
            return 0;
        RDFDataMgr.write(writer, this.g, RDFWriterRegistry.defaultSerialization(this.getRdfLanguage()));

        // Clear the dataset graph
        @SuppressWarnings("unchecked")
        List<Node> graphNames = IteratorUtils.toList(this.g.listGraphNodes());
        for (Node graphName : graphNames) {
            this.g.removeGraph(graphName);
        }
        this.g.getDefaultGraph().clear();

        return this.g.size();
    }

    /**
     * Gets the RDF language used for output
     * 
     * @return RDF language
     */
    protected abstract Lang getRdfLanguage();
}
