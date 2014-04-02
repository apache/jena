/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.readers;

import org.apache.jena.riot.Lang;

/**
 * A record reader for TriG files
 * 
 * @author rvesse
 * 
 */
public class TriGReader extends AbstractWholeFileQuadReader {

    @Override
    protected Lang getRdfLanguage() {
        return Lang.TRIG;
    }

}
