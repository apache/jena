/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */ 

package com.yarcdata.urika.hadoop.rdf.mapreduce.count.positional;

import com.yarcdata.urika.hadoop.rdf.mapreduce.count.NodeCountReducer;
import com.yarcdata.urika.hadoop.rdf.mapreduce.count.QuadNodeCountMapper;
import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * A mapper for counting subject node usages within quads designed primarily for use
 * in conjunction with {@link NodeCountReducer}
 * 
 * @author rvesse
 * 
 * @param <TKey> Key type
 */
public class QuadSubjectCountMapper<TKey> extends QuadNodeCountMapper<TKey> {

    @Override
    protected NodeWritable[] getNodes(QuadWritable tuple) {
        return new NodeWritable[] { new NodeWritable(tuple.get().getSubject()) };
    }
}
