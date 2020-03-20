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

package org.apache.jena.hadoop.rdf.mapreduce.count;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.jena.hadoop.rdf.mapreduce.AbstractMapperTests;
import org.apache.jena.hadoop.rdf.types.AbstractNodeTupleWritable;
import org.apache.jena.hadoop.rdf.types.NodeWritable;
import org.junit.Test;


/**
 * Abstract tests for mappers derived from
 * {@link AbstractNodeTupleNodeCountMapper}
 * 
 * 
 * 
 * @param <TValue>
 *            Tuple type
 * @param <T>
 *            Writable tuple type
 */
public abstract class AbstractNodeTupleNodeCountTests<TValue, T extends AbstractNodeTupleWritable<TValue>> extends
        AbstractMapperTests<LongWritable, T, NodeWritable, LongWritable> {

    /**
     * Generates tuples for the tests
     * 
     * @param driver
     *            Driver
     * @param num
     *            Number of tuples to generate
     */
    protected void generateData(MapDriver<LongWritable, T, NodeWritable, LongWritable> driver, int num) {
        LongWritable expectedCount = new LongWritable(1);
        for (int i = 0; i < num; i++) {
            LongWritable key = new LongWritable(i);
            T value = this.createValue(i);
            NodeWritable[] nodes = this.getNodes(value);

            driver.addInput(key, value);
            for (NodeWritable n : nodes) {
                driver.addOutput(n, expectedCount);
            }
        }
    }

    /**
     * Creates a tuple value
     * 
     * @param i
     *            Index
     * @return Tuple value
     */
    protected abstract T createValue(int i);

    /**
     * Splits the tuple value into its constituent nodes
     * 
     * @param tuple
     *            Tuple value
     * @return Nodes
     */
    protected abstract NodeWritable[] getNodes(T tuple);

    /**
     * Runs a node count test
     * 
     * @param num
     *            Number of tuples to generate
     * @throws IOException
     */
    protected void testNodeCount(int num) throws IOException {
        MapDriver<LongWritable, T, NodeWritable, LongWritable> driver = this.getMapDriver();
        this.generateData(driver, num);
        driver.runTest();
    }

    /**
     * Tests node counting
     * 
     * @throws IOException
     */
    @Test
    public void node_count_01() throws IOException {
        this.testNodeCount(1);
    }

    /**
     * Tests node counting
     * 
     * @throws IOException
     */
    @Test
    public void node_count_02() throws IOException {
        this.testNodeCount(100);
    }

    /**
     * Tests node counting
     * 
     * @throws IOException
     */
    @Test
    public void node_count_03() throws IOException {
        this.testNodeCount(1000);
    }

    /**
     * Tests node counting
     * 
     * @throws IOException
     */
    @Test
    public void node_count_04() throws IOException {
        this.testNodeCount(2500);
    }
}
