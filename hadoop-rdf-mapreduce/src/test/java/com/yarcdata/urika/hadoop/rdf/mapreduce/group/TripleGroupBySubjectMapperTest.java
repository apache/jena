/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.group;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;

import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * Tests for the {@link TripleGroupBySubjectMapper}
 * 
 * @author rvesse
 * 
 */
public class TripleGroupBySubjectMapperTest extends AbstractTripleGroupingTests {

    @Override
    protected NodeWritable getOutputKey(TripleWritable tuple) {
        return new NodeWritable(tuple.get().getSubject());
    }

    @Override
    protected Mapper<LongWritable, TripleWritable, NodeWritable, TripleWritable> getInstance() {
        return new TripleGroupBySubjectMapper<LongWritable>();
    }

}
