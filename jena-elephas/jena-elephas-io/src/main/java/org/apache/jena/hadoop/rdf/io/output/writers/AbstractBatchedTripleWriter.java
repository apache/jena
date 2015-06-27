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

import org.apache.jena.graph.Graph ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.graph.GraphFactory ;

/**
 * Abstract batched record writer for triple formats
 * 
 * 
 * 
 * @param <TKey>
 */
public abstract class AbstractBatchedTripleWriter<TKey> extends AbstractBatchedNodeTupleWriter<TKey, Triple, TripleWritable> {

    private Graph g = GraphFactory.createDefaultGraph();

    protected AbstractBatchedTripleWriter(Writer writer, long batchSize) {
        super(writer, batchSize);
    }

    @Override
    protected final long add(TripleWritable value) {
        g.add(value.get());
        return g.size();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected final long writeOutput(Writer writer) {
        if (this.g.size() == 0)
            return 0;
        RDFDataMgr.write(writer, this.g, this.getRdfLanguage());
        this.g.clear();
        return this.g.size();
    }

    /**
     * Gets the RDF language used for output
     * 
     * @return RDF language
     */
    protected abstract Lang getRdfLanguage();
}
