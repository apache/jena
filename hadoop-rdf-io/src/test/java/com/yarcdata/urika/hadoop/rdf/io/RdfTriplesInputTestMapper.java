/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */ 

package com.yarcdata.urika.hadoop.rdf.io;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;

import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * A test mapper which takes in line based RDF triple input and just produces triples
 * @author rvesse
 *
 */
public class RdfTriplesInputTestMapper extends Mapper<LongWritable, TripleWritable, NullWritable, TripleWritable> {
    
    private static final Logger LOG = Logger.getLogger(RdfTriplesInputTestMapper.class);

    @Override
    protected void map(LongWritable key, TripleWritable value, Context context)
            throws IOException, InterruptedException {
        LOG.info("Line " + key.toString() + " => " + value.toString());
        context.write(NullWritable.get(), value);
    }

    
}
