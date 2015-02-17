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

package org.apache.jena.hadoop.rdf.io.input.compressed.ntriples;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.jena.hadoop.rdf.io.input.compressed.AbstractCompressedTriplesInputFormatTests;
import org.apache.jena.hadoop.rdf.io.input.ntriples.NTriplesInputFormat;
import org.apache.jena.hadoop.rdf.types.TripleWritable;


/**
 * Abstract compressed NTriples input tests
 * 
 * 
 * 
 */
public abstract class AbstractCompressedNTriplesInputFormatTests extends AbstractCompressedTriplesInputFormatTests {

    private String ext;
    private CompressionCodec codec;

    /**
     * Creates new tests
     * 
     * @param ext
     *            File extension
     * @param codec
     *            Compression codec
     */
    public AbstractCompressedNTriplesInputFormatTests(String ext, CompressionCodec codec) {
        this.ext = ext;
        this.codec = codec;
    }

    @Override
    protected final String getFileExtension() {
        return this.ext;
    }

    @Override
    protected final CompressionCodec getCompressionCodec() {
        return this.codec;
    }

    @Override
    protected final InputFormat<LongWritable, TripleWritable> getInputFormat() {
        return new NTriplesInputFormat();
    }

}