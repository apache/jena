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

package org.apache.jena.hadoop.rdf.mapreduce.split;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.jena.hadoop.rdf.mapreduce.AbstractMapperTests;
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
public abstract class AbstractNodeTupleSplitToNodesTests<TValue, T extends AbstractNodeTupleWritable<TValue>> extends
        AbstractMapperTests<LongWritable, T, LongWritable, NodeWritable> {

    /**
     * Generates data for use in tests
     * 
     * @param driver
     *            Driver
     * @param num
     *            Number of tuples to generate
     */
    protected void generateData(MapDriver<LongWritable, T, LongWritable, NodeWritable> driver, int num) {
        for (int i = 0; i < num; i++) {
            LongWritable key = new LongWritable(i);
            T value = this.createValue(i);
            NodeWritable[] nodes = this.getNodes(value);

            driver.addInput(key, value);
            for (NodeWritable n : nodes) {
                driver.addOutput(key, n);
            }
        }
    }

    protected abstract T createValue(int i);

    protected abstract NodeWritable[] getNodes(T tuple);

    protected final void testSplitToNodes(int num) throws IOException {
        MapDriver<LongWritable, T, LongWritable, NodeWritable> driver = this.getMapDriver();
        this.generateData(driver, num);
        driver.runTest();
    }

    /**
     * Test splitting tuples into their constituent nodes
     * 
     * @throws IOException
     */
    @Test
    public final void split_to_nodes_01() throws IOException {
        this.testSplitToNodes(1);
    }
    
    /**
     * Test splitting tuples into their constituent nodes
     * 
     * @throws IOException
     */
    @Test
    public final void split_to_nodes_02() throws IOException {
        this.testSplitToNodes(100);
    }
    
    /**
     * Test splitting tuples into their constituent nodes
     * 
     * @throws IOException
     */
    @Test
    public final void split_to_nodes_03() throws IOException {
        this.testSplitToNodes(1000);
    }
    
    /**
     * Test splitting tuples into their constituent nodes
     * 
     * @throws IOException
     */
    @Test
    public final void split_to_nodes_04() throws IOException {
        this.testSplitToNodes(2500);
    }
}
