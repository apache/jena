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

package org.apache.jena.hadoop.rdf.mapreduce.filter;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;
import org.apache.jena.datatypes.xsd.XSDDatatype ;
import org.apache.jena.graph.NodeFactory ;
import org.apache.jena.graph.Triple ;
import org.apache.jena.hadoop.rdf.mapreduce.RdfMapReduceConstants;
import org.apache.jena.hadoop.rdf.mapreduce.filter.positional.TripleFilterByPredicateUriMapper;
import org.apache.jena.hadoop.rdf.types.TripleWritable;
import org.apache.jena.vocabulary.RDF ;
import org.apache.jena.vocabulary.RDFS ;

/**
 * Tests for the {@link TripleFilterByPredicateUriMapper}
 * 
 * 
 * 
 */
public class TripleInvertedFilterByPredicateMapperTest extends AbstractNodeTupleFilterTests<Triple, TripleWritable> {

    private static final String[] DEFAULT_PREDICATE_POOL = new String[] { RDF.type.getURI(), RDFS.range.getURI(),
            RDFS.domain.getURI() };

    @Override
    protected Mapper<LongWritable, TripleWritable, LongWritable, TripleWritable> getInstance() {
        return new TripleFilterByPredicateUriMapper<LongWritable>();
    }

    @Override
    protected void configureDriver(MapDriver<LongWritable, TripleWritable, LongWritable, TripleWritable> driver) {
        super.configureDriver(driver);
        driver.getContext().getConfiguration().setStrings(RdfMapReduceConstants.FILTER_PREDICATE_URIS, this.getPredicatePool());
        driver.getContext().getConfiguration().setBoolean(RdfMapReduceConstants.FILTER_INVERT, true);
    }

    @Override
    protected boolean isInverted() {
        return true;
    }

    /**
     * Gets the pool of predicates considered valid
     * 
     * @return Predicate pool
     */
    protected String[] getPredicatePool() {
        return DEFAULT_PREDICATE_POOL;
    }

    @Override
    protected TripleWritable createInvalidValue(int i) {
        return new TripleWritable(
                new Triple(NodeFactory.createURI("http://subjects/" + i), NodeFactory.createURI("http://predicate"),
                        NodeFactory.createLiteral(Integer.toString(i), XSDDatatype.XSDinteger)));
    }

    @Override
    protected TripleWritable createValidValue(int i) {
        String[] predicates = this.getPredicatePool();
        if (predicates.length == 0)
            return this.createInvalidValue(i);
        return new TripleWritable(new Triple(NodeFactory.createURI("http://subjects/" + i), NodeFactory.createURI(predicates[i
                % predicates.length]), NodeFactory.createLiteral(Integer.toString(i), XSDDatatype.XSDinteger)));
    }

}
