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
package org.apache.jena.hadoop.rdf.io.input.bnodes;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.jena.hadoop.rdf.io.input.rdfjson.RdfJsonInputFormat;
import org.apache.jena.hadoop.rdf.io.output.rdfjson.RdfJsonOutputFormat;
import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.apache.jena.riot.Lang;

/**
 * Tests blank node divergence when using the {@link RdfJsonInputFormat}
 */
public class RdfJsonBlankNodeTest extends AbstractTripleBlankNodeTests {

    @Override
    protected Lang getLanguage() {
        return Lang.RDFJSON;
    }

    @Override
    protected String getInitialInputExtension() {
        return ".rj";
    }

    @Override
    protected InputFormat<LongWritable, TripleWritable> createInitialInputFormat() {
        return new RdfJsonInputFormat();
    }

    @Override
    protected OutputFormat<LongWritable, TripleWritable> createIntermediateOutputFormat() {
        return new RdfJsonOutputFormat<>();
    }

    @Override
    protected InputFormat<LongWritable, TripleWritable> createIntermediateInputFormat() {
        return new RdfJsonInputFormat();
    }

}
