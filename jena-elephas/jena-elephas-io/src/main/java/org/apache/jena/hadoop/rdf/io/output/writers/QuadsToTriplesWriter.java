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
package org.apache.jena.hadoop.rdf.io.output.writers;

import java.io.IOException;

import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.jena.hadoop.rdf.types.QuadWritable;
import org.apache.jena.hadoop.rdf.types.TripleWritable;

/**
 * A record writer that converts quads into triples by stripping off the graph
 * field
 * 
 * @param <TKey>
 *            Key type
 */
public class QuadsToTriplesWriter<TKey> extends RecordWriter<TKey, QuadWritable> {

    private RecordWriter<TKey, TripleWritable> writer;

    /**
     * Creates a new writer
     * 
     * @param tripleWriter
     *            Triple writer to use
     */
    public QuadsToTriplesWriter(RecordWriter<TKey, TripleWritable> tripleWriter) {
        if (tripleWriter == null)
            throw new NullPointerException("tripleWriter cannot be null");
        this.writer = tripleWriter;
    }

    @Override
    public void write(TKey key, QuadWritable value) throws IOException, InterruptedException {
        this.writer.write(key, new TripleWritable(value.get().asTriple()));
    }

    @Override
    public void close(TaskAttemptContext context) throws IOException, InterruptedException {
        this.writer.close(context);
    }
}
