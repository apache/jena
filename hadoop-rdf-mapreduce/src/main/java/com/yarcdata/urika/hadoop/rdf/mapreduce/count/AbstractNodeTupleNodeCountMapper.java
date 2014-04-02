/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.count;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;

import com.yarcdata.urika.hadoop.rdf.types.AbstractNodeTupleWritable;
import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;

/**
 * Abstract mapper class for mappers which split node tuple values into pairs of
 * node keys with a long value of 1. Can be used in conjunction with a
 * {@link NodeCountReducer} to count the usages of each unique node.
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
public abstract class AbstractNodeTupleNodeCountMapper<TKey, TValue, T extends AbstractNodeTupleWritable<TValue>> extends
        Mapper<TKey, T, NodeWritable, LongWritable> {
    
    private LongWritable initialCount = new LongWritable(1);

    @Override
    protected void map(TKey key, T value, Context context) throws IOException,
            InterruptedException {
        NodeWritable[] ns = this.getNodes(value);
        for (NodeWritable n : ns) {
            context.write(n, this.initialCount);
        }
    }

    /**
     * Gets the nodes of the tuple which are to be counted
     * 
     * @param tuple
     *            Tuple
     * @return Nodes
     */
    protected abstract NodeWritable[] getNodes(T tuple);

}
