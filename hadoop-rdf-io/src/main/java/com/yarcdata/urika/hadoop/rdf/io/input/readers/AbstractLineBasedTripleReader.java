/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.readers;

import java.util.Iterator;

import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.tokens.Tokenizer;
import com.hp.hpl.jena.graph.Triple;
import com.yarcdata.urika.hadoop.rdf.types.TripleWritable;

/**
 * An abstract record reader for line based triple formats
 * 
 * @author rvesse
 * 
 */
public abstract class AbstractLineBasedTripleReader extends AbstractLineBasedNodeTupleReader<Triple, TripleWritable> {

    @Override
    protected Iterator<Triple> getIterator(String line, ParserProfile profile) {
        Tokenizer tokenizer = getTokenizer(line);
        return getTriplesIterator(tokenizer, profile);
    }

    @Override
    protected TripleWritable createInstance(Triple t) {
        return new TripleWritable(t);
    }
    
    protected abstract Tokenizer getTokenizer(String line);

    protected abstract Iterator<Triple> getTriplesIterator(Tokenizer tokenizer, ParserProfile profile);

}
