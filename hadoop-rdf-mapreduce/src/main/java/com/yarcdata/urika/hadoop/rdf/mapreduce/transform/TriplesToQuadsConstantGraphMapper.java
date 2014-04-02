/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.mapreduce.transform;

import java.io.IOException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

/**
 * A mapper which converts triples to quads where all triples are placed in the
 * same graph
 * 
 * @author rvesse
 * 
 * @param <TKey>
 *            Key type
 */
public class TriplesToQuadsConstantGraphMapper<TKey> extends AbstractTriplesToQuadsMapper<TKey> {

    private Node graphNode;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        super.setup(context);
        this.graphNode = this.getGraphNode();
    }

    /**
     * Gets the graph node that will be used for all quads, this will be called
     * once and only once during the
     * {@link #setup(org.apache.hadoop.mapreduce.Mapper.Context)} method and the
     * value returned cached for use throughout the lifetime of this mapper.
     * <p>
     * This implementation always used the default graph as the graph for
     * generated quads. You can override this method in your own derived
     * implementation to put triples into a different graph than the default
     * graph.
     * </p>
     * <p>
     * If instead you wanted to select different graphs for each triple you
     * should extend {@link AbstractTriplesToQuadsMapper} instead and override
     * the {@link #selectGraph(Triple)} method which is sealed in this
     * implementation.
     * </p>
     * 
     * @return
     */
    protected Node getGraphNode() {
        return Quad.defaultGraphNodeGenerated;
    }

    @Override
    protected final Node selectGraph(Triple triple) {
        return this.graphNode;
    }

}
