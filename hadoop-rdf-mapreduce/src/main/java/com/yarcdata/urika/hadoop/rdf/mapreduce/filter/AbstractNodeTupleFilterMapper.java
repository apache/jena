/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.filter;

import java.io.IOException;

import org.apache.hadoop.mapreduce.Mapper;
import com.yarcdata.urika.hadoop.rdf.mapreduce.RdfMapReduceConstants;
import com.yarcdata.urika.hadoop.rdf.types.AbstractNodeTupleWritable;

/**
 * Abstract mapper implementation which helps in filtering tuples from the
 * input, derived implementations provide an implementation of the
 * {@link #accepts(TKey, T)}
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
@SuppressWarnings("javadoc")
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
