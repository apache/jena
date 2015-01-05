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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.jena.hadoop.rdf.types.AbstractNodeTupleWritable;
import org.apache.jena.hadoop.rdf.types.CharacteristicSetWritable;
import org.apache.jena.hadoop.rdf.types.CharacteristicWritable;
import org.apache.jena.hadoop.rdf.types.NodeWritable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Abstract reducer which takes in tuples grouped by some node and generating
 * initial characteristic sets.
 * <p>
 * This produces the characteristic sets as both the key and value so that in a
 * subsequent job the characteristic steps may be further combined together to
 * total up the usage counts appropriately.
 * </p>
 * <p>
 * It is important to note that the output from this mapper can be very large
 * and since it typically needs to be written to HDFS before being processed by
 * further jobs it is strongly recommended that you use appropriate output
 * compression
 * </p>
 * 
 * 
 * 
 * @param <TValue>
 *            Tuple type
 * @param <T>
 *            Writable tuple type
 */
public abstract class AbstractCharacteristicSetGeneratingReducer<TValue, T extends AbstractNodeTupleWritable<TValue>> extends
        Reducer<NodeWritable, T, CharacteristicSetWritable, NullWritable> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractCharacteristicSetGeneratingReducer.class);

    private boolean tracing = false;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        this.tracing = LOG.isTraceEnabled();
    }

    @Override
    protected void reduce(NodeWritable key, Iterable<T> values, Context context) throws IOException, InterruptedException {
        Map<NodeWritable, CharacteristicWritable> characteristics = new TreeMap<NodeWritable, CharacteristicWritable>();

        // Firstly need to find individual characteristics
        Iterator<T> iter = values.iterator();
        while (iter.hasNext()) {
            T tuple = iter.next();
            NodeWritable predicate = this.getPredicate(tuple);

            if (characteristics.containsKey(predicate)) {
                characteristics.get(predicate).increment();
            } else {
                characteristics.put(predicate, new CharacteristicWritable(predicate.get()));
            }
        }

        // Then we need to produce all the possible characteristic sets based on
        // this information
        List<CharacteristicWritable> cs = new ArrayList<CharacteristicWritable>(characteristics.values());
        if (cs.size() == 0)
            return;
        for (int i = 1; i <= cs.size(); i++) {
            this.outputSets(cs, i, context);
        }
    }

    /**
     * Output all sets of a given size
     * 
     * @param cs
     *            Characteristics
     * @param perSet
     *            Set size
     * @param context
     *            Context to output sets to
     * @throws IOException
     * @throws InterruptedException
     */
    protected void outputSets(List<CharacteristicWritable> cs, int perSet, Context context) throws IOException,
            InterruptedException {
        if (perSet == 1) {
            for (CharacteristicWritable c : cs) {
                CharacteristicSetWritable set = new CharacteristicSetWritable(c);
                context.write(set, NullWritable.get());
                if (this.tracing) {
                    LOG.trace("Key = {}", set);
                }
            }
        } else if (perSet == cs.size()) {
            CharacteristicSetWritable set = new CharacteristicSetWritable();
            for (CharacteristicWritable c : cs) {
                set.add(c);
            }
            context.write(set, NullWritable.get());
            if (this.tracing) {
                LOG.trace("Key = {}", set);
            }
        } else {
            CharacteristicWritable[] members = new CharacteristicWritable[perSet];
            this.combinations(cs, perSet, 0, members, context);
        }
    }

    /**
     * Calculate all available combinations of N elements from the given
     * characteristics
     * 
     * @param cs
     *            Characteristics
     * @param len
     *            Desired number of elements
     * @param startPosition
     *            Start position
     * @param result
     *            Result array to fill
     * @param context
     *            Context to write completed combinations to
     * @throws IOException
     * @throws InterruptedException
     */
    protected final void combinations(List<CharacteristicWritable> cs, int len, int startPosition,
            CharacteristicWritable[] result, Context context) throws IOException, InterruptedException {
        if (len == 0) {
            CharacteristicSetWritable set = new CharacteristicSetWritable(result);
            context.write(set, NullWritable.get());
            if (this.tracing) {
                LOG.trace("Key = {}", set);
            }
            return;
        }
        for (int i = startPosition; i <= cs.size() - len; i++) {
            result[result.length - len] = cs.get(i);
            combinations(cs, len - 1, i + 1, result, context);
        }
    }

    /**
     * Gets the predicate for the tuple
     * 
     * @param tuple
     *            Tuple
     * @return
     */
    protected abstract NodeWritable getPredicate(T tuple);

}
