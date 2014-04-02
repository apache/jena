/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.input.readers;

import org.apache.jena.riot.Lang;

/**
 * A record reader for RDF/XML files
 * 
 * @author rvesse
 * 
 */
public class RdfXmlReader extends AbstractWholeFileTripleReader {

    @Override
    protected Lang getRdfLanguage() {
        return Lang.RDFXML;
    }

}
