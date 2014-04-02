/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.readers;

import java.util.Iterator;

import org.apache.jena.riot.lang.LangNTriples;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.riot.tokens.TokenizerFactory;

import com.hp.hpl.jena.graph.Triple;

/**
 * A record reader for NTriples
 * 
 * @author rvesse
 * 
 */
public class NTriplesReader extends AbstractLineBasedTripleReader {

    @Override
    protected Iterator<Triple> getTriplesIterator(Tokenizer tokenizer, ParserProfile profile) {
        return new LangNTriples(tokenizer, profile, null);
    }

    @Override
    protected Tokenizer getTokenizer(String line) {
        return TokenizerFactory.makeTokenizerString(line);
    }
}
