/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import com.yarcdata.urika.hadoop.rdf.io.input.readers.WholeFileNQuadsReader;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * NQuads input format where files are processed as complete files rather than
 * in a line based manner as with the {@link NQuadsInputFormat}
 * <p>
 * This has the advantage of less parser setup overhead but the disadvantage
 * that the input cannot be split over multiple mappers.
 * </p>
 * 
 * @author rvesse
 * 
 */
public class WholeFileNQuadsInputFormat extends AbstractWholeFileInputFormat<LongWritable, QuadWritable> {

    @Override
    public RecordReader<LongWritable, QuadWritable> createRecordReader(InputSplit split, TaskAttemptContext context)
            throws IOException, InterruptedException {
        return new WholeFileNQuadsReader();
    }

}
