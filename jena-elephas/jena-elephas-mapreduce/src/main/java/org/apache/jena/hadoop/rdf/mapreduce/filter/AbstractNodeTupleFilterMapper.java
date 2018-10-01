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

package org.apache.jena.hadoop.rdf.mapreduce.filter;

import java.io.IOException;

import org.apache.hadoop.mapreduce.Mapper;
import org.apache.jena.hadoop.rdf.mapreduce.RdfMapReduceConstants;
import org.apache.jena.hadoop.rdf.types.AbstractNodeTupleWritable;


/**
 * Abstract mapper implementation which helps in filtering tuples from the
 * input, derived implementations provide an implementation of the
 * {@link #accepts(Object, AbstractNodeTupleWritable)}
 * 
 * @param <TKey>
 *            Key type
 * @param <TValue>
 *            Tuple type
 * @param <T>
 *            Writable tuple type
 */
public abstract class AbstractNodeTupleFilterMapper<TKey, TValue, T extends AbstractNodeTupleWritable<TValue>> extends
        Mapper<TKey, T, TKey, T> {

    private boolean invert = false;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        this.invert = context.getConfiguration().getBoolean(RdfMapReduceConstants.FILTER_INVERT, this.invert);
    }

    @Override
    protected final void map(TKey key, T value, Context context) throws IOException, InterruptedException {
        if (this.accepts(key, value)) {
            if (!this.invert)
                context.write(key, value);
        } else if (this.invert) {
            context.write(key, value);
        }
    }

    /**
     * Gets whether the mapper accepts the key value pair and will pass it as
     * output
     * 
     * @param key
     *            Key
     * @param tuple
     *            Tuple value
     * @return True if the mapper accepts the given key value pair, false
     *         otherwise
     */
    protected abstract boolean accepts(TKey key, T tuple);
}
