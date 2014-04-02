/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.transform;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

/**
 * A mapper which converts triples into quads using the subjects of the triples
 * as the graph nodes
 * 
 * @author rvesse
 * @param <TKey>
 *            Key type
 * 
 */
public class TriplesToQuadsBySubjectMapper<TKey> extends AbstractTriplesToQuadsMapper<TKey> {

    @Override
    protected final Node selectGraph(Triple triple) {
        return triple.getSubject();
    }

}
