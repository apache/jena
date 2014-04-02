/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.readers;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * A record reader that converts triples into quads by wrapping a
 * {@code RecordReader<LongWritable, TripleWritable>} implementation
 * 
 * @author rvesse
 * 
 */
public class TriplesToQuadsReader extends RecordReader<LongWritable, QuadWritable> {

    private final RecordReader<LongWritable, TripleWritable> reader;
    private Node graph;

    /**
     * Creates a new reader
     * 
     * @param reader
     *            Triple reader
     */
    public TriplesToQuadsReader(RecordReader<LongWritable, TripleWritable> reader) {
        this(reader, Quad.defaultGraphNodeGenerated);
    }

    /**
     * Creates a new reader
     * 
     * @param reader
     *            Triple reader
     * @param graphNode
     *            Graph node
     */
    public TriplesToQuadsReader(RecordReader<LongWritable, TripleWritable> reader, Node graphNode) {
        if (reader == null)
            throw new NullPointerException("reader cannot be null");
        if (graphNode == null)
            throw new NullPointerException("Graph node cannot be null");
        this.reader = reader;
        this.graph = graphNode;
    }

    @Override
    public void initialize(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
        this.reader.initialize(split, context);
    }

    @Override
    public final boolean nextKeyValue() throws IOException, InterruptedException {
        return this.reader.nextKeyValue();
    }

    @Override
    public final LongWritable getCurrentKey() throws IOException, InterruptedException {
        return this.reader.getCurrentKey();
    }

    @Override
    public final QuadWritable getCurrentValue() throws IOException, InterruptedException {
        TripleWritable t = this.reader.getCurrentValue();
        return new QuadWritable(new Quad(this.graph, t.get()));
    }

    @Override
    public final float getProgress() throws IOException, InterruptedException {
        return this.reader.getProgress();
    }

    @Override
    public final void close() throws IOException {
        this.reader.close();
    }
}
