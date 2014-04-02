/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import com.yarcdata.urika.hadoop.rdf.io.input.readers.QuadsReader;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * RDF input format that can handle any RDF quads format that ARQ supports
 * selecting the format to use for each file based upon the file extension
 * 
 * @author rvesse
 * 
 */
public class QuadsInputFormat extends AbstractWholeFileInputFormat<LongWritable, QuadWritable> {

    @Override
    public RecordReader<LongWritable, QuadWritable> createRecordReader(InputSplit split, TaskAttemptContext context)
            throws IOException, InterruptedException {
        return new QuadsReader();
    }

}
