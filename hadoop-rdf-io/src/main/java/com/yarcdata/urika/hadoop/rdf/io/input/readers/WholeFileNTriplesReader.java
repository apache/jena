/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.readers;

import org.apache.jena.riot.Lang;

/**
 * A record record for NTriples
 * <p>
 * Unlike the {@link NTriplesReader} this processes files as a whole rather than
 * individual lines. This has the advantage of less parser setup overhead but
 * the disadvantage that the input cannot be split between multiple mappers.
 * </p>
 * 
 * @author rvesse
 * 
 */
public class WholeFileNTriplesReader extends AbstractWholeFileTripleReader {

    @Override
    protected Lang getRdfLanguage() {
        return Lang.NTRIPLES;
    }

}
