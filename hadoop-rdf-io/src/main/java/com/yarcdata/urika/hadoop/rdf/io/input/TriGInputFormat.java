/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import com.yarcdata.urika.hadoop.rdf.io.input.readers.TriGReader;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * Input format for TriG
 * 
 * @author rvesse
 * 
 */
public class TriGInputFormat extends AbstractWholeFileInputFormat<LongWritable, QuadWritable> {

    @Override
    public RecordReader<LongWritable, QuadWritable> createRecordReader(InputSplit split, TaskAttemptContext context)
            throws IOException, InterruptedException {
        return new TriGReader();
    }

}
