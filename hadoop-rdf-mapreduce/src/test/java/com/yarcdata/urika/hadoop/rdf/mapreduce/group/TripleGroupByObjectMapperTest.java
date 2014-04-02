/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.group;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;

import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * Tests for the {@link TripleGroupByObjectMapper}
 * 
 * @author rvesse
 * 
 */
public class TripleGroupByObjectMapperTest extends AbstractTripleGroupingTests {

    @Override
    protected NodeWritable getOutputKey(TripleWritable tuple) {
        return new NodeWritable(tuple.get().getObject());
    }

    @Override
    protected Mapper<LongWritable, TripleWritable, NodeWritable, TripleWritable> getInstance() {
        return new TripleGroupByObjectMapper<LongWritable>();
    }

}
