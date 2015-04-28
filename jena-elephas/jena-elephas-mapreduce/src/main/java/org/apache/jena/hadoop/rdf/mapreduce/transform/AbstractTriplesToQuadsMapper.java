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

import org.apache.hadoop.mapreduce.Mapper;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.hadoop.rdf.types.QuadWritable;
import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.apache.jena.sparql.core.Quad ;

/**
 * An abstract mapper which transforms triples into quads. Derived
 * implementations may choose how the graph to which triples are assigned is
 * decided.
 * <p>
 * Keys are left as is by this mapper.
 * </p>
 * 
 * 
 * 
 * @param <TKey>
 */
public abstract class AbstractTriplesToQuadsMapper<TKey> extends Mapper<TKey, TripleWritable, TKey, QuadWritable> {

    @Override
    protected final void map(TKey key, TripleWritable value, Context context) throws IOException, InterruptedException {
        Triple triple = value.get();
        Node graphNode = this.selectGraph(triple);
        context.write(key, new QuadWritable(new Quad(graphNode, triple)));
    }

    /**
     * Selects the graph name to use for converting the given triple into a quad
     * 
     * @param triple
     *            Triple
     * @return Tuple
     */
    protected abstract Node selectGraph(Triple triple);
}
