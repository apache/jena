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
 * nodes using the tuples as the keys and the nodes as the values
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
public abstract class AbstractNodeTupleSplitWithNodesMapper<TKey, TValue, T extends AbstractNodeTupleWritable<TValue>> extends
        Mapper<TKey, T, T, NodeWritable> {

    @Override
    protected void map(TKey key, T value, Context context) throws IOException, InterruptedException {
        NodeWritable[] ns = this.split(value);
        for (NodeWritable n : ns) {
            context.write(value, n);
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
