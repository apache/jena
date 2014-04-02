/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.filter;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * Abstract tests for triple filter mappers that check triple validity
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractTripleValidityFilterTests extends AbstractNodeTupleFilterTests<Triple, TripleWritable> {

    @Override
    protected TripleWritable createValidValue(int i) {
        return new TripleWritable(
                new Triple(NodeFactory.createURI("http://subjects/" + i), NodeFactory.createURI("http://predicate"),
                        NodeFactory.createLiteral(Integer.toString(i), XSDDatatype.XSDinteger)));
    }

    @Override
    protected TripleWritable createInvalidValue(int i) {
        switch (i % 6) {
        case 0:
            // Invalid to use Literal as Subject
            return new TripleWritable(new Triple(NodeFactory.createLiteral("invalid"), NodeFactory.createURI("http://predicate"),
                    NodeFactory.createLiteral(Integer.toString(i), XSDDatatype.XSDinteger)));
        case 1:
            // Invalid to use Variable as Subject
            return new TripleWritable(new Triple(NodeFactory.createVariable("invalid"),
                    NodeFactory.createURI("http://predicate"), NodeFactory.createLiteral(Integer.toString(i),
                            XSDDatatype.XSDinteger)));
        case 2:
            // Invalid to use Blank Node as Predicate
            return new TripleWritable(new Triple(NodeFactory.createURI("http://subjects/" + i), NodeFactory.createAnon(),
                    NodeFactory.createLiteral(Integer.toString(i), XSDDatatype.XSDinteger)));
        case 3:
            // Invalid to use Literal as Predicate
            return new TripleWritable(new Triple(NodeFactory.createURI("http://subjects/" + i),
                    NodeFactory.createLiteral("invalid"), NodeFactory.createLiteral(Integer.toString(i), XSDDatatype.XSDinteger)));
        case 4:
            // Invalid to use Variable as Predicate
            return new TripleWritable(
                    new Triple(NodeFactory.createURI("http://subjects/" + i), NodeFactory.createVariable("invalid"),
                            NodeFactory.createLiteral(Integer.toString(i), XSDDatatype.XSDinteger)));
        default:
            // Invalid to use Variable as Object
            return new TripleWritable(new Triple(NodeFactory.createURI("http://subjects/" + i),
                    NodeFactory.createURI("http://predicate"), NodeFactory.createVariable("invalid")));
        }
    }
}
