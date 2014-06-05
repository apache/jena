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

package org.apache.jena.hadoop.rdf.mapreduce.group;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.jena.hadoop.rdf.mapreduce.AbstractMapperTests;
import org.apache.jena.hadoop.rdf.mapreduce.split.AbstractNodeTupleSplitToNodesMapper;
import org.apache.jena.hadoop.rdf.types.AbstractNodeTupleWritable;
import org.apache.jena.hadoop.rdf.types.NodeWritable;
import org.junit.Test;


/**
 * Abstract tests for {@link AbstractNodeTupleSplitToNodesMapper}
 * implementations
 * 
 * 
 * 
 * @param <TValue>
 *            Tuple type
 * @param <T>
 *            Writable tuple type
 */
public abstract class AbstractNodeTupleGroupingTests<TValue, T extends AbstractNodeTupleWritable<TValue>> extends
        AbstractMapperTests<LongWritable, T, NodeWritable, T> {

    /**
     * Generates data for use in tests
     * 
     * @param driver
     *            Driver
     * @param num
     *            Number of tuples to generate
     */
    protected void generateData(MapDriver<LongWritable, T, NodeWritable, T> driver, int num) {
        for (int i = 0; i < num; i++) {
            LongWritable inputKey = new LongWritable(i);
            T value = this.createValue(i);
            NodeWritable outputKey = this.getOutputKey(value);

            driver.addInput(inputKey, value);
            driver.addOutput(outputKey, value);
        }
    }

    protected abstract T createValue(int i);

    protected abstract NodeWritable getOutputKey(T tuple);

    protected final void testGrouping(int num) throws IOException {
        MapDriver<LongWritable, T, NodeWritable, T> driver = this.getMapDriver();
        this.generateData(driver, num);
        driver.runTest();
    }

    /**
     * Test grouping tuples by nodes
     * 
     * @throws IOException
     */
    @Test
    public final void grouping_01() throws IOException {
        this.testGrouping(1);
    }
    
    /**
     * Test grouping tuples by nodes
     * 
     * @throws IOException
     */
    @Test
    public final void grouping_02() throws IOException {
        this.testGrouping(100);
    }
    
    /**
     * Test grouping tuples by nodes
     * 
     * @throws IOException
     */
    @Test
    public final void grouping_03() throws IOException {
        this.testGrouping(1000);
    }
    
    /**
     * Test grouping tuples by nodes
     * 
     * @throws IOException
     */
    @Test
    public final void grouping_04() throws IOException {
        this.testGrouping(2500);
    }
}
