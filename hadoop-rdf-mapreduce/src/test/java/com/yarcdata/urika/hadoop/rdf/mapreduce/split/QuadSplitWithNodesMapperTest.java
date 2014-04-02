/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.split;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;

import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * Tests for the {@link QuadSplitWithNodesMapper}
 * 
 * @author rvesse
 * 
 */
public class QuadSplitWithNodesMapperTest extends AbstractQuadSplitWithNodesTests {

    @Override
    protected Mapper<LongWritable, QuadWritable, QuadWritable, NodeWritable> getInstance() {
        return new QuadSplitWithNodesMapper<LongWritable>();
    }

}
