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

package org.apache.jena.hadoop.rdf.mapreduce.transform;

import java.io.IOException;

import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.sparql.core.Quad ;

/**
 * A mapper which converts triples to quads where all triples are placed in the
 * same graph
 * 
 * 
 * 
 * @param <TKey>
 *            Key type
 */
public class TriplesToQuadsConstantGraphMapper<TKey> extends AbstractTriplesToQuadsMapper<TKey> {

    private Node graphNode;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        this.graphNode = this.getGraphNode();
    }

    /**
     * Gets the graph node that will be used for all quads, this will be called
     * once and only once during the
     * {@link #setup(org.apache.hadoop.mapreduce.Mapper.Context)} method and the
     * value returned cached for use throughout the lifetime of this mapper.
     * <p>
     * This implementation always used the default graph as the graph for
     * generated quads. You can override this method in your own derived
     * implementation to put triples into a different graph than the default
     * graph.
     * </p>
     * <p>
     * If instead you wanted to select different graphs for each triple you
     * should extend {@link AbstractTriplesToQuadsMapper} instead and override
     * the {@link #selectGraph(Triple)} method which is sealed in this
     * implementation.
     * </p>
     * 
     * @return
     */
    protected Node getGraphNode() {
        return Quad.defaultGraphNodeGenerated;
    }

    @Override
    protected final Node selectGraph(Triple triple) {
        return this.graphNode;
    }

}
