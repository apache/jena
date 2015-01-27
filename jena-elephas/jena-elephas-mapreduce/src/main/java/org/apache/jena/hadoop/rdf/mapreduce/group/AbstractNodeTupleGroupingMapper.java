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

import java.io.IOException;

import org.apache.hadoop.mapreduce.Mapper;
import org.apache.jena.hadoop.rdf.types.AbstractNodeTupleWritable;
import org.apache.jena.hadoop.rdf.types.NodeWritable;


/**
 * Abstract mapper implementation which helps in grouping tuples by assigning
 * them a {@link NodeWritable} key in place of their existing key. Derived
 * implementations of this may select the key based on some component of the
 * tuple or by other custom logic.
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
public abstract class AbstractNodeTupleGroupingMapper<TKey, TValue, T extends AbstractNodeTupleWritable<TValue>> extends
        Mapper<TKey, T, NodeWritable, T> {

    @Override
    protected final void map(TKey key, T value, Context context) throws IOException, InterruptedException {
        NodeWritable newKey = this.selectKey(value);
        context.write(newKey, value);
    }

    /**
     * Gets the key to associated with the tuple
     * 
     * @param tuple
     *            Tuple
     * @return Node to use as key
     */
    protected abstract NodeWritable selectKey(T tuple);
}
