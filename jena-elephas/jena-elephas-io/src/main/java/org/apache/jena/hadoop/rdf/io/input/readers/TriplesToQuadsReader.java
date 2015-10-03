/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.hadoop.rdf.io.input.readers;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.jena.graph.Node ;
import org.apache.jena.hadoop.rdf.types.QuadWritable;
import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.apache.jena.sparql.core.Quad ;

/**
 * A record reader that converts triples into quads by wrapping a
 * {@code RecordReader<LongWritable, TripleWritable>} implementation
 * 
 * 
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
