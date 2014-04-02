/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.group;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;

import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * Tests for the {@link QuadGroupByObjectMapper}
 * 
 * @author rvesse
 * 
 */
public class QuadGroupByObjectMapperTest extends AbstractQuadGroupingTests {

    @Override
    protected NodeWritable getOutputKey(QuadWritable tuple) {
        return new NodeWritable(tuple.get().getObject());
    }

    @Override
    protected Mapper<LongWritable, QuadWritable, NodeWritable, QuadWritable> getInstance() {
        return new QuadGroupByObjectMapper<LongWritable>();
    }

}
