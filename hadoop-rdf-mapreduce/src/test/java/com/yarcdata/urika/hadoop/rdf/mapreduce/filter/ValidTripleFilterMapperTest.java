/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.filter;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;

import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * Tests for the {@link ValidTripleFilterMapper}
 * 
 * @author rvesse
 * 
 */
public class ValidTripleFilterMapperTest extends AbstractTripleValidityFilterTests {

    @Override
    protected Mapper<LongWritable, TripleWritable, LongWritable, TripleWritable> getInstance() {
        return new ValidTripleFilterMapper<LongWritable>();
    }

}
