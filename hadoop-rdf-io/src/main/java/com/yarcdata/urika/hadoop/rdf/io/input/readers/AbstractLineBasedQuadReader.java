/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.readers;

import java.util.Iterator;

import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.tokens.Tokenizer;
import com.hp.hpl.jena.sparql.core.Quad;
import com.yarcdata.urika.hadoop.rdf.types.QuadWritable;

/**
 * An abstract reader for line based quad formats
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractLineBasedQuadReader extends AbstractLineBasedNodeTupleReader<Quad, QuadWritable> {

    @Override
    protected Iterator<Quad> getIterator(String line, ParserProfile profile) {
        Tokenizer tokenizer = getTokenizer(line);
        return getQuadsIterator(tokenizer, profile);
    }

    @Override
    protected QuadWritable createInstance(Quad q) {
        return new QuadWritable(q);
    }

    protected abstract Tokenizer getTokenizer(String line);

    protected abstract Iterator<Quad> getQuadsIterator(Tokenizer tokenizer, ParserProfile profile);
}
