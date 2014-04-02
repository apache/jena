/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import com.yarcdata.urika.hadoop.rdf.io.input.readers.TriplesOrQuadsReader;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * RDF input format that can handle any RDF triple/quads format that ARQ
 * supports selecting the format to use for each file based upon the file
 * extension. Triples are converted into quads in the default graph.
 * 
 * @author rvesse
 * 
 */
public class TriplesOrQuadsInputFormat extends AbstractWholeFileInputFormat<LongWritable, QuadWritable> {

    @Override
    public RecordReader<LongWritable, QuadWritable> createRecordReader(InputSplit split, TaskAttemptContext context)
            throws IOException, InterruptedException {
        return new TriplesOrQuadsReader();
    }

}
