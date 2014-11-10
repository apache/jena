package org.apache.jena.hadoop.rdf.io.input;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.jena.hadoop.rdf.io.input.readers.JsonLDTripleReader;
import org.apache.jena.hadoop.rdf.types.TripleWritable;

public class JsonLDTripleInputFormat extends AbstractWholeFileInputFormat<LongWritable, TripleWritable> {

    @Override
    public RecordReader<LongWritable, TripleWritable> createRecordReader(InputSplit split, TaskAttemptContext context)
            throws IOException, InterruptedException {
        return new JsonLDTripleReader();
    }

}
