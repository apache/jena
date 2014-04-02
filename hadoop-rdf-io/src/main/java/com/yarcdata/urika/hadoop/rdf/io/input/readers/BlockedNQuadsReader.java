/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.readers;

import org.apache.jena.riot.Lang;

/**
 * A record record for NQuads
 * <p>
 * This is a hybrid of the {@link NQuadsReader} and the
 * {@link WholeFileNQuadsReader} in that it does not process individual lines
 * rather it processes the inputs in blocks of lines parsing the whole block
 * rather than individual lines. This provides a compromise between the higher
 * parser setup of creating more parsers and the benefit of being able to split
 * input files over multiple mappers.
 * </p>
 * 
 * @author rvesse
 * 
 */
public class BlockedNQuadsReader extends AbstractBlockBasedQuadReader {

    @Override
    protected Lang getRdfLanguage() {
        return Lang.NQUADS;
    }

}
