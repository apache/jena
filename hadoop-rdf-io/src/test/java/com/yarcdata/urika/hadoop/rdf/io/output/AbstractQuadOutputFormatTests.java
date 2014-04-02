/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.output;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.core.Quad;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * Abstract tests for quad output formats
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractQuadOutputFormatTests extends AbstractNodeTupleOutputFormatTests<Quad, QuadWritable> {

    @Override
    protected Iterator<QuadWritable> generateTuples(int num) {
        List<QuadWritable> qs = new ArrayList<QuadWritable>();
        for (int i = 0; i < num; i++) {
            Quad q = new Quad(NodeFactory.createURI("http://example.org/graphs/" + i),
                    NodeFactory.createURI("http://example.org/subjects/" + i),
                    NodeFactory.createURI("http://example.org/predicate"), NodeFactory.createLiteral(Integer.toString(i),
                            XSDDatatype.XSDinteger));
            qs.add(new QuadWritable(q));
        }
        return qs.iterator();
    }
}
