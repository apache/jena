/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.readers;

import java.util.Iterator;

import org.apache.jena.riot.lang.LangNQuads;
import org.apache.jena.riot.system.ParserProfile;
import org.apache.jena.riot.tokens.Tokenizer;
import org.apache.jena.riot.tokens.TokenizerFactory;

import com.hp.hpl.jena.sparql.core.Quad;

/**
 * A record reader for NQuads
 * 
 * @author rvesse
 * 
 */
public class NQuadsReader extends AbstractLineBasedQuadReader {

    @Override
    protected Tokenizer getTokenizer(String line) {
        return TokenizerFactory.makeTokenizerString(line);
    }

    @Override
    protected Iterator<Quad> getQuadsIterator(Tokenizer tokenizer, ParserProfile profile) {
        return new LangNQuads(tokenizer, profile, null);
    }

}
