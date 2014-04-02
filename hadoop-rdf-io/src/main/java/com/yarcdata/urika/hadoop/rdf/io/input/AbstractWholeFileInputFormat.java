/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

/**
 * Abstract implementation of a while file input format where each file is a
 * single split
 * 
 * @author rvesse
 * 
 * @param <TKey>
 *            Key type
 * @param <TValue>
 *            Value type
 */
public abstract class AbstractWholeFileInputFormat<TKey, TValue> extends FileInputFormat<TKey, TValue> {

    @Override
    protected final boolean isSplitable(JobContext context, Path filename) {
        return false;
    }
}
