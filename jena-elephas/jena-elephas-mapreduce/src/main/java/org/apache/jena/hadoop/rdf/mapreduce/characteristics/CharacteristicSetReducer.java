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

package org.apache.jena.hadoop.rdf.mapreduce.characteristics;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.jena.hadoop.rdf.types.CharacteristicSetWritable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Reducer which takes in characteristic sets and sums up all their usage counts
 * 
 * 
 */
public class CharacteristicSetReducer extends
        Reducer<CharacteristicSetWritable, CharacteristicSetWritable, CharacteristicSetWritable, NullWritable> {

    private static final Logger LOG = LoggerFactory.getLogger(CharacteristicSetReducer.class);
    private boolean tracing = false;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        this.tracing = LOG.isTraceEnabled();
    }

    @Override
    protected void reduce(CharacteristicSetWritable key, Iterable<CharacteristicSetWritable> values, Context context)
            throws IOException, InterruptedException {
        Iterator<CharacteristicSetWritable> iter = values.iterator();
        CharacteristicSetWritable output = new CharacteristicSetWritable(0);

        if (this.tracing) {
            LOG.trace("Key = {}", key);
        }

        while (iter.hasNext()) {
            CharacteristicSetWritable set = iter.next();
            if (this.tracing) {
                LOG.trace("Value = {}", set);
            }
            output.add(set);
        }

        context.write(output, NullWritable.get());
    }
}
