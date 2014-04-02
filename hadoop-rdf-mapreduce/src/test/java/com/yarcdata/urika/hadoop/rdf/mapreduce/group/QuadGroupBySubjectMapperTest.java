/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.group;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;

import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * Tests for the {@link QuadGroupBySubjectMapper}
 * 
 * @author rvesse
 * 
 */
public class QuadGroupBySubjectMapperTest extends AbstractQuadGroupingTests {

    @Override
    protected NodeWritable getOutputKey(QuadWritable tuple) {
        return new NodeWritable(tuple.get().getSubject());
    }

    @Override
    protected Mapper<LongWritable, QuadWritable, NodeWritable, QuadWritable> getInstance() {
        return new QuadGroupBySubjectMapper<LongWritable>();
    }

}
