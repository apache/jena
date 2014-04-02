/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import com.yarcdata.urika.hadoop.rdf.io.input.readers.BlockedNQuadsReader;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * NTriples input format where files are processed as blocks of lines rather
 * than in a line based manner as with the {@link NQuadsInputFormat} or as
 * whole files with the {@link WholeFileNQuadsInputFormat}
 * <p>
 * This provides a compromise between the higher parser setup of creating more
 * parsers and the benefit of being able to split input files over multiple
 * mappers.
 * </p>
 * 
 * @author rvesse
 * 
 */
public class BlockedNQuadsInputFormat extends AbstractNLineFileInputFormat<LongWritable, QuadWritable> {

    @Override
    public RecordReader<LongWritable, QuadWritable> createRecordReader(InputSplit split, TaskAttemptContext context)
            throws IOException, InterruptedException {
        return new BlockedNQuadsReader();
    }

}
