/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.readers;

import org.apache.jena.riot.Lang;

/**
 * A record reader for Turtle files
 * 
 * @author rvesse
 * 
 */
public class TurtleReader extends AbstractWholeFileTripleReader {

    @Override
    protected Lang getRdfLanguage() {
        return Lang.TURTLE;
    }

}
