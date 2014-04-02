/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.group;

import java.io.IOException;

import org.apache.hadoop.mapreduce.Mapper;

import com.yarcdata.urika.hadoop.rdf.types.AbstractNodeTupleWritable;
import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;

/**
 * Abstract mapper implementation which helps in grouping tuples by assigning
 * them a {@link NodeWritable} key in place of their existing key. Derived
 * implementations of this may select the key based on some component of the
 * tuple or by other custom logic.
 * 
 * @author rvesse
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
