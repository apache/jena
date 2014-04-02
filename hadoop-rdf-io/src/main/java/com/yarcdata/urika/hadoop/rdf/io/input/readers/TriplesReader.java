/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.readers;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

import com.hp.hpl.jena.graph.Triple;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * A record reader that reads triples from any RDF triples format
 * @author rvesse
 *
 */
public class TriplesReader extends AbstractRdfReader<Triple, TripleWritable> {

    @Override
    protected RecordReader<LongWritable, TripleWritable> selectRecordReader(Lang lang) throws IOException {
        if (!RDFLanguages.isTriples(lang))
            throw new IOException(lang.getLabel() + " is not a RDF triples format, perhaps you wanted QuadsInputFormat or TriplesOrQuadsInputFormat instead?");

        if (lang.equals(Lang.NTRIPLES) || lang.equals(Lang.NT)) {
            return new WholeFileNTriplesReader();
        } else if (lang.equals(Lang.TTL) || lang.equals(Lang.TURTLE) || lang.equals(Lang.N3)) {
            return new TurtleReader();
        } else if (lang.equals(Lang.RDFXML)) {
            return new RdfXmlReader();
        } else if (lang.equals(Lang.RDFJSON)) {
            return new RdfJsonReader();
        }
        throw new IOException(lang.getLabel() + " has no associated RecordReader implementation");
    }

}
