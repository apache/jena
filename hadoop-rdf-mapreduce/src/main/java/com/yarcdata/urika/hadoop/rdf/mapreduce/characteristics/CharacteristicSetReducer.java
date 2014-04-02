/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.characteristics;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Reducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yarcdata.urika.hadoop.rdf.types.CharacteristicSetWritable;

/**
 * Reducer which takes in characteristic sets and sums up all their usage counts
 * 
 * @author rvesse
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
