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

package org.apache.jena.hadoop.rdf.io.output.trix;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.jena.hadoop.rdf.io.output.AbstractQuadOutputFormatTests;
import org.apache.jena.hadoop.rdf.types.QuadWritable;
import org.apache.jena.riot.Lang;

/**
 * Tests for TriX output format
 */
public class TriXOutputTest extends AbstractQuadOutputFormatTests {

    @Override
    protected String getFileExtension() {
        return ".trix";
    }

    @Override
    protected Lang getRdfLanguage() {
        return Lang.TRIX;
    }

    @Override
    protected OutputFormat<NullWritable, QuadWritable> getOutputFormat() {
        return new TriXOutputFormat<NullWritable>();
    }

}
