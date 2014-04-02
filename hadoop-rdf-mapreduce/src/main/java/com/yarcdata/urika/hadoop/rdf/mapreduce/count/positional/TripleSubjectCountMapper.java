/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */ 

package com.yarcdata.urika.hadoop.rdf.mapreduce.count.positional;

import com.yarcdata.urika.hadoop.rdf.mapreduce.count.NodeCountReducer;
import com.yarcdata.urika.hadoop.rdf.mapreduce.count.TripleNodeCountMapper;
import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * A mapper for counting subject node usages within triples designed primarily for use
 * in conjunction with {@link NodeCountReducer}
 * 
 * @author rvesse
 * 
 * @param <TKey> Key type
 */
public class TripleSubjectCountMapper<TKey> extends TripleNodeCountMapper<TKey> {

    @Override
    protected NodeWritable[] getNodes(TripleWritable tuple) {
        return new NodeWritable[] { new NodeWritable(tuple.get().getSubject()) };
    }
}
