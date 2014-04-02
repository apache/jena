/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.split;

import java.io.IOException;

import org.apache.hadoop.mapreduce.Mapper;

import com.yarcdata.urika.hadoop.rdf.types.AbstractNodeTupleWritable;
import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;

/**
 * Abstract mapper implementation which splits the tuples into their constituent
 * nodes preserving the keys as-is
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
