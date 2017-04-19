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

package org.apache.jena.hadoop.rdf.mapreduce.group;

import org.apache.jena.hadoop.rdf.types.NodeWritable;
import org.apache.jena.hadoop.rdf.types.QuadWritable;
import org.apache.jena.sparql.core.Quad ;

/**
 * Abstract mapper implementation which helps in grouping quads by assigning
 * them a {@link NodeWritable} key in place of their existing key. Derived
 * implementations of this may select the key based on some component of the
 * quad or by other custom logic.
 * 
 * 
 * 
 * @param <TKey>
 */
public abstract class AbstractQuadGroupingMapper<TKey> extends AbstractNodeTupleGroupingMapper<TKey, Quad, QuadWritable> {

    @Override
    protected final NodeWritable selectKey(QuadWritable tuple) {
        return this.selectKey(tuple.get());
    }

    /**
     * Selects the key to use
     * 
     * @param quad
     *            Quad
     * @return Key to use
     */
    protected abstract NodeWritable selectKey(Quad quad);
}
