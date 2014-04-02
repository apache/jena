/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.split;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;

import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * Tests for the {@link QuadSplitToNodesMapper}
 * 
 * @author rvesse
 * 
 */
public class QuadSplitToNodesMapperTest extends AbstractQuadSplitToNodesTests {

    @Override
    protected Mapper<LongWritable, QuadWritable, LongWritable, NodeWritable> getInstance() {
        return new QuadSplitToNodesMapper<LongWritable>();
    }

}
