/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */ 

package com.yarcdata.urika.hadoop.rdf.mapreduce;

import java.io.IOException;

import org.apache.hadoop.mapreduce.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A mapper which discards the value replacing it with the key
 * @author rvesse
 *
 * @param <TKey> Key type
 * @param <TValue> Value type
 */
public class KeyMapper<TKey, TValue> extends Mapper<TKey, TValue, TKey, TKey> {
    private static final Logger LOG = LoggerFactory.getLogger(KeyMapper.class);

    private boolean tracing = false;
    
    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        this.tracing = LOG.isTraceEnabled();
    }

    @Override
    protected void map(TKey key, TValue value, Context context) throws IOException,
            InterruptedException {
        if (this.tracing) {
            LOG.trace("Key = {}", key);
        }
        context.write(key, key);
    }

}
