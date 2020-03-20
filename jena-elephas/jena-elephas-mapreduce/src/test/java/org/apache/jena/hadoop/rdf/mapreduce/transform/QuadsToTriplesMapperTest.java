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

package org.apache.jena.hadoop.rdf.mapreduce.transform;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.hadoop.mrunit.types.Pair;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.hadoop.rdf.mapreduce.AbstractMapperTests;
import org.apache.jena.hadoop.rdf.types.QuadWritable;
import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.apache.jena.sparql.core.Quad ;
import org.junit.Test;

/**
 * Tests for the {@link QuadsToTriplesMapper}
 * 
 * 
 * 
 */
public class QuadsToTriplesMapperTest extends AbstractMapperTests<LongWritable, QuadWritable, LongWritable, TripleWritable> {

    @Override
    protected Mapper<LongWritable, QuadWritable, LongWritable, TripleWritable> getInstance() {
        return new QuadsToTriplesMapper<LongWritable>();
    }

    protected void generateData(MapDriver<LongWritable, QuadWritable, LongWritable, TripleWritable> driver, int num) {
        for (int i = 0; i < num; i++) {
            Triple t = new Triple(NodeFactory.createURI("http://subjects/" + i), NodeFactory.createURI("http://predicate"),
                    NodeFactory.createLiteral(Integer.toString(i), XSDDatatype.XSDinteger));
            Quad q = new Quad(Quad.defaultGraphNodeGenerated, t);
            driver.addInput(new LongWritable(i), new QuadWritable(q));
            driver.addOutput(new LongWritable(i), new TripleWritable(t));
        }
    }

    /**
     * Tests quads to triples conversion
     * 
     * @throws IOException
     */
    @Test
    public void quads_to_triples_mapper_01() throws IOException {
        MapDriver<LongWritable, QuadWritable, LongWritable, TripleWritable> driver = this.getMapDriver();

        Triple t = new Triple(NodeFactory.createURI("http://s"), NodeFactory.createURI("http://p"),
                NodeFactory.createLiteral("test"));
        Quad q = new Quad(Quad.defaultGraphNodeGenerated, t);
        driver.withInput(new Pair<LongWritable, QuadWritable>(new LongWritable(1), new QuadWritable(q))).withOutput(
                new Pair<LongWritable, TripleWritable>(new LongWritable(1), new TripleWritable(t)));
        driver.runTest();
    }
    
    /**
     * Tests quads to triples conversion
     * 
     * @throws IOException
     */
    @Test
    public void quads_to_triples_mapper_02() throws IOException {
        MapDriver<LongWritable, QuadWritable, LongWritable, TripleWritable> driver = this.getMapDriver();
        this.generateData(driver, 100);
        driver.runTest();
    }
    
    /**
     * Tests quads to triples conversion
     * 
     * @throws IOException
     */
    @Test
    public void quads_to_triples_mapper_03() throws IOException {
        MapDriver<LongWritable, QuadWritable, LongWritable, TripleWritable> driver = this.getMapDriver();
        this.generateData(driver, 1000);
        driver.runTest();
    }
    
    /**
     * Tests quads to triples conversion
     * 
     * @throws IOException
     */
    @Test
    public void quads_to_triples_mapper_04() throws IOException {
        MapDriver<LongWritable, QuadWritable, LongWritable, TripleWritable> driver = this.getMapDriver();
        this.generateData(driver, 10000);
        driver.runTest();
    }
}
