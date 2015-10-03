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

package org.apache.jena.hadoop.rdf.mapreduce;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mrunit.mapreduce.MapReduceDriver;
import org.apache.jena.graph.Node ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.junit.Assert;
import org.junit.Test;

public class TestDistinctTriples
        extends
        AbstractMapReduceTests<LongWritable, TripleWritable, TripleWritable, NullWritable, NullWritable, TripleWritable> {

    @Override
    protected Mapper<LongWritable, TripleWritable, TripleWritable, NullWritable> getMapperInstance() {
        return new ValuePlusNullMapper<LongWritable, TripleWritable>();
    }

    @Override
    protected Reducer<TripleWritable, NullWritable, NullWritable, TripleWritable> getReducerInstance() {
        return new NullPlusKeyReducer<TripleWritable, NullWritable>();
    }

    @Test
    public void distinct_triples_01() throws IOException {
        MapReduceDriver<LongWritable, TripleWritable, TripleWritable, NullWritable, NullWritable, TripleWritable> driver = this
                .getMapReduceDriver();

        Triple t = new Triple(NodeFactory.createURI("urn:s"), NodeFactory.createURI("urn:p"),
                NodeFactory.createLiteral("1"));
        TripleWritable tw = new TripleWritable(t);
        driver.addInput(new LongWritable(1), tw);
        driver.addOutput(NullWritable.get(), tw);

        driver.runTest();
    }

    @Test
    public void distinct_triples_02() throws IOException {
        MapReduceDriver<LongWritable, TripleWritable, TripleWritable, NullWritable, NullWritable, TripleWritable> driver = this
                .getMapReduceDriver();

        Triple t = new Triple(NodeFactory.createURI("urn:s"), NodeFactory.createURI("urn:p"),
                NodeFactory.createLiteral("1"));
        TripleWritable tw = new TripleWritable(t);
        for (int i = 0; i < 100; i++) {
            driver.addInput(new LongWritable(i), tw);
        }
        driver.addOutput(NullWritable.get(), tw);

        driver.runTest();
    }

    @Test
    public void distinct_triples_03() throws IOException {
        MapReduceDriver<LongWritable, TripleWritable, TripleWritable, NullWritable, NullWritable, TripleWritable> driver = this
                .getMapReduceDriver();

        Triple t = new Triple(NodeFactory.createURI("urn:s"), NodeFactory.createURI("urn:p"),
                NodeFactory.createLiteral("1"));
        Triple t2 = new Triple(t.getSubject(), t.getPredicate(), NodeFactory.createLiteral("2"));
        Assert.assertNotEquals(t, t2);

        TripleWritable tw = new TripleWritable(t);
        TripleWritable tw2 = new TripleWritable(t2);
        Assert.assertNotEquals(tw, tw2);

        driver.addInput(new LongWritable(1), tw);
        driver.addInput(new LongWritable(2), tw2);
        driver.addOutput(NullWritable.get(), tw);
        driver.addOutput(NullWritable.get(), tw2);

        driver.runTest(false);
    }

    @Test
    public void distinct_triples_04() throws IOException {
        MapReduceDriver<LongWritable, TripleWritable, TripleWritable, NullWritable, NullWritable, TripleWritable> driver = this
                .getMapReduceDriver();

        Node s1 = NodeFactory.createURI("urn:nf#cbf2b2c7-109e-4097-bbea-f67f272c7fcc");
        Node s2 = NodeFactory.createURI("urn:nf#bb08b75c-1ad2-47ef-acd2-eb2d92b94b89");
        Node p = NodeFactory.createURI("urn:p");
        Node o = NodeFactory.createURI("urn:66.230.159.118");
        Assert.assertNotEquals(s1, s2);

        Triple t1 = new Triple(s1, p, o);
        Triple t2 = new Triple(s2, p, o);
        Assert.assertNotEquals(t1, t2);

        TripleWritable tw1 = new TripleWritable(t1);
        TripleWritable tw2 = new TripleWritable(t2);
        Assert.assertNotEquals(tw1, tw2);
        Assert.assertNotEquals(0, tw1.compareTo(tw2));

        driver.addInput(new LongWritable(1), tw1);
        driver.addInput(new LongWritable(2), tw2);
        driver.addOutput(NullWritable.get(), tw1);
        driver.addOutput(NullWritable.get(), tw2);

        driver.runTest(false);
    }
}
