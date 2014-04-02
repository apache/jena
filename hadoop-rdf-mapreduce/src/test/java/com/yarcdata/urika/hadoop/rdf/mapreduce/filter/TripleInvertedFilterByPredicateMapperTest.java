/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.filter;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mrunit.mapreduce.MapDriver;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.yarcdata.urika.hadoop.rdf.mapreduce.RdfMapReduceConstants;
import com.yarcdata.urika.hadoop.rdf.mapreduce.filter.positional.TripleFilterByPredicateUriMapper;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * Tests for the {@link TripleFilterByPredicateUriMapper}
 * 
 * @author rvesse
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
