package org.apache.jena.hadoop.rdf.io.input;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.jena.hadoop.rdf.io.input.readers.JsonLDQuadReader;
import org.apache.jena.hadoop.rdf.types.QuadWritable;

public class JsonLDQuadInputFormat extends AbstractWholeFileInputFormat<LongWritable, QuadWritable> {

    @Override
    public RecordReader<LongWritable, QuadWritable> createRecordReader(InputSplit split, TaskAttemptContext context)
            throws IOException, InterruptedException {
        return new JsonLDQuadReader();
    }

}
