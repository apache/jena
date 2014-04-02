/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.group;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * Abstract tests for {@link AbstractQuadGroupingMapper} implementations
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractQuadGroupingTests extends AbstractNodeTupleGroupingTests<Quad, QuadWritable> {

    @Override
    protected QuadWritable createValue(int i) {
        return new QuadWritable(new Quad(Quad.defaultGraphNodeGenerated, new Triple(
                NodeFactory.createURI("http://subjects/" + i), NodeFactory.createURI("http://predicate"),
                NodeFactory.createLiteral(Integer.toString(i), XSDDatatype.XSDinteger))));
    }
}
