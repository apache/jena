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

package org.apache.jena.hadoop.rdf.io.output.turtle;

import java.io.Writer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.jena.graph.Triple ;
import org.apache.jena.hadoop.rdf.io.output.AbstractStreamRdfNodeTupleOutputFormat;
import org.apache.jena.hadoop.rdf.io.output.writers.StreamRdfTripleWriter;
import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.writer.WriterStreamRDFBlocks;

/**
 * Turtle output format
 *
 * @param <TKey>
 *            Key type
 */
public class TurtleOutputFormat<TKey> extends AbstractStreamRdfNodeTupleOutputFormat<TKey, Triple, TripleWritable> {

    @Override
    protected String getFileExtension() {
        return ".ttl";
    }

    @Override
    protected RecordWriter<TKey, TripleWritable> getRecordWriter(StreamRDF stream, Writer writer, Configuration config) {
        return new StreamRdfTripleWriter<TKey>(stream, writer);
    }

    @Override
    protected StreamRDF getStream(Writer writer, Configuration config) {
        return new WriterStreamRDFBlocks(writer, null);
    }

}
