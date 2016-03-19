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

package org.apache.jena.hadoop.rdf.io.input.jsonld;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.jena.hadoop.rdf.io.input.AbstractWholeFileTripleInputFormatTests;
import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.apache.jena.riot.Lang;


/**
 * Tests for JSON-LD input
 * 
 *
 */
public class JsonLDTripleInputTest extends AbstractWholeFileTripleInputFormatTests {

    @Override
    protected Lang getRdfLanguage() {
        return Lang.JSONLD;
    }

    @Override
    protected String getFileExtension() {
        return ".jsonld";
    }

    @Override
    protected InputFormat<LongWritable, TripleWritable> getInputFormat() {
        return new JsonLDTripleInputFormat();
    }
    
    /** JSON_LD does not produce any triples from a bad document (no partial streaming).
     * @see #single_input_05()
    */
    @Override
    protected int single_input_05_expected() {
        return 0 ;
    }
    
    /** JSON_LD does not produce any triples from a bad document (no partial streaming).
     * @see #multiple_inputs_02()
     */
    @Override
    protected int multiple_inputs_02_expected() {
        return EMPTY_SIZE + SMALL_SIZE + LARGE_SIZE ;
    }
}
