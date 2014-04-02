/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.group;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;

import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * Tests for the {@link QuadGroupByPredicateMapper}
 * 
 * @author rvesse
 * 
 */
public class QuadGroupByPredicateMapperTest extends AbstractQuadGroupingTests {

    @Override
    protected NodeWritable getOutputKey(QuadWritable tuple) {
        return new NodeWritable(tuple.get().getPredicate());
    }

    @Override
    protected Mapper<LongWritable, QuadWritable, NodeWritable, QuadWritable> getInstance() {
        return new QuadGroupByPredicateMapper<LongWritable>();
    }

}
