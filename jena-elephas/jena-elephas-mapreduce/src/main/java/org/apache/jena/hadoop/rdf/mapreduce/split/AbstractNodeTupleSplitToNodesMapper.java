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

package org.apache.jena.hadoop.rdf.mapreduce.split;

import java.io.IOException;

import org.apache.hadoop.mapreduce.Mapper;
import org.apache.jena.hadoop.rdf.types.AbstractNodeTupleWritable;
import org.apache.jena.hadoop.rdf.types.NodeWritable;


/**
 * Abstract mapper implementation which splits the tuples into their constituent
 * nodes preserving the keys as-is
 * 
 * 
 * 
 * @param <TKey>
 *            Key type
 * @param <TValue>
 *            Tuple type
 * @param <T>
 *            Writable tuple type
 */
public abstract class AbstractNodeTupleSplitToNodesMapper<TKey, TValue, T extends AbstractNodeTupleWritable<TValue>> extends
        Mapper<TKey, T, TKey, NodeWritable> {

    @Override
    protected final void map(TKey key, T value, Context context) throws IOException, InterruptedException {
        NodeWritable[] ns = this.split(value);
        for (NodeWritable n : ns) {
            context.write(key, n);
        }
    }

    /**
     * Splits the node tuple type into the individual nodes
     * 
     * @param tuple
     *            Tuple
     * @return Nodes
     */
    protected abstract NodeWritable[] split(T tuple);
}
