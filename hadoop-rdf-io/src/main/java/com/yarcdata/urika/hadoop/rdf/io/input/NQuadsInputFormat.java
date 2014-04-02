/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import com.yarcdata.urika.hadoop.rdf.io.input.readers.NQuadsReader;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * NQuads input format
 * 
 * @author rvesse
 * 
 */
public class NQuadsInputFormat extends AbstractNLineFileInputFormat<LongWritable, QuadWritable> {

    @Override
    public RecordReader<LongWritable, QuadWritable> createRecordReader(InputSplit arg0, TaskAttemptContext arg1)
            throws IOException, InterruptedException {
        return new NQuadsReader();
    }

}
