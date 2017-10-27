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

package org.apache.jena.hadoop.rdf.io.input.turtle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.jena.atlas.lib.StrUtils;
import org.apache.jena.hadoop.rdf.io.RdfIOConstants;
import org.apache.jena.hadoop.rdf.io.input.AbstractWholeFileTripleInputFormatTests;
import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.apache.jena.riot.Lang;
import org.junit.Test;


/**
 * Tests for turtle input format
 * 
 * 
 * 
 */
public class TurtleInputTest extends AbstractWholeFileTripleInputFormatTests {

    @Override
    protected final String getFileExtension() {
        return ".ttl";
    }

    @Override
    protected final Lang getRdfLanguage() {
        return Lang.TURTLE;
    }
    
    @Override
    protected InputFormat<LongWritable, TripleWritable> getInputFormat() {
        return new TurtleInputFormat();
    }
    
    @Test
    public void turtle_with_prefixes_01() throws IOException, InterruptedException {
        // Try to reproduce JENA-1075
        
        // Create test data
        File f = folder.newFile("prefixes.ttl");
        
        try (FileWriter writer = new FileWriter(f)) {
            //@formatter:off
            writer.write(StrUtils.strjoinNL("@prefix : <http://test/ns#> .",
                                            ":s :p :o ."));
            //@formatter:on
            writer.close();
        }
        
        Configuration config = this.prepareConfiguration();
        config.setBoolean(RdfIOConstants.INPUT_IGNORE_BAD_TUPLES, false);
        this.testSingleInput(config, f, 1, 1);
        
        // Clean up
        if (f.exists()) f.delete();
    }
}
