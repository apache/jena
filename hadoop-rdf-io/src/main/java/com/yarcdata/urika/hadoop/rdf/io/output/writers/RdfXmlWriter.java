/*
 * Copyright 2013 YarcData LLC All Rights Reserved.
 */

package com.yarcdata.urika.hadoop.rdf.io.output.writers;

import java.io.Writer;

import org.apache.jena.riot.Lang;

/**
 * A record writer for RDF/XML
 * 
 * @author rvesse
 * @param <TKey>
 *            Key type
 * 
 */
public class RdfXmlWriter<TKey> extends AbstractWholeFileTripleWriter<TKey> {

    /**
     * Creates a new record writer
     * 
     * @param writer
     *            Writer
     */
    public RdfXmlWriter(Writer writer) {
        super(writer);
    }

    @Override
    protected Lang getRdfLanguage() {
        return Lang.RDFXML;
    }

}
