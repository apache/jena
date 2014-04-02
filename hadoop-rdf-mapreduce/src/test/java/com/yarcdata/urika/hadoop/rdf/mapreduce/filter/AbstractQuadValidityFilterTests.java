/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.filter;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.core.Quad;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * Abstract tests for triple filter mappers that check triple validity
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractQuadValidityFilterTests extends AbstractNodeTupleFilterTests<Quad, QuadWritable> {

    @Override
    protected QuadWritable createValidValue(int i) {
        return new QuadWritable(
                new Quad(Quad.defaultGraphNodeGenerated, NodeFactory.createURI("http://subjects/" + i),
                        NodeFactory.createURI("http://predicate"), NodeFactory.createLiteral(Integer.toString(i),
                                XSDDatatype.XSDinteger)));
    }

    @Override
    protected QuadWritable createInvalidValue(int i) {
        switch (i % 8) {
        case 0:
            // Invalid to use Literal as Graph
            return new QuadWritable(new Quad(NodeFactory.createLiteral("invalid"), NodeFactory.createURI("http://subjects/" + i),
                    NodeFactory.createURI("http://predicate"), NodeFactory.createLiteral(Integer.toString(i),
                            XSDDatatype.XSDinteger)));
        case 1:
            // Invalid to use Variable as Graph
            return new QuadWritable(new Quad(NodeFactory.createVariable("invalid"),
                    NodeFactory.createURI("http://subjects/" + i), NodeFactory.createURI("http://predicate"),
                    NodeFactory.createLiteral(Integer.toString(i), XSDDatatype.XSDinteger)));
        case 2:
            // Invalid to use Literal as Subject
            return new QuadWritable(new Quad(Quad.defaultGraphNodeGenerated, NodeFactory.createLiteral("invalid"),
                    NodeFactory.createURI("http://predicate"), NodeFactory.createLiteral(Integer.toString(i),
                            XSDDatatype.XSDinteger)));
        case 3:
            // Invalid to use Variable as Subject
            return new QuadWritable(new Quad(Quad.defaultGraphNodeGenerated, NodeFactory.createVariable("invalid"),
                    NodeFactory.createURI("http://predicate"), NodeFactory.createLiteral(Integer.toString(i),
                            XSDDatatype.XSDinteger)));
        case 4:
            // Invalid to use Blank Node as Predicate
            return new QuadWritable(new Quad(Quad.defaultGraphNodeGenerated, NodeFactory.createURI("http://subjects/" + i),
                    NodeFactory.createAnon(), NodeFactory.createLiteral(Integer.toString(i), XSDDatatype.XSDinteger)));
        case 5:
            // Invalid to use Literal as Predicate
            return new QuadWritable(new Quad(Quad.defaultGraphNodeGenerated, NodeFactory.createURI("http://subjects/" + i),
                    NodeFactory.createLiteral("invalid"), NodeFactory.createLiteral(Integer.toString(i), XSDDatatype.XSDinteger)));
        case 6:
            // Invalid to use Variable as Predicate
            return new QuadWritable(
                    new Quad(Quad.defaultGraphNodeGenerated, NodeFactory.createURI("http://subjects/" + i),
                            NodeFactory.createVariable("invalid"), NodeFactory.createLiteral(Integer.toString(i),
                                    XSDDatatype.XSDinteger)));
        default:
            // Invalid to use Variable as Object
            return new QuadWritable(new Quad(Quad.defaultGraphNodeGenerated, NodeFactory.createURI("http://subjects/" + i),
                    NodeFactory.createURI("http://predicate"), NodeFactory.createVariable("invalid")));
        }
    }
}
