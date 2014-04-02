/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.count;

import java.io.IOException;
import java.util.Iterator;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Reducer;

import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;

/**
 * A reducer which takes node keys with a sequence of longs representing counts
 * as the values and sums the counts together into pairs consisting of a node
 * key and a count value.
 * 
 * @author rvesse
 * 
 */
public class NodeCountReducer extends Reducer<NodeWritable, LongWritable, NodeWritable, LongWritable> {

    @Override
    protected void reduce(NodeWritable key, Iterable<LongWritable> values, Context context) throws IOException,
            InterruptedException {
        long count = 0;
        Iterator<LongWritable> iter = values.iterator();
        while (iter.hasNext()) {
            count += iter.next().get();
        }
        context.write(key, new LongWritable(count));
    }

}
