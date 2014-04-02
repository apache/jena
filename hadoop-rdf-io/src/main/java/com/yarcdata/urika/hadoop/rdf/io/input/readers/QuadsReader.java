/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.readers;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;

import com.hp.hpl.jena.sparql.core.Quad;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * A record reader that reads triples from any RDF quads format
 * 
 * @author rvesse
 * 
 */
public class QuadsReader extends AbstractRdfReader<Quad, QuadWritable> {

    @Override
    protected RecordReader<LongWritable, QuadWritable> selectRecordReader(Lang lang) throws IOException {
        if (!RDFLanguages.isQuads(lang))
            throw new IOException(lang.getLabel() + " is not a RDF quads format, perhaps you wanted TriplesInputFormat or TriplesOrQuadsInputFormat instead?");

        if (lang.equals(Lang.NQ) || lang.equals(Lang.NQUADS)) {
            return new WholeFileNQuadsReader();
        } else if (lang.equals(Lang.TRIG)) {
            return new TriGReader();
        }
        throw new IOException(lang.getLabel() + " has no associated RecordReader implementation");
    }

}
