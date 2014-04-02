/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.readers;

import org.apache.jena.riot.Lang;

/**
 * A record record for NQuads
 * <p>
 * Unlike the {@link NQuadsReader} this processes files as a whole rather than
 * individual lines. This has the advantage of less parser setup overhead but
 * the disadvantage that the input cannot be split between multiple mappers.
 * </p>
 * 
 * @author rvesse
 * 
 */
public class WholeFileNQuadsReader extends AbstractWholeFileQuadReader {

    @Override
    protected Lang getRdfLanguage() {
        return Lang.NQUADS;
    }

}
