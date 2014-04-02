/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.readers;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * A record reader that reads RDF from any triples/quads format. Triples are
 * converted into quads in the default graph. This behaviour can be changed by
 * deriving from this class and overriding the {@link #getGraphNode()} method
 * 
 * @author rvesse
 * 
 */
@SuppressWarnings("javadoc")
public class TriplesOrQuadsReader extends AbstractRdfReader<Quad, QuadWritable> {

    @Override
    protected RecordReader<LongWritable, QuadWritable> selectRecordReader(Lang lang) throws IOException {
        if (!RDFLanguages.isQuads(lang) && !RDFLanguages.isTriples(lang))
            throw new IOException(lang.getLabel() + " is not a RDF triples/quads format");

        if (lang.equals(Lang.NQ) || lang.equals(Lang.NQUADS)) {
            return new WholeFileNQuadsReader();
        } else if (lang.equals(Lang.TRIG)) {
            return new TriGReader();
        } else if (lang.equals(Lang.NTRIPLES) || lang.equals(Lang.NT)) {
            return new TriplesToQuadsReader(new WholeFileNTriplesReader());
        } else if (lang.equals(Lang.TTL) || lang.equals(Lang.TURTLE) || lang.equals(Lang.N3)) {
            return new TriplesToQuadsReader(new TurtleReader());
        } else if (lang.equals(Lang.RDFXML)) {
            return new TriplesToQuadsReader(new RdfXmlReader());
        } else if (lang.equals(Lang.RDFJSON)) {
            return new TriplesToQuadsReader(new RdfJsonReader());
        }
        throw new IOException(lang.getLabel() + " has no associated RecordReader implementation");
    }

    /**
     * Gets the graph node which represents the graph into which triples will be
     * indicated to belong to when they are converting into quads.
     * <p>
     * Defaults to {@link Quad#defaultGraphNodeGenerated} which represents the
     * default graph
     * </p>
     * 
     * @return Graph node
     */
    protected Node getGraphNode() {
        return Quad.defaultGraphNodeGenerated;
    }
}
