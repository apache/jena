/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.split;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;

import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * Tests for the {@link TripleSplitToNodesMapper}
 * 
 * @author rvesse
 * 
 */
public class TripleSplitToNodesMapperTest extends AbstractTripleSplitToNodesTests {

    @Override
    protected Mapper<LongWritable, TripleWritable, LongWritable, NodeWritable> getInstance() {
        return new TripleSplitToNodesMapper<LongWritable>();
    }

}
