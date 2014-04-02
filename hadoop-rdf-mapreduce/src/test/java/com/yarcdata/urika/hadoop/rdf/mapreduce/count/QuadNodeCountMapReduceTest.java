/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.count;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;
import com.yarcdata.urika.hadoop.rdf.types.NodeWritable;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * Tests for the {@link TripleNodeCountMapper} used in conjunction with the
 * {@link NodeCountReducer}
 * 
 * @author rvesse
 * 
 */
public class QuadNodeCountMapReduceTest extends AbstractNodeTupleNodeCountReducedTests<Quad, QuadWritable> {

    @Override
    protected Mapper<LongWritable, QuadWritable, NodeWritable, LongWritable> getMapperInstance() {
        return new QuadNodeCountMapper<LongWritable>();
    }

    @Override
    protected Reducer<NodeWritable, LongWritable, NodeWritable, LongWritable> getReducerInstance() {
        return new NodeCountReducer();
    }

    @Override
    protected QuadWritable createValue(int i) {
        return new QuadWritable(new Quad(Quad.defaultGraphNodeGenerated, new Triple(
                NodeFactory.createURI("http://subjects/" + i), NodeFactory.createURI("http://predicate"),
                NodeFactory.createLiteral(Integer.toString(i), XSDDatatype.XSDinteger))));
    }

    @Override
    protected NodeWritable[] getNodes(QuadWritable tuple) {
        Quad q = tuple.get();
        return new NodeWritable[] { new NodeWritable(q.getGraph()), new NodeWritable(q.getSubject()),
                new NodeWritable(q.getPredicate()), new NodeWritable(q.getObject()) };
    }
}
