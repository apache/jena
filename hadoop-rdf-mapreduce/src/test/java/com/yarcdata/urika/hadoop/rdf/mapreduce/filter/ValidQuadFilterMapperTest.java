/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.filter;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;

import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * Tests for the {@link ValidQuadFilterMapper}
 * 
 * @author rvesse
 * 
 */
public class ValidQuadFilterMapperTest extends AbstractQuadValidityFilterTests {

    @Override
    protected Mapper<LongWritable, QuadWritable, LongWritable, QuadWritable> getInstance() {
        return new ValidQuadFilterMapper<LongWritable>();
    }

}
