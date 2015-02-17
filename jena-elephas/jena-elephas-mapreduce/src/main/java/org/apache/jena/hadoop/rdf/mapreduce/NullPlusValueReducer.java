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

package org.apache.jena.hadoop.rdf.mapreduce;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A reducer that outputs a pair for each value consisting of a null key and the
 * value
 * 
 * @author rvesse
 * 
 * @param <TKey>
 *            Key
 * @param <TValue>
 *            Value
 */
public class NullPlusValueReducer<TKey, TValue> extends Reducer<TKey, TValue, NullWritable, TValue> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NullPlusValueReducer.class);
    private boolean tracing = false;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        this.tracing = LOGGER.isTraceEnabled();
    }

    @Override
    protected void reduce(TKey key, Iterable<TValue> values, Context context) throws IOException, InterruptedException {
        if (this.tracing) {
            LOGGER.trace("Input Key = {}", key);
        }
        Iterator<TValue> iter = values.iterator();
        while (iter.hasNext()) {
            TValue value = iter.next();
            if (tracing) {
                LOGGER.trace("Input Value = {}", value);
            }
            context.write(NullWritable.get(), value);
        }
    }
}
