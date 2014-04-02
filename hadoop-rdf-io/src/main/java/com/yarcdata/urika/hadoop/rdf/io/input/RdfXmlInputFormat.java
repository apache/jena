/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import com.yarcdata.urika.hadoop.rdf.io.input.readers.RdfXmlReader;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * RDF/XML input format
 * 
 * @author rvesse
 * 
 */
public class RdfXmlInputFormat extends AbstractWholeFileInputFormat<LongWritable, TripleWritable> {

    @Override
    public RecordReader<LongWritable, TripleWritable> createRecordReader(InputSplit split, TaskAttemptContext context)
            throws IOException, InterruptedException {
        return new RdfXmlReader();
    }

}
