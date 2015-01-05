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

import java.util.Arrays;
import java.util.Collection;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.jena.hadoop.rdf.io.RdfIOConstants;
import org.apache.jena.hadoop.rdf.io.output.AbstractTripleOutputFormatTests;
import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.apache.jena.riot.Lang;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


/**
 * Tests for Turtle output
 * 
 * 
 * 
 */
@RunWith(Parameterized.class)
public class StreamedTurtleOutputTest extends AbstractTripleOutputFormatTests {

    static long $bs1 = RdfIOConstants.DEFAULT_OUTPUT_BATCH_SIZE;
    static long $bs2 = 1000;
    static long $bs3 = 100;
    static long $bs4 = 1;

    /**
     * @return Test parameters
     */
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] { { $bs1 }, { $bs2 }, { $bs3 }, { $bs4 } });
    }

    private final long batchSize;

    /**
     * Creates new tests
     * 
     * @param batchSize
     *            Batch size
     */
    public StreamedTurtleOutputTest(long batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    protected String getFileExtension() {
        return ".ttl";
    }

    @Override
    protected Lang getRdfLanguage() {
        return Lang.TURTLE;
    }
    
    @Override
    protected Configuration prepareConfiguration() {
        Configuration config = super.prepareConfiguration();
        config.setLong(RdfIOConstants.OUTPUT_BATCH_SIZE, this.batchSize);
        return config;
    }

    @Override
    protected OutputFormat<NullWritable, TripleWritable> getOutputFormat() {
        return new TurtleOutputFormat<NullWritable>();
    }

}
